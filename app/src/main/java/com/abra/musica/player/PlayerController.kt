package com.abra.musica.player

import androidx.media3.common.Player
import com.abra.musica.data.model.Song
import kotlinx.coroutines.flow.StateFlow

// Exposed interface (implement fully)
interface PlayerController {
    val currentSong: StateFlow<Song?>
    val playbackState: StateFlow<Int>   // Player state values: IDLE, BUFFERING, READY, ENDED
    val isPlaying: StateFlow<Boolean>
    val currentPosition: StateFlow<Long>           // ms, updated every 500ms
    val duration: StateFlow<Long>
    val repeatMode: StateFlow<RepeatMode>          // OFF, ONE, ALL
    val shuffleEnabled: StateFlow<Boolean>

    fun play(song: Song, queue: List<Song>)
    fun playPause()
    fun seekTo(position: Long)
    fun skipNext()
    fun skipPrevious()
    fun setRepeatMode(mode: RepeatMode)
    fun toggleShuffle()
    fun addToQueue(song: Song)
    fun addToQueueNext(song: Song)
    fun removeFromQueue(index: Int)
    fun reorderQueue(from: Int, to: Int)
}

enum class RepeatMode { OFF, ONE, ALL }
