# Native Code Editor (Android)

Project ini adalah aplikasi Android native sederhana yang berfungsi sebagai code editor ringan:

- area input kode berbasis `EditText` monospace
- line number otomatis
- starter template kode Kotlin
- dapat dibuild otomatis via GitHub Actions

## Menjalankan lokal

1. Buka project di Android Studio (JDK 17).
2. Sync Gradle.
3. Jalankan di emulator/perangkat Android.

## Build via command line

```bash
gradle assembleDebug
```

> Catatan: repo ini saat ini belum menyertakan Gradle Wrapper (`./gradlew`), jadi build CLI memakai Gradle yang sudah terpasang.

## CI/CD GitHub Actions

Workflow ada di `.github/workflows/android-build.yml` untuk:

- setup JDK 17
- install Gradle 8.7 secara eksplisit
- setup Android SDK + build-tools
- menjalankan `gradle --no-daemon assembleDebug`
- upload artifact APK debug (`app-debug-apk`)
