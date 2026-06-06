package com.abra.musica.ui.screens.folders

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abra.musica.R
import com.abra.musica.data.model.Song
import com.abra.musica.ui.components.SongItemEvent
import com.abra.musica.ui.components.SongListItem

@Composable
fun FolderDetailScreen(
    onBackClick: () -> Unit,
    viewModel: FolderDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FolderDetailContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onSongClick = viewModel::playSong
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FolderDetailContent(
    uiState: FolderDetailUiState,
    onBackClick: () -> Unit,
    onSongClick: (Song) -> Unit
) {
    val listState = rememberLazyListState()
    val title = uiState.name.ifBlank { stringResource(R.string.folders) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        if (uiState.songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() + 16.dp
                )
            ) {
                items(uiState.songs, key = { it.id }) { song ->
                    SongListItem(
                        song = song,
                        onEvent = { event ->
                            when (event) {
                                SongItemEvent.OnClick -> onSongClick(song)
                                SongItemEvent.OnPlayNext,
                                SongItemEvent.OnAddToQueue,
                                SongItemEvent.OnToggleFavorite,
                                SongItemEvent.OnGoToAlbum,
                                SongItemEvent.OnGoToArtist,
                                SongItemEvent.OnAddToPlaylist,
                                SongItemEvent.OnShare,
                                SongItemEvent.OnDelete -> Unit
                            }
                        }
                    )
                }
            }
        }
    }
}
