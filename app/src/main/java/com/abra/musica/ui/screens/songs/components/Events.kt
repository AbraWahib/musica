package com.abra.musica.ui.screens.songs.components

import com.abra.musica.data.model.Song

sealed class Events {
    data class OnSongClick(val song: Song) : Events()
}
