package com.abra.musica.ui.components

import com.abra.musica.data.model.Song
import com.abra.musica.player.RepeatMode

data class PlayerUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleEnabled: Boolean = false,
    val isFavorite: Boolean = false,
    val isExpanded: Boolean = false
)
