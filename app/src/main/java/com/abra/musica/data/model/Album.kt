package com.abra.musica.data.model

import android.net.Uri

// Album.kt
data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val songCount: Int,
    val year: Int,
    val artUri: Uri             // content://media/external/audio/albumart/{id}
)
