package com.abra.musica.ui.screens.albums

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abra.musica.R
import com.abra.musica.data.model.Song
import com.abra.musica.ui.components.CollapsingCoverTopBar
import com.abra.musica.ui.components.SongListItem

@Composable
fun AlbumDetailScreen(
    onBackClick: () -> Unit,
    onGoToArtist: (Long) -> Unit,
    viewModel: AlbumDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AlbumDetailContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onSongClick = viewModel::playSong,
        onGoToArtist = onGoToArtist
    )
}

@Composable
private fun AlbumDetailContent(
    uiState: AlbumDetailUiState,
    onBackClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    onGoToArtist: (Long) -> Unit
) {
    val listState = rememberLazyListState()
    val expandedHeight = 280.dp
    val collapsedHeight = 64.dp
    val density = LocalDensity.current
    
    val collapseRangePx = with(density) { (expandedHeight - collapsedHeight).toPx() }
    
    val collapsedFraction by remember {
        derivedStateOf {
            val scrollPx = if (listState.firstVisibleItemIndex == 0) {
                listState.firstVisibleItemScrollOffset.toFloat()
            } else {
                collapseRangePx
            }
            (scrollPx / collapseRangePx).coerceIn(0f, 1f)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.songs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Album,
                        contentDescription = null,
                        modifier = Modifier.padding(bottom = 100.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = expandedHeight,
                        bottom = innerPadding.calculateBottomPadding() + 16.dp
                    )
                ) {
                    items(uiState.songs, key = { it.id }) { song ->
                        SongListItem(
                            song = song,
                            onClick = { onSongClick(song) },
                            onGoToArtist = { onGoToArtist(song.artistId) }
                        )
                    }
                }
            }

            CollapsingCoverTopBar(
                title = uiState.title.ifBlank { stringResource(R.string.albums) },
                subtitle = stringResource(R.string.song_count, uiState.songCount),
                coverArtUri = uiState.coverArtUri,
                collapsedFraction = collapsedFraction,
                onBackClick = onBackClick,
                expandedHeight = expandedHeight,
                collapsedHeight = collapsedHeight
            )
        }
    }
}
