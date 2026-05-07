package com.abra.musica.ui.screens.songs

import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abra.musica.R
import com.abra.musica.data.model.Song
import com.abra.musica.data.model.SortOrder
import com.abra.musica.ui.components.AddToPlaylistDialog
import com.abra.musica.ui.components.SongListItem
import com.abra.musica.ui.screens.albums.AlbumsScreen
import com.abra.musica.ui.screens.artists.ArtistsScreen
import com.abra.musica.ui.screens.folders.FoldersScreen
import kotlinx.coroutines.launch

@Composable
fun SongsScreen(
    viewModel: SongsViewModel = hiltViewModel(),
    reselectionCount: Int = 0,
    onAlbumClick: (Long) -> Unit = {},
    onArtistClick: (Long) -> Unit = {},
    onFolderClick: (Long) -> Unit = {}
) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val favoriteSongIds by viewModel.favoriteSongIds.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var playlistTargetSong by remember { mutableStateOf<Song?>(null) }
    val pagerState = rememberPagerState(pageCount = { 4 })
    val songsListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val tabs = listOf(
        R.string.all_songs,
        R.string.artists,
        R.string.albums,
        R.string.folders
    )

    LaunchedEffect(reselectionCount) {
        if (reselectionCount <= 0) return@LaunchedEffect
        pagerState.animateScrollToPage(0)
        songsListState.animateScrollToItem(0)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.songs),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
        )
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, titleRes ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(stringResource(titleRes)) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> SongScreenContent(
                    songs = songs,
                    isLoading = isLoading,
                    favoriteSongIds = favoriteSongIds,
                    listState = songsListState,
                    onSongClick = {
                        viewModel.playSong(it)
                        Log.d("Play Song", "SongsScreen: ${it.title}")
                    },
                    onSortOrderChange = viewModel::setSortOrder,
                    onPlayNext = viewModel::playNext,
                    onAddToQueue = viewModel::addToQueue,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onGoToAlbum = { onAlbumClick(it.albumId) },
                    onGoToArtist = { onArtistClick(it.artistId) },
                    onAddToPlaylist = { playlistTargetSong = it },
                    onShare = { song ->
                        context.startActivity(
                            Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "audio/*"
                                    putExtra(Intent.EXTRA_STREAM, song.uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                },
                                context.getString(R.string.share)
                            )
                        )
                    },
                    onDelete = viewModel::deleteSong,
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

    playlistTargetSong?.let { song ->
        AddToPlaylistDialog(
            playlists = playlists,
            onDismiss = { playlistTargetSong = null },
            onAddToPlaylist = { playlistId ->
                viewModel.addSongToPlaylist(song, playlistId)
                playlistTargetSong = null
            },
            onCreatePlaylist = { name ->
                viewModel.createPlaylist(name, song)
                playlistTargetSong = null
            }
        )
    }
}

@Composable
fun SongScreenContent(
    songs: List<Song>,
    isLoading: Boolean,
    favoriteSongIds: Set<Long>,
    listState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState(),
    onSongClick: (Song) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onGoToAlbum: (Song) -> Unit,
    onGoToArtist: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onShare: (Song) -> Unit,
    onDelete: (Song) -> Unit,
    showHeader: Boolean = true
) {
    val showSortButton by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }

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
            AnimatedVisibility(visible = showSortButton) {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = stringResource(R.string.sort_by)
                    )
                }
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

        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            songs.isEmpty() -> Box(
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
                items(songs, key = { it.id }) { song ->
                    SongListItem(
                        song = song,
                        onClick = { onSongClick(song) },
                        isFavorite = song.id in favoriteSongIds,
                        onPlayNext = { onPlayNext(song) },
                        onAddToQueue = { onAddToQueue(song) },
                        onToggleFavorite = { onToggleFavorite(song) },
                        onAddToPlaylist = { onAddToPlaylist(song) },
                        onGoToAlbum = { onGoToAlbum(song) },
                        onGoToArtist = { onGoToArtist(song) },
                        onShare = { onShare(song) },
                        onDelete = { onDelete(song) }
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
