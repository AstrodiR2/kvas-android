package com.astrodir.kvas.termux

import android.os.Build
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

/**
 * Скачивает и распаковывает официальный Termux bootstrap-архив
 * (собранные бинарники bash/busybox/dpkg/apt под Android) в приватную
 * папку нашего приложения и позволяет запускать их как обычный процесс.
 *
 * Требует targetSdk <= 28 — начиная с 29 Android запрещает exec файлов,
 * скачанных в приватную папку приложения (W^X). См. app/build.gradle.kts.
 *
 * Источник: https://github.com/termux/termux-packages/releases
 * Используем алиас releases/latest/download/, чтобы не хардкодить версию —
 * GitHub сам отдаёт актив из последнего релиза под этим путём.
 *
 * ВАЖНО про SYMLINKS.txt: в архиве нет настоящих symlink-записей — вместо
 * них один файл SYMLINKS.txt со строками "<target>←<link-path>" (разделитель
 * — символ U+2190, НЕ дефис). target часто содержит хардкод
 * "/data/data/com.termux/files/usr/..." — это нужно переписать на наш
 * реальный путь, иначе ссылки будут битыми.
 */
object TermuxBootstrap {

    private const val BASE = "https://github.com/termux/termux-packages/releases/latest/download"
    private val URLS = mapOf(
        "aarch64" to "$BASE/bootstrap-aarch64.zip",
        "arm" to "$BASE/bootstrap-arm.zip",
        "x86_64" to "$BASE/bootstrap-x86_64.zip",
        "i686" to "$BASE/bootstrap-i686.zip",
    )

    private const val TERMUX_HARDCODED_PREFIX = "/data/data/com.termux/files/usr"
    private const val ARROW = "\u2190" // ←

    fun deviceArch(): String {
        for (abi in Build.SUPPORTED_ABIS) {
            when (abi) {
                "arm64-v8a" -> return "aarch64"
                "armeabi-v7a" -> return "arm"
                "x86_64" -> return "x86_64"
                "x86" -> return "i686"
            }
        }
        return "aarch64"
    }

    fun prefixDir(home: File) = File(home, "usr")
    fun isInstalled(home: File) = File(prefixDir(home), "bin/bash").exists()

    sealed class Progress {
        data class Downloading(val bytes: Long, val total: Long) : Progress()
        data class Extracting(val done: Int, val total: Int) : Progress()
        data class Error(val message: String) : Progress()
        object Done : Progress()
    }

    /** Вызывать из фонового потока/корутины. onProgress может прийти с любого потока. */
    fun install(home: File, onProgress: (Progress) -> Unit) {
        val arch = deviceArch()
        val url = URLS[arch] ?: URLS.getValue("aarch64")
        val zipFile = File(home, "bootstrap-$arch.zip")
        val staging = File(home, "usr-staging")
        val finalPrefix = prefixDir(home)

        try {
            staging.deleteRecursively()
            staging.mkdirs()

            // 1. Скачивание
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 20_000
                instanceFollowRedirects = true
            }
            conn.connect()
            if (conn.responseCode !in 200..299) {
                onProgress(Progress.Error("Сервер вернул ${conn.responseCode}"))
                return
            }
            val total = conn.contentLengthLong
            var read = 0L
            conn.inputStream.use { input ->
                FileOutputStream(zipFile).use { out ->
                    val buf = ByteArray(64 * 1024)
                    while (true) {
                        val n = input.read(buf)
                        if (n < 0) break
                        out.write(buf, 0, n)
                        read += n
                        onProgress(Progress.Downloading(read, total))
                    }
                }
            }

            // 2. Распаковка во временную папку (staging), без symlink-записей —
            // те идут отдельным списком в SYMLINKS.txt.
            val symlinkLines = mutableListOf<String>()
            var count = 0
            val estTotal = 1800 // ориентир для прогресс-бара, в архиве нет явного счётчика заранее
            ZipInputStream(zipFile.inputStream().buffered()).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val name = entry.name.removePrefix("./")
                    when {
                        name == "SYMLINKS.txt" -> {
                            val text = zis.readBytes().toString(Charsets.UTF_8)
                            symlinkLines += text.lines().filter { it.isNotBlank() }
                        }
                        !entry.isDirectory -> {
                            val outFile = File(staging, name)
                            outFile.parentFile?.mkdirs()
                            FileOutputStream(outFile).use { fos ->
                                val buf = ByteArray(64 * 1024)
                                while (true) {
                                    val n = zis.read(buf)
                                    if (n < 0) break
                                    fos.write(buf, 0, n)
                                }
                            }
                            if (shouldBeExecutable(name)) outFile.setExecutable(true, false)
                        }
                        else -> File(staging, name).mkdirs()
                    }
                    count++
                    onProgress(Progress.Extracting(count, estTotal))
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
            zipFile.delete()

            // 3. Symlinks: формат "<target>←<link-path>", target часто содержит
            // хардкод пути Termux — переписываем на наш реальный prefix.
            symlinkLines.forEach { line ->
                val idx = line.indexOf(ARROW)
                if (idx < 0) return@forEach
                var target = line.substring(0, idx).trim()
                val linkRel = line.substring(idx + ARROW.length).trim()
                if (target.startsWith(TERMUX_HARDCODED_PREFIX)) {
                    target = finalPrefix.absolutePath + target.removePrefix(TERMUX_HARDCODED_PREFIX)
                }
                try {
                    val link = File(staging, linkRel)
                    link.parentFile?.mkdirs()
                    if (link.exists()) link.delete()
                    java.nio.file.Files.createSymbolicLink(link.toPath(), File(target).toPath())
                } catch (_: Exception) {
                    // не критично для базовой работы bash — не валим всю установку
                }
            }

            // 4. Атомарная публикация: только когда staging полностью готов.
            finalPrefix.deleteRecursively()
            if (!staging.renameTo(finalPrefix)) {
                staging.copyRecursively(finalPrefix, overwrite = true)
                staging.deleteRecursively()
            }

            onProgress(Progress.Done)
        } catch (e: Exception) {
            onProgress(Progress.Error(e.message ?: "неизвестная ошибка"))
        }
    }

    private fun shouldBeExecutable(zipEntryName: String): Boolean =
        zipEntryName.startsWith("usr/bin/") ||
            zipEntryName.startsWith("usr/libexec/") ||
            zipEntryName.startsWith("usr/lib/apt/") ||
            zipEntryName.contains("/bin/")
}
