package com.abra.musica.data.model

import android.content.ContentUris
import android.net.Uri

// Song.kt — primary media entity
data class Song(
    val id: Long,               // MediaStore._ID
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,          // For album art URI construction
    val artistId: Long,
    val duration: Long,         // Milliseconds
    val path: String,           // Absolute file path
    val uri: Uri,               // Content URI (content://media/...)
    val trackNumber: Int,
    val year: Int,
    val size: Long,             // Bytes
    val dateAdded: Long,        // Epoch seconds
    val folderId: Long,
    val folderName: String
)

// Extension function for album art URI
fun Song.albumArtUri(): Uri =
    ContentUris.withAppendedId(
        Uri.parse("content://media/external/audio/albumart"),
        albumId
    )
