<div align="center">

<img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android&logoColor=white" />
<img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white" />
<img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white" />
<img src="https://img.shields.io/badge/Min%20SDK-25-informational?style=flat" />
<img src="https://img.shields.io/badge/License-MIT-green?style=flat" />

<br /><br />

# 🎵 Musica

**A clean, fast, fully offline music player for Android.**  
Browse your local library by songs, albums, artists, and folders. Build playlists, control your queue, and enjoy gapless playback — no account, no internet, no ads.

<br />

</div>

---

## Screenshots

> _Screenshots coming soon — add device mockups here once the first build is ready._

---

## Features

### Library Management
- **Songs** — full song list with sort options (title, artist, album, date added, duration)
- **Albums** — grid view with artwork, sorted by title or year
- **Artists** — browse by artist, drill into albums and tracks
- **Folders** — navigate your file system structure
- **Playlists** — create, edit, reorder, and delete your own playlists

### Playback
- Gapless playback via **ExoPlayer / Media3**
- Background playback with a persistent **media notification** (play, pause, skip, seek)
- Lockscreen and headset controls via **MediaSession**
- **Shuffle** — true shuffle with original order restore on toggle
- **Repeat** — off / repeat one / repeat all
- **Skip previous** — tap within 3 seconds to go back, otherwise restart current track
- **Sleep timer** — 15 / 30 / 45 / 60 min or custom; fades out gracefully

### Queue
- Full queue view in a bottom sheet from the Now Playing screen
- Drag to reorder, tap to jump, remove individual tracks
- "Play next" and "Add to queue" from any context menu

### Search
- Instant local search across songs, albums, artists, and playlists
- Results grouped by category with debounced input

### Extras
- **System equalizer** integration (launches your device's built-in EQ)
- **Material 3** dynamic theming — adapts to your wallpaper on Android 12+
- Manual light / dark / system theme override
- Excluded folders — hide specific paths from the library
- No internet permission. No telemetry. No ads.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Repository |
| Dependency Injection | Hilt |
| Playback | Media3 ExoPlayer + MediaSession |
| Local Database | Room (playlists only) |
| Preferences | DataStore |
| Image Loading | Coil |
| Navigation | Navigation Compose |
| Async | Kotlin Coroutines + Flow |

---

## Architecture Overview

```
┌─────────────────────────────────────────┐
│              UI Layer                   │
│   Compose Screens  ←→  ViewModels       │
└──────────────┬──────────────────────────┘
               │ StateFlow / collectAsStateWithLifecycle
┌──────────────▼──────────────────────────┐
│           Domain / Player Layer         │
│   PlayerController   QueueManager       │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│            Data Layer                   │
│  MediaStoreRepository   PlaylistRepo    │
│  (ContentResolver)      (Room DB)       │
└─────────────────────────────────────────┘
               │ Foreground Service
┌──────────────▼──────────────────────────┐
│          MusicService                   │
│  MediaSessionService + ExoPlayer        │
└─────────────────────────────────────────┘
```

- **Single Activity** — `MainActivity` hosts the entire Compose `NavHost`
- **No internet permission** — all data comes from the device MediaStore or Room
- **Foreground service** — playback survives app backgrounding and screen off
- Playlists are the only data persisted in Room; everything else is queried live from MediaStore

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or newer
- JDK 11+
- Android device or emulator running API 25 (Android 7.1) or higher

### Clone & Run

```bash
git clone https://github.com/your-username/musica.git
cd musica
```

Open in Android Studio, let Gradle sync, then run on a device or emulator:

```bash
./gradlew :app:installDebug
```

The app will request audio read permission on first launch. Grant it to scan your library.

### Build Variants

```bash
# Debug build
./gradlew :app:assembleDebug

# Release build (requires signing config)
./gradlew :app:assembleRelease

# Run unit tests
./gradlew :app:testDebug

# Run instrumented tests (requires connected device/emulator)
./gradlew :app:connectedAndroidTest

# Lint
./gradlew :app:lint
```

---

## Project Structure

```
app/src/main/java/com/abra/musica/
├── MainActivity.kt
├── data/
│   ├── model/          # Song, Album, Artist, Folder, Playlist
│   ├── db/             # Room database, DAOs, entities
│   └── repository/     # MediaStoreRepository, PlaylistRepository
├── service/
│   └── MusicService.kt # MediaSessionService
├── player/
│   ├── PlayerController.kt
│   └── QueueManager.kt
├── ui/
│   ├── theme/          # Material3 theme, colors, typography
│   ├── navigation/     # NavHost, Screen routes
│   ├── components/     # MiniPlayer, SongListItem, BottomNavBar, …
│   └── screens/        # One folder per feature screen
└── di/
    └── AppModule.kt    # Hilt module
```

For a full breakdown of every file, conventions, and patterns — see [AGENTS.md](./AGENTS.md).

---

## Permissions

| Permission | Reason |
|---|---|
| `READ_MEDIA_AUDIO` (API 33+) | Read local audio files |
| `READ_EXTERNAL_STORAGE` (API ≤ 32) | Read local audio files on older Android |
| `FOREGROUND_SERVICE` | Keep music playing in the background |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Media playback foreground service type |
| `WAKE_LOCK` | Prevent CPU sleep during playback |

No network permissions are requested or used.

---

## Roadmap

- [ ] Lyrics display (LRC file support)
- [ ] Last.fm scrobbling
- [ ] Android Auto support
- [ ] Widget (4×1 and 4×2 home screen widgets)
- [ ] ReplayGain support
- [ ] Crossfade between tracks
- [ ] Tag editor (title, artist, album, artwork)
- [ ] Backup & restore playlists

---

## Contributing

Contributions are welcome! Please open an issue first to discuss any significant change.

1. Fork the repo
2. Create your branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m 'Add some feature'`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

---

## License

```
MIT License

Copyright (c) 2025 Abra

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

<div align="center">
  <sub>Built with Kotlin and Jetpack Compose · No internet required · No tracking</sub>
</div>
