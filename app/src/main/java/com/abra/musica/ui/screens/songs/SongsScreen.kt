package com.abra.musica.ui.screens.songs

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abra.musica.R
import com.abra.musica.data.model.Song
import com.abra.musica.ui.components.SongListItem
import com.abra.musica.ui.screens.albums.AlbumsScreen
import com.abra.musica.ui.screens.artists.ArtistsScreen
import com.abra.musica.ui.screens.folders.FoldersScreen

@Composable
fun SongsScreen(
   viewModel: SongsViewModel = hiltViewModel(),
   onAlbumClick: (Long) -> Unit = {},
   onArtistClick: (Long) -> Unit = {},
   onFolderClick: (Long) -> Unit = {}
) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        R.string.all_songs,
        R.string.artists,
        R.string.albums,
        R.string.folders
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.songs),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
        )
        ScrollableTabRow(
            modifier = Modifier.fillMaxWidth(),
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp
        ) {
            tabs.forEachIndexed { index, titleRes ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(stringResource(titleRes)) }
                )
            }
        }

        when (selectedTab) {
            0 -> SongScreenContent(
                songs = songs,
                onSongClick = {
                    viewModel.playSong(it)
                    Log.d("Play Song", "SongsScreen: ${it.title}")
                },
                onSortOrderChange = viewModel::setSortOrder,
                showHeader = false
            )
            1 -> ArtistsScreen(
                onArtistClick = onArtistClick,
                showHeader = false
            )
            2 -> AlbumsScreen(
                onAlbumClick = onAlbumClick,
                showHeader = false
            )
            3 -> FoldersScreen(
                onFolderClick = onFolderClick,
                showHeader = false
            )
        }
    }
}

@Composable
fun SongScreenContent(
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    showHeader: Boolean = true
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showHeader) {
                Text(
                    text = stringResource(R.string.songs),
                    style = MaterialTheme.typography.headlineSmall
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
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
                        onClick = { onSongClick(song)
                            }
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
