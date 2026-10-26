package com.astrodir.kvas

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.astrodir.kvas.ui.components.LOGOS
import com.astrodir.kvas.ui.components.LOGO_NAMES
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Msg(val id: Long, val user: Boolean, text: String, val fresh: Boolean = false) {
    var text by mutableStateOf(text)
    var loading by mutableStateOf(false)
    var vote by mutableIntStateOf(0) // 0 нет, 1 нравится, 2 не нравится
}

class Chat(val id: Int, name: String) {
    var name by mutableStateOf(name)
    val msgs = mutableStateListOf<Msg>()
}

class KvasState(private val prefs: SharedPreferences) {
    private val replies = listOf(
        "да понял, это макет — настоящий ответ тут появится, когда подключим ИИ",
        "ща подумаю… шучу, я пока просто картинка 🙂",
        "окей, записал. в реальной версии тут будет живой ответ Кваса",
        "хорошо звучит, продолжай",
    )
    private var idc = 0L
    private var ri = 0
    private var nid = 5

    private fun m(user: Boolean, t: String, fresh: Boolean = false) = Msg(idc++, user, t, fresh)

    val chats = mutableStateListOf<Chat>().also { list ->
        fun chat(id: Int, name: String, vararg ms: Pair<Boolean, String>) =
            Chat(id, name).also { c -> ms.forEach { c.msgs.add(m(it.first, it.second)) } }
        list.add(chat(1, "Курилка", true to "квас как дела", false to "да норм, сижу торчу тут. у тебя как", true to "скинь погоду на завтра"))
        list.add(chat(2, "Годжо Сатору", true to "картинку кваса сделай", false to replies[1]))
        list.add(chat(3, "Семья", true to "напомни про встречу", false to replies[2]))
        list.add(chat(4, "Рабочий чат", true to "что нового в чате?", false to replies[3]))
    }
    var current by mutableStateOf(chats[0])
    var typingChat by mutableStateOf<Chat?>(null)

    var logo by mutableIntStateOf(prefs.getInt("kvasLogo", 0) % LOGOS.size)
    var searchOn by mutableStateOf(false)
    var memoryOn by mutableStateOf(true)

    var toastText by mutableStateOf<String?>(null)
    var toastTick by mutableIntStateOf(0)
    fun toast(t: String) { toastText = t; toastTick++ }

    private fun nextReply() = replies[ri++ % replies.size]

    fun cycleLogo() {
        logo = (logo + 1) % LOGOS.size
        prefs.edit().putInt("kvasLogo", logo).apply()
        toast("Логотип: " + LOGO_NAMES[logo])
    }

    fun send(v: String, scope: CoroutineScope) {
        val c = current
        if (c.msgs.isEmpty()) c.name = if (v.length > 26) v.take(26) + "…" else v
        c.msgs.add(m(true, v, fresh = true))
        typingChat = c
        scope.launch {
            delay(1300)
            typingChat = null
            c.msgs.add(m(false, nextReply(), fresh = true))
        }
    }

    fun redo(msg: Msg, scope: CoroutineScope) {
        msg.loading = true
        scope.launch {
            delay(1100)
            msg.text = nextReply()
            msg.vote = 0
            msg.loading = false
        }
    }

    fun newChat() {
        val empty = chats.firstOrNull { it.msgs.isEmpty() }
        if (empty != null) { current = empty; return }
        val c = Chat(nid++, "Новый чат")
        chats.add(0, c)
        current = c
    }
}
