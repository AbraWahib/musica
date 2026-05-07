package com.abra.musica.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abra.musica.data.model.Song
import com.abra.musica.data.repository.SongCollectionRepository
import com.abra.musica.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

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
