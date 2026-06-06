package com.abra.musica.ui.screens.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abra.musica.data.model.Playlist
import com.abra.musica.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistsUiState(
    val playlists: List<Playlist> = emptyList(),
    val selectedPlaylist: Playlist? = null
)

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {
    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    val playlists: StateFlow<List<Playlist>> = playlistRepository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<PlaylistsUiState> = combine(
        playlists,
        selectedPlaylist
    ) { playlists, selectedPlaylist ->
        PlaylistsUiState(
            playlists = playlists,
            selectedPlaylist = selectedPlaylist
        )
    }
        .catch { emit(PlaylistsUiState()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlaylistsUiState())

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            runCatching { playlistRepository.createPlaylist(name) }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlistId)
        }
    }

    fun renamePlaylist(playlistId: Long, newName: String) {
        viewModelScope.launch {
            runCatching { playlistRepository.renamePlaylist(playlistId, newName) }
        }
    }

    fun onPlaylistLongPressed(playlist: Playlist) {
        _selectedPlaylist.value = playlist
    }

    fun clearSelection() {
        _selectedPlaylist.value = null
    }
}
