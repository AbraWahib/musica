package com.abra.musica.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.abra.musica.ui.player.NowPlayingScreen
import com.abra.musica.ui.screens.albums.AlbumsScreen
import com.abra.musica.ui.screens.artists.ArtistsScreen
import com.abra.musica.ui.screens.folders.FoldersScreen
import com.abra.musica.ui.screens.playlists.PlaylistsScreen
import com.abra.musica.ui.screens.songs.SongsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Songs.route,
        modifier = modifier
    ) {
        composable(Screen.Songs.route) {
            SongsScreen()
        }
        composable(Screen.Albums.route) {
            AlbumsScreen()
        }
        composable(Screen.AlbumDetail.route) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId")?.toLongOrNull()
            if (albumId != null) {
                // TODO: AlbumDetailScreen(albumId)
            }
        }
        composable(Screen.Artists.route) {
            ArtistsScreen()
        }
        composable(Screen.ArtistDetail.route) { backStackEntry ->
            val artistId = backStackEntry.arguments?.getString("artistId")?.toLongOrNull()
            if (artistId != null) {
                // TODO: ArtistDetailScreen(artistId)
            }
        }
        composable(Screen.Folders.route) {
            FoldersScreen()
        }
        composable(Screen.FolderDetail.route) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString("folderId")?.toLongOrNull()
            if (folderId != null) {
                // TODO: FolderDetailScreen(folderId)
            }
        }
        composable(Screen.Playlists.route) {
            PlaylistsScreen()
        }
        composable(Screen.PlaylistDetail.route) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId")?.toLongOrNull()
            if (playlistId != null) {
                // TODO: PlaylistDetailScreen(playlistId)
            }
        }
        composable(Screen.NowPlaying.route) {
            NowPlayingScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Search.route) {
            // TODO: SearchScreen()
        }
        composable(Screen.Settings.route) {
            // TODO: SettingsScreen()
        }
    }
}
