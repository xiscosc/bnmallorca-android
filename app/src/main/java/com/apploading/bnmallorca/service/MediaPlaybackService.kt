package com.apploading.bnmallorca.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import com.apploading.bnmallorca.MainActivity
import com.apploading.bnmallorca.R
import com.apploading.bnmallorca.bncore.TrackManager
import com.google.common.collect.ImmutableList

class MediaPlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private val defaultAlbumArt =
        "android.resource://com.apploading.bnmallorca/drawable/album_placeholder"
    private lateinit var nBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager

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

            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                nBuilder.setSubText(track.artist)
                nBuilder.setContentTitle(track.name)
                notificationManager.notify(1, nBuilder.build())
            }
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val filter = IntentFilter().apply {
            addAction(ACTION_PAUSE)
        }

        registerReceiver(receiver, filter)

        TrackManager.storePlayingStatus(false, this)
        val sharedPreferences = TrackManager.getSharedPreferencesForNotification(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceListener)

        val player = ExoPlayer.Builder(this).build()
        val forwardingPlayer = object : ForwardingPlayer(player) {
            override fun play() {
                val track = TrackManager.getTrackFromSharedPreferences(this@MediaPlaybackService)
                val current = MediaItem.fromUri(Uri.parse(getString(R.string.stream_url)))
                val metaCopy = current.mediaMetadata
                    .buildUpon()
                    .setArtist(track?.artist ?: "Bn Mallorca Radio")
                    .setTitle(track?.name ?: "Independencia Musical")
                    .setArtworkUri(Uri.parse(track?.let { TrackManager.getAlbumArtUrl(it) }
                        ?: defaultAlbumArt))
                    .build()

                val itemCopy = current.buildUpon()
                    .setMediaMetadata(metaCopy)
                    .build()

                this.setMediaItem(itemCopy)
                this.prepare()
                super.play()
                TrackManager.storePlayingStatus(true, this@MediaPlaybackService)
            }

            override fun pause() {
                super.stop()
                TrackManager.storePlayingStatus(false, this@MediaPlaybackService)
            }

            override fun stop() {
                super.stop()
                TrackManager.storePlayingStatus(false, this@MediaPlaybackService)
            }
        }

        mediaSession = MediaSession.Builder(this, forwardingPlayer).build()
        this.setMediaNotificationProvider(object : MediaNotification.Provider {
            override fun createNotification(
                mediaSession: MediaSession,
                customLayout: ImmutableList<CommandButton>,
                actionFactory: MediaNotification.ActionFactory,
                onNotificationChangedCallback: MediaNotification.Provider.Callback
            ): MediaNotification {
                createNotification(mediaSession)
                return MediaNotification(1, nBuilder.build())
            }

            override fun handleCustomCommand(
                session: MediaSession,
                action: String,
                extras: Bundle
            ): Boolean {
                TODO("Not yet implemented")
            }
        })
    }

    @OptIn(UnstableApi::class)
    fun createNotification(session: MediaSession) {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(
            NotificationChannel(
                "bnmallorca",
                "Channel",
                NotificationManager.IMPORTANCE_LOW
            )
        )
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE)

        val style = MediaStyleNotificationHelper.MediaStyle(session)
        nBuilder = NotificationCompat.Builder(this, "bnmallorca")
            .setContentIntent(openAppPendingIntent)
            .setSmallIcon(androidx.media3.session.R.drawable.media3_notification_small_icon)


        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            val track = TrackManager.getTrackFromSharedPreferences(this@MediaPlaybackService)
            nBuilder.setSubText(track?.artist ?: "Bn Mallorca Radio")
            nBuilder.setContentTitle(track?.name ?: "Independencia Musical")
            nBuilder.setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.album_placeholder))
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            val pauseIntent = Intent(ACTION_PAUSE)

            val pausePendingIntent: PendingIntent = PendingIntent.getBroadcast(
                this,
                REQUEST_CODE_PAUSE,
                pauseIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            nBuilder.addAction(androidx.media3.session.R.drawable.media3_notification_pause, "Pause", pausePendingIntent)
            nBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            style.setShowActionsInCompactView(0)
        }

        nBuilder.setStyle(style)
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
        unregisterReceiver(receiver)
        super.onDestroy()
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
    }


    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? = mediaSession

    companion object {
        const val ACTION_PAUSE = "action.PAUSE"
        const val REQUEST_CODE_PAUSE = 0
    }

    // Create a BroadcastReceiver in your MediaSessionService
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_PAUSE -> {
                    mediaSession?.player?.pause()
                }
            }
        }
    }
}