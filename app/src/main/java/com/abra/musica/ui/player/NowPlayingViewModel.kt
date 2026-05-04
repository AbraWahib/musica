package com.abra.musica.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abra.musica.data.model.Song
import com.abra.musica.data.model.mockSong
import com.abra.musica.player.PlayerController
import com.abra.musica.player.QueueManager
import com.abra.musica.player.RepeatMode
import com.abra.musica.ui.components.PlayerUiState
import com.abra.musica.ui.player.components.NowPlayingEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _isExpanded = MutableStateFlow(false)
    val isExpanded: StateFlow<Boolean> = _isExpanded.asStateFlow()

    // Combined state for UI

    val playerState: StateFlow<PlayerUiState> = combine(
        currentSong,
        isPlaying,
        currentPosition,
        duration,
        repeatMode,
        shuffleEnabled,
        isExpanded
    ) { array ->
        PlayerUiState(
            currentSong = array[0] as Song?,
            isPlaying = array[1] as Boolean,
            currentPosition = array[2] as Long,
            duration = array[3] as Long,
            repeatMode = array[4] as RepeatMode,
            shuffleEnabled = array[5] as Boolean,
            isExpanded = array[6] as Boolean
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerUiState())

    fun onEvent(event: NowPlayingEvents){
        when(event){
            is NowPlayingEvents.SeekTo -> seekTo(event.position)
            is NowPlayingEvents.SetRepeatMode -> setRepeatMode(event.repeatMode)
            is NowPlayingEvents.SkipToNext -> skipToNext()
            is NowPlayingEvents.SkipToPrevious -> skipToPrevious()
            is NowPlayingEvents.TogglePlayPause -> togglePlayPause()
            is NowPlayingEvents.ToggleShuffle -> toggleShuffle()
        }
    }
    fun expand() { _isExpanded.value = true }

    fun collapse() { _isExpanded.value = false }

    fun playSong(song: Song, queue: List<Song> = listOf(song)) {
        viewModelScope.launch {
            playerController.play(song, queue)
        }
    }

    private fun togglePlayPause() {
        viewModelScope.launch {
            playerController.playPause()
        }
    }

    private fun skipToNext() {
        viewModelScope.launch {
            playerController.skipNext()
        }
    }

    private fun skipToPrevious() {
        viewModelScope.launch {
            playerController.skipPrevious()
        }
    }

    private fun seekTo(position: Long) {
        viewModelScope.launch {
            playerController.seekTo(position)
        }
    }

    private fun toggleShuffle() {
        viewModelScope.launch {
            playerController.toggleShuffle()
        }
    }

    private fun setRepeatMode(mode: RepeatMode) {
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



