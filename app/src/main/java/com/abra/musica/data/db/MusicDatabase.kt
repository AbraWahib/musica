package com.abra.musica.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.abra.musica.data.db.dao.FavoriteSongDao
import com.abra.musica.data.db.dao.PlaylistDao
import com.abra.musica.data.db.dao.PlaylistSongDao
import com.abra.musica.data.db.dao.RecentlyPlayedDao
import com.abra.musica.data.db.entity.FavoriteSongEntity
import com.abra.musica.data.db.entity.PlaylistEntity
import com.abra.musica.data.db.entity.PlaylistSongEntity
import com.abra.musica.data.db.entity.RecentlyPlayedEntity

@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistSongEntity::class,
        FavoriteSongEntity::class,
        RecentlyPlayedEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistSongDao(): PlaylistSongDao
    abstract fun favoriteSongDao(): FavoriteSongDao
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS favorite_songs (
                        songId INTEGER NOT NULL PRIMARY KEY,
                        addedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS recently_played (
                        songId INTEGER NOT NULL PRIMARY KEY,
                        playedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
