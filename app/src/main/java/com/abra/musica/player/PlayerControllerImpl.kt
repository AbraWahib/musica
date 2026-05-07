package com.abra.musica.player

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import com.abra.musica.data.model.Song
import com.abra.musica.data.model.albumArtUri
import com.abra.musica.data.repository.SongCollectionRepository
import com.abra.musica.service.MusicService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val player: ExoPlayer,
    private val queueManager: QueueManager,
    private val songCollectionRepository: SongCollectionRepository
) : PlayerController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

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

    init {
        player.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    _playbackState.value = playbackState
                    _duration.value = player.duration.takeIf { it > 0 } ?: 0L
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    val songId = mediaItem?.mediaId?.toLongOrNull()
                    _currentSong.value = queueManager.currentQueue.value.firstOrNull { it.id == songId }
                    _duration.value = player.duration.takeIf { it > 0 } ?: _currentSong.value?.duration ?: 0L
                    songId?.let(::recordRecentlyPlayed)
                }
            }
        )

        scope.launch {
            while (isActive) {
                _currentPosition.value = player.currentPosition.coerceAtLeast(0L)
                _duration.value = player.duration.takeIf { it > 0 } ?: _duration.value
                delay(500)
            }
        }
    }

    override fun play(song: Song, queue: List<Song>) {
        startPlaybackService()
        val playbackQueue = queue.ifEmpty { listOf(song) }
        val startIndex = playbackQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        queueManager.setQueue(playbackQueue, startIndex)

        player.setMediaItems(playbackQueue.map { it.toMediaItem() }, startIndex, 0L)
        player.prepare()
        player.play()

        _currentSong.value = song
        _duration.value = song.duration
        recordRecentlyPlayed(song.id)
    }

    override fun playPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    override fun seekTo(position: Long) {
        player.seekTo(position)
    }

    override fun skipNext() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        } else if (queueManager.skipToNext()) {
            val index = queueManager.currentIndex.value
            player.seekTo(index, 0L)
        }
    }

    override fun skipPrevious() {
        if (player.currentPosition > 3000L) {
            player.seekTo(0L)
        } else if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        } else if (queueManager.skipToPrevious()) {
            val index = queueManager.currentIndex.value
            player.seekTo(index, 0L)
        }
    }

    override fun setRepeatMode(mode: RepeatMode) {
        _repeatMode.value = mode
        queueManager.setRepeatMode(mode)
        player.repeatMode = when (mode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
    }

    override fun toggleShuffle() {
        _shuffleEnabled.value = !_shuffleEnabled.value
        queueManager.toggleShuffle()
        player.shuffleModeEnabled = _shuffleEnabled.value
    }

    override fun addToQueue(song: Song) {
        queueManager.addToQueue(song)
        player.addMediaItem(song.toMediaItem())
    }

    override fun addToQueueNext(song: Song) {
        queueManager.addToQueueNext(song)
        player.addMediaItem(player.currentMediaItemIndex + 1, song.toMediaItem())
    }

    override fun removeFromQueue(index: Int) {
        queueManager.removeFromQueue(index)
        if (index in 0 until player.mediaItemCount) {
            player.removeMediaItem(index)
        }
    }

    override fun reorderQueue(from: Int, to: Int) {
        queueManager.reorderQueue(from, to)
        if (from in 0 until player.mediaItemCount && to in 0 until player.mediaItemCount) {
            player.moveMediaItem(from, to)
        }
    }

    private fun Song.toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setUri(uri)
            .setMediaId(id.toString())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setAlbumTitle(album)
                    .setArtworkUri(albumArtUri())
                    .build()
            )
            .build()
    }

    private fun startPlaybackService() {
        val intent = Intent(context, MusicService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, intent)
        } else {
            context.startService(intent)
        }
    }

    private fun recordRecentlyPlayed(songId: Long) {
        scope.launch(Dispatchers.IO) {
            songCollectionRepository.recordRecentlyPlayed(songId)
        }
    }
}
