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
import com.apploading.bnmallorca.bncore.PushManager
import com.apploading.bnmallorca.bncore.TrackManager
import com.google.common.collect.ImmutableList
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MediaPlaybackService : MediaSessionService() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private var mediaSession: MediaSession? = null
    private val defaultAlbumArt =
        "android.resource://com.apploading.bnmallorca/drawable/album_placeholder"
    private lateinit var nBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private val pauseReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_PAUSE -> {
                    mediaSession?.player?.pause()
                }
            }
        }
    }

    private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        val track = TrackManager.getTrackFromNotificationSharedPreferences(this)
        Log.d(TAG, "New track received: $track")
        if (track !== null && mediaSession?.player?.isPlaying == true) {
            Log.d(TAG, "Updating notification...")
            val media = mediaSession!!.player.currentMediaItem!!
            val metaCopy = media.mediaMetadata
                .buildUpon()
                .setArtist(TrackManager.filterTrackString(track.artist))
                .setTitle(TrackManager.filterTrackString(track.name))
                .setArtworkUri(Uri.parse(TrackManager.getAlbumArtUrl(track) ?: defaultAlbumArt))
                .build()

            val itemCopy = media.buildUpon()
                .setMediaMetadata(metaCopy)
                .build()

            mediaSession!!.player.replaceMediaItem(0, itemCopy)

            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                nBuilder.setSubText(TrackManager.filterTrackString(track.artist))
                nBuilder.setContentTitle(TrackManager.filterTrackString(track.name))
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

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(pauseReceiver, filter)
        } else {
            registerReceiver(pauseReceiver, filter, RECEIVER_NOT_EXPORTED)
        }

        TrackManager.storePlayingStatus(false, this)
        val sharedPreferences = TrackManager.getSharedPreferencesForNotification(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceListener)

        val player = ExoPlayer.Builder(this).build()
        val forwardingPlayer = object : ForwardingPlayer(player) {
            override fun play() {
                val track = TrackManager.getTrackFromSharedPreferences(this@MediaPlaybackService)
                val uri = Uri.parse(Firebase.remoteConfig.getString("streaming_url"))
                val current = MediaItem.fromUri(uri)
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
                scope.launch {
                    try {
                        PushManager.registerDevice(this@MediaPlaybackService, null)
                    } catch (e: Exception) {
                        return@launch
                    }
                }
            }

            override fun pause() {
                super.stop()
                TrackManager.storePlayingStatus(false, this@MediaPlaybackService)
                scope.launch {
                    try {
                        PushManager.unregisterDevice(this@MediaPlaybackService)
                    } catch (e: Exception) {
                        return@launch
                    }
                }
            }

            override fun stop() {
                super.stop()
                TrackManager.storePlayingStatus(false, this@MediaPlaybackService)
                scope.launch {
                    try {
                        PushManager.unregisterDevice(this@MediaPlaybackService)
                    } catch (e: Exception) {
                        return@launch
                    }
                }
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
            nBuilder.setLargeIcon(
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.album_placeholder
                )
            )
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            val pausePendingIntent: PendingIntent = PendingIntent.getBroadcast(
                this,
                REQUEST_CODE_PAUSE,
                Intent(ACTION_PAUSE),
                PendingIntent.FLAG_IMMUTABLE
            )
            nBuilder.addAction(
                androidx.media3.session.R.drawable.media3_icon_pause,
                "Pause",
                pausePendingIntent
            )
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
        unregisterReceiver(pauseReceiver)
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
        const val TAG = "MediaPlaybackService"
    }
}