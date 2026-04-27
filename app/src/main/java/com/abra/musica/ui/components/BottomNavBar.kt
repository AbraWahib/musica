package com.abra.musica.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.abra.musica.R
import com.abra.musica.ui.navigation.Screen

@Composable
fun BottomNavBar(
    navController: NavController
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        val items = listOf(
            Triple(Screen.Songs.route, R.string.songs, Icons.Default.MusicNote),
            Triple(Screen.Albums.route, R.string.albums, Icons.Default.Album),
            Triple(Screen.Artists.route, R.string.artists, Icons.Default.Person),
            Triple(Screen.Folders.route, R.string.folders, Icons.Default.Folder),
            Triple(Screen.Playlists.route, R.string.playlists, Icons.AutoMirrored.Filled.PlaylistPlay
            )
        )

        items.forEach { (route, labelRes, icon) ->
            NavigationBarItem(
                icon = { Icon(imageVector = icon, contentDescription = null) },
                label = { Text(stringResource(labelRes)) },
                selected = currentRoute == route,
                onClick = {
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                    }
                }
            )
        }
    }
}
