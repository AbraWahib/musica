package com.abra.musica.ui.screens.songs

import android.content.IntentSender
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abra.musica.data.model.SortOrder
import com.abra.musica.data.model.Song
import com.abra.musica.data.repository.MediaStoreRepository
import com.abra.musica.data.repository.PlaylistRepository
import com.abra.musica.data.repository.SettingsRepository
import com.abra.musica.data.repository.SongCollectionRepository
import com.abra.musica.data.repository.DeleteSongResult
import com.abra.musica.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongsViewModel @Inject constructor(
    private val mediaStoreRepository: MediaStoreRepository,
    private val playerController: PlayerController,
    private val songCollectionRepository: SongCollectionRepository,
    private val settingsRepository: SettingsRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _deletePermissionRequests = MutableSharedFlow<IntentSender>()
    val deletePermissionRequests: SharedFlow<IntentSender> = _deletePermissionRequests.asSharedFlow()

    val sortOrder: StateFlow<SortOrder> = settingsRepository.songsSortOrder
    val favoriteSongIds: StateFlow<Set<Long>> = songCollectionRepository.favoriteSongIds
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
    val playlists = playlistRepository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val songs: StateFlow<List<Song>> = mediaStoreRepository.getSongs()
        .onStart { _isLoading.value = true }
        .onEach { _isLoading.value = false }
        .catch {
            _isLoading.value = false
            emit(emptyList())
        }
        .combine(sortOrder) { songs, order -> songs.sortedBy(order) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentSong: StateFlow<Song?> = playerController.currentSong
    val isPlaying: StateFlow<Boolean> = playerController.isPlaying

    fun playSong(song: Song) {
        viewModelScope.launch {
            playerController.play(song, songs.value)
            songCollectionRepository.recordRecentlyPlayed(song.id)
            Log.d("Play Song", "playSong in viewModel: ${song.title}")
        }
    }

    fun playAll() {
        val queue = songs.value
        val firstSong = queue.firstOrNull() ?: return
        viewModelScope.launch {
            playerController.play(firstSong, queue)
            songCollectionRepository.recordRecentlyPlayed(firstSong.id)
        }
    }

    fun setSortOrder(order: SortOrder) {
        settingsRepository.setSongsSortOrder(order)
    }

    fun addToQueue(song: Song) {
        playerController.addToQueue(song)
    }

    fun playNext(song: Song) {
        playerController.addToQueueNext(song)
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            songCollectionRepository.toggleFavorite(song.id)
        }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            when (val result = mediaStoreRepository.deleteSong(song)) {
                DeleteSongResult.Deleted,
                DeleteSongResult.Failed -> Unit
                is DeleteSongResult.NeedsUserConsent -> {
                    _deletePermissionRequests.emit(result.intentSender)
                }
            }
        }
    }

    fun addSongToPlaylist(song: Song, playlistId: Long) {
        viewModelScope.launch {
            playlistRepository.addSongToPlaylist(playlistId, song.id)
        }
    }

    fun createPlaylist(name: String, song: Song? = null) {
        viewModelScope.launch {
            runCatching { playlistRepository.createPlaylist(name) }
                .onSuccess { playlistId ->
                    song?.let { playlistRepository.addSongToPlaylist(playlistId, it.id) }
                }
        }
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
            SortOrder.DATE_ADDED_ASC -> sortedBy { it.dateAdded }
            SortOrder.DATE_ADDED_DESC -> sortedByDescending { it.dateAdded }
        }
    }
}
