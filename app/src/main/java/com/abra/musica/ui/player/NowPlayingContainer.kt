package com.abra.musica.ui.player

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

private const val TAG = "Mini Player Test"

@Composable
fun NowPlayingContainer(
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val uiState by viewModel.playerState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val screenHeightPx = LocalWindowInfo.current.containerSize.height.toFloat()
    val offsetY = remember { Animatable(screenHeightPx) }
    val collapsePlayer: suspend () -> Unit = {
        offsetY.animateTo(screenHeightPx)
        viewModel.collapse()
    }

    val expandPlayer: suspend () -> Unit = {
        offsetY.animateTo(0f)
        viewModel.expand()
    }

    Log.d(TAG, "NowPlayingContainer: ${uiState.currentSong}")

    LaunchedEffect(uiState.isExpanded, screenHeightPx) {
        if (screenHeightPx <= 0f) return@LaunchedEffect

        if (uiState.isExpanded) {
            offsetY.animateTo(0f)
        } else {
            offsetY.animateTo(screenHeightPx)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .offset { IntOffset(0, offsetY.value.toInt()) }
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        val newOffset = offsetY.value + delta
                        scope.launch {
                            offsetY.snapTo(newOffset.coerceIn(0f, screenHeightPx))
                        }
                    },
                    onDragStopped = {
                        val shouldExpand = offsetY.value < screenHeightPx / 2
                        scope.launch {
                            if (shouldExpand) {
                                expandPlayer()
                            } else {
                                collapsePlayer()
                            }
                        }
                    }
                )
        ) {
            NowPlayingScreen(
                playerState = uiState,
                onCollapse = {
                    scope.launch { collapsePlayer() }
                }
            )
        }

        Log.d(TAG, "NowPlayingContainer: Now Playing Screen Is Called")

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 80.dp)
        ) {
            MiniPlayer(
                uiState = uiState,
                onMiniPlayerClick = {
                    scope.launch {
                        expandPlayer()
                    }
                },
                onPlayPauseClick = viewModel::togglePlayPause
            )
            Log.d(TAG, "NowPlayingContainer: Mini PlayingScreen Is Called")
        }
    }
}
