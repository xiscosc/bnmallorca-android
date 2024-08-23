package com.apploading.bnmallorca.bncore

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Named

class PlayManager @Inject constructor(@Named("playSharedPreferences") private val playPreferences: SharedPreferences) {
    private val preferenceListeners = mutableMapOf<String, SharedPreferences.OnSharedPreferenceChangeListener>()
    private val playingField = "playing"

    fun isPlaying() = playPreferences.getBoolean(playingField, false)

    fun storePlayingStatus(playing: Boolean) {
        playPreferences.edit().putBoolean(playingField, playing).apply()
    }

    fun registerOnPlayChangeListener(key: String, listener: () -> Unit) {
        unregisterOnPlayChangeListener(key)
        val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            listener()
        }
        preferenceListeners[key] = preferenceListener
        playPreferences.registerOnSharedPreferenceChangeListener(preferenceListener)
    }

    fun unregisterOnPlayChangeListener(key: String) {
        val preferenceListener = preferenceListeners[key]
        if (preferenceListener != null) {
            playPreferences.unregisterOnSharedPreferenceChangeListener(preferenceListener)
            preferenceListeners.remove(key)
        }
    }

    companion object {
        private const val PLAY_PREFERENCES = "play_preferences"

        fun getSharedPreferencesForPlay(context: Context): SharedPreferences {
            return context.getSharedPreferences(PLAY_PREFERENCES, Context.MODE_PRIVATE)
        }
    }
}