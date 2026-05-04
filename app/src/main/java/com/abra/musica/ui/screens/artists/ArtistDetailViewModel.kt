package com.abra.musica.ui.screens.artists

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

data class ArtistDetailUiState(
    val name: String = "",
    val songCount: Int = 0,
    val coverArtUri: Uri = Uri.EMPTY,
    val songs: List<Song> = emptyList()
)

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    mediaStoreRepository: MediaStoreRepository,
    private val playerController: PlayerController
) : ViewModel() {

    private val artistId: Long =
        savedStateHandle.get<String>("artistId")?.toLongOrNull() ?: UNKNOWN_ID

    val uiState: StateFlow<ArtistDetailUiState> = mediaStoreRepository.getSongs()
        .map { songs ->
            val artistSongs = songs
                .filter { song ->
                    if (artistId == UNKNOWN_ID) {
                        song.artist.equals("Unknown Artist", ignoreCase = true)
                    } else {
                        song.artistId == artistId
                    }
                }
                .sortedWith(
                    compareBy<Song> { it.album.lowercase() }
                        .thenBy { it.trackNumber }
                        .thenBy { it.title.lowercase() }
                )
            val firstSong = artistSongs.firstOrNull()

            ArtistDetailUiState(
                name = firstSong?.artist.orEmpty(),
                songCount = artistSongs.size,
                coverArtUri = firstSong?.albumArtUri() ?: Uri.EMPTY,
                songs = artistSongs
            )
        }
        .catch { emit(ArtistDetailUiState()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ArtistDetailUiState())

    fun playSong(song: Song) {
        viewModelScope.launch {
            playerController.play(song, uiState.value.songs)
        }
    }

    private companion object {
        const val UNKNOWN_ID = -1L
    }
}
