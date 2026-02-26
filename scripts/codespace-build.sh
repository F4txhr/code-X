#!/usr/bin/env bash
set -euo pipefail

# Build debug APK in Codespaces/local Linux environment.
gradle --no-daemon assembleDebug

APK_SOURCE="app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK_SOURCE" ]; then
  echo "APK tidak ditemukan di $APK_SOURCE"
  exit 1
fi

TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
APK_TARGET="artifacts/native-code-editor-debug-${TIMESTAMP}.apk"
cp "$APK_SOURCE" "$APK_TARGET"

echo "Build sukses: $APK_SOURCE"
echo "Salinan artifact: $APK_TARGET"
