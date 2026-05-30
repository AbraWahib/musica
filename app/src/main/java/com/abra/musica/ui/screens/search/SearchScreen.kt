package com.abra.musica.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abra.musica.R
import com.abra.musica.data.model.Album
import com.abra.musica.data.model.Artist
import com.abra.musica.data.model.Playlist
import com.abra.musica.ui.components.MainScreenCustomAppBar
import com.abra.musica.ui.components.SongListItem

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
                onSongClick = viewModel::playSong,
                onPlayNext = viewModel::playNext,
                onAddToQueue = viewModel::addToQueue,
                onToggleFavorite = viewModel::toggleFavorite,
                onArtistClick = onArtistClick,
                onAlbumClick = onAlbumClick,
                onPlaylistClick = onPlaylistClick
            )
        }
    }
}

@Composable
private fun SearchResultsList(
    results: SearchResults,
    favoriteSongIds: Set<Long>,
    onSongClick: (com.abra.musica.data.model.Song) -> Unit,
    onPlayNext: (com.abra.musica.data.model.Song) -> Unit,
    onAddToQueue: (com.abra.musica.data.model.Song) -> Unit,
    onToggleFavorite: (com.abra.musica.data.model.Song) -> Unit,
    onArtistClick: (Long) -> Unit,
    onAlbumClick: (Long) -> Unit,
    onPlaylistClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        if (results.songs.isNotEmpty()) {
            item { SearchSectionHeader(stringResource(R.string.songs)) }
            items(results.songs.take(6), key = { it.id }) { song ->
                SongListItem(
                    song = song,
                    onClick = { onSongClick(song) },
                    isFavorite = song.id in favoriteSongIds,
                    onPlayNext = { onPlayNext(song) },
                    onAddToQueue = { onAddToQueue(song) },
                    onToggleFavorite = { onToggleFavorite(song) },
                    onGoToAlbum = { onAlbumClick(song.albumId) },
                    onGoToArtist = { onArtistClick(song.artistId) }
                )
            }
        }

        if (results.artists.isNotEmpty()) {
            item { SearchSectionHeader(stringResource(R.string.artists)) }
            items(results.artists.take(5), key = { it.id }) { artist ->
                ArtistResultRow(
                    artist = artist,
                    onClick = { onArtistClick(artist.id) }
                )
            }
        }

        if (results.albums.isNotEmpty()) {
            item { SearchSectionHeader(stringResource(R.string.albums)) }
            items(results.albums.take(5), key = { it.id }) { album ->
                AlbumResultRow(
                    album = album,
                    onClick = { onAlbumClick(album.id) }
                )
            }
        }

        if (results.playlists.isNotEmpty()) {
            item { SearchSectionHeader(stringResource(R.string.playlists)) }
            items(results.playlists.take(5), key = { it.id }) { playlist ->
                PlaylistResultRow(
                    playlist = playlist,
                    onClick = { onPlaylistClick(playlist.id) }
                )
            }
        }
    }
}

@Composable
private fun SearchSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 16.dp, top = 18.dp, end = 16.dp, bottom = 6.dp)
    )
}

@Composable
private fun ArtistResultRow(
    artist: Artist,
    onClick: () -> Unit
) {
    SearchEntityRow(
        icon = Icons.Default.Person,
        title = artist.name,
        subtitle = stringResource(R.string.artist_info, artist.albumCount, artist.songCount),
        onClick = onClick
    )
}

@Composable
private fun AlbumResultRow(
    album: Album,
    onClick: () -> Unit
) {
    SearchEntityRow(
        icon = Icons.Default.Album,
        title = album.title,
        subtitle = album.artist,
        onClick = onClick
    )
}

@Composable
private fun PlaylistResultRow(
    playlist: Playlist,
    onClick: () -> Unit
) {
    SearchEntityRow(
        icon = Icons.AutoMirrored.Filled.PlaylistPlay,
        title = playlist.name,
        subtitle = stringResource(R.string.song_count, playlist.songCount),
        onClick = onClick
    )
}

@Composable
private fun SearchEntityRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
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
