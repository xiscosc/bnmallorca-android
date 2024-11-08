package com.apploading.bnmallorca.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import com.apploading.bnmallorca.MainActivity
import com.apploading.bnmallorca.R
import com.apploading.bnmallorca.bncore.PlayManager
import com.apploading.bnmallorca.bncore.PushManager
import com.apploading.bnmallorca.bncore.RemoteSettingsManager
import com.apploading.bnmallorca.bncore.TrackManager
import com.google.common.collect.ImmutableList
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MediaPlaybackService : MediaSessionService() {
    @Inject
    lateinit var pushManager: PushManager

    @Inject
    lateinit var trackManager: TrackManager

    @Inject
    lateinit var playManager: PlayManager

    @Inject
    lateinit var remoteSettingsManager: RemoteSettingsManager

    private val job = SupervisorJob()
    private val sharedPreferencesListenerKey = "mediaPlayer"
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private var mediaSession: MediaSession? = null
    private val defaultAlbumArt =
        "android.resource://com.apploading.bnmallorca/drawable/new_album_placeholder_300"
    private lateinit var nBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private val playPauseReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_PAUSE -> {
                    mediaSession?.player?.pause()
                }

                ACTION_PLAY -> {
                    mediaSession?.player?.play()
                }
            }
        }
    }

    private fun updateMediaNotification() {
        if (mediaSession?.player?.currentMediaItem !== null) {
            val isPlaying = playManager.isPlaying()
            val (artist, name, albumArt) = if (isPlaying) {
                val track = trackManager.getCurrentTrack()
                Log.d(TAG, "Updating media player notification with track...")
                Log.d(TAG, "New track received: $track")
                Triple(
                    TrackManager.filterTrackString(track.artist),
                    TrackManager.filterTrackString(track.name),
                    TrackManager.getAlbumArtUrl(track) ?: defaultAlbumArt
                )
            } else {
                Log.d(TAG, "Updating media player notification with to empty...")
                Triple("BN Mallorca", "Radio", defaultAlbumArt)
            }

            Log.d(TAG, "Updating media player notification...")
            val media = mediaSession!!.player.currentMediaItem!!
            val metaCopy = media.mediaMetadata
                .buildUpon()
                .setArtist(artist)
                .setTitle(name)
                .setArtworkUri(Uri.parse(albumArt))
                .build()

            val itemCopy = media.buildUpon()
                .setMediaMetadata(metaCopy)
                .build()

            mediaSession!!.player.replaceMediaItem(0, itemCopy)

            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                nBuilder.setSubText(artist)
                nBuilder.setContentTitle(name)
                notificationManager.notify(1, nBuilder.build())
            }
        } else {
            Log.d(TAG, "No current media item to update notification")
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val filter = IntentFilter().apply {
            addAction(ACTION_PAUSE)
            addAction(ACTION_PLAY)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(playPauseReceiver, filter)
        } else {
            registerReceiver(playPauseReceiver, filter, RECEIVER_NOT_EXPORTED)
        }

        trackManager.registerOnTrackChangeListener(sharedPreferencesListenerKey) { updateMediaNotification() }
        playManager.registerOnPlayChangeListener(sharedPreferencesListenerKey) { updateMediaNotification() }

        val audioAttributes = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build()
        val player = ExoPlayer.Builder(this).setAudioAttributes(audioAttributes, true).build()

        val forwardingPlayer = object : ForwardingPlayer(player) {
            override fun play() {
                val track = trackManager.getCurrentTrack()
                val uri = Uri.parse(remoteSettingsManager.getSettings().streamingUrl)
                val current = MediaItem.fromUri(uri)
                val metaCopy = current.mediaMetadata
                    .buildUpon()
                    .setArtist(track.artist)
                    .setTitle(track.name)
                    .setArtworkUri(Uri.parse(track.let { TrackManager.getAlbumArtUrl(it) }
                        ?: defaultAlbumArt))
                    .build()

                val itemCopy = current.buildUpon()
                    .setMediaMetadata(metaCopy)
                    .build()

                trackManager.setTrackLoading()
                playManager.storePlayingStatus(true)
                this.setMediaItem(itemCopy)
                scope.launch {
                    try {
                        val updateLastTrackJob = async { trackManager.updateLastTrackFromApi() }
                        val registerDeviceJob = async { pushManager.registerDevice(null) }
                        updateLastTrackJob.await()
                        registerDeviceJob.await()
                    } catch (e: Exception) {
                        return@launch
                    }
                }
                this.prepare()
                super.play()
            }
        }

        forwardingPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (!isPlaying) {
                    playManager.storePlayingStatus(false)
                    scope.launch {
                        try {
                            pushManager.unregisterDevice()
                        } catch (e: Exception) {
                            return@launch
                        }
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                playManager.storePlayingStatus(false)
                if (forwardingPlayer.isPlaying) forwardingPlayer.stop()
                Log.e(TAG, "Player error")
            }
        })

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
            .setSmallIcon(R.drawable.radio_icon)

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            val track = trackManager.getCurrentTrack()
            nBuilder.setSubText(track.artist)
            nBuilder.setContentTitle(track.name)
            CoroutineScope(Dispatchers.Main).launch {
                val albumArtBitmap = TrackManager.getAlbumArtBitmap(track)
                if (albumArtBitmap != null) {
                    nBuilder.setLargeIcon(albumArtBitmap)
                } else {
                    nBuilder.setLargeIcon(
                        BitmapFactory.decodeResource(
                            resources,
                            R.drawable.new_album_placeholder_300
                        )
                    )
                }
                notificationManager.notify(1, nBuilder.build())
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            val isPlaying = session.player.isPlaying
            val pausePendingIntent: PendingIntent = PendingIntent.getBroadcast(
                this,
                REQUEST_CODE_PAUSE,
                Intent(ACTION_PAUSE),
                PendingIntent.FLAG_IMMUTABLE
            )

            val playPendingIntent: PendingIntent = PendingIntent.getBroadcast(
                this,
                REQUEST_CODE_PLAY,
                Intent(ACTION_PLAY),
                PendingIntent.FLAG_IMMUTABLE
            )

            if (isPlaying) {
                nBuilder.addAction(
                    androidx.media3.session.R.drawable.media3_icon_pause,
                    "Pause",
                    pausePendingIntent
                )
            } else {
                nBuilder.addAction(
                    androidx.media3.session.R.drawable.media3_icon_play,
                    "Play",
                    playPendingIntent
                )
            }

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

        trackManager.unregisterOnTrackChangeListener(sharedPreferencesListenerKey)
        playManager.unregisterOnPlayChangeListener(sharedPreferencesListenerKey)
        unregisterReceiver(playPauseReceiver)
        playManager.storePlayingStatus(false)
        super.onDestroy()
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        mediaSession?.run {
            player.pause()
        }
        stopSelf()
    }


    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? = mediaSession

    companion object {
        const val ACTION_PAUSE = "action.PAUSE"
        const val ACTION_PLAY = "action.PLAY"
        const val REQUEST_CODE_PAUSE = 0
        const val REQUEST_CODE_PLAY = 1
        const val TAG = "MediaPlaybackService"
    }
}