package com.abra.musica.service

import android.content.Context
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import com.abra.musica.R

@UnstableApi
class MusicaNotificationProvider(
    private val context: Context
) : DefaultMediaNotificationProvider(
    context,
    { NOTIFICATION_ID },
    CHANNEL_ID,
    R.string.playback_notification_channel
) {

    init {
        setSmallIcon(R.drawable.ic_stat_music_note)
    }

    override fun getNotificationContentTitle(metadata: MediaMetadata): CharSequence {
        return metadata.title?.takeIf { it.isNotBlank() }
            ?: metadata.displayTitle?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.no_song_playing)
    }

    override fun getNotificationContentText(metadata: MediaMetadata): CharSequence? {
        val artist = metadata.artist?.takeIf { it.isNotBlank() }
        val album = metadata.albumTitle?.takeIf { it.isNotBlank() }
        return when {
            artist != null && album != null -> context.getString(
                R.string.notification_artist_album,
                artist,
                album
            )
            artist != null -> artist
            album != null -> album
            else -> null
        }
    }

    companion object {
        const val CHANNEL_ID = "musica_playback"
        const val NOTIFICATION_ID = 1001
    }
}
