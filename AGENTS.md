# AGENTS.md — AI Agent Guide for Musica (Local Music Player)

> **Purpose**: This file is the authoritative reference for AI coding agents (GitHub Copilot, Claude, Cursor, etc.) working on the Musica Android local music player. Read this entire file before writing or modifying any code.

---

## Table of Contents
1. [Project Overview](#1-project-overview)
2. [Architecture & Project Structure](#2-architecture--project-structure)
3. [Tech Stack & Dependencies](#3-tech-stack--dependencies)
4. [Data Layer — Models & Database](#4-data-layer--models--database)
5. [Media Playback Architecture](#5-media-playback-architecture)
6. [UI Architecture & Screen Map](#6-ui-architecture--screen-map)
7. [Features & Functional Requirements](#7-features--functional-requirements)
8. [Navigation](#8-navigation)
9. [Permissions & Platform Constraints](#9-permissions--platform-constraints)
10. [State Management Patterns](#10-state-management-patterns)
11. [Theming & Design System](#11-theming--design-system)
12. [Testing Strategy](#12-testing-strategy)
13. [Build & Gradle Conventions](#13-build--gradle-conventions)
14. [Code Style & Non-Negotiables](#14-code-style--non-negotiables)
15. [Common Tasks — Step-by-Step](#15-common-tasks--step-by-step)

---

## 1. Project Overview

**Musica** is a fully offline, local music player for Android. It scans the device's media store for audio files and provides a rich playback experience with library management, queue control, and playlist editing — no internet connection required.

**Target users**: Android users who store music locally (downloaded files, sideloaded MP3s, etc.)  
**Min SDK**: 25 (Android 7.1)  
**Target SDK**: 36  
**Package**: `com.abra.musica`

---

## 2. Architecture & Project Structure

The app follows **MVVM + Repository + Clean-ish layering**. Do not deviate from this structure.

```
app/src/main/java/com/abra/musica/
│
├── MainActivity.kt                  # Single Activity host; owns NavHost
│
├── data/
│   ├── model/                       # Pure Kotlin data classes (no Android deps)
│   │   ├── Song.kt
│   │   ├── Album.kt
│   │   ├── Artist.kt
│   │   ├── Folder.kt
│   │   └── Playlist.kt
│   ├── db/
│   │   ├── MusicDatabase.kt         # Room database definition
│   │   ├── dao/
│   │   │   ├── PlaylistDao.kt
│   │   │   └── PlaylistSongDao.kt
│   │   └── entity/
│   │       ├── PlaylistEntity.kt
│   │       └── PlaylistSongEntity.kt
│   └── repository/
│       ├── MediaStoreRepository.kt  # Reads songs/albums/artists/folders from MediaStore
│       └── PlaylistRepository.kt   # CRUD for playlists via Room
│
├── service/
│   └── MusicService.kt             # MediaSessionService (Media3 / ExoPlayer)
│
├── player/
│   ├── PlayerController.kt         # Singleton wrapper around ExoPlayer
│   └── QueueManager.kt             # Queue state: list, index, shuffle, repeat
│
├── ui/
│   ├── theme/
│   │   ├── Theme.kt
│   │   ├── Color.kt
│   │   └── Type.kt
│   ├── navigation/
│   │   ├── AppNavHost.kt           # NavHost definition
│   │   └── Screen.kt               # Sealed class of all routes
│   ├── components/                  # Reusable composables
│   │   ├── MiniPlayer.kt
│   │   ├── SongListItem.kt
│   │   ├── AlbumCard.kt
│   │   ├── ArtistCard.kt
│   │   ├── BottomNavBar.kt
│   │   └── TopBar.kt
│   └── screens/
│       ├── home/
│       │   └── HomeScreen.kt
│       ├── songs/
│       │   ├── SongsScreen.kt
│       │   └── SongsViewModel.kt
│       ├── albums/
│       │   ├── AlbumsScreen.kt
│       │   ├── AlbumsViewModel.kt
│       │   └── AlbumDetailScreen.kt
│       ├── artists/
│       │   ├── ArtistsScreen.kt
│       │   ├── ArtistsViewModel.kt
│       │   └── ArtistDetailScreen.kt
│       ├── folders/
│       │   ├── FoldersScreen.kt
│       │   ├── FoldersViewModel.kt
│       │   └── FolderDetailScreen.kt
│       ├── playlists/
│       │   ├── PlaylistsScreen.kt
│       │   ├── PlaylistsViewModel.kt
│       │   ├── PlaylistDetailScreen.kt
│       │   └── PlaylistDetailViewModel.kt
│       ├── player/
│       │   ├── NowPlayingScreen.kt
│       │   └── NowPlayingViewModel.kt
│       └── search/
│           ├── SearchScreen.kt
│           └── SearchViewModel.kt
│
└── di/
    └── AppModule.kt                 # Hilt module (repositories, DB, player)
```

### Key Architectural Rules
- **No business logic in Composables.** All logic lives in ViewModels or Repositories.
- **ViewModels never import Android UI** (`Context` is injected via Hilt's `@ApplicationContext` only).
- **Repository = single source of truth.** Screens never query MediaStore directly.
- **One ViewModel per screen.** Shared state (player, queue) lives in a shared `NowPlayingViewModel` scoped to the NavGraph.

---

## 3. Tech Stack & Dependencies

All versions are declared in `gradle/libs.versions.toml`. Never hardcode version strings.

### Core
| Library | Purpose | Alias in TOML |
|---|---|---|
| Jetpack Compose BOM `2024.09.00` | Compose version pinning | `androidx-compose-bom` |
| Material3 | UI components and theming | `androidx-material3` |
| Navigation Compose | Screen routing | `androidx-navigation-compose` |
| Hilt | Dependency injection | `hilt-android`, `hilt-compiler` |
| Hilt Navigation Compose | ViewModel injection in NavGraph | `androidx-hilt-navigation-compose` |

### Data
| Library | Purpose | Alias in TOML |
|---|---|---|
| Room (runtime, KTX, compiler) | Playlist persistence | `androidx-room-*` |
| Kotlin Coroutines + Flow | Async data streams | `kotlinx-coroutines-android` |
| DataStore Preferences | Simple settings (sort order, repeat mode) | `androidx-datastore-preferences` |

### Playback
| Library | Purpose | Alias in TOML |
|---|---|---|
| Media3 ExoPlayer | Audio playback engine | `androidx-media3-exoplayer` |
| Media3 Session | MediaSession + notification | `androidx-media3-session` |
| Media3 UI | Player controls (optional) | `androidx-media3-ui` |

### Image Loading
| Library | Purpose | Alias in TOML |
|---|---|---|
| Coil Compose | Album art from URI | `coil-compose` |

### Testing
| Library | Purpose |
|---|---|
| JUnit 4 | Unit tests |
| Mockk | Kotlin-friendly mocking |
| Turbine | Flow testing |
| Compose UI Test JUnit4 | Instrumented UI tests |
| Espresso Core | Instrumented integration tests |

---

## 4. Data Layer — Models & Database

### Core Data Models (data/model/)

```kotlin
// Song.kt — primary media entity
data class Song(
    val id: Long,               // MediaStore._ID
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,          // For album art URI construction
    val artistId: Long,
    val duration: Long,         // Milliseconds
    val path: String,           // Absolute file path
    val uri: Uri,               // Content URI (content://media/...)
    val trackNumber: Int,
    val year: Int,
    val size: Long,             // Bytes
    val dateAdded: Long,        // Epoch seconds
    val folderId: Long,
    val folderName: String
)

// Album.kt
data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val songCount: Int,
    val year: Int,
    val artUri: Uri             // content://media/external/audio/albumart/{id}
)

// Artist.kt
data class Artist(
    val id: Long,
    val name: String,
    val albumCount: Int,
    val songCount: Int
)

// Folder.kt
data class Folder(
    val id: Long,
    val name: String,
    val path: String,
    val songCount: Int
)

// Playlist.kt — backed by Room
data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int,
    val createdAt: Long
)
```

### Room Database (data/db/)

Only **user-created playlists** are stored in Room. Everything else is queried live from MediaStore.

```kotlin
// PlaylistEntity — playlists table
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

// PlaylistSongEntity — junction table
@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [ForeignKey(
        entity = PlaylistEntity::class,
        parentColumns = ["id"],
        childColumns = ["playlistId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PlaylistSongEntity(
    val playlistId: Long,
    val songId: Long,           // MediaStore song ID (not a FK — no Room table for songs)
    val position: Int           // For ordered playlists
)
```

### MediaStore Queries (data/repository/MediaStoreRepository.kt)

Use `ContentResolver` with `MediaStore.Audio.*` projections. All queries run on `Dispatchers.IO`. Return `Flow<List<T>>` so screens react to library changes.

```kotlin
// Example pattern — always follow this for MediaStore queries
fun getSongs(): Flow<List<Song>> = flow {
    val songs = mutableListOf<Song>()
    val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        // ... other columns
    )
    val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1"
    context.contentResolver.query(uri, projection, selection, null, null)?.use { cursor ->
        while (cursor.moveToNext()) {
            songs += cursor.toSong()  // extension function
        }
    }
    emit(songs)
}.flowOn(Dispatchers.IO)
```

**MediaStore rules:**
- Always use `cursor.use { }` to prevent leaks.
- Check `IS_MUSIC = 1` to filter non-music audio.
- Album art URI pattern: `ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)`
- On Android 10+ (API 29+), never use raw file `path` for playback; always use the `Uri`.

---

## 5. Media Playback Architecture

### ExoPlayer + Media3 Session

Playback runs in a **foreground service** (`MusicService`) to survive app backgrounding.

```kotlin
// MusicService.kt — skeleton
@AndroidEntryPoint
class MusicService : MediaSessionService() {
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    override fun onDestroy() {
        mediaSession.release()
        player.release()
        super.onDestroy()
    }
}
```

### PlayerController (player/PlayerController.kt)

A Hilt-injected singleton that connects to `MusicService` via `MediaController`. UI ViewModels interact only with `PlayerController`, never with `ExoPlayer` directly.

```kotlin
// Exposed interface (implement fully)
interface PlayerController {
    val currentSong: StateFlow<Song?>
    val playbackState: StateFlow<PlaybackState>   // IDLE, BUFFERING, READY, ENDED
    val isPlaying: StateFlow<Boolean>
    val currentPosition: StateFlow<Long>           // ms, updated every 500ms
    val duration: StateFlow<Long>
    val repeatMode: StateFlow<RepeatMode>          // OFF, ONE, ALL
    val shuffleEnabled: StateFlow<Boolean>

    fun play(song: Song, queue: List<Song>)
    fun playPause()
    fun seekTo(position: Long)
    fun skipNext()
    fun skipPrevious()
    fun setRepeatMode(mode: RepeatMode)
    fun toggleShuffle()
    fun addToQueue(song: Song)
    fun addToQueueNext(song: Song)
    fun removeFromQueue(index: Int)
    fun reorderQueue(from: Int, to: Int)
}

enum class RepeatMode { OFF, ONE, ALL }
```

### Queue Rules
- **Shuffle** randomizes the upcoming queue but remembers the original order to restore when shuffle is toggled off.
- **Repeat ONE** loops the current track.
- **Repeat ALL** wraps back to start when queue ends.
- `skipPrevious()` — if `currentPosition > 3000ms`, seek to 0; otherwise go to previous track.

---

## 6. UI Architecture & Screen Map

### Screen Hierarchy

```
MainActivity
└── AppNavHost
    ├── BottomNavBar (persistent: Songs, Albums, Artists, Folders, Playlists)
    ├── MiniPlayer (persistent, above BottomNavBar, visible when song is loaded)
    │
    ├── SongsScreen          /songs
    ├── AlbumsScreen         /albums
    │   └── AlbumDetailScreen   /albums/{albumId}
    ├── ArtistsScreen        /artists
    │   └── ArtistDetailScreen  /artists/{artistId}
    ├── FoldersScreen        /folders
    │   └── FolderDetailScreen  /folders/{folderId}
    ├── PlaylistsScreen      /playlists
    │   └── PlaylistDetailScreen  /playlists/{playlistId}
    ├── NowPlayingScreen     /now-playing  (full-screen modal)
    └── SearchScreen         /search
```

### MiniPlayer Behavior
- Always visible when a song is loaded (even paused).
- Shows: album art thumbnail, title, artist, play/pause button, skip-next button.
- Tapping anywhere on the MiniPlayer (except buttons) navigates to `NowPlayingScreen`.
- Hides/shows with an animated slide from bottom using `AnimatedVisibility`.

### NowPlayingScreen Layout
```
[ Album Art — large, rounded corners ]
[ Title, Artist ]
[ Seek Bar + timestamps ]
[ ← Prev | ⏯ Play/Pause | Next → ]
[ 🔀 Shuffle | 🔁 Repeat ]
[ ⋮ More (add to playlist, share, info) ]
[ Queue — collapsible bottom sheet ]
```

### Bottom Sheet for Queue
Use `ModalBottomSheet` (Material3). The queue is a `LazyColumn` with drag-to-reorder using `rememberReorderableLazyListState` (ReorderableCompose library or manual drag detection).

---

## 7. Features & Functional Requirements

### 7.1 Library Scanning
- Trigger MediaStore scan on app launch and when `ContentObserver` detects changes.
- Show an empty state illustration when no music is found.
- Provide a "Rescan" button in the overflow menu.
- Filter: only show files where `IS_MUSIC = 1` and `DURATION >= 30000` (≥30 seconds).

### 7.2 Songs Screen
- Alphabetically sorted list (default). Support sort options: Title, Artist, Album, Duration, Date Added.
- Each item: album art thumbnail (via Coil), title, artist, duration, overflow menu (→ play next, add to queue, add to playlist, go to album, go to artist, share, delete).
- Long-press enters multi-select mode: select all, deselect all, add selected to playlist, delete selected.
- Fast-scroll index (alphabetical letters on the right edge).

### 7.3 Albums Screen
- Grid layout (2 columns, adjustable in settings). Each card: album art, title, artist, song count.
- **AlbumDetailScreen**: header (large album art, title, artist, year, song count, total duration), song list sorted by track number, "Play All" and "Shuffle" action buttons.

### 7.4 Artists Screen
- List with artist name, album count, song count.
- **ArtistDetailScreen**: artist header, list of albums (horizontal scrolling grid), then all songs grouped by album.

### 7.5 Folders Screen
- List of folders containing music files, with path, song count.
- **FolderDetailScreen**: shows all songs in that folder, sorted by filename by default.

### 7.6 Playlists Screen
- User-created playlists only (no auto-generated "Recently Added" etc. in v1).
- FAB to create a new playlist (shows a dialog to enter a name).
- Each playlist: name, song count, first album art thumbnail.
- Long-press to rename or delete.
- **PlaylistDetailScreen**: reorderable song list (drag handle), "Play All", "Shuffle", add more songs button.

### 7.7 Search
- Single search bar queries songs (title, artist, album), albums, artists, and playlists simultaneously.
- Results grouped by category with a "Show all" link per category when results exceed 3.
- Search is performed locally on the in-memory list (no DB query needed for songs/albums/artists).
- Debounce input by 300ms before triggering search.

### 7.8 Now Playing / Player Controls
- Full details in [Section 6](#6-ui-architecture--screen-map).
- Seek bar: update `currentPosition` every 500ms via `LaunchedEffect` + `delay`.
- Lyrics (v2): placeholder tab, show "No lyrics available" for now.

### 7.9 Playback Queue
- Accessible from NowPlayingScreen via a bottom sheet.
- Shows current queue; currently playing item is highlighted.
- Drag-to-reorder supported.
- "Clear queue" option.
- Tapping any queue item plays it immediately.

### 7.10 Sleep Timer
- Settings screen entry → choose duration (15 / 30 / 45 / 60 min, or custom).
- Countdown shown in the notification and optionally on NowPlayingScreen.
- When timer expires, fade out volume over 10 seconds then pause.

### 7.11 Equalizer
- Launch system equalizer via `Intent("android.media.action.DISPLAY_AUDIO_EFFECT_CONTROL_PANEL")` with the ExoPlayer `audioSessionId`.
- Do not implement a custom equalizer UI in v1.

### 7.12 Settings Screen
- Theme: System default / Light / Dark.
- Sort orders per section (saved in DataStore).
- Grid column count for Albums (2 / 3).
- Sleep timer.
- Excluded folders (folders whose music will be hidden).
- About section: version, licenses.

---

## 8. Navigation

Use **Navigation Compose** with a `NavHostController`.

```kotlin
// Screen.kt
sealed class Screen(val route: String) {
    object Songs     : Screen("songs")
    object Albums    : Screen("albums")
    object AlbumDetail : Screen("albums/{albumId}") {
        fun createRoute(albumId: Long) = "albums/$albumId"
    }
    object Artists   : Screen("artists")
    object ArtistDetail : Screen("artists/{artistId}") {
        fun createRoute(artistId: Long) = "artists/$artistId"
    }
    object Folders   : Screen("folders")
    object FolderDetail : Screen("folders/{folderId}") {
        fun createRoute(folderId: Long) = "folders/$folderId"
    }
    object Playlists : Screen("playlists")
    object PlaylistDetail : Screen("playlists/{playlistId}") {
        fun createRoute(playlistId: Long) = "playlists/$playlistId"
    }
    object NowPlaying : Screen("now-playing")
    object Search    : Screen("search")
    object Settings  : Screen("settings")
}
```

### Navigation Rules
- Bottom nav tabs use `launchSingleTop = true` and `restoreState = true`.
- `NowPlayingScreen` is NOT in the bottom nav; it is navigated to from `MiniPlayer` or play actions.
- Back from `NowPlayingScreen` always returns to the previous back stack entry.
- Deep links into `AlbumDetailScreen` from `SongsScreen` overflow menu: use `navController.navigate(Screen.AlbumDetail.createRoute(albumId))`.

---

## 9. Permissions & Platform Constraints

### Required Permissions (AndroidManifest.xml)

```xml
<!-- Reading local music files -->
<!-- API < 33 -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />

<!-- API 33+ (Android 13+) -->
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />

<!-- Foreground service for playback -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />

<!-- Wake lock to prevent CPU sleep during playback -->
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

### Runtime Permission Flow
1. On first launch, show a rationale dialog explaining why audio permission is needed.
2. Use `rememberPermissionState` (Accompanist Permissions) or `ActivityResultContracts.RequestPermission`.
3. If denied, show an empty state with a "Grant Permission" button that re-triggers the request.
4. If permanently denied, show a button to open app settings.

### API-Level Guards
```kotlin
// Album art reading
val artUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    // Use MediaStore thumbnail API
    context.contentResolver.loadThumbnail(song.uri, Size(512, 512), null).toUri()
} else {
    ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), song.albumId)
}

// File deletion
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    // Use MediaStore.createDeleteRequest (shows system dialog)
} else {
    // Delete via ContentResolver.delete
}
```

---

## 10. State Management Patterns

### ViewModel Pattern (all screens follow this)

```kotlin
@HiltViewModel
class SongsViewModel @Inject constructor(
    private val mediaStoreRepository: MediaStoreRepository
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(SortOrder.TITLE_ASC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    val songs: StateFlow<List<Song>> = mediaStoreRepository.getSongs()
        .combine(_sortOrder) { songs, order -> songs.sortedBy(order) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSortOrder(order: SortOrder) { _sortOrder.value = order }
}
```

### UI State Sealed Class Pattern
Use for screens with loading / success / error states:

```kotlin
sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
```

### Shared Player State
`NowPlayingViewModel` is scoped to the root `NavGraph` and injected into any screen that needs player state (e.g., `SongsScreen` needs `isPlaying` to show a playing indicator on the current song).

```kotlin
// In any composable needing player state:
val playerViewModel: NowPlayingViewModel = hiltViewModel(
    viewModelStoreOwner = remember(navController) {
        navController.getBackStackEntry(Screen.Songs.route) // or root route
    }
)
```

---

## 11. Theming & Design System

### Color Palette
- **Primary**: Deep purple `#6750A4` (Material3 default purple).
- **Secondary**: Teal accent for playing indicators.
- **Surface**: Near-black `#1C1B1F` in dark mode; white in light mode.
- Dynamic color enabled on Android 12+ (`dynamicDarkColorScheme` / `dynamicLightColorScheme`).

### Typography
- Headlines: `Poppins` (or system default if not bundled).
- Body: Material3 default type scale.
- Song titles: `titleMedium`. Artist names: `bodyMedium` with reduced alpha.

### Component Conventions
```kotlin
// Album art loading — always use this pattern with Coil
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(song.albumArtUri)
        .crossfade(true)
        .placeholder(R.drawable.ic_album_placeholder)
        .error(R.drawable.ic_album_placeholder)
        .build(),
    contentDescription = stringResource(R.string.album_art_desc, song.album),
    contentScale = ContentScale.Crop,
    modifier = Modifier
        .size(48.dp)
        .clip(RoundedCornerShape(4.dp))
)

// Playing indicator — animated bars shown on currently playing song
@Composable
fun PlayingIndicator(modifier: Modifier = Modifier) { /* Animated 3-bar equalizer */ }
```

### Spacing & Shape Constants
```kotlin
object MusicaSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
}

object MusicaShapes {
    val card = RoundedCornerShape(12.dp)
    val albumArt = RoundedCornerShape(8.dp)
    val miniPlayer = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
}
```

---

## 12. Testing Strategy

### Unit Tests (`src/test/`)
- **Repository tests**: Mock `ContentResolver`, verify correct MediaStore column mapping.
- **ViewModel tests**: Use `TestCoroutineDispatcher` + Turbine to assert `StateFlow` emissions.
- **QueueManager tests**: Verify shuffle/unshuffle preserves original order, repeat modes, skip logic.
- **PlayerController tests**: Verify state transitions (idle → playing → paused).

### Instrumented Tests (`src/androidTest/`)
- **Permission flow**: Verify empty state shows on denial.
- **Navigation**: Verify correct screen opens from MiniPlayer tap, bottom nav tabs.
- **Playlist CRUD**: Create → add songs → reorder → delete via Room DAO tests.
- **NowPlayingScreen**: Play/pause button toggles `isPlaying`; seek bar updates position.

### Test Naming Convention
```kotlin
@Test
fun `given empty MediaStore, songs screen shows empty state`() { }

@Test
fun `when shuffle enabled, skipNext cycles through all songs before repeating`() { }
```

---

## 13. Build & Gradle Conventions

### libs.versions.toml — Required Sections

```toml
[versions]
kotlin = "2.0.21"
agp = "8.13.2"
compose-bom = "2024.09.00"
hilt = "2.51"
room = "2.6.1"
media3 = "1.4.1"
navigation-compose = "2.8.3"
coil = "2.7.0"
datastore = "1.1.1"
coroutines = "1.8.1"
mockk = "1.13.12"
turbine = "1.1.0"

[libraries]
# Compose
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation-compose" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
androidx-hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }

# Room
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# Media3
androidx-media3-exoplayer = { group = "androidx.media3", name = "media3-exoplayer", version.ref = "media3" }
androidx-media3-session = { group = "androidx.media3", name = "media3-session", version.ref = "media3" }
androidx-media3-ui = { group = "androidx.media3", name = "media3-ui", version.ref = "media3" }

# Coil
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }

# DataStore
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# Testing
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
```

### build.gradle.kts — Required Plugins

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)       // For Room + Hilt annotation processing
}

kapt { correctErrorTypes = true }  // or use KSP for Room
```

### Gradle Commands
```bash
./gradlew :app:assembleDebug          # Build debug APK
./gradlew :app:testDebug              # Unit tests
./gradlew :app:connectedAndroidTest   # Instrumented tests (needs device/emulator)
./gradlew :app:lint                   # Lint checks
./gradlew :app:kspDebugKotlin         # Run KSP (Room/Hilt codegen)
```

---

## 14. Code Style & Non-Negotiables

1. **Compose-only UI** — zero View-based XML layouts in `src/main/res/layout/`. Existing XMLs (themes, strings, drawables) are fine.
2. **Kotlin Coroutines everywhere** — no `Thread`, no `AsyncTask`, no `RxJava`.
3. **StateFlow + collectAsStateWithLifecycle** — never use `LiveData` or `collectAsState()` without lifecycle awareness.
4. **Hilt for all DI** — no manual `object` singletons for repositories or the player.
5. **String resources** — all user-visible strings must be in `res/values/strings.xml`. No hardcoded strings in Kotlin files.
6. **Accessibility** — every interactive element must have a `contentDescription`. Album art must describe what it shows.
7. **Error boundaries** — every `Flow` collected in a ViewModel must have a `.catch { }` operator.
8. **No blocking the main thread** — all I/O (MediaStore, Room, file ops) must be on `Dispatchers.IO`.
9. **@Preview for every composable** — wrap in `MusicaTheme`; provide sample data via `@PreviewParameter`.
10. **Stable function signatures** — composables with list parameters should use `ImmutableList` (kotlinx.collections.immutable) or `@Stable` annotations to prevent unnecessary recompositions.

---

## 15. Common Tasks — Step-by-Step

### Add a New Screen

1. Create `ui/screens/<feature>/<Feature>Screen.kt` and `<Feature>ViewModel.kt`.
2. Add a `Screen` object in `Screen.kt`.
3. Add a `composable(Screen.Feature.route) { FeatureScreen() }` entry in `AppNavHost.kt`.
4. Inject ViewModel with `val vm: FeatureViewModel = hiltViewModel()`.
5. Collect state: `val uiState by vm.uiState.collectAsStateWithLifecycle()`.
6. Add `@Preview` annotated function at the bottom of the file.

### Add a New MediaStore Query

1. Add a new method to `MediaStoreRepository` returning `Flow<List<YourModel>>`.
2. Define the projection array (only columns you need — don't use `null` projection).
3. Wrap in `flow { ... }.flowOn(Dispatchers.IO)`.
4. Expose via a `StateFlow` in the relevant ViewModel using `.stateIn(...)`.

### Create / Modify a Playlist

1. All playlist writes go through `PlaylistRepository` which wraps `PlaylistDao`.
2. Use `viewModelScope.launch { repository.createPlaylist(name) }` in the ViewModel.
3. Never do Room operations on the main thread; the DAO suspend functions handle this.

### Change Playback Behavior (Shuffle / Repeat)

1. Call `playerController.toggleShuffle()` or `playerController.setRepeatMode(mode)`.
2. These methods update `ExoPlayer`'s shuffle/repeat settings AND update the relevant `StateFlow`.
3. The `NowPlayingViewModel` observes these flows and exposes them to the UI.
4. Persist the last used repeat mode and shuffle state in `DataStore` so they survive app restarts.

### Handling Album Art

```kotlin
// Always compute album art URI this way — centralize in an extension function
fun Song.albumArtUri(): Uri =
    ContentUris.withAppendedId(
        Uri.parse("content://media/external/audio/albumart"),
        albumId
    )

// Load with Coil — don't use Glide or Picasso
AsyncImage(
    model = song.albumArtUri(),
    contentDescription = null, // decorative; title is already in adjacent Text
    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)),
    contentScale = ContentScale.Crop
)
```

### Adding a Sort Option

1. Add a value to the `SortOrder` enum in `data/model/SortOrder.kt`.
2. Add a branch to the `List<Song>.sortedBy(order: SortOrder)` extension function.
3. Add a menu item in the relevant screen's `TopBar` dropdown.
4. Persist the selected sort order in `DataStore` keyed per section (`songs_sort_order`, `albums_sort_order`, etc.).

---

*Last updated: project bootstrap. Keep this file up to date as new patterns are established.*
