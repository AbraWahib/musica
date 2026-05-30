package com.abra.musica.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.abra.musica.R
import com.abra.musica.ui.components.MainScreenCustomAppBar

@Composable
fun LibraryScreen(
    reselectionCount: Int = 0,
    onNavigateToFavourites: () -> Unit = {},
    onNavigateToPlaylists: () -> Unit = {},
    onNavigateToRecentlyPlayed: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val listState = rememberLazyListState()

    LaunchedEffect(reselectionCount) {
        if (reselectionCount <= 0) return@LaunchedEffect
        listState.animateScrollToItem(0)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MainScreenCustomAppBar(title = stringResource(R.string.library))

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
        ) {
            item {
                LibraryNavigationItem(
                    icon = Icons.Default.Favorite,
                    title = stringResource(R.string.favourites),
                    subtitle = stringResource(R.string.favourites_desc),
                    onClick = onNavigateToFavourites
                )
            }
            item {
                LibraryNavigationItem(
                    icon = Icons.AutoMirrored.Filled.PlaylistPlay,
                    title = stringResource(R.string.playlists),
                    subtitle = stringResource(R.string.playlists_desc),
                    onClick = onNavigateToPlaylists
                )
            }
            item {
                LibraryNavigationItem(
                    icon = Icons.Default.History,
                    title = stringResource(R.string.recently_played),
                    subtitle = stringResource(R.string.recently_played_desc),
                    onClick = onNavigateToRecentlyPlayed
                )
            }
            item {
                LibraryNavigationItem(
                    icon = Icons.Default.Settings,
                    title = stringResource(R.string.settings),
                    subtitle = stringResource(R.string.settings_desc),
                    onClick = onNavigateToSettings
                )
            }
        }
    }
}

@Composable
private fun LibraryNavigationItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
