package com.abra.musica.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abra.musica.R
import com.abra.musica.data.model.Song
import com.abra.musica.ui.components.SongItemEvent
import com.abra.musica.ui.components.SongListItem

@Composable
fun FavoriteSongsScreen(
    viewModel: SongCollectionViewModel = hiltViewModel()
) {
    val songs by viewModel.favoriteSongs.collectAsStateWithLifecycle()
    SongCollectionContent(
        title = stringResource(R.string.favourites),
        emptyMessage = stringResource(R.string.favourites_placeholder),
        songs = songs,
        favoriteSongIds = songs.map { it.id }.toSet(),
        onSongClick = viewModel::playFavoriteSong,
        onToggleFavorite = viewModel::toggleFavorite
    )
}

@Composable
fun RecentlyPlayedScreen(
    viewModel: SongCollectionViewModel = hiltViewModel()
) {
    val songs by viewModel.recentlyPlayedSongs.collectAsStateWithLifecycle()
    val favoriteSongIds by viewModel.favoriteSongIds.collectAsStateWithLifecycle()
    SongCollectionContent(
        title = stringResource(R.string.recently_played),
        emptyMessage = stringResource(R.string.recently_played_placeholder),
        songs = songs,
        favoriteSongIds = favoriteSongIds,
        onSongClick = viewModel::playRecentlyPlayedSong,
        onToggleFavorite = viewModel::toggleFavorite
    )
}

@Composable
private fun SongCollectionContent(
    title: String,
    emptyMessage: String,
    songs: List<Song>,
    favoriteSongIds: Set<Long>,
    onSongClick: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        if (songs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = emptyMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(songs, key = { it.id }) { song ->
                    SongListItem(
                        song = song,
                        isFavorite = song.id in favoriteSongIds,
                        onEvent = { event ->
                            when (event) {
                                SongItemEvent.OnClick -> onSongClick(song)
                                SongItemEvent.OnToggleFavorite -> onToggleFavorite(song)
                                SongItemEvent.OnPlayNext,
                                SongItemEvent.OnAddToQueue,
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
