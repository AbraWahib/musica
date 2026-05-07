package com.abra.musica.data.repository

import com.abra.musica.data.db.dao.FavoriteSongDao
import com.abra.musica.data.db.dao.RecentlyPlayedDao
import com.abra.musica.data.db.entity.FavoriteSongEntity
import com.abra.musica.data.db.entity.RecentlyPlayedEntity
import com.abra.musica.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongCollectionRepository @Inject constructor(
    private val favoriteSongDao: FavoriteSongDao,
    private val recentlyPlayedDao: RecentlyPlayedDao,
    private val mediaStoreRepository: MediaStoreRepository
) {
    val favoriteSongIds: Flow<List<Long>> = favoriteSongDao.getFavoriteSongIds()

    fun isFavorite(songId: Long): Flow<Boolean> = favoriteSongDao.isFavorite(songId)

    fun getFavoriteSongs(): Flow<List<Song>> {
        return combine(
            favoriteSongDao.getFavorites(),
            mediaStoreRepository.getSongs()
        ) { favorites, songs ->
            val songsById = songs.associateBy { it.id }
            favorites.mapNotNull { songsById[it.songId] }
        }.flowOn(Dispatchers.IO)
    }

    fun getRecentlyPlayedSongs(): Flow<List<Song>> {
        return combine(
            recentlyPlayedDao.getRecentlyPlayed(),
            mediaStoreRepository.getSongs()
        ) { recent, songs ->
            val songsById = songs.associateBy { it.id }
            recent.mapNotNull { songsById[it.songId] }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun toggleFavorite(songId: Long) {
        if (isFavoriteOneShot(songId)) {
            favoriteSongDao.removeFavorite(songId)
        } else {
            favoriteSongDao.addFavorite(FavoriteSongEntity(songId = songId))
        }
    }

    suspend fun addFavorite(songId: Long) {
        favoriteSongDao.addFavorite(FavoriteSongEntity(songId = songId))
    }

    suspend fun removeFavorite(songId: Long) {
        favoriteSongDao.removeFavorite(songId)
    }

    suspend fun recordRecentlyPlayed(songId: Long) {
        recentlyPlayedDao.upsertRecentlyPlayed(RecentlyPlayedEntity(songId = songId))
    }

    private suspend fun isFavoriteOneShot(songId: Long): Boolean {
        return favoriteSongDao.isFavorite(songId).first()
    }
}
