package com.abra.musica.ui.screens.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abra.musica.data.model.Album
import com.abra.musica.data.repository.MediaStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AlbumsUiState(
    val albums: List<Album> = emptyList()
)

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val mediaStoreRepository: MediaStoreRepository
) : ViewModel() {

    val albums: StateFlow<List<Album>> = mediaStoreRepository.getAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<AlbumsUiState> = mediaStoreRepository.getAlbums()
        .map { albums -> AlbumsUiState(albums = albums) }
        .catch { emit(AlbumsUiState()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AlbumsUiState())
}
