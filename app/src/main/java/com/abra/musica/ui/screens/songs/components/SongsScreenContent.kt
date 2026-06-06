package com.abra.musica.ui.screens.songs.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.abra.musica.R
import com.abra.musica.ui.components.SongListItem

@Composable
fun SongScreenContent(
    uiState: SongsUiState,
    listState: LazyListState = rememberLazyListState(),
    onEvent: (SongsScreenEvent) -> Unit,
) {

    var showSortDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            uiState.songs.isEmpty() -> Box(
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
            else -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onEvent(SongsScreenEvent.OnPlayAll) },
                            enabled = uiState.songs.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.play_all_with_count, uiState.songs.size),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        IconButton(onClick = { showSortDialog = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = stringResource(R.string.sort_by)
                            )
                        }
                    }
                }
                items(uiState.songs, key = { it.id }) { song ->
                    SongListItem(
                        song = song,
                        isFavorite = song.id in uiState.favoriteSongIds,
                        onEvent = { itemEvent ->
                            onEvent(SongsScreenEvent.OnItemEvent(song, itemEvent))
                        }
                    )
                }
            }
        }
    }

    if (showSortDialog) {
        SortDialog(
            currentSortOrder = uiState.sortOrder,
            onDismiss = { showSortDialog = false },
            onApply = { order ->
                onEvent(SongsScreenEvent.OnSortOrderChange(order))
                showSortDialog = false
            }
        )
    }
}
