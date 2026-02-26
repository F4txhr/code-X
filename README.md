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

Gunakan JDK 17 saat build (AGP tidak kompatibel dengan Java 25):

```bash
JAVA_HOME=/root/.local/share/mise/installs/java/17.0.2 \
PATH=/root/.local/share/mise/installs/java/17.0.2/bin:$PATH \
gradle --no-daemon assembleDebug
```

> Catatan: repo ini saat ini belum menyertakan Gradle Wrapper (`./gradlew`), jadi build CLI memakai Gradle yang sudah terpasang.

## Pakai GitHub Codespaces (bisa)

Bisa. Repo ini sekarang sudah disiapkan `.devcontainer` agar Codespaces:

- otomatis pakai **Java 17**
- install Android SDK command-line tools
- install package SDK yang dibutuhkan (`platform-tools`, `platforms;android-34`, `build-tools;34.0.0`)

Langkah:

1. Buka repo di Codespaces.
2. Tunggu `postCreate` selesai.
3. Jalankan build:

```bash
gradle --no-daemon assembleDebug
```

APK debug akan ada di:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## CI/CD GitHub Actions

Workflow ada di `.github/workflows/android-build.yml` untuk:

- setup JDK 17
- install Gradle 8.7 secara eksplisit
- setup Android SDK + build-tools
- menjalankan `gradle --no-daemon assembleDebug`
- upload artifact APK debug (`app-debug-apk`)
