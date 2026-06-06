package com.abra.musica.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abra.musica.data.model.Song
import com.abra.musica.data.repository.SongCollectionRepository
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

data class SongCollectionUiState(
    val songs: List<Song> = emptyList(),
    val favoriteSongIds: Set<Long> = emptySet()
)

@HiltViewModel
class SongCollectionViewModel @Inject constructor(
    private val songCollectionRepository: SongCollectionRepository,
    private val playerController: PlayerController
) : ViewModel() {
    val favoriteSongs: StateFlow<List<Song>> = songCollectionRepository.getFavoriteSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayedSongs: StateFlow<List<Song>> = songCollectionRepository.getRecentlyPlayedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val favoriteSongIds: StateFlow<Set<Long>> = songCollectionRepository.favoriteSongIds
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val favoriteSongsUiState: StateFlow<SongCollectionUiState> = combine(
        favoriteSongs,
        favoriteSongIds
    ) { songs, favoriteSongIds ->
        SongCollectionUiState(
            songs = songs,
            favoriteSongIds = favoriteSongIds
        )
    }
        .catch { emit(SongCollectionUiState()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SongCollectionUiState())

    val recentlyPlayedUiState: StateFlow<SongCollectionUiState> = combine(
        recentlyPlayedSongs,
        favoriteSongIds
    ) { songs, favoriteSongIds ->
        SongCollectionUiState(
            songs = songs,
            favoriteSongIds = favoriteSongIds
        )
    }
        .catch { emit(SongCollectionUiState()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SongCollectionUiState())

    fun playFavoriteSong(song: Song) {
        play(song, favoriteSongs.value)
    }

    fun playRecentlyPlayedSong(song: Song) {
        play(song, recentlyPlayedSongs.value)
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            songCollectionRepository.toggleFavorite(song.id)
        }
    }

    private fun play(song: Song, queue: List<Song>) {
        viewModelScope.launch {
            playerController.play(song, queue)
        }
    }
}
