package com.abra.musica.ui.screens.search.components

import com.abra.musica.data.model.Song
import com.abra.musica.ui.components.SongItemEvent

sealed class SearchScreenEvent {
    data class OnPlayListClicked(val playlistId: Long): SearchScreenEvent()
    data class OnArtistClicked(val artistId: Long): SearchScreenEvent()
    data class OnAlbumClicked(val albumId: Long): SearchScreenEvent()
    data class OnItemEvent(
        val song: Song,
        val itemEvent: SongItemEvent
    ): SearchScreenEvent()
}