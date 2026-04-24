package com.abra.musica.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import androidx.room.Room
import com.abra.musica.data.db.MusicDatabase
import com.abra.musica.data.db.dao.PlaylistDao
import com.abra.musica.data.db.dao.PlaylistSongDao
import com.abra.musica.player.PlayerController
import com.abra.musica.player.PlayerControllerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMusicDatabase(
        @ApplicationContext context: Context
    ): MusicDatabase {
        return Room.databaseBuilder(
            context,
            MusicDatabase::class.java,
            "music_database"
        ).build()
    }

    @Provides
    @Singleton
    fun providePlaylistDao(database: MusicDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    @Singleton
    fun providePlaylistSongDao(database: MusicDatabase): PlaylistSongDao {
        return database.playlistSongDao()
    }

    @Provides
    @Singleton
    fun provideExoPlayer(
        @ApplicationContext context: Context
    ): ExoPlayer {
        return ExoPlayer.Builder(context).build()
    }

    @Provides
    @Singleton
    fun providePlayerController(
        playerControllerImpl: PlayerControllerImpl
    ): PlayerController {
        return playerControllerImpl
    }
}
