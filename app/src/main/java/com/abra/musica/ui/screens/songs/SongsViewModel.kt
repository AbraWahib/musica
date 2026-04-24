package com.abra.musica.ui.screens.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abra.musica.data.model.Song
import com.abra.musica.data.repository.MediaStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOrder {
    TITLE_ASC,
    TITLE_DESC,
    ARTIST_ASC,
    ARTIST_DESC,
    ALBUM_ASC,
    ALBUM_DESC,
    DURATION_ASC,
    DURATION_DESC,
    DATE_ADDED_DESC
}

@HiltViewModel
class SongsViewModel @Inject constructor(
    private val mediaStoreRepository: MediaStoreRepository
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(SortOrder.TITLE_ASC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    val songs: StateFlow<List<Song>> = mediaStoreRepository.getSongs()
        .combine(_sortOrder) { songs, order -> songs.sortedBy(order) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    private fun List<Song>.sortedBy(order: SortOrder): List<Song> {
        return when (order) {
            SortOrder.TITLE_ASC -> sortedBy { it.title.lowercase() }
            SortOrder.TITLE_DESC -> sortedByDescending { it.title.lowercase() }
            SortOrder.ARTIST_ASC -> sortedBy { it.artist.lowercase() }
            SortOrder.ARTIST_DESC -> sortedByDescending { it.artist.lowercase() }
            SortOrder.ALBUM_ASC -> sortedBy { it.album.lowercase() }
            SortOrder.ALBUM_DESC -> sortedByDescending { it.album.lowercase() }
            SortOrder.DURATION_ASC -> sortedBy { it.duration }
            SortOrder.DURATION_DESC -> sortedByDescending { it.duration }
            SortOrder.DATE_ADDED_DESC -> sortedByDescending { it.dateAdded }
        }
    }
}
