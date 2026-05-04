package com.abra.musica.ui.screens.albums

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abra.musica.data.model.Song
import com.abra.musica.data.model.albumArtUri
import com.abra.musica.data.repository.MediaStoreRepository
import com.abra.musica.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlbumDetailUiState(
    val title: String = "",
    val songCount: Int = 0,
    val coverArtUri: Uri = Uri.EMPTY,
    val songs: List<Song> = emptyList()
)

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    mediaStoreRepository: MediaStoreRepository,
    private val playerController: PlayerController
) : ViewModel() {

    private val albumId: Long =
        savedStateHandle.get<String>("albumId")?.toLongOrNull() ?: UNKNOWN_ID

    val uiState: StateFlow<AlbumDetailUiState> = mediaStoreRepository.getSongs()
        .map { songs ->
            val albumSongs = songs
                .filter { song ->
                    if (albumId == UNKNOWN_ID) {
                        song.album.equals("Unknown Album", ignoreCase = true)
                    } else {
                        song.albumId == albumId
                    }
                }
                .sortedWith(compareBy<Song> { it.trackNumber }.thenBy { it.title.lowercase() })
            val firstSong = albumSongs.firstOrNull()

            AlbumDetailUiState(
                title = firstSong?.album.orEmpty(),
                songCount = albumSongs.size,
                coverArtUri = firstSong?.albumArtUri() ?: Uri.EMPTY,
                songs = albumSongs
            )
        }
        .catch { emit(AlbumDetailUiState()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AlbumDetailUiState())

    fun playSong(song: Song) {
        viewModelScope.launch {
            playerController.play(song, uiState.value.songs)
        }
    }

    private companion object {
        const val UNKNOWN_ID = -1L
    }
}
