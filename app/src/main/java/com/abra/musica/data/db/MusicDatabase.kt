package com.abra.musica.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.abra.musica.data.db.dao.PlaylistDao
import com.abra.musica.data.db.dao.PlaylistSongDao
import com.abra.musica.data.db.entity.PlaylistEntity
import com.abra.musica.data.db.entity.PlaylistSongEntity

@Database(
    entities = [PlaylistEntity::class, PlaylistSongEntity::class],
    version = 1,
    exportSchema = true
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistSongDao(): PlaylistSongDao
}
