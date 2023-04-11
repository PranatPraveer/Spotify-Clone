package com.example.spotifyclone.ExoPlayer

import android.support.v4.media.MediaMetadataCompat
import com.example.spotifyclone.models.Song

fun MediaMetadataCompat.toSong(): Song? {
    return description?.let {
        Song(
            it.mediaId ?: "",
            it.title.toString(),
            it.subtitle.toString(),
            it.mediaUri.toString(),
            it.iconUri.toString()
        )
    }
}