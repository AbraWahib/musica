package com.abra.musica.ui.components

sealed class SongItemEvent {
    object OnClick : SongItemEvent()
    object OnPlayNext : SongItemEvent()
    object OnAddToQueue : SongItemEvent()
    object OnToggleFavorite : SongItemEvent()
    object OnGoToAlbum : SongItemEvent()
    object OnGoToArtist : SongItemEvent()
    object OnAddToPlaylist : SongItemEvent()
    object OnShare : SongItemEvent()
    object OnDelete : SongItemEvent()
}
