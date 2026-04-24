package com.abra.musica.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
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
            Triple(Screen.Songs.route, R.string.songs, R.drawable.ic_music_note),
            Triple(Screen.Albums.route, R.string.albums, R.drawable.ic_album),
            Triple(Screen.Artists.route, R.string.artists, R.drawable.ic_person),
            Triple(Screen.Folders.route, R.string.folders, R.drawable.ic_folder),
            Triple(Screen.Playlists.route, R.string.playlists, R.drawable.ic_playlist)
        )

        items.forEach { (route, labelRes, iconRes) ->
            NavigationBarItem(
                icon = { Icon(painterResource(iconRes), contentDescription = null) },
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
