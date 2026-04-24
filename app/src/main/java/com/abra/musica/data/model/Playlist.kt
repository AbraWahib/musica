package com.abra.musica.data.model

// Playlist.kt — backed by Room
data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int,
    val createdAt: Long
)
