package com.apploading.bnmallorca.service

import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.apploading.bnmallorca.R
import com.apploading.bnmallorca.bncore.TrackManager

class MediaPlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private val defaultAlbumArt = "android.resource://com.apploading.bnmallorca/drawable/album_placeholder"

    private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        val track = TrackManager.getTrackFromNotificationSharedPreferences(this)
        Log.d("MEDIA3", "registered $track")
        if (track !== null && mediaSession?.player?.isPlaying == true) {
            val media = mediaSession!!.player.currentMediaItem!!
            val metaCopy = media.mediaMetadata
                .buildUpon()
                .setArtist(track.artist)
                .setTitle(track.name)
                .setArtworkUri(Uri.parse(TrackManager.getAlbumArtUrl(track) ?: defaultAlbumArt))
                .build()

            val itemCopy = media.buildUpon()
                .setMediaMetadata(metaCopy)
                .build()

            mediaSession!!.player.replaceMediaItem(0, itemCopy)
        }
    }

    @OptIn(UnstableApi::class) override fun onCreate() {
        super.onCreate()


        val context = this
        TrackManager.storePlayingStatus(false, this)
        val sharedPreferences = TrackManager.getSharedPreferencesForNotification(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceListener)

        val player = ExoPlayer.Builder(this).build()
        val forwardingPlayer = object : ForwardingPlayer(player) {
            override fun play() {
                val track = TrackManager.getTrackFromSharedPreferences(context)
                val current = MediaItem.fromUri(Uri.parse(getString(R.string.stream_url)))
                val metaCopy = current.mediaMetadata
                    .buildUpon()
                    .setArtist(track?.artist ?: "Bn Mallorca Radio")
                    .setTitle(track?.name ?: "Independencia Musical")
                    .setArtworkUri(Uri.parse(track?.let { TrackManager.getAlbumArtUrl(it) } ?: defaultAlbumArt))
                    .build()

                val itemCopy = current.buildUpon()
                    .setMediaMetadata(metaCopy)
                    .build()

                this.setMediaItem(itemCopy)
                this.prepare()
                super.play()
                TrackManager.storePlayingStatus(true, context)
            }

            override fun pause() {
                super.stop()
                TrackManager.storePlayingStatus(false, context)
            }

            override fun stop() {
                super.stop()
                TrackManager.storePlayingStatus(false, context)
            }
        }

        mediaSession = MediaSession.Builder(this, forwardingPlayer).build()
    }

    // Remember to release the player and media session in onDestroy
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }

        val sharedPreferences = TrackManager.getSharedPreferencesForNotification(this)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceListener)
        super.onDestroy()
    }


    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? = mediaSession
}