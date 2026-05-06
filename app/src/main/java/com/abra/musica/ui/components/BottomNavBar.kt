package com.abra.musica.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
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
            BottomNavItem(Screen.Songs.route, R.string.songs, Icons.Default.MusicNote),
            BottomNavItem(Screen.Search.route, R.string.search, Icons.Default.Search),
            BottomNavItem(Screen.Library.route, R.string.library, Icons.Default.LibraryMusic)
        )

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = null) },
                label = { Text(stringResource(item.labelRes)) },
                selected = item.route == selectedTopLevelRoute(currentRoute),
                onClick = {
                    navController.navigate(item.route) {
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

private fun selectedTopLevelRoute(currentRoute: String?): String? {
    return when (currentRoute) {
        Screen.Albums.route,
        Screen.AlbumDetail.route,
        Screen.Artists.route,
        Screen.ArtistDetail.route,
        Screen.Folders.route,
        Screen.FolderDetail.route -> Screen.Songs.route
        Screen.Playlists.route,
        Screen.PlaylistDetail.route,
        Screen.Favourites.route,
        Screen.RecentlyPlayed.route,
        Screen.Settings.route -> Screen.Library.route
        else -> currentRoute
    }
}

private data class BottomNavItem(
    val route: String,
    val labelRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
