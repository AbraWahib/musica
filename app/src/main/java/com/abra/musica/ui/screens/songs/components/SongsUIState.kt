package com.abra.musica.ui.screens.songs.components

import com.abra.musica.data.model.Playlist
import com.abra.musica.data.model.Song
import com.abra.musica.data.model.SortOrder

data class SongsUiState(
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = true,
    val favoriteSongIds: Set<Long> = emptySet(),
    val sortOrder: SortOrder = SortOrder.TITLE_ASC,
    val playlists: List<Playlist> = emptyList()
)