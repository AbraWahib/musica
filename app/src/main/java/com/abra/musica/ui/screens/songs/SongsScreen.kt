package com.abra.musica.ui.screens.songs

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abra.musica.R
import com.abra.musica.data.model.Song
import com.abra.musica.data.model.SortOrder
import com.abra.musica.ui.components.AddToPlaylistDialog
import com.abra.musica.ui.components.MainScreenCustomAppBar
import com.abra.musica.ui.components.SongListItem
import com.abra.musica.ui.screens.albums.AlbumsScreen
import com.abra.musica.ui.screens.artists.ArtistsScreen
import com.abra.musica.ui.screens.folders.FoldersScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsScreen(
    viewModel: SongsViewModel = hiltViewModel(),
    reselectionCount: Int = 0,
    onAlbumClick: (Long) -> Unit = {},
    onArtistClick: (Long) -> Unit = {},
    onFolderClick: (Long) -> Unit = {}
) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val favoriteSongIds by viewModel.favoriteSongIds.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var playlistTargetSong by remember { mutableStateOf<Song?>(null) }
    var deleteTargetSong by remember { mutableStateOf<Song?>(null) }
    val deletePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { }
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

    LaunchedEffect(viewModel) {
        viewModel.deletePermissionRequests.collect { intentSender ->
            deletePermissionLauncher.launch(
                IntentSenderRequest.Builder(intentSender).build()
            )
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        MainScreenCustomAppBar(title = stringResource(R.string.songs))
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
                    sortOrder = sortOrder,
                    onSongClick = {
                        viewModel.playSong(it)
                        Log.d("Play Song", "SongsScreen: ${it.title}")
                    },
                    onSortOrderChange = viewModel::setSortOrder,
                    onPlayAll = viewModel::playAll,
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
                    onDelete = { deleteTargetSong = it },
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

    deleteTargetSong?.let { song ->
        AlertDialog(
            onDismissRequest = { deleteTargetSong = null },
            title = { Text(stringResource(R.string.delete_song_title)) },
            text = { Text(stringResource(R.string.delete_song_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSong(song)
                        deleteTargetSong = null
                    }
                ) {
                    Text(stringResource(R.string.delete_permanently))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTargetSong = null }) {
                    Text(stringResource(R.string.cancel))
                }
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
    sortOrder: SortOrder,
    onSongClick: (Song) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onPlayAll: () -> Unit,
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

    var showSortDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
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
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onPlayAll,
                            enabled = songs.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.play_all_with_count, songs.size),
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

    if (showSortDialog) {
        SortDialog(
            currentSortOrder = sortOrder,
            onDismiss = { showSortDialog = false },
            onApply = { order ->
                onSortOrderChange(order)
                showSortDialog = false
            }
        )
    }
}

@Composable
private fun SortDialog(
    currentSortOrder: SortOrder,
    onDismiss: () -> Unit,
    onApply: (SortOrder) -> Unit
) {
    var category by remember(currentSortOrder) { mutableStateOf(currentSortOrder.category()) }
    var ascending by remember(currentSortOrder) { mutableStateOf(currentSortOrder.isAscending()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sort_by)) },
        text = {
            Column {
                SortCategory.entries.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = category == option,
                            onClick = { category = option }
                        )
                        Text(text = option.label())
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (ascending) {
                            stringResource(R.string.ascending)
                        } else {
                            stringResource(R.string.descending)
                        }
                    )
                    Switch(
                        checked = ascending,
                        onCheckedChange = { ascending = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(category.toSortOrder(ascending)) }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

private enum class SortCategory {
    NAME,
    TIME,
    ARTIST,
    ALBUM,
    DURATION
}

@Composable
private fun SortCategory.label(): String {
    return when (this) {
        SortCategory.NAME -> stringResource(R.string.sort_name)
        SortCategory.TIME -> stringResource(R.string.sort_time)
        SortCategory.ARTIST -> stringResource(R.string.sort_artist)
        SortCategory.ALBUM -> stringResource(R.string.sort_album)
        SortCategory.DURATION -> stringResource(R.string.sort_duration)
    }
}

private fun SortOrder.category(): SortCategory {
    return when (this) {
        SortOrder.TITLE_ASC,
        SortOrder.TITLE_DESC -> SortCategory.NAME
        SortOrder.DATE_ADDED_ASC,
        SortOrder.DATE_ADDED_DESC -> SortCategory.TIME
        SortOrder.ARTIST_ASC,
        SortOrder.ARTIST_DESC -> SortCategory.ARTIST
        SortOrder.ALBUM_ASC,
        SortOrder.ALBUM_DESC -> SortCategory.ALBUM
        SortOrder.DURATION_ASC,
        SortOrder.DURATION_DESC -> SortCategory.DURATION
    }
}

private fun SortOrder.isAscending(): Boolean {
    return when (this) {
        SortOrder.TITLE_ASC,
        SortOrder.DATE_ADDED_ASC,
        SortOrder.ARTIST_ASC,
        SortOrder.ALBUM_ASC,
        SortOrder.DURATION_ASC -> true
        SortOrder.TITLE_DESC,
        SortOrder.DATE_ADDED_DESC,
        SortOrder.ARTIST_DESC,
        SortOrder.ALBUM_DESC,
        SortOrder.DURATION_DESC -> false
    }
}

private fun SortCategory.toSortOrder(ascending: Boolean): SortOrder {
    return when (this) {
        SortCategory.NAME -> if (ascending) SortOrder.TITLE_ASC else SortOrder.TITLE_DESC
        SortCategory.TIME -> if (ascending) SortOrder.DATE_ADDED_ASC else SortOrder.DATE_ADDED_DESC
        SortCategory.ARTIST -> if (ascending) SortOrder.ARTIST_ASC else SortOrder.ARTIST_DESC
        SortCategory.ALBUM -> if (ascending) SortOrder.ALBUM_ASC else SortOrder.ALBUM_DESC
        SortCategory.DURATION -> if (ascending) SortOrder.DURATION_ASC else SortOrder.DURATION_DESC
    }
}
