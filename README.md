# Native Code Editor (Android)

Project ini adalah aplikasi Android native sederhana yang berfungsi sebagai code editor ringan.

## Fitur saat ini (fase bertahap)

- UI ala editor (toolbar, tab bar, explorer drawer, status bar)
- Explorer membaca file dari **internal storage** (folder yang dipilih user)
- Tombol project minimalis ada di samping label **EXPLORER** (open/new/close)
- Explorer tampil model folder bertingkat (klik folder untuk masuk, bukan path `folder/file` datar)
- Tab terbuka saat file dipilih dari explorer
- Line number + status cursor (Ln/Col)

## Alur izin akses storage (awal aplikasi)

Saat pertama kali buka aplikasi:

1. Muncul popup permintaan akses folder.
2. Klik **Pilih folder**.
3. Pilih folder internal storage yang ingin dieksplorasi.
4. App menyimpan izin baca folder tersebut (persisted URI permission).

Setelah itu, daftar file teks akan muncul di panel **EXPLORER**.

## Format file yang didukung explorer

Contoh ekstensi yang ditampilkan:

- `.kt`, `.kts`, `.java`, `.xml`
- `.txt`, `.md`, `.json`, `.yaml`, `.yml`, `.gradle`

## Menjalankan lokal

1. Buka project di Android Studio (JDK 17).
2. Sync Gradle.
3. Jalankan di emulator/perangkat Android.

## Build via command line

Gunakan JDK 17 saat build:

```bash
JAVA_HOME=/root/.local/share/mise/installs/java/17.0.2 \
PATH=/root/.local/share/mise/installs/java/17.0.2/bin:$PATH \
gradle --no-daemon assembleDebug
```

## Pakai GitHub Codespaces

1. Buka repo di Codespaces.
2. Tunggu `postCreate` selesai.
3. Jalankan build:

```bash
./scripts/codespace-build.sh
```

APK output:

- `app/build/outputs/apk/debug/app-debug.apk`
- `artifacts/native-code-editor-debug-YYYYMMDD-HHMMSS.apk`

## CI/CD GitHub Actions

Workflow `.github/workflows/android-build.yml`:

- setup JDK 17
- install Gradle 8.7
- setup Android SDK + build-tools
- jalankan `gradle --no-daemon assembleDebug`
- upload artifact APK debug


## Manajemen project directory

Di header panel **EXPLORER** (ikon minimalis), tersedia:

- **Open Project Directory**: ganti project aktif ke folder lain
- **Close Project**: tutup project aktif dan reset explorer/tab
- **Create New Project Directory**: buat folder project baru di parent folder yang dipilih, lalu langsung dibuka

## Deteksi jenis file

Explorer mendeteksi file berbasis MIME text dan beberapa MIME source code umum, serta menyaring file biner populer (gambar/video/archive/apk/dll). Jadi berbagai file bahasa pemrograman tetap terbaca tanpa perlu daftar ekstensi kaku.
