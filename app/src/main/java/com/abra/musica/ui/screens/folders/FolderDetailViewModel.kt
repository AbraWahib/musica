package com.abra.musica.ui.screens.folders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abra.musica.data.model.Song
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

data class FolderDetailUiState(
    val name: String = "",
    val path: String = "",
    val songCount: Int = 0,
    val songs: List<Song> = emptyList()
)

@HiltViewModel
class FolderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    mediaStoreRepository: MediaStoreRepository,
    private val playerController: PlayerController
) : ViewModel() {

    private val folderId: Long =
        savedStateHandle.get<String>("folderId")?.toLongOrNull() ?: 0L

    val uiState: StateFlow<FolderDetailUiState> = mediaStoreRepository.getSongs()
        .map { songs ->
            val folderSongs = songs
                .filter { it.folderId == folderId }
                .sortedBy { it.path.substringAfterLast("/").lowercase() }
            val firstSong = folderSongs.firstOrNull()

            FolderDetailUiState(
                name = firstSong?.folderName.orEmpty(),
                path = firstSong?.path?.substringBeforeLast("/", missingDelimiterValue = "").orEmpty(),
                songCount = folderSongs.size,
                songs = folderSongs
            )
        }
        .catch { emit(FolderDetailUiState()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FolderDetailUiState())

    fun playSong(song: Song) {
        viewModelScope.launch {
            playerController.play(song, uiState.value.songs)
        }
    }
}
