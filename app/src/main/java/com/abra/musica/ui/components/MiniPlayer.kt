package com.abra.musica.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.abra.musica.R
import com.abra.musica.data.model.Song
import com.abra.musica.data.model.albumArtUri
import com.abra.musica.data.model.mockSong
import com.abra.musica.ui.navigation.Screen
import com.abra.musica.ui.player.NowPlayingViewModel
import com.abra.musica.ui.theme.MusicaTheme

@Composable
fun MiniPlayer(
    navController: NavController,
    viewModel: NowPlayingViewModel = hiltViewModel(),
) {
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    var isFavorite by rememberSaveable(currentSong?.id) { mutableStateOf(false) }

    MiniPlayerContent(
        currentSong = currentSong,
        isPlaying = isPlaying,
        isFavorite = isFavorite,
        onPlayPauseClick = { viewModel.togglePlayPause() },
        onMiniPlayerClick = { navController.navigate(Screen.NowPlaying.route) },
        onFavoriteClick = { isFavorite = !isFavorite }
    )
}

@Composable
fun MiniPlayerContent(
    currentSong: Song?,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onPlayPauseClick: () -> Unit,
    onMiniPlayerClick: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    val pillShape = RoundedCornerShape(28.dp)
    val overlayBrush = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        )
    )

    AnimatedVisibility(
        visible = currentSong != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
    ) {
        currentSong?.let { song ->
            Surface(
                color = Color.Transparent,
                shape = pillShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clickable(onClick = onMiniPlayerClick)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(pillShape)
                ) {
                    AsyncImage(
                        model = song.albumArtUri(),
                        contentDescription = stringResource(R.string.album_art_desc, song.album),
                        modifier = Modifier
                            .matchParentSize()
                            .blur(100.dp),
                        placeholder = rememberVectorPainter(Icons.Default.Album),
                        error = rememberVectorPainter(Icons.Default.Album),
                        contentScale = ContentScale.Crop,
                        alpha = 0.8f
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(overlayBrush)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(46.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            )
                        ) {
                            IconButton(onClick = onPlayPauseClick) {
                                Icon(
                                    imageVector = if (isPlaying) {
                                        Icons.Default.Pause
                                    } else {
                                        Icons.Default.PlayArrow
                                    },
                                    contentDescription = if (isPlaying) {
                                        stringResource(R.string.pause)
                                    } else {
                                        stringResource(R.string.play)
                                    },
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 14.dp)
                        ) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = song.artist,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(onClick = onFavoriteClick) {
                            Icon(
                                imageVector = if (isFavorite) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Outlined.FavoriteBorder
                                },
                                contentDescription = if (isFavorite) {
                                    stringResource(R.string.remove_from_favorites)
                                } else {
                                    stringResource(R.string.add_to_favorites)
                                },
                                tint = if (isFavorite) {
                                    Color(0xFFFF6B6B)
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun MiniPlayerContentPreview() {
    MusicaTheme {
        Surface {
            MiniPlayerContent(
                currentSong = mockSong,
                isPlaying = true,
                isFavorite = true,
                onPlayPauseClick = {},
                onMiniPlayerClick = {},
                onFavoriteClick = {}
            )
        }
    }
}
