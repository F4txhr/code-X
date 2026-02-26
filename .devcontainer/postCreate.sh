#!/usr/bin/env bash
set -euo pipefail

export DEBIAN_FRONTEND=noninteractive
sudo apt-get update
sudo apt-get install -y wget unzip

ANDROID_SDK_ROOT="$HOME/android-sdk"
mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools"

if [ ! -d "$ANDROID_SDK_ROOT/cmdline-tools/latest" ]; then
  wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/cmdline-tools.zip
  rm -rf /tmp/android-cmdline-tools
  mkdir -p /tmp/android-cmdline-tools
  unzip -q /tmp/cmdline-tools.zip -d /tmp/android-cmdline-tools
  mv /tmp/android-cmdline-tools/cmdline-tools "$ANDROID_SDK_ROOT/cmdline-tools/latest"
fi

export PATH="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH"
yes | sdkmanager --licenses >/dev/null || true
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

echo "ANDROID_SDK_ROOT=$ANDROID_SDK_ROOT" >> "$HOME/.bashrc"
echo 'export PATH="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH"' >> "$HOME/.bashrc"

echo "Codespace Android setup selesai. Jalankan: gradle --no-daemon assembleDebug"
