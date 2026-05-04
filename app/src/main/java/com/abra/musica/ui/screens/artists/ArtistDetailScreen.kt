package com.abra.musica.ui.screens.artists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
fun ArtistDetailScreen(
    onBackClick: () -> Unit,
    onGoToAlbum: (Long) -> Unit,
    viewModel: ArtistDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ArtistDetailContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onSongClick = viewModel::playSong,
        onGoToAlbum = onGoToAlbum
    )
}

@Composable
private fun ArtistDetailContent(
    uiState: ArtistDetailUiState,
    onBackClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    onGoToAlbum: (Long) -> Unit
) {
    val fallbackTitle = stringResource(R.string.artists)
    val title = uiState.name.ifBlank { fallbackTitle }

    val listState = rememberLazyListState()
    val expandedHeight = 280.dp
    val collapsedHeight = 64.dp
    val collapseRangePx = with(LocalDensity.current) { (expandedHeight - collapsedHeight).toPx() }
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

    Box(Modifier.fillMaxSize()) {
        if (uiState.songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = expandedHeight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = expandedHeight, bottom = 16.dp)
            ) {
                items(uiState.songs, key = { it.id }) { song ->
                    SongListItem(
                        song = song,
                        onClick = { onSongClick(song) },
                        onGoToAlbum = { onGoToAlbum(song.albumId) }
                    )
                }
            }
        }

        CollapsingCoverTopBar(
            title = title,
            subtitle = stringResource(R.string.song_count, uiState.songCount),
            coverArtUri = uiState.coverArtUri,
            collapsedFraction = collapsedFraction,
            onBackClick = onBackClick,
            expandedHeight = expandedHeight,
            collapsedHeight = collapsedHeight
        )
    }
}
