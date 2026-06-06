package com.abra.musica.ui.screens.songs.components

import com.abra.musica.data.model.Song
import com.abra.musica.data.model.SortOrder
import com.abra.musica.ui.components.SongItemEvent

sealed class SongsScreenEvent {
    data class OnSortOrderChange(val sortOrder: SortOrder) : SongsScreenEvent()
    object OnPlayAll : SongsScreenEvent()
    data class OnItemEvent(val song: Song, val itemEvent: SongItemEvent) : SongsScreenEvent()
}