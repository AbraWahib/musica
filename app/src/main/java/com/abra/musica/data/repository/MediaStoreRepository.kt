package com.abra.musica.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.abra.musica.data.model.Album
import com.abra.musica.data.model.Artist
import com.abra.musica.data.model.Folder
import com.abra.musica.data.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getSongs(): Flow<List<Song>> = flow {
        val songs = mutableListOf<Song>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1 AND ${MediaStore.Audio.Media.DURATION} >= 30000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
            while (cursor.moveToNext()) {
                val song = cursor.toSong()
                if (song != null) {
                    songs += song
                }
            }
        }
        emit(songs)
    }.flowOn(Dispatchers.IO)

    fun getAlbums(): Flow<List<Album>> = flow {
        val albums = mutableListOf<Album>()
        val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS,
            MediaStore.Audio.Albums.FIRST_YEAR
        )

        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val album = cursor.toAlbum()
                if (album != null) {
                    albums += album
                }
            }
        }
        emit(albums)
    }.flowOn(Dispatchers.IO)

    fun getArtists(): Flow<List<Artist>> = flow {
        val artists = mutableListOf<Artist>()
        val uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS
        )

        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val artist = cursor.toArtist()
                if (artist != null) {
                    artists += artist
                }
            }
        }
        emit(artists)
    }.flowOn(Dispatchers.IO)

    fun getFolders(): Flow<List<Folder>> = flow {
        val folders = mutableMapOf<String, MutableList<Song>>()

        // First get all songs to group by folder
        getSongs().collect { songs ->
            songs.forEach { song ->
                val folderPath = song.path.substringBeforeLast("/")
                val folderName = folderPath.substringAfterLast("/")
                folders.getOrPut(folderPath) { mutableListOf() }.add(song)
            }

            val folderList = folders.map { (path, songsInFolder) ->
                Folder(
                    id = path.hashCode().toLong(),
                    name = path.substringAfterLast("/"),
                    path = path,
                    songCount = songsInFolder.size
                )
            }
            emit(folderList)
        }
    }.flowOn(Dispatchers.IO)

    // Extension functions for cursor mapping
    private fun android.database.Cursor.toSong(): Song? {
        return try {
            val id = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
            val title = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)) ?: "Unknown"
            val artist = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)) ?: "Unknown Artist"
            val album = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)) ?: "Unknown Album"
            val albumId = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
            val artistId = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID))
            val duration = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
            val path = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)) ?: ""
            val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
            val trackNumber = getInt(getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK))
            val year = getInt(getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR))
            val size = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
            val dateAdded = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED))
            val folderPath = path.substringBeforeLast("/")
            val folderId = folderPath.hashCode().toLong()
            val folderName = folderPath.substringAfterLast("/")

            Song(
                id = id,
                title = title,
                artist = artist,
                album = album,
                albumId = albumId,
                artistId = artistId,
                duration = duration,
                path = path,
                uri = uri,
                trackNumber = trackNumber,
                year = year,
                size = size,
                dateAdded = dateAdded,
                folderId = folderId,
                folderName = folderName
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun android.database.Cursor.toAlbum(): Album? {
        return try {
            val id = getLong(getColumnIndexOrThrow(MediaStore.Audio.Albums._ID))
            val title = getString(getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)) ?: "Unknown Album"
            val artist = getString(getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)) ?: "Unknown Artist"
            val songCount = getInt(getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS))
            val year = getInt(getColumnIndexOrThrow(MediaStore.Audio.Albums.FIRST_YEAR))
            val artUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), id)

            Album(
                id = id,
                title = title,
                artist = artist,
                songCount = songCount,
                year = year,
                artUri = artUri
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun android.database.Cursor.toArtist(): Artist? {
        return try {
            val id = getLong(getColumnIndexOrThrow(MediaStore.Audio.Artists._ID))
            val name = getString(getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)) ?: "Unknown Artist"
            val albumCount = getInt(getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS))
            val songCount = getInt(getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS))

            Artist(
                id = id,
                name = name,
                albumCount = albumCount,
                songCount = songCount
            )
        } catch (e: Exception) {
            null
        }
    }
}
