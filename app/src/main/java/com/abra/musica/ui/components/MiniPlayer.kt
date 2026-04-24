package com.abra.musica.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.abra.musica.R
import com.abra.musica.data.model.albumArtUri
import com.abra.musica.ui.navigation.Screen
import com.abra.musica.ui.player.NowPlayingViewModel

@Composable
fun MiniPlayer(
    navController: androidx.navigation.NavController,
    modifier: Modifier = Modifier
) {
    val viewModel: NowPlayingViewModel = hiltViewModel()
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()

    AnimatedVisibility(
        visible = currentSong != null,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        currentSong?.let { song ->
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(Screen.NowPlaying.route)
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album art
                    AsyncImage(
                        model = song.albumArtUri(),
                        contentDescription = stringResource(R.string.album_art_desc, song.album),
                        modifier = Modifier
                            .size(48.dp)
                            .padding(end = 16.dp),
                        placeholder = painterResource(R.drawable.ic_album_placeholder),
                        error = painterResource(R.drawable.ic_album_placeholder)
                    )

                    // Song info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    // Play/pause button
                    IconButton(
                        onClick = { viewModel.togglePlayPause() }
                    ) {
                        Icon(
                            painter = painterResource(
                                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
                            ),
                            contentDescription = if (isPlaying)
                                stringResource(R.string.pause)
                            else
                                stringResource(R.string.play)
                        )
                    }

                    // Skip next button
                    IconButton(
                        onClick = { viewModel.skipToNext() }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_skip_next),
                            contentDescription = stringResource(R.string.skip_next)
                        )
                    }
                }
            }
        }
    }
}
