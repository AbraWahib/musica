package com.abra.musica.data.db.dao

import androidx.room.*
import com.abra.musica.data.db.entity.PlaylistEntity
import com.abra.musica.data.db.entity.PlaylistSongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query(
        """
        SELECT playlists.id, playlists.name, playlists.createdAt, COUNT(playlist_songs.songId) AS songCount
        FROM playlists
        LEFT JOIN playlist_songs ON playlists.id = playlist_songs.playlistId
        GROUP BY playlists.id
        ORDER BY playlists.createdAt DESC
        """
    )
    fun getAllPlaylistsWithSongCount(): Flow<List<PlaylistWithSongCount>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylistById(id: Long)
}

data class PlaylistWithSongCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val songCount: Int
)
