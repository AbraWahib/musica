package com.abra.musica.data.model

import android.content.ContentUris
import android.net.Uri
import androidx.core.net.toUri

// Song.kt — primary media entity
data class Song(
    val id: Long,               // MediaStore._ID
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,          // For album art URI construction
    val artistId: Long,
    val duration: Long,         // Milliseconds
    val path: String,           // Display path; may be relative on scoped-storage devices
    val uri: Uri,               // Content URI (content://media/...)
    val trackNumber: Int,
    val year: Int,
    val size: Long,             // Bytes
    val dateAdded: Long,        // Epoch seconds
    val folderId: Long,
    val folderName: String
)

val mockSong = Song(
    id = 1L,
    title = "Song Title",
    artist = "Artist Name",
    album = "Album Title",
    albumId = 123153,
    artistId = 123153,
    duration = 180000L,
    path = "/path/to/song.mp3",
    uri = "content://media/external/audio/media/1".toUri(),
    trackNumber = 1,
    year = 2020,
    size = 1000000L,
    dateAdded = 1623456789L,
    folderId = 1L,
    folderName = "Folder Name"
)

// Extension function for album art URI
fun Song.albumArtUri(): Uri =
    ContentUris.withAppendedId(
        "content://media/external/audio/albumart".toUri(),
        albumId
    )
