package com.abra.musica.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.abra.musica.ui.screens.albums.AlbumDetailScreen
import com.abra.musica.ui.screens.albums.AlbumsScreen
import com.abra.musica.ui.screens.artists.ArtistDetailScreen
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
            AlbumsScreen(
                onAlbumClick = { albumId ->
                    navController.navigate(Screen.AlbumDetail.createRoute(albumId))
                }
            )
        }
        composable(Screen.AlbumDetail.route) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId")?.toLongOrNull()
            if (albumId != null) {
                AlbumDetailScreen(
                    onBackClick = { navController.popBackStack() },
                    onGoToArtist = { artistId ->
                        navController.navigate(Screen.ArtistDetail.createRoute(artistId))
                    }
                )
            }
        }
        composable(Screen.Artists.route) {
            ArtistsScreen(
                onArtistClick = { artistId ->
                    navController.navigate(Screen.ArtistDetail.createRoute(artistId))
                }
            )
        }
        composable(Screen.ArtistDetail.route) { backStackEntry ->
            val artistId = backStackEntry.arguments?.getString("artistId")?.toLongOrNull()
            if (artistId != null) {
                ArtistDetailScreen(
                    onBackClick = { navController.popBackStack() },
                    onGoToAlbum = { albumId ->
                        navController.navigate(Screen.AlbumDetail.createRoute(albumId))
                    }
                )
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
        composable(Screen.Search.route) {
            // TODO: SearchScreen()
        }
        composable(Screen.Settings.route) {
            // TODO: SettingsScreen()
        }
    }
}
