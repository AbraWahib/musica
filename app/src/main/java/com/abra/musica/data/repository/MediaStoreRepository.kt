package com.abra.musica.data.repository

import android.annotation.SuppressLint
import android.app.RecoverableSecurityException
import android.content.IntentSender
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toUri
import com.abra.musica.data.model.Album
import com.abra.musica.data.model.Artist
import com.abra.musica.data.model.Folder
import com.abra.musica.data.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {

    fun getSongs(): Flow<List<Song>> = combine(
        querySongs(),
        settingsRepository.minimumSongDurationMs,
        settingsRepository.includedFolderPaths
    ) { songs, minimumDurationMs, includedFolderPaths ->
        songs.filter { song ->
            song.duration >= minimumDurationMs &&
                (includedFolderPaths.isEmpty() || song.folderPath() in includedFolderPaths)
        }
    }

    private fun querySongs(): Flow<List<Song>> = flow {
        val songs = mutableListOf<Song>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val useRelativePath = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        val projection = buildList {
            add(MediaStore.Audio.Media._ID)
            add(MediaStore.Audio.Media.TITLE)
            add(MediaStore.Audio.Media.ARTIST)
            add(MediaStore.Audio.Media.ALBUM)
            add(MediaStore.Audio.Media.ALBUM_ID)
            add(MediaStore.Audio.Media.ARTIST_ID)
            add(MediaStore.Audio.Media.DURATION)
            if (useRelativePath) {
                add(MediaStore.Audio.Media.DISPLAY_NAME)
                add(MediaStore.Audio.Media.RELATIVE_PATH)
            } else {
                add(MediaStore.Audio.Media.DATA)
            }
            add(MediaStore.Audio.Media.TRACK)
            add(MediaStore.Audio.Media.YEAR)
            add(MediaStore.Audio.Media.SIZE)
            add(MediaStore.Audio.Media.DATE_ADDED)
        }.toTypedArray()
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1"

        context.contentResolver.query(uri, projection, selection, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val artistIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol =
                if (useRelativePath) -1 else cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val displayNameCol = if (useRelativePath) {
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            } else {
                -1
            }
            val relativePathCol = if (useRelativePath) {
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH)
            } else {
                -1
            }
            val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol) ?: "Unknown"
                val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                val album = cursor.getString(albumCol) ?: "Unknown Album"
                val albumId = cursor.getLong(albumIdCol)
                val artistId = cursor.getLong(artistIdCol)
                val duration = cursor.getLong(durationCol)
                val displayName =
                    if (useRelativePath) cursor.getString(displayNameCol).orEmpty() else ""
                val relativePath =
                    if (useRelativePath) cursor.getString(relativePathCol).orEmpty() else ""
                val path = if (useRelativePath) {
                    buildDisplayPath(relativePath, displayName)
                } else {
                    cursor.getString(dataCol).orEmpty()
                }
                val uri =
                    ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                val trackNumber = cursor.getInt(trackCol)
                val year = cursor.getInt(yearCol)
                val size = cursor.getLong(sizeCol)
                val dateAdded = cursor.getLong(dateAddedCol)
                val folderPath = if (useRelativePath) {
                    normalizeFolderPath(relativePath)
                } else {
                    path.substringBeforeLast("/", missingDelimiterValue = "")
                }
                val folderName = folderPath.substringAfterLast("/").ifBlank { "Unknown Folder" }
                val folderId = folderPath.ifBlank { folderName }.hashCode().toLong()
                songs.add(
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
                )
            }
        }
        emit(songs)
    }.flowOn(Dispatchers.IO)

    suspend fun deleteSong(song: Song): DeleteSongResult = kotlinx.coroutines.withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val request = MediaStore.createDeleteRequest(context.contentResolver, listOf(song.uri))
            return@withContext DeleteSongResult.NeedsUserConsent(request.intentSender)
        }

        try {
            val deleted = context.contentResolver.delete(song.uri, null, null) > 0
            if (deleted) {
                DeleteSongResult.Deleted
            } else {
                DeleteSongResult.Failed
            }
        } catch (securityException: SecurityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                securityException.toRecoverableDeleteRequest()
            } else {
                DeleteSongResult.Failed
            }
        }
    }


    private fun String?.isUnknownAlbum(): Boolean =
        isNullOrBlank() || trim().lowercase() == "<unknown>"

    fun getAlbums(): Flow<List<Album>> = getSongs().map { songs ->
        songs.groupBy { song ->
            if (song.album.isUnknownAlbum()) UNKNOWN_ID else song.albumId
        }.map { (albumId, albumSongs) ->
            val firstSong = albumSongs.first()
            Album(
                id = albumId,
                title = if (albumId == UNKNOWN_ID) "Unknown Album" else firstSong.album,
                artist = if (albumSongs.map { it.artist }.distinct().size > 1) {
                    "Various Artists"
                } else {
                    firstSong.artist
                },
                songCount = albumSongs.size,
                year = albumSongs.map { it.year }.filter { it > 0 }.minOrNull() ?: 0,
                artUri = if (albumId == UNKNOWN_ID) {
                    Uri.EMPTY
                } else {
                    ContentUris.withAppendedId(
                        "content://media/external/audio/albumart".toUri(),
                        albumId
                    )
                }
            )
        }.sortedBy { it.title.lowercase() }
    }

    private fun buildUnknownAlbum(unknownAlbumIds: List<Long>): Album {
        // One focused query: count songs that belong to any of the unknown album IDs
        // and whose IS_MUSIC = 1
        val placeholders = unknownAlbumIds.joinToString(",") { "?" }
        val selection =
            "${MediaStore.Audio.Media.IS_MUSIC} = 1 AND " +
                    "${MediaStore.Audio.Media.ALBUM_ID} IN ($placeholders)"
        val selectionArgs = unknownAlbumIds.map { it.toString() }.toTypedArray()

        var songCount = 0
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media._ID),   // minimal projection — we only need the count
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            songCount = cursor.count
        }

        return Album(
            id = -1L,          // sentinel — no real MediaStore ID
            title = "Unknown Album",
            artist = "Various Artists",
            songCount = songCount,
            year = 0,
            artUri = Uri.EMPTY     // no artwork; your placeholder will show
        )
    }

    private fun String?.isUnknownArtist(): Boolean =
        isNullOrBlank() || trim().lowercase() == "<unknown>"

    fun getArtists(): Flow<List<Artist>> = getSongs().map { songs ->
        songs.groupBy { song ->
            if (song.artist.isUnknownArtist()) UNKNOWN_ID else song.artistId
        }.map { (artistId, artistSongs) ->
            Artist(
                id = artistId,
                name = if (artistId == UNKNOWN_ID) "Unknown Artist" else artistSongs.first().artist,
                albumCount = artistSongs.map { it.albumId }.distinct().size,
                songCount = artistSongs.size
            )
        }.sortedBy { it.name.lowercase() }
    }

    private fun buildUnknownArtist(unknownArtistIds: List<Long>): Artist {
        val placeholders = unknownArtistIds.joinToString(",") { "?" }
        val selectionArgs = unknownArtistIds.map { it.toString() }.toTypedArray()

        // Count distinct albums and total songs in one pass over Audio.Media
        var songCount = 0
        var albumCount = 0
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ALBUM_ID
            ),
            "${MediaStore.Audio.Media.IS_MUSIC} = 1 AND " +
                    "${MediaStore.Audio.Media.ARTIST_ID} IN ($placeholders)",
            selectionArgs,
            null
        )?.use { cursor ->
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val seenAlbums = mutableSetOf<Long>()

            while (cursor.moveToNext()) {
                songCount++
                seenAlbums += cursor.getLong(albumIdCol)
            }
            albumCount = seenAlbums.size
        }

        return Artist(
            id = -1L,
            name = "Unknown Artist",
            albumCount = albumCount,
            songCount = songCount
        )
    }

    fun getFolders(): Flow<List<Folder>> = getSongs().map { songs ->
        songs.groupBy { it.folderPath() }
            .map { (path, folderSongs) ->
                Folder(
                    id = path.ifBlank { folderSongs.first().folderName }.hashCode().toLong(),
                    name = folderSongs.first().folderName,
                    path = path,
                    songCount = folderSongs.size
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    fun getAvailableFolders(): Flow<List<Folder>> = queryAvailableFolders()

    private fun queryAvailableFolders(): Flow<List<Folder>> = flow {
        val useRelativePath = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        val projection = buildList {
            add(MediaStore.Audio.Media._ID)
            if (useRelativePath) {
                add(MediaStore.Audio.Media.RELATIVE_PATH)
            } else {
                add(MediaStore.Audio.Media.DATA)
            }
            add(MediaStore.Audio.Media.BUCKET_ID)
            add(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)
        }.toTypedArray()

        // bucket = folder in MediaStore terminology
        val folderMap = mutableMapOf<Long, FolderAccumulator>()

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            "${MediaStore.Audio.Media.IS_MUSIC} = 1",
            null,
            null
        )?.use { cursor ->
            val bucketIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.BUCKET_ID)
            val bucketNameCol =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)
            val dataCol =
                if (useRelativePath) -1 else cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val relativePathCol = if (useRelativePath) {
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH)
            } else {
                -1
            }

            while (cursor.moveToNext()) {
                val bucketId = cursor.getLong(bucketIdCol)
                val bucketName = cursor.getString(bucketNameCol) ?: "Unknown Folder"
                val folderPath = if (useRelativePath) {
                    normalizeFolderPath(cursor.getString(relativePathCol))
                } else {
                    val filePath = cursor.getString(dataCol).orEmpty()
                    filePath.substringBeforeLast("/", missingDelimiterValue = "").ifBlank { "/" }
                }
                val folderKey = folderPath.ifBlank { bucketName }.hashCode().toLong()

                val accumulator: FolderAccumulator =
                    folderMap.getOrPut(folderKey.takeIf { it != 0L } ?: bucketId) {
                        FolderAccumulator(name = bucketName, path = folderPath)
                    }
                accumulator.songCount++
            }
        }

        val folders = folderMap.map { (id, acc) ->
            Folder(
                id = id,
                name = acc.name,
                path = acc.path,
                songCount = acc.songCount
            )
        }.sortedBy { it.name.lowercase() }

        emit(folders)
    }.flowOn(Dispatchers.IO)


    private fun Song.folderPath(): String {
        return path.substringBeforeLast("/", missingDelimiterValue = "")
            .ifBlank { "/$folderName".trimEnd('/') }
    }

    private companion object {
        const val UNKNOWN_ID = -1L
    }
}

sealed interface DeleteSongResult {
    data object Deleted : DeleteSongResult
    data object Failed : DeleteSongResult
    data class NeedsUserConsent(val intentSender: IntentSender) : DeleteSongResult
}

@SuppressLint("NewApi")
private fun SecurityException.toRecoverableDeleteRequest(): DeleteSongResult {
    return if (this is RecoverableSecurityException) {
        DeleteSongResult.NeedsUserConsent(userAction.actionIntent.intentSender)
    } else {
        DeleteSongResult.Failed
    }
}

private fun normalizeFolderPath(relativePath: String?): String {
    val normalized = relativePath
        .orEmpty()
        .trim()
        .trim('/')
    return if (normalized.isBlank()) "/" else "/$normalized"
}

private fun buildDisplayPath(relativePath: String?, displayName: String): String {
    val folderPath = normalizeFolderPath(relativePath)
    return if (displayName.isBlank()) folderPath else "$folderPath/$displayName"
}

private data class FolderAccumulator(
    val name: String,
    val path: String,
    var songCount: Int = 0
)
