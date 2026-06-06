package com.abra.musica.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abra.musica.data.model.Album
import com.abra.musica.data.model.Artist
import com.abra.musica.data.model.Playlist
import com.abra.musica.data.model.Song
import com.abra.musica.data.repository.MediaStoreRepository
import com.abra.musica.data.repository.PlaylistRepository
import com.abra.musica.data.repository.SongCollectionRepository
import com.abra.musica.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val mediaStoreRepository: MediaStoreRepository,
    playlistRepository: PlaylistRepository,
    private val songCollectionRepository: SongCollectionRepository,
    private val playerController: PlayerController
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val results: StateFlow<SearchResults> = combine(
        _query.debounce(300),
        mediaStoreRepository.getSongs(),
        mediaStoreRepository.getArtists(),
        mediaStoreRepository.getAlbums(),
        playlistRepository.getAllPlaylists()
    ) { query, songs, artists, albums, playlists ->
        val term = query.trim()
        if (term.isBlank()) {
            SearchResults()
        } else {
            SearchResults(
                songs = songs.filter { song ->
                    song.title.contains(term, ignoreCase = true) ||
                        song.artist.contains(term, ignoreCase = true) ||
                        song.album.contains(term, ignoreCase = true)
                },
                artists = artists.filter { artist ->
                    artist.name.contains(term, ignoreCase = true)
                },
                albums = albums.filter { album ->
                    album.title.contains(term, ignoreCase = true) ||
                        album.artist.contains(term, ignoreCase = true)
                },
                playlists = playlists.filter { playlist ->
                    playlist.name.contains(term, ignoreCase = true)
                }
            )
        }
    }
        .catch { emit(SearchResults()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResults())

    val favoriteSongIds: StateFlow<Set<Long>> = songCollectionRepository.favoriteSongIds
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val uiState: StateFlow<SearchUiState> = combine(
        query,
        results,
        favoriteSongIds
    ) { query, results, favoriteSongIds ->
        SearchUiState(
            query = query,
            results = results,
            favoriteSongIds = favoriteSongIds
        )
    }
        .catch { emit(SearchUiState()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchUiState())

    fun setQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            playerController.play(song, results.value.songs)
        }
    }

    fun playNext(song: Song) {
        playerController.addToQueueNext(song)
    }

    fun addToQueue(song: Song) {
        playerController.addToQueue(song)
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            songCollectionRepository.toggleFavorite(song.id)
        }
    }
}

data class SearchResults(
    val songs: List<Song> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList(),
    val playlists: List<Playlist> = emptyList()
) {
    val isEmpty: Boolean
        get() = songs.isEmpty() && artists.isEmpty() && albums.isEmpty() && playlists.isEmpty()
}

data class SearchUiState(
    val query: String = "",
    val results: SearchResults = SearchResults(),
    val favoriteSongIds: Set<Long> = emptySet()
)
