# 📺 IPTV Pro

A clean, dark-themed Android IPTV player app built with Kotlin and ExoPlayer.

## ✨ Features

- 🔴 **Live HLS Streaming** — powered by ExoPlayer/Media3
- 🔍 **Channel Search** — instantly filter by name or group
- 📋 **Channel List** — loads from OpenSourceFlix M3U8 playlist
- 🖼️ **Channel Logos** — displayed via Glide image loading
- 🌙 **Dark Theme** — easy on the eyes, built for TV-watching
- ↔️ **Landscape Player** — full-screen player with status overlay
- 🔄 **Pull to Refresh** — reload channel list anytime

## 📱 Screenshots

> Coming soon

## 🚀 Getting Started

### Requirements
- Android Studio Hedgehog or later
- Android SDK 34
- Kotlin 1.9+

### Build & Run

```bash
git clone https://github.com/asmasud500/IPTV-pro.git
cd IPTV-pro
# Open in Android Studio and run on device/emulator
```

## 🛠️ Tech Stack

| Library | Purpose |
|---|---|
| ExoPlayer / Media3 | HLS video playback |
| OkHttp | M3U8 playlist fetching |
| Glide | Channel logo image loading |
| Kotlin Coroutines | Async data loading |
| ViewModel + LiveData | MVVM architecture |
| Material Components | UI components |

## 📡 Stream Source

Channels are loaded from:
```
https://raw.githubusercontent.com/opensourceflix/OpenSourceFlix/refs/heads/main/iptv.m3u8
```

## 📄 License

MIT License — free to use and modify.
