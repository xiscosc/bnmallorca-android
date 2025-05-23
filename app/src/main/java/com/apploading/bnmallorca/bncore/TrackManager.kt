package com.apploading.bnmallorca.bncore

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.apploading.bnmallorca.bncore.PushManager.Companion
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class TrackManager @Inject constructor(
    @Named("trackSharedPreferences") private val trackPreferences: SharedPreferences,
    private val remoteSettingsManager: RemoteSettingsManager
) {
    private val preferenceListeners =
        mutableMapOf<String, SharedPreferences.OnSharedPreferenceChangeListener>()

    @Inject
    lateinit var playManager: PlayManager

    fun storeTrackFromPushNotification(notification: String) {
        if (playManager.isPlaying() && getCurrentTrack().id != LOADING_TRACK_ID) {
            val track = buildTrackFromPushNotification(notification)
            storeTrack(track, false)
        }
    }

    fun setTrackLoading() {
        storeTrack(Track(LOADING_TRACK_ID, "", "", 0, emptyList()), true)
    }

    suspend fun updateLastTrackFromApi() {
        Log.d(TAG, "Getting last track from API")
        val api = BnApi.build(this.remoteSettingsManager.getSettings())
        val track = try {
            val tracks = api.getLastTrack().tracks
            if (tracks.isEmpty()) defaultTrack else tracks[0]
        } catch (e: Exception) {
            defaultTrack
        }

        val filteredTrack = Track(
            track.id,
            filterTrackString(track.name),
            filterTrackString(track.artist),
            track.timestamp,
            track.albumArt
        )

        Log.d(TAG, "Last track from API: $filteredTrack")
        storeTrack(filteredTrack, true)
    }

    fun getCurrentTrack(): Track {
        val gson = Gson()
        val trackJson = trackPreferences.getString("track", "") ?: ""
        if (trackJson.isEmpty()) return defaultTrack
        return gson.fromJson(trackJson, Track::class.java)
    }

    fun registerOnTrackChangeListener(key: String, listener: () -> Unit) {
        unregisterOnTrackChangeListener(key)
        val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            listener()
        }
        preferenceListeners[key] = preferenceListener
        trackPreferences.registerOnSharedPreferenceChangeListener(preferenceListener)
    }

    fun unregisterOnTrackChangeListener(key: String) {
        val preferenceListener = preferenceListeners[key]
        if (preferenceListener != null) {
            trackPreferences.unregisterOnSharedPreferenceChangeListener(preferenceListener)
            preferenceListeners.remove(key)
        }
    }

    suspend fun getTrackList(lastTrack: Number?): TrackResponse {
        val api = BnApi.build(this.remoteSettingsManager.getSettings())
        return api.getLastTracks(lastTrack = lastTrack)
    }

    private fun storeTrack(
        track: Track,
        override: Boolean = false
    ) {
        if (!override) {
            val currentTrack = getCurrentTrack()
            if (track.timestamp.toInt() <= currentTrack.timestamp.toInt()) return
        }

        val gson = Gson()
        val trackJson = gson.toJson(track)
        trackPreferences.edit().putString("track", trackJson).apply()
    }

    companion object {
        private const val TAG = "TrackManager"
        const val LOADING_TRACK_ID = "loading id"
        private const val TRACK_PREFERENCES = "track_preferences"
        private val defaultTrack = Track(
            id = "0",
            name = "BN Mallorca",
            artist = "Radio",
            timestamp = 0,
            albumArt = listOf()
        )

        fun getAlbumArtUrl(track: Track, highestQuality: Boolean = true): String? {
            return getAlbumArtUrl(track.albumArt, highestQuality)
        }

        fun getAlbumArtUrl(albumArts: List<AlbumArt>, highestQuality: Boolean = true): String? {
            val sizes = sortedSetOf<Int>()
            val urlMap = mutableMapOf<Number, String>()
            albumArts.forEach {
                val size = it.size.split("x")[0].toIntOrNull() ?: 0
                sizes.add(size)
                urlMap[size] = it.downloadUrl
            }

            if (sizes.isEmpty()) return null
            return urlMap[if (highestQuality) sizes.last() else sizes.first()]
        }

        suspend fun getAlbumArtBitmap(track: Track): Bitmap? {
            val imageUrl = getAlbumArtUrl(track)
            if (imageUrl.isNullOrEmpty()) return null
            return withContext(Dispatchers.IO) {
                try {
                    val url = URL(imageUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()
                    val inputStream = connection.inputStream
                    BitmapFactory.decodeStream(inputStream)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }

        fun getTimeAgo(track: Track, unit: DurationUnit): Number {
            val timestampInMillis = track.timestamp.toLong() * 1000
            val currentTimeInMillis = System.currentTimeMillis()
            val durationInMillis = currentTimeInMillis - timestampInMillis
            val duration = durationInMillis.toDuration(DurationUnit.MILLISECONDS)
            return duration.toInt(unit)
        }

        fun getSharedPreferencesForTrack(context: Context): SharedPreferences {
            return context.getSharedPreferences(TRACK_PREFERENCES, Context.MODE_PRIVATE)
        }

        fun filterTrackString(field: String?): String {
            if (field.isNullOrEmpty()) {
                return "BN Mallorca"
            }

            return if (field.lowercase().trim() in listOf(
                    "desconocido",
                    "unknown"
                )
            ) "BN Mallorca" else field
        }

        private fun buildTrackFromPushNotification(notification: String): Track {
            val gson = Gson()
            val type = object : TypeToken<HashMap<String, String>>() {}.type
            val map = gson.fromJson<HashMap<String, String>>(notification, type)
            val albumType = object : TypeToken<Array<AlbumArt>>() {}.type
            val array = gson.fromJson<Array<AlbumArt>>(map["albumArt"], albumType)
            return Track(
                id = map["id"] ?: "",
                name = filterTrackString(map["name"]),
                artist = filterTrackString(map["artist"]),
                timestamp = map["timestamp"]?.toInt() ?: 0,
                albumArt = array.toList()
            )
        }
    }
}