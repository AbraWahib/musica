package com.abra.musica.ui.player.components

import com.abra.musica.player.RepeatMode

sealed class NowPlayingEvents {
    data class SeekTo(val position: Long) : NowPlayingEvents()
    class ToggleShuffle() : NowPlayingEvents()
    class SkipToPrevious(): NowPlayingEvents()
    class TogglePlayPause(): NowPlayingEvents()
    class SkipToNext(): NowPlayingEvents()
    class SetRepeatMode(val repeatMode: RepeatMode): NowPlayingEvents()
}