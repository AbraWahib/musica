package com.abra.musica.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.abra.musica.R
import com.abra.musica.ui.screens.albums.AlbumDetailScreen
import com.abra.musica.ui.screens.albums.AlbumsScreen
import com.abra.musica.ui.screens.artists.ArtistDetailScreen
import com.abra.musica.ui.screens.artists.ArtistsScreen
import com.abra.musica.ui.screens.folders.FolderDetailScreen
import com.abra.musica.ui.screens.folders.FoldersScreen
import com.abra.musica.ui.screens.library.FavoriteSongsScreen
import com.abra.musica.ui.screens.library.LibraryScreen
import com.abra.musica.ui.screens.library.RecentlyPlayedScreen
import com.abra.musica.ui.screens.playlists.PlaylistDetailScreen
import com.abra.musica.ui.screens.playlists.PlaylistsScreen
import com.abra.musica.ui.screens.search.SearchScreen
import com.abra.musica.ui.screens.settings.SettingsScreen
import com.abra.musica.ui.screens.songs.SongsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    songsReselectionCount: Int = 0,
    libraryReselectionCount: Int = 0,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Songs.route,
        modifier = modifier
    ) {
        composable(Screen.Songs.route) {
            SongsScreen(
                reselectionCount = songsReselectionCount,
                onAlbumClick = { albumId ->
                    navController.navigate(Screen.AlbumDetail.createRoute(albumId))
                },
                onArtistClick = { artistId ->
                    navController.navigate(Screen.ArtistDetail.createRoute(artistId))
                },
                onFolderClick = { folderId ->
                    navController.navigate(Screen.FolderDetail.createRoute(folderId))
                }
            )
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
            FoldersScreen(
                onFolderClick = { folderId ->
                    navController.navigate(Screen.FolderDetail.createRoute(folderId))
                }
            )
        }
        composable(Screen.FolderDetail.route) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString("folderId")?.toLongOrNull()
            if (folderId != null) {
                FolderDetailScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
        composable(Screen.Playlists.route) {
            PlaylistsScreen(
                onPlaylistClick = { playlistId ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                }
            )
        }
        composable(Screen.PlaylistDetail.route) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId")?.toLongOrNull()
            if (playlistId != null) {
                PlaylistDetailScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
        composable(Screen.Search.route) {
            SearchScreen(
                onArtistClick = { artistId ->
                    navController.navigate(Screen.ArtistDetail.createRoute(artistId))
                },
                onAlbumClick = { albumId ->
                    navController.navigate(Screen.AlbumDetail.createRoute(albumId))
                },
                onPlaylistClick = { playlistId ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                }
            )
        }
        composable(Screen.Library.route) {
            LibraryScreen(
                reselectionCount = libraryReselectionCount,
                onNavigateToFavourites = {
                    navController.navigate(Screen.Favourites.route)
                },
                onNavigateToPlaylists = {
                    navController.navigate(Screen.Playlists.route)
                },
                onNavigateToRecentlyPlayed = {
                    navController.navigate(Screen.RecentlyPlayed.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(Screen.Favourites.route) {
            FavoriteSongsScreen()
        }
        composable(Screen.RecentlyPlayed.route) {
            RecentlyPlayedScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    message: String
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
