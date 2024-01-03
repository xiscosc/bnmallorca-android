package com.apploading.bnmallorca.bncore

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class TrackManager {

    companion object {
        private const val TRACK_PREFERENCES = "track_preferences"
        private const val NOTIFICATION_PREFERENCES = "track_notification_preferences"
        const val TRACK_ARTIST_FIELD = "track_artist"
        const val TRACK_NAME_FIELD = "track_name"
        const val TRACK_ALBUM_ART_URL_FIELD = "track_album_art_url"
        const val TRACK_PLAYING_STATUS_FIELD = "track_playing_status"

        fun getAlbumArtUrl(track: Track): String? {
            val sizes = sortedSetOf<Int>()
            val urlMap = mutableMapOf<Number, String>()
            track.albumArt.forEach {
                val size = it.size.split("x")[0].toIntOrNull() ?: 0
                sizes.add(size)
                urlMap[size] = it.downloadUrl
            }

            if (sizes.isEmpty()) return null
            return urlMap[sizes.last()]
        }

        fun buildTrackFromNotification(notification: String): Track {
            val gson = Gson()
            val type = object : TypeToken<HashMap<String, String>>() {}.type
            val map = gson.fromJson<HashMap<String, String>>(notification, type)
            val albumType = object : TypeToken<Array<AlbumArt>>() {}.type
            val array = gson.fromJson<Array<AlbumArt>>(map["albumArt"], albumType)
            return Track(
                id = map["id"] ?: "",
                name = map["name"] ?: "",
                artist = map["artist"] ?: "",
                timestamp = map["timestamp"]?.toInt() ?: 0,
                albumArt = array.toList()
            )
        }

        suspend fun updateLastTrackFromApi(context: Context) {
            val api = BnApi.build(context)
            val defaultTrack = Track(
                id = "0",
                name = "Bn Mallorca Radio",
                artist = "Bn Mallorca Radio",
                timestamp = 0,
                albumArt = listOf()
            )
            val track = try {
                val tracks = api.getLastTrack().tracks
                if (tracks.isEmpty()) defaultTrack else tracks[0]
            } catch (e: Exception) {
                defaultTrack
            }

            storeTrackInSharedPreferences(track, context, true)
        }

        fun storePlayingStatus(status: Boolean, context: Context) {
            val trackSharedPreferences = getSharedPreferencesForTrack(context)
            trackSharedPreferences.edit().putBoolean(TRACK_PLAYING_STATUS_FIELD, status).apply()
        }

        fun storeTrackInSharedPreferences(
            track: Track,
            context: Context,
            override: Boolean = false
        ) {
            val trackPreferences = getSharedPreferencesForTrack(context)
            val notificationPreferences = getSharedPreferencesForNotification(context)

            if (!override) {
                val currentTimestamp = trackPreferences.getInt("track_timestamp", 0)
                if (track.timestamp.toInt() <= currentTimestamp) return
            }

            trackPreferences.edit().putString(TRACK_ARTIST_FIELD, track.artist).apply()
            trackPreferences.edit().putString(TRACK_NAME_FIELD, track.name).apply()
            trackPreferences.edit()
                .putString(TRACK_ALBUM_ART_URL_FIELD, getAlbumArtUrl(track) ?: "").apply()
            trackPreferences.edit().putInt("track_timestamp", track.timestamp.toInt()).apply()

            val gson = Gson()
            val trackJson = gson.toJson(track)
            notificationPreferences.edit().putString("track", trackJson).apply()
        }

        fun getSharedPreferencesForTrack(context: Context): SharedPreferences {
            return context.getSharedPreferences(TRACK_PREFERENCES, Context.MODE_PRIVATE)
        }

        fun getSharedPreferencesForNotification(context: Context): SharedPreferences {
            return context.getSharedPreferences(NOTIFICATION_PREFERENCES, Context.MODE_PRIVATE)
        }

        fun getTrackFromNotificationSharedPreferences(context: Context): Track? {
            val sharedPreferences = getSharedPreferencesForNotification(context)
            val gson = Gson()
            val trackJson = sharedPreferences.getString("track", "") ?: ""
            if (trackJson.isEmpty()) return null
            return gson.fromJson(trackJson, Track::class.java)
        }

        fun getTrackFromSharedPreferences(context: Context): Track? {
            val sharedPreferences = getSharedPreferencesForTrack(context)
            val artist = sharedPreferences.getString(TRACK_ARTIST_FIELD, "") ?: return null
            val name = sharedPreferences.getString(TRACK_NAME_FIELD, "") ?: return null
            val albumArtUrl = sharedPreferences.getString(TRACK_ALBUM_ART_URL_FIELD, "") ?: ""
            val albumArtList = if (albumArtUrl.isEmpty()) {
                listOf()
            } else {
                listOf(AlbumArt(downloadUrl = albumArtUrl, size = "1x1"))
            }

            return Track(
                id = "",
                name = name,
                artist = artist,
                timestamp = 0,
                albumArt = albumArtList
            )
        }

        fun filterTrackString(field: String) =
            if (field.lowercase().trim() in listOf(
                    "desconocido",
                    "unknown"
                )
            ) "BN Mallorca" else field
    }
}