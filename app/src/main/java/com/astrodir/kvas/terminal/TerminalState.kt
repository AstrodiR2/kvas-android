package com.astrodir.kvas.terminal

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import java.io.File
import java.io.OutputStream
import kotlin.concurrent.thread

/**
 * Настоящая оболочка Android: /system/bin/sh (toybox) в домашней папке приложения.
 * Без PTY: работают обычные команды (ls, cd, cat, ping, ...), но не vim/htop/pkg/apt.
 */
class TerminalState(private val home: File) {
    val lines = mutableStateListOf<String>()
    var partial by mutableStateOf("")
    var input by mutableStateOf(TextFieldValue(""))
    var ctrl by mutableStateOf(false)
    var alt by mutableStateOf(false)

    private val history = mutableListOf<String>()
    private var hi = 0
    private var proc: Process? = null
    private var out: OutputStream? = null
    private var alive = false
    private val main = Handler(Looper.getMainLooper())
    private val ansi = Regex("\u001B\\[[0-9;?]*[ -/]*[@-~]")

    init {
        lines.add("Квас · консоль")
        lines.add("shell: /system/bin/sh")
        lines.add("HOME: ${home.absolutePath}")
        start()
    }

    private fun start() {
        try {
            val pb = ProcessBuilder("/system/bin/sh").redirectErrorStream(true)
            pb.directory(home)
            pb.environment().apply {
                put("HOME", home.absolutePath)
                put("TERM", "dumb")
            }
            val p = pb.start()
            proc = p
            out = p.outputStream
            alive = true
            thread(isDaemon = true, name = "kvas-shell") {
                val ins = p.inputStream
                val buf = ByteArray(4096)
                while (true) {
                    val n = try { ins.read(buf) } catch (e: Exception) { -1 }
                    if (n < 0) break
                    val s = String(buf, 0, n, Charsets.UTF_8)
                    main.post { if (proc === p) append(s) }
                }
                main.post {
                    if (proc === p) {
                        alive = false
                        flush()
                        lines.add("[процесс завершён — введите команду, чтобы запустить заново]")
                    }
                }
            }
        } catch (e: Exception) {
            lines.add("не удалось запустить sh: ${e.message}")
        }
    }

    private fun restart() {
        proc?.destroy()
        proc = null
        start()
    }

    private fun append(raw: String) {
        val t = ansi.replace(raw, "").replace("\r\n", "\n").replace("\r", "")
        for (ch in t) {
            if (ch == '\n') { lines.add(partial); partial = "" } else partial += ch
        }
        while (lines.size > 2000) lines.removeAt(0)
    }

    private fun flush() {
        if (partial.isNotEmpty()) { lines.add(partial); partial = "" }
    }

    fun run() {
        val v = input.text
        flush()
        lines.add("\$ $v")
        if (v.isNotBlank()) {
            history.add(v)
            if (v.trim() == "clear") {
                lines.clear()
            } else {
                if (!alive) restart()
                try {
                    out?.write((v + "\n").toByteArray())
                    out?.flush()
                } catch (e: Exception) {
                    lines.add("ошибка записи: ${e.message}")
                }
            }
        }
        hi = history.size
        input = TextFieldValue("")
        ctrl = false; alt = false
    }

    fun onInput(nv: TextFieldValue) {
        if ((ctrl || alt) && nv.text.length == input.text.length + 1) {
            val k = nv.text.getOrNull(nv.selection.start - 1)?.lowercaseChar()
            if (ctrl && k != null) ctrlKey(k)
            ctrl = false; alt = false
            return
        }
        input = nv
    }

    fun ctrlKey(k: Char) {
        when (k) {
            'c' -> { flush(); lines.add("\$ ${input.text}^C"); input = TextFieldValue(""); hi = history.size; restart() }
            'l' -> { lines.clear(); partial = "" }
            'a' -> moveTo(0)
            'e' -> moveTo(input.text.length)
            'u' -> input = TextFieldValue("")
        }
    }

    fun moveTo(i: Int) {
        val p = i.coerceIn(0, input.text.length)
        input = input.copy(selection = TextRange(p))
    }

    fun insert(s: String) {
        val sel = input.selection
        val t = input.text.replaceRange(sel.min, sel.max, s)
        input = TextFieldValue(t, TextRange(sel.min + s.length))
    }

    fun historyTo(i: Int) {
        hi = i.coerceIn(0, history.size)
        val t = history.getOrElse(hi) { "" }
        input = TextFieldValue(t, TextRange(t.length))
    }

    fun historyUp() = historyTo(hi - 1)
    fun historyDown() = historyTo(hi + 1)
    fun cursorLeft() = moveTo(input.selection.start - 1)
    fun cursorRight() = moveTo(input.selection.end + 1)
}
