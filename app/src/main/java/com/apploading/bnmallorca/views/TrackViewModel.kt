package com.apploading.bnmallorca.views

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.apploading.bnmallorca.bncore.TrackManager

class TrackViewModel(application: Application) : AndroidViewModel(application) {
    val artist : LiveData<String>
    val name : LiveData<String>
    val albumArtUrl : LiveData<String>
    val playing: LiveData<Boolean>

    init {
        val sharedPreferences = TrackManager.getSharedPreferencesForTrack(getApplication())
        artist = sharedPreferences.stringLiveData(TrackManager.TRACK_ARTIST_FIELD, "")
        name = sharedPreferences.stringLiveData(TrackManager.TRACK_NAME_FIELD, "")
        albumArtUrl = sharedPreferences.stringLiveData(TrackManager.TRACK_ALBUM_ART_URL_FIELD, "")
        playing = sharedPreferences.booleanLiveData(TrackManager.TRACK_PLAYING_STATUS_FIELD, false)
    }
}

fun SharedPreferences.stringLiveData(key: String, defaultValue: String): LiveData<String> {
    return SharedPreferenceLiveDataString(this, key, defaultValue)
}

fun SharedPreferences.booleanLiveData(key: String, defaultValue: Boolean): LiveData<Boolean> {
    return SharedPreferenceLiveDataBoolean(this, key, defaultValue)
}

class SharedPreferenceLiveDataString(
    private val prefs: SharedPreferences,
    private val key: String,
    private val defaultValue: String
) : LiveData<String>() {

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, k ->
        if (k == key) {
            value = prefs.getString(key, defaultValue)
        }
    }

    override fun onActive() {
        super.onActive()
        value = prefs.getString(key, defaultValue)
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onInactive() {
        prefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }
}

class SharedPreferenceLiveDataBoolean(
    private val prefs: SharedPreferences,
    private val key: String,
    private val defaultValue: Boolean
) : LiveData<Boolean>() {

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, k ->
        if (k == key) {
            value = prefs.getBoolean(key, defaultValue)
        }
    }

    override fun onActive() {
        super.onActive()
        value = prefs.getBoolean(key, defaultValue)
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onInactive() {
        prefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }
}