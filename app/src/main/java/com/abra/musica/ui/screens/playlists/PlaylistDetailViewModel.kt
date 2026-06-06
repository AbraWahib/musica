package com.abra.musica.ui.screens.playlists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abra.musica.data.model.Playlist
import com.abra.musica.data.model.Song
import com.abra.musica.data.repository.MediaStoreRepository
import com.abra.musica.data.repository.PlaylistRepository
import com.abra.musica.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistDetailUiState(
    val playlist: Playlist? = null,
    val songs: List<Song> = emptyList()
)

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    mediaStoreRepository: MediaStoreRepository,
    playlistRepository: PlaylistRepository,
    private val playerController: PlayerController
) : ViewModel() {
    private val playlistId: Long = savedStateHandle.get<String>("playlistId")?.toLongOrNull() ?: -1L

    val playlist: StateFlow<Playlist?> = playlistRepository.getAllPlaylists()
        .map { playlists -> playlists.firstOrNull { it.id == playlistId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val songs: StateFlow<List<Song>> = combine(
        mediaStoreRepository.getSongs(),
        playlistRepository.getSongsForPlaylist(playlistId)
    ) { allSongs, playlistSongs ->
        if (playlistId <= 0) return@combine emptyList()
        val songsById = allSongs.associateBy { it.id }
        playlistSongs.mapNotNull { playlistSong -> songsById[playlistSong.songId] }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<PlaylistDetailUiState> = combine(
        playlist,
        songs
    ) { playlist, songs ->
        PlaylistDetailUiState(
            playlist = playlist,
            songs = songs
        )
    }
        .catch { emit(PlaylistDetailUiState()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlaylistDetailUiState())

    fun playSong(song: Song) {
        viewModelScope.launch {
            playerController.play(song, songs.value)
        }
    }
}
