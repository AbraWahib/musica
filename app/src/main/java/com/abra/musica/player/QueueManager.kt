package com.abra.musica.player

import com.abra.musica.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QueueManager @Inject constructor() {

    private val _currentQueue = MutableStateFlow<List<Song>>(emptyList())
    val currentQueue: StateFlow<List<Song>> = _currentQueue.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _originalQueue = MutableStateFlow<List<Song>>(emptyList())
    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    val currentSong: StateFlow<Song?> = combine(
        _currentQueue,
        _currentIndex
    ) { queue, index ->
        if (index in queue.indices) queue[index] else null
    }.stateIn(
        CoroutineScope(Dispatchers.Default),
        SharingStarted.WhileSubscribed(5000),
        null
    )

    fun setQueue(songs: List<Song>, startIndex: Int = 0) {
        _originalQueue.value = songs
        _currentQueue.value = songs
        _currentIndex.value = if (songs.isNotEmpty() && startIndex in songs.indices) startIndex else 0
    }

    fun addToQueue(song: Song) {
        _currentQueue.value = _currentQueue.value + song
        if (_originalQueue.value.isNotEmpty()) {
            _originalQueue.value = _originalQueue.value + song
        }
    }

    fun addToQueueNext(song: Song) {
        val currentList = _currentQueue.value.toMutableList()
        val insertIndex = _currentIndex.value + 1
        if (insertIndex <= currentList.size) {
            currentList.add(insertIndex, song)
            _currentQueue.value = currentList

            // Also add to original queue if not shuffled
            if (!_shuffleEnabled.value && _originalQueue.value.isNotEmpty()) {
                val originalList = _originalQueue.value.toMutableList()
                val originalInsertIndex = _originalQueue.value.indexOfFirst { it.id == currentSong.value?.id } + 1
                if (originalInsertIndex <= originalList.size) {
                    originalList.add(originalInsertIndex, song)
                    _originalQueue.value = originalList
                }
            }
        }
    }

    fun removeFromQueue(index: Int) {
        if (index in _currentQueue.value.indices) {
            val newQueue = _currentQueue.value.toMutableList()
            newQueue.removeAt(index)
            _currentQueue.value = newQueue

            // Adjust current index if necessary
            if (index < _currentIndex.value) {
                _currentIndex.value = _currentIndex.value - 1
            } else if (index == _currentIndex.value) {
                // If we removed the current song, stay at the same index (which now points to the next song)
                if (_currentIndex.value >= newQueue.size) {
                    _currentIndex.value = newQueue.size - 1
                }
            }

            // Also remove from original queue if not shuffled
            if (!_shuffleEnabled.value && _originalQueue.value.isNotEmpty()) {
                val songToRemove = _currentQueue.value.getOrNull(index)
                songToRemove?.let { song ->
                    _originalQueue.value = _originalQueue.value.filter { it.id != song.id }
                }
            }
        }
    }

    fun reorderQueue(from: Int, to: Int) {
        if (from in _currentQueue.value.indices && to in _currentQueue.value.indices) {
            val newQueue = _currentQueue.value.toMutableList()
            val movedSong = newQueue.removeAt(from)
            newQueue.add(to, movedSong)
            _currentQueue.value = newQueue

            // Update current index if it was affected
            when {
                from == _currentIndex.value -> _currentIndex.value = to
                from < _currentIndex.value && to >= _currentIndex.value -> _currentIndex.value--
                from > _currentIndex.value && to <= _currentIndex.value -> _currentIndex.value++
            }
        }
    }

    fun skipToNext(): Boolean {
        val queue = _currentQueue.value
        if (queue.isEmpty()) return false

        val nextIndex = when (_repeatMode.value) {
            RepeatMode.ONE -> _currentIndex.value
            RepeatMode.ALL -> (_currentIndex.value + 1) % queue.size
            RepeatMode.OFF -> {
                if (_currentIndex.value + 1 < queue.size) _currentIndex.value + 1 else return false
            }
        }

        _currentIndex.value = nextIndex
        return true
    }

    fun skipToPrevious(): Boolean {
        val queue = _currentQueue.value
        if (queue.isEmpty()) return false

        val prevIndex = when (_repeatMode.value) {
            RepeatMode.ONE -> _currentIndex.value
            RepeatMode.ALL -> if (_currentIndex.value - 1 < 0) queue.size - 1 else _currentIndex.value - 1
            RepeatMode.OFF -> {
                if (_currentIndex.value - 1 >= 0) _currentIndex.value - 1 else return false
            }
        }

        _currentIndex.value = prevIndex
        return true
    }

    fun toggleShuffle() {
        _shuffleEnabled.value = !_shuffleEnabled.value

        if (_shuffleEnabled.value) {
            // Save original queue and shuffle
            if (_originalQueue.value.isEmpty()) {
                _originalQueue.value = _currentQueue.value
            }
            val shuffled = _currentQueue.value.shuffled()
            _currentQueue.value = shuffled
            // Find new index of current song
            val currentSong = this.currentSong.value
            _currentIndex.value = if (currentSong != null) {
                shuffled.indexOfFirst { it.id == currentSong.id }
            } else 0
        } else {
            // Restore original queue
            val original = _originalQueue.value
            if (original.isNotEmpty()) {
                _currentQueue.value = original
                // Find index of current song in original queue
                val currentSong = this.currentSong.value
                _currentIndex.value = if (currentSong != null) {
                    original.indexOfFirst { it.id == currentSong.id }
                } else 0
            }
        }
    }

    fun setRepeatMode(mode: RepeatMode) {
        _repeatMode.value = mode
    }

    fun clearQueue() {
        _currentQueue.value = emptyList()
        _originalQueue.value = emptyList()
        _currentIndex.value = -1
    }
}
