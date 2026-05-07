package com.abra.musica.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.abra.musica.data.db.entity.FavoriteSongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteSongDao {
    @Query("SELECT * FROM favorite_songs ORDER BY addedAt DESC")
    fun getFavorites(): Flow<List<FavoriteSongEntity>>

    @Query("SELECT songId FROM favorite_songs")
    fun getFavoriteSongIds(): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_songs WHERE songId = :songId)")
    fun isFavorite(songId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(entity: FavoriteSongEntity)

    @Query("DELETE FROM favorite_songs WHERE songId = :songId")
    suspend fun removeFavorite(songId: Long)
}
