package com.abra.musica.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abra.musica.data.model.Song
import com.abra.musica.data.model.mockSong
import com.abra.musica.player.PlayerController
import com.abra.musica.player.QueueManager
import com.abra.musica.player.RepeatMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val playerController: PlayerController,
    private val queueManager: QueueManager
) : ViewModel() {

    val currentSong: StateFlow<Song?> = playerController.currentSong
    val isPlaying: StateFlow<Boolean> = playerController.isPlaying
    val currentPosition: StateFlow<Long> = playerController.currentPosition
    val duration: StateFlow<Long> = playerController.duration
    val repeatMode: StateFlow<RepeatMode> = playerController.repeatMode
    val shuffleEnabled: StateFlow<Boolean> = playerController.shuffleEnabled

    val currentQueue: StateFlow<List<Song>> = queueManager.currentQueue
    val currentIndex: StateFlow<Int> = queueManager.currentIndex

    // Combined state for UI
    
    val playerState: StateFlow<PlayerUiState> = combine(
        currentSong,
        isPlaying,
        currentPosition,
        duration,
        repeatMode,
        shuffleEnabled
    ) { array ->
        PlayerUiState(
            currentSong = array[0] as Song?,
            isPlaying = array[1] as Boolean,
            currentPosition = array[2] as Long,
            duration = array[3] as Long,
            repeatMode = array[4] as RepeatMode,
            shuffleEnabled = array[5] as Boolean
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerUiState())

    fun playSong(song: Song, queue: List<Song> = listOf(song)) {
        viewModelScope.launch {
            playerController.play(song, queue)
        }
    }

    fun togglePlayPause() {
        viewModelScope.launch {
            playerController.playPause()
        }
    }

    fun skipToNext() {
        viewModelScope.launch {
            playerController.skipNext()
        }
    }

    fun skipToPrevious() {
        viewModelScope.launch {
            playerController.skipPrevious()
        }
    }

    fun seekTo(position: Long) {
        viewModelScope.launch {
            playerController.seekTo(position)
        }
    }

    fun toggleShuffle() {
        viewModelScope.launch {
            playerController.toggleShuffle()
        }
    }

    fun setRepeatMode(mode: RepeatMode) {
        viewModelScope.launch {
            playerController.setRepeatMode(mode)
        }
    }

    fun addToQueue(song: Song) {
        viewModelScope.launch {
            playerController.addToQueue(song)
        }
    }

    fun addToQueueNext(song: Song) {
        viewModelScope.launch {
            playerController.addToQueueNext(song)
        }
    }

    fun removeFromQueue(index: Int) {
        viewModelScope.launch {
            playerController.removeFromQueue(index)
        }
    }

    fun reorderQueue(from: Int, to: Int) {
        viewModelScope.launch {
            playerController.reorderQueue(from, to)
        }
    }
}

data class PlayerUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleEnabled: Boolean = false
)
val mockUiState = PlayerUiState(
    currentSong = mockSong,
    isPlaying = true,
    currentPosition = 10000L,
    duration = 20000L,
    repeatMode = RepeatMode.ALL,
    shuffleEnabled = true
)
