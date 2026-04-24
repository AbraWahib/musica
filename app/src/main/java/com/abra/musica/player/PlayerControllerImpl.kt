package com.abra.musica.player

import androidx.media3.common.Player
import com.abra.musica.data.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerControllerImpl @Inject constructor(
    private val queueManager: QueueManager
) : PlayerController {

    private val _currentSong = MutableStateFlow<Song?>(null)
    override val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _playbackState = MutableStateFlow(Player.STATE_IDLE)
    override val playbackState: StateFlow<Int> = _playbackState.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    override val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    override val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    override val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    override val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    override fun play(song: Song, queue: List<Song>) {
        queueManager.setQueue(queue, queue.indexOf(song))
        _currentSong.value = song
        _isPlaying.value = true
        _playbackState.value = Player.STATE_READY
        _duration.value = song.duration
    }

    override fun playPause() {
        _isPlaying.value = !_isPlaying.value
    }

    override fun seekTo(position: Long) {
        _currentPosition.value = position
    }

    override fun skipNext() {
        if (queueManager.skipToNext()) {
            _currentSong.value = queueManager.currentSong.value
        }
    }

    override fun skipPrevious() {
        if (queueManager.skipToPrevious()) {
            _currentSong.value = queueManager.currentSong.value
        }
    }

    override fun setRepeatMode(mode: RepeatMode) {
        _repeatMode.value = mode
        queueManager.setRepeatMode(mode)
    }

    override fun toggleShuffle() {
        _shuffleEnabled.value = !_shuffleEnabled.value
        queueManager.toggleShuffle()
    }

    override fun addToQueue(song: Song) {
        queueManager.addToQueue(song)
    }

    override fun addToQueueNext(song: Song) {
        queueManager.addToQueueNext(song)
    }

    override fun removeFromQueue(index: Int) {
        queueManager.removeFromQueue(index)
    }

    override fun reorderQueue(from: Int, to: Int) {
        queueManager.reorderQueue(from, to)
    }
}
