package com.abra.musica.ui.screens.songs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abra.musica.R
import com.abra.musica.data.model.Song
import com.abra.musica.ui.components.SongListItem
import com.abra.musica.ui.screens.songs.components.Events

@Composable
fun SongsScreen(
   viewModel: SongsViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()

    SongScreenContent(
        songs = songs,
        sortOrder = sortOrder,
        currentSong = currentSong,
        isPlaying = isPlaying,
        onEvent = viewModel::onEvent,
        onSortOrderChange = viewModel::setSortOrder
    )
}

@Composable
fun SongScreenContent(
    songs: List<Song>,
    sortOrder: SortOrder,
    currentSong: Song?,
    isPlaying: Boolean,
    onEvent: (Events) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar with sort options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.songs),
                style = MaterialTheme.typography.headlineSmall
            )

            var showSortMenu by remember { mutableStateOf(false) }
            IconButton(onClick = { showSortMenu = true }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = stringResource(R.string.sort_by)
                )
            }

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false }
            ) {
                SortOrder.entries.forEach { order ->
                    DropdownMenuItem(
                        text = { Text(order.displayName()) },
                        onClick = {
                            onSortOrderChange(order)
                            showSortMenu = false
                        }
                    )
                }
            }
        }

        if (songs.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = stringResource(R.string.empty_library),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = stringResource(R.string.empty_library_desc),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            // Songs list
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(songs, key = { it.id }) { song ->
                    SongListItem(
                        song = song,
                        isPlaying = isPlaying && song.id == currentSong?.id,
                        onClick = { onEvent(Events.OnSongClick(song)) }
                    )
                }
            }
        }
    }
}

private fun SortOrder.displayName(): String {
    return when (this) {
        SortOrder.TITLE_ASC -> "Title (A-Z)"
        SortOrder.TITLE_DESC -> "Title (Z-A)"
        SortOrder.ARTIST_ASC -> "Artist (A-Z)"
        SortOrder.ARTIST_DESC -> "Artist (Z-A)"
        SortOrder.ALBUM_ASC -> "Album (A-Z)"
        SortOrder.ALBUM_DESC -> "Album (Z-A)"
        SortOrder.DURATION_ASC -> "Duration (Shortest)"
        SortOrder.DURATION_DESC -> "Duration (Longest)"
        SortOrder.DATE_ADDED_DESC -> "Date Added (Newest)"
    }
}
