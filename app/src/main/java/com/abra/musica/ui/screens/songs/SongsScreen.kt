package com.abra.musica.ui.screens.songs

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abra.musica.R
import com.abra.musica.data.model.Song
import com.abra.musica.ui.components.AddToPlaylistDialog
import com.abra.musica.ui.components.MainScreenCustomAppBar
import com.abra.musica.ui.components.SongItemEvent
import com.abra.musica.ui.screens.albums.AlbumsScreen
import com.abra.musica.ui.screens.artists.ArtistsScreen
import com.abra.musica.ui.screens.folders.FoldersScreen
import com.abra.musica.ui.screens.songs.components.SongScreenContent
import com.abra.musica.ui.screens.songs.components.SongsScreenEvent
import kotlinx.coroutines.launch

@Composable
fun SongsScreen(
    viewModel: SongsViewModel = hiltViewModel(),
    reselectionCount: Int = 0,
    onAlbumClick: (Long) -> Unit = {},
    onArtistClick: (Long) -> Unit = {},
    onFolderClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
    val resources = LocalResources.current

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

    fun handleEvent(event: SongsScreenEvent) {
        when (event) {
            SongsScreenEvent.OnPlayAll -> viewModel.playAll()
            is SongsScreenEvent.OnSortOrderChange -> viewModel.setSortOrder(event.sortOrder)
            is SongsScreenEvent.OnItemEvent -> when (event.itemEvent) {
                SongItemEvent.OnClick -> viewModel.playSong(event.song)
                SongItemEvent.OnPlayNext -> viewModel.playNext(event.song)
                SongItemEvent.OnAddToQueue -> viewModel.addToQueue(event.song)
                SongItemEvent.OnToggleFavorite -> viewModel.toggleFavorite(event.song)
                SongItemEvent.OnGoToAlbum -> onAlbumClick(event.song.albumId)
                SongItemEvent.OnGoToArtist -> onArtistClick(event.song.artistId)
                SongItemEvent.OnAddToPlaylist -> playlistTargetSong = event.song
                SongItemEvent.OnShare -> {
                    context.startActivity(
                        Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply {
                                type = "audio/*"
                                putExtra(Intent.EXTRA_STREAM, event.song.uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            },
                            resources.getString(R.string.share)
                        )
                    )
                }
                SongItemEvent.OnDelete -> deleteTargetSong = event.song
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
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
                    uiState = uiState,
                    listState = songsListState,
                    onEvent = ::handleEvent
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
            playlists = uiState.playlists,
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
