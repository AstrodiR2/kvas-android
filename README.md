# Квас — нативное Android-приложение (Kotlin + Jetpack Compose)

Без WebView. Структура: `MainActivity`, `ui/theme`, `ui/components`, `ui/screens`, `terminal`.

## Сборка через GitHub
1. Залейте содержимое папки в репозиторий (ветка main).
2. Вкладка **Actions** → **Build APK** → дождаться зелёной галочки.
3. Внутри запуска, внизу, в **Artifacts** скачайте `kvas-debug-apk`, внутри лежит `app-debug.apk`.

Gradle wrapper (`gradlew`) не включён, workflow ставит Gradle 8.9 сам и запускает `gradle assembleDebug`.
Локально: один раз `gradle wrapper --gradle-version 8.9`, дальше `./gradlew assembleDebug`.

## Консоль
Настоящая оболочка `/system/bin/sh` Android (без PTY): ls, cd, cat, ping, grep и т. д.
Не работают полноэкранные программы (vim, htop) и pkg/apt: для этого нужен полный Termux-userland.
