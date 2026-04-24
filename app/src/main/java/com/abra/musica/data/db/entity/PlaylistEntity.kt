package com.abra.musica.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// PlaylistEntity — playlists table
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
