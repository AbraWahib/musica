package com.abra.musica.ui.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOn
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.abra.musica.R
import com.abra.musica.data.model.albumArtUri
import com.abra.musica.player.RepeatMode
import com.abra.musica.ui.components.PlayerUiState
import com.abra.musica.ui.player.components.NowPlayingEvents


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    playerState: PlayerUiState,
    onEvent: (NowPlayingEvents) -> Unit,
    onCollapse: () -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(
                        onClick = onCollapse,
                        modifier = Modifier
                            .padding(4.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
                    .copy(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        playerState.currentSong?.let { song ->
            //screen background
            AsyncImage(
                model = song.albumArtUri(),
                contentDescription = stringResource(R.string.album_art_desc, song.album),
                modifier = Modifier
                    .fillMaxSize()
                    .blur(200.dp)
                    .blur(200.dp)
                    .blur(200.dp)
                    .alpha(0.2f),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.music_placeholder),
                error = painterResource(R.drawable.music_placeholder),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            playerState.currentSong?.let { song ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(28.dp),
                    shadowElevation = 20.dp,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.78f),
                    border = BorderStroke(
                        2.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
                    )
                ) {
                    AsyncImage(
                        model = song.albumArtUri(),
                        contentDescription = stringResource(
                            R.string.album_art_desc,
                            song.album
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(28.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.music_placeholder),
                        error = painterResource(R.drawable.music_placeholder),
                    )
                }
                Spacer(modifier = Modifier.height(64.dp))
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.Start)
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .align(Alignment.Start)
                )
                Spacer(modifier = Modifier.weight(1f))

                Slider(
                    value = playerState.currentPosition.toFloat(),
                    onValueChange = {
                        onEvent(NowPlayingEvents.SeekTo(it.toLong()))
                    },
                    valueRange = 0f..playerState.duration.toFloat().coerceAtLeast(1f),
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.onSurface,
                        activeTrackColor = MaterialTheme.colorScheme.onSurface,
                        inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = playerState.currentPosition.formatDuration(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = playerState.duration.formatDuration(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlayerModeButton(
                        icon = Icons.Default.Shuffle,
                        contentDescription = stringResource(R.string.shuffle),
                        selected = playerState.shuffleEnabled,
                        onClick = {
                            onEvent(NowPlayingEvents.ToggleShuffle())
                        }
                    )

                    IconButton(
                        onClick = {
                            onEvent(NowPlayingEvents.SkipToPrevious())
                        },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = stringResource(R.string.skip_previous),
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(78.dp)
                    ) {
                        IconButton(
                            onClick = {
                                onEvent(NowPlayingEvents.TogglePlayPause())
                            },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = if (playerState.isPlaying) {
                                    Icons.Default.Pause
                                } else {
                                    Icons.Default.PlayArrow
                                },
                                contentDescription = if (playerState.isPlaying) {
                                    stringResource(R.string.pause)
                                } else {
                                    stringResource(R.string.play)
                                },
                                tint = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            onEvent(NowPlayingEvents.SkipToNext())
                        },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = stringResource(R.string.skip_next),
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    PlayerModeButton(
                        icon = when (playerState.repeatMode) {
                            RepeatMode.OFF -> Icons.Default.Repeat
                            RepeatMode.ALL -> Icons.Default.RepeatOn
                            RepeatMode.ONE -> Icons.Default.RepeatOne
                        },
                        contentDescription = stringResource(R.string.repeat),
                        selected = playerState.repeatMode != RepeatMode.OFF,
                        onClick = {
                            val nextMode = when (playerState.repeatMode) {
                                RepeatMode.OFF -> RepeatMode.ALL
                                RepeatMode.ALL -> RepeatMode.ONE
                                RepeatMode.ONE -> RepeatMode.OFF
                            }
                            onEvent(NowPlayingEvents.SetRepeatMode(nextMode))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_song_playing),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }
    }
}


@Composable
private fun PlayerModeButton(
    icon: ImageVector,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = if (selected) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        } else {
            Color.Transparent
        }
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (selected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

private fun Long.formatDuration(): String {
    val minutes = this / 60000
    val seconds = (this % 60000) / 1000
    return "%d:%02d".format(minutes, seconds)
}

