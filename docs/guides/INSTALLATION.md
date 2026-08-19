# Installation Guide

This guide covers every way to install AirPlayer on your Android TV device.

---

## Prerequisites

- An Android TV / Google TV device (see [supported devices](../spec/REQUIREMENTS.md))
- A computer (Windows, macOS, or Linux) with ADB installed — OR — a direct APK sideload method
- Both devices on the same Wi-Fi network

---

## Method 1: ADB (Recommended for developers)

### Step 1: Enable ADB on your TV

1. Settings → System → About → Android TV OS Build → click 7 times
2. Settings → System → Developer Options → USB debugging → ON

### Step 2: Find your TV's IP address

Settings → Network & Internet → your Wi-Fi → scroll down to see IP

### Step 3: Connect ADB

```bash
adb connect <TV-IP-ADDRESS>:5555
# Example: adb connect 192.168.1.42:5555
```

Confirm the connection prompt that appears on your TV.

### Step 4: Install

```bash
adb install app-release.apk
```

### Step 5: Launch

Find **AirPlayer** in your app list and launch it.

---

## Method 2: Direct Sideload via USB

Use a sideloading app like **Downloader** (available on most Android TV app stores) to download the APK directly to your device from a URL.

1. Install "Downloader" from your TV's app store
2. Open Downloader and enter the APK download URL
3. Follow the prompts to install

---

## Method 3: Build from Source

```bash
git clone https://github.com/mazer666/AirPlayer.git
cd AirPlayer
./gradlew assembleRelease
```

The APK is in `app/build/outputs/apk/`.

---

## After Installation

1. Launch AirPlayer — the **HomeScreen** appears showing the AirPlay status card
2. On your Mac: click the AirPlay icon → select your TV

See [Troubleshooting](TROUBLESHOOTING.md) if the device doesn't appear in your sender's list.
