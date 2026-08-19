# AirPlayer

AirPlayer is a free, open-source, ad-free AirPlay 2 receiver for Android TV. It lets your macOS or iOS/iPadOS device mirror its screen and audio directly to your TV — no Apple TV required.

```
 macOS (Monterey+)            Android TV
 iOS / iPadOS (16+)           ┌──────────────────────┐
 ┌────────────────┐  AirPlay  │                      │
 │  [Your Screen] │ ────────► │  [Your TV Screen]    │
 │                │           │                      │
 └────────────────┘           └──────────────────────┘
      Click AirPlay →              AirPlayer
      Select your TV →             (this app)
      Done. ✓
```

---

## Current Status — v1.0.0-beta.1

AirPlayer's AirPlay 2 receiver is fully implemented and available as a signed beta release. Download the APK directly from the [GitHub Releases page](https://github.com/mazer666/AirPlayer/releases).

The AirPlay 2 stack is complete end-to-end: mDNS advertising, RTSP handshake, HomeKit-style pairing, FairPlay key decryption, H.264 mirroring, AAC-ELD/AAC-LC/ALAC audio, NTP A/V sync, and DACP reverse remote. Real-device validation with macOS and iOS senders is the current focus.

AirPlayer is AirPlay 2 only — see [ADR-004](docs/decisions/ADR-004-airplay-only.md) for why Miracast and Google Cast support were removed.

## Features

### AirPlay 2 (fully implemented)
- Screen mirroring from macOS 12+ and iOS/iPadOS 16+ — H.264 hardware decode
- FairPlay session decryption (fp-setup v2/v3 + legacy rsaaeskey) via native libplayfair
- HomeKit-style pairing (Ed25519/X25519) and legacy SRP PIN pairing
- Mirroring audio: AAC-ELD, AAC-LC, ALAC — with independent A/V start/stop
- System audio streaming (ALAC, unencrypted) — reliable path for app audio
- AirPlay video URL mode (`/play` content) + transport controls (play/pause/scrub)
- Now-playing metadata (DMAP) with album artwork overlay
- DACP reverse remote — TV remote controls the sender's playback
- NTP timing and UDP audio retransmit (packet-loss recovery)
- AirPlay photo receiver — JPEG/PNG from iOS Photos app displayed full-screen
- Access-control lockout after repeated failed pairing attempts

### App & Platform
- Android TV app shell with foreground service and status UI
- Mirror audio toggle and PIN-auth toggle in Settings
- Works on Android TV / Google TV (Android 7.1+)
- Zero ads, zero analytics, zero internet required
- Open source — Apache 2.0 license

## What AirPlayer Does NOT Do

- **FairPlay DRM content** (Netflix, Disney+, Apple TV+) — Apple DRM; not decryptable by any open-source receiver
- **Apple Music in-app audio** — protected on every AirPlay path; use system audio output instead
- **Buffered audio playback** (AirPlay 2 type 103) — accepted but not played back yet
- **Cloud/remote streaming** — local network only
- **Miracast / Google Cast** — not supported; AirPlayer is AirPlay 2 only (see [ADR-004](docs/decisions/ADR-004-airplay-only.md))

---

## Requirements

**On your TV:**
- Android TV / Google TV (Android 7.1+)
- Connected to the same Wi-Fi network as your Mac
- Sideloading or ADB enabled

**On your Mac:**
- macOS 12 (Monterey) or later
- Connected to the same Wi-Fi network as your TV

**Network:**
- Both devices on the same subnet (common home router setup works)
- Multicast/mDNS must not be blocked (most home routers are fine)
- 5 GHz Wi-Fi or Ethernet strongly recommended for best performance

---

## Installation

### Option A: Download a Release APK (easiest)

Go to the [Releases page](https://github.com/mazer666/AirPlayer/releases) and download `AirPlayer-vX.Y.Z.apk`, then install it via ADB (see the Sideloading Guide below) or a sideloading app like *Downloader*.

### Option B: Build from Source

1. **Install prerequisites**
   ```bash
   # Install Android Studio from https://developer.android.com/studio
   # Install JDK 17 or later
   ```

2. **Clone the repository**
   ```bash
   git clone https://github.com/mazer666/AirPlayer.git
   cd AirPlayer
   ```

3. **Build the APK**
   ```bash
   ./gradlew assembleDebug
   ```
   The APK will be in `app/build/outputs/apk/`.

   To run the same local checks used by CI before testing on a TV:
   ```bash
   ./gradlew :test-runner:test
   ./gradlew :app:lintDebug :app:assembleDebug
   ```

4. **Install via ADB**
   ```bash
   # Enable ADB on your TV first (see below)
   adb connect <TV-IP-ADDRESS>
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

---

## Sideloading Guide

### Android TV / Google TV

1. Go to **Settings → System → About → Android TV OS build** and click it 7 times to enable Developer Options.
2. Go to **Settings → System → Developer Options** and enable **USB debugging**.
3. Note your TV's IP address from **Settings → Network & Internet**.
4. On your Mac/PC, run:
   ```bash
   adb connect <TV-IP>
   adb install app-debug.apk
   ```
5. Launch AirPlayer from your app list.

---

## How to Use

1. Launch AirPlayer on your TV. You will see the Waiting Screen with your TV's name.
2. On your Mac, click the **AirPlay** icon in the menu bar (or go to **System Preferences → Displays → AirPlay Display**).
3. Select your TV from the list (it should appear as your TV's name).
4. Your Mac's screen will appear on the TV instantly.
5. To stop: click the AirPlay icon on your Mac and select "Turn Off AirPlay Mirroring", or just quit AirPlayer on the TV.

---

## Known Limitations

- **Beta software** — the AirPlay 2 stack is complete but real-device validation with various macOS/iOS senders is ongoing. Please report issues.
- **Apple Music in-app audio is not decryptable.** macOS protects it with FairPlay on every AirPlay path. Route the Mac's system audio output instead (works fine).
- **FairPlay-protected video** (Netflix, Disney+, Apple TV+) cannot be mirrored — this is Apple's DRM, not a AirPlayer limitation.
- **Buffered audio (AirPlay 2 type 103)** is accepted but not yet played back.
- If your router has **AP isolation** or **multicast filtering** enabled, AirPlayer may not appear in the AirPlay menu. Disable these settings on your router.
- On very busy 2.4 GHz Wi-Fi networks, you may experience latency above 100 ms. Use 5 GHz or Ethernet for best results.
- **PIN auth is optional.** When disabled (default), any device on the same network can mirror to the TV. Enable PIN auth in Settings if you're on a shared network.

For real-device failures, run `tools/collect-device-logs.sh` before restarting the app. It captures package state, memory, CPU, and filtered AirPlayer logs into `device-test-logs/`.

---

## Contributing

Contributions are welcome! Please read [docs/CONTRIBUTING.md](docs/CONTRIBUTING.md) before submitting a pull request.

Key points:
- Follow the coding rules in CONTRIBUTING.md (file size ≤400 lines soft / ≤550 lines hard max, class comments, test coverage)
- All PRs require passing CI (build + tests + lint)
- Discuss major changes in a GitHub Issue first

## License

Apache License 2.0 — see [LICENSE](LICENSE) for details.

---

## Acknowledgments

- [openairplay/airplay-spec](https://github.com/openairplay/airplay-spec) — Community-maintained AirPlay protocol documentation
- [UxPlay](https://github.com/FDH2/UxPlay) — Open-source AirPlay mirror server (reference implementation)
- [RPiPlay](https://github.com/FD-/RPiPlay) — AirPlay mirroring for Raspberry Pi (reference implementation)
