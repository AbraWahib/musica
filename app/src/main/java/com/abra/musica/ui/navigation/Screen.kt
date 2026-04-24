package com.abra.musica.ui.navigation

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
