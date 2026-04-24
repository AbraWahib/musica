package com.abra.musica.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey

// PlaylistSongEntity — junction table
@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [ForeignKey(
        entity = PlaylistEntity::class,
        parentColumns = ["id"],
        childColumns = ["playlistId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PlaylistSongEntity(
    val playlistId: Long,
    val songId: Long,           // MediaStore song ID (not a FK — no Room table for songs)
    val position: Int           // For ordered playlists
)
