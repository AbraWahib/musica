<div align="center">

<img src="app/src/main/ic_launcher-playstore.png" alt="Musica" width="128" height="128" />

<br />

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
- **Songs** — full song list with sort options (title, artist, album, date added, duration) persisted across restarts; multi-select mode; fast-scroll alphabetical index; "Play All" with song count
- **Albums** — 2/3 column grid with artwork, sorted by title or year; detail screen with track list sorted by number, "Play All" and "Shuffle" buttons
- **Artists** — browse by artist with album/song counts; detail screen with horizontal album grid and songs grouped by album
- **Folders** — navigate your file system structure by folder; detail screen sorted by filename
- **Playlists** — create, rename, reorder, and delete user-created playlists via FAB dialog; detail screen with drag-to-reorder, "Play All", "Shuffle", and "Add songs" button
- **Favourites** — backed by Room, toggle from any song menu, MiniPlayer, or NowPlayingScreen
- **Recently Played** — automatically tracks playback history via Room

### Playback
- Gapless playback via **ExoPlayer / Media3** in a foreground `MediaSessionService`
- Background playback with persistent **media notification** (play, pause, skip, seek)
- Lockscreen and headset controls via **MediaSession**
- **Shuffle** — true shuffle with original order restore on toggle-off
- **Repeat** — off / repeat one / repeat all (persisted across restarts)
- **Skip previous** — within 3s restarts current track; after 3s goes to previous
- **Sleep timer** — 15 / 30 / 45 / 60 minutes; pauses playback on expiry

### Queue
- Full queue view in a `ModalBottomSheet` from NowPlayingScreen
- Drag to reorder, tap to jump, remove individual tracks; clear queue
- "Play next" and "Add to queue" from any song context menu

### Search
- Instant local search across songs, albums, artists, and playlists simultaneously
- Results grouped by category with "Show all" when >3 per category
- 300ms input debounce

### Extras
- **System equalizer** integration (launches device EQ via `DISPLAY_AUDIO_EFFECT_CONTROL_PANEL`)
- **Material 3** dynamic theming — adapts to wallpaper on Android 12+; manual light/dark/system override
- Library folder filters — exclude specific paths from scanning
- Minimum song duration filter in Settings
- No internet permission. No telemetry. No ads.

---

## Tech Stack

| Category | Technology |
|---|---|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 (Compose BOM 2024.09) |
| Architecture | MVVM + Repository + Clean-ish layering |
| Dependency Injection | Hilt 2.51 |
| Playback | Media3 ExoPlayer 1.4.1 + MediaSession |
| Local Database | Room 2.6.1 (playlists, favourites, recently played) |
| Preferences | DataStore Preferences |
| Image Loading | Coil Compose 2.7 |
| Navigation | Navigation Compose 2.8 |
| Async | Kotlin Coroutines 1.8 + Flow |
| Testing | JUnit 4, Mockk, Turbine, Compose UI Test |

---

## Architecture Overview

```
┌─────────────────────────────────────────┐
│              UI Layer                   │
│   Compose Screens  ←→  ViewModels       │
└──────────────┬──────────────────────────┘
               │ StateFlow / collectAsStateWithLifecycle
┌──────────────▼──────────────────────────┐
│           Player Layer                  │
│   PlayerController   QueueManager       │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│            Data Layer                   │
│  MediaStoreRepository   PlaylistRepo    │
│  (ContentResolver)      (Room DB)       │
└──────────────┬──────────────────────────┘
               │ Foreground Service
┌──────────────▼──────────────────────────┐
│          MusicService                   │
│  MediaSessionService + ExoPlayer        │
└─────────────────────────────────────────┘
```

- **Single Activity** — `MainActivity` hosts the entire Compose `NavHost`
- **No internet permission** — all data comes from the device MediaStore or Room
- **Foreground service** — playback survives app backgrounding and screen off
- Playlists, favourites, and recently played are persisted in Room; all other metadata is queried live from MediaStore
- `NowPlayingViewModel` is scoped to the NavGraph root for shared player state across screens

### Navigation

Bottom nav contains **Songs**, **Search**, and **Library**. The **NowPlaying** screen is a full-screen modal reached via MiniPlayer tap. Detail screens (Album, Artist, Folder, Playlist) are secondary routes. All routes use `launchSingleTop` + `restoreState` for bottom nav tabs.

---

## MiniPlayer

Always visible when a song is loaded. Shows album art thumbnail, title, artist, play/pause, and skip-next buttons. Tapping anywhere except buttons navigates to `NowPlayingScreen`. Animates in/out with `AnimatedVisibility`.

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

# KSP codegen (Room + Hilt)
./gradlew :app:kspDebugKotlin
```

---

## Project Structure

```
app/src/main/java/com/abra/musica/
├── MainActivity.kt                  # Single Activity host
├── data/
│   ├── model/                       # Song, Album, Artist, Folder, Playlist
│   ├── db/                          # Room database, DAOs, entities
│   │   ├── MusicDatabase.kt
│   │   ├── dao/
│   │   │   ├── PlaylistDao.kt
│   │   │   └── PlaylistSongDao.kt
│   │   └── entity/
│   │       ├── PlaylistEntity.kt
│   │       ├── PlaylistSongEntity.kt
│   │       ├── FavoriteSongEntity.kt
│   │       └── RecentlyPlayedEntity.kt
│   └── repository/
│       ├── MediaStoreRepository.kt  # ContentResolver queries
│       └── PlaylistRepository.kt    # Room CRUD
├── service/
│   └── MusicService.kt             # MediaSessionService
├── player/
│   ├── PlayerController.kt         # Singleton ExoPlayer wrapper
│   └── QueueManager.kt             # Queue state management
├── ui/
│   ├── theme/                      # Color, Type, Theme (dynamic + manual)
│   ├── navigation/                 # AppNavHost, Screen sealed class
│   ├── components/                 # MiniPlayer, SongListItem, AlbumCard, etc.
│   └── screens/                    # One subdir per feature
└── di/
    └── AppModule.kt                # Hilt DI module
```

For a full breakdown of every file, conventions, and architectural patterns — see [AGENTS.md](./AGENTS.md).

---

## Permissions

| Permission | Reason |
|---|---|
| `READ_MEDIA_AUDIO` (API 33+) | Read local audio files |
| `READ_EXTERNAL_STORAGE` (API ≤ 32) | Read local audio files on older Android |
| `FOREGROUND_SERVICE` | Keep music playing in the background |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Media playback foreground service type (API 34+) |
| `WAKE_LOCK` | Prevent CPU sleep during playback |

No network permissions are requested or used.

---

## Code Style Highlights

- **Compose-only UI** — no XML layouts; Material3 `TopAppBar` with scroll behavior
- **StateFlow + `collectAsStateWithLifecycle`** — no LiveData, no bare `collectAsState()`
- **Hilt for DI** — no manual `object` singletons
- **All I/O on `Dispatchers.IO`** — MediaStore, Room, file ops never block the main thread
- **String resources** — all user-visible strings in `res/values/strings.xml`
- **Album art** loaded via Coil with `ContentUris.withAppendedId` for the albumart URI
- **@Preview for every composable** — wrapped in `MusicaTheme` with sample data

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
