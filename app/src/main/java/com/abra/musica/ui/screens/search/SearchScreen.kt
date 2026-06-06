package com.abra.musica.ui.screens.search

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abra.musica.R
import com.abra.musica.ui.components.MainScreenCustomAppBar
import com.abra.musica.ui.components.SongItemEvent
import com.abra.musica.ui.screens.search.components.SearchResultsList
import com.abra.musica.ui.screens.search.components.SearchScreenEvent

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onArtistClick: (Long) -> Unit = {},
    onAlbumClick: (Long) -> Unit = {},
    onPlaylistClick: (Long) -> Unit = {}
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val favoriteSongIds by viewModel.favoriteSongIds.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        MainScreenCustomAppBar(title = stringResource(R.string.search))
        OutlinedTextField(
            value = query,
            onValueChange = viewModel::setQuery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            singleLine = true,
            shape = MaterialTheme.shapes.extraLarge
        )

        when {
            query.isBlank() -> SearchMessage(
                title = stringResource(R.string.search_empty_title),
                body = stringResource(R.string.search_empty_body)
            )

            results.isEmpty -> SearchMessage(
                title = stringResource(R.string.search_no_results),
                body = stringResource(R.string.search_no_results_desc)
            )

            else -> SearchResultsList(
                results = results,
                favoriteSongIds = favoriteSongIds,
                onEvent = { event ->
                    when(event){
                        is SearchScreenEvent.OnAlbumClicked -> onAlbumClick
                        is SearchScreenEvent.OnArtistClicked -> onArtistClick
                        is SearchScreenEvent.OnItemEvent -> {
                            when (event.itemEvent) {
                                SongItemEvent.OnAddToPlaylist -> {
                                    Toast.makeText(context, "Add ${event.song.title} to playlist clicked", Toast.LENGTH_LONG).show()
                                }
                                SongItemEvent.OnAddToQueue -> {
                                    viewModel.addToQueue(event.song)
                                }
                                SongItemEvent.OnClick -> {
                                    viewModel.playSong(event.song)
                                }
                                SongItemEvent.OnDelete -> {
                                    Toast.makeText(context, "Delete ${event.song.title} clicked", Toast.LENGTH_LONG).show()
                                }
                                SongItemEvent.OnGoToAlbum -> {
                                    onAlbumClick(event.song.albumId)
                                }
                                SongItemEvent.OnGoToArtist -> {
                                    onArtistClick(event.song.artistId)
                                }
                                SongItemEvent.OnPlayNext -> {
                                    viewModel.playNext(event.song)
                                }
                                SongItemEvent.OnShare -> {
                                    Toast.makeText(context, "Share ${event.song.title} clicked", Toast.LENGTH_LONG).show()
                                }
                                SongItemEvent.OnToggleFavorite -> {
                                    viewModel.toggleFavorite(event.song)
                                }
                            }
                        }
                        is SearchScreenEvent.OnPlayListClicked -> {
                            onPlaylistClick(event.playlistId)
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun SearchMessage(
    title: String,
    body: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
