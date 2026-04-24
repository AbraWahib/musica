package com.abra.musica.data.repository

import com.abra.musica.data.db.dao.PlaylistDao
import com.abra.musica.data.db.dao.PlaylistSongDao
import com.abra.musica.data.db.entity.PlaylistEntity
import com.abra.musica.data.db.entity.PlaylistSongEntity
import com.abra.musica.data.model.Playlist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val playlistSongDao: PlaylistSongDao
) {

    fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { it.toPlaylist() }
        }
    }

    suspend fun getPlaylistById(id: Long): Playlist? {
        return playlistDao.getPlaylistById(id)?.toPlaylist()
    }

    suspend fun createPlaylist(name: String): Long {
        val entity = PlaylistEntity(name = name)
        return playlistDao.insertPlaylist(entity)
    }

    suspend fun updatePlaylist(playlist: Playlist) {
        val entity = PlaylistEntity(
            id = playlist.id,
            name = playlist.name,
            createdAt = playlist.createdAt
        )
        playlistDao.updatePlaylist(entity)
    }

    suspend fun deletePlaylist(id: Long) {
        playlistDao.deletePlaylistById(id)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        val currentCount = playlistSongDao.getSongCountForPlaylist(playlistId)
        val playlistSong = PlaylistSongEntity(
            playlistId = playlistId,
            songId = songId,
            position = currentCount
        )
        playlistSongDao.insertPlaylistSong(playlistSong)
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        // Get the position of the song being removed
        // We use .first() to get a one-shot list from the Flow
        val songs = playlistSongDao.getSongsForPlaylist(playlistId).first()
        val songToRemove = songs.find { it.songId == songId }
        songToRemove?.let { song ->
            playlistSongDao.removeSongFromPlaylist(playlistId, songId)
            // Decrement positions of songs after the removed one to keep the sequence gapless
            playlistSongDao.decrementPositionsAfter(playlistId, song.position)
        }
    }

    suspend fun reorderPlaylistSongs(playlistId: Long, fromPosition: Int, toPosition: Int) {
        // Fetch current list once
        val songs = playlistSongDao.getSongsForPlaylist(playlistId).first().toMutableList()

        if (fromPosition in songs.indices && toPosition in songs.indices) {
            val movedSong = songs.removeAt(fromPosition)
            songs.add(toPosition, movedSong)

            // Map the new indices to the position field
            val updatedSongs = songs.mapIndexed { index, song ->
                song.copy(position = index)
            }
            // Update the whole list to persist new ordering
            playlistSongDao.insertPlaylistSongs(updatedSongs)
        }
    }

    fun getSongsForPlaylist(playlistId: Long): Flow<List<PlaylistSongEntity>> {
        return playlistSongDao.getSongsForPlaylist(playlistId)
    }

    suspend fun getSongCountForPlaylist(playlistId: Long): Int {
        return playlistSongDao.getSongCountForPlaylist(playlistId)
    }

    private fun PlaylistEntity.toPlaylist(): Playlist {
        return Playlist(
            id = id,
            name = name,
            songCount = 0, // We'll calculate this when needed
            createdAt = createdAt
        )
    }
}
