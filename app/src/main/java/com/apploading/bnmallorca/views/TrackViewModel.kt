package com.apploading.bnmallorca.views

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.apploading.bnmallorca.bncore.TrackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class TrackViewModel @Inject constructor(@Named("trackSharedPreferences") private val sharedPreferences: SharedPreferences) :
    ViewModel(), SharedPreferences.OnSharedPreferenceChangeListener {

    // MutableStateFlow to hold the TrackInfo data
    private val _trackInfoFlow = MutableStateFlow(getInitialTrackInfo())
    val trackInfoFlow: StateFlow<TrackInfo> get() = _trackInfoFlow

    init {
        // Register the listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    // Function to read initial data from SharedPreferences
    private fun getInitialTrackInfo(): TrackInfo {
        return TrackInfo(
            artist = sharedPreferences.getString(TrackManager.TRACK_ARTIST_FIELD, "") ?: "",
            name = sharedPreferences.getString(TrackManager.TRACK_NAME_FIELD, "") ?: "",
            albumArtUrl = sharedPreferences.getString(TrackManager.TRACK_ALBUM_ART_URL_FIELD, "")
                ?: "",
            playing = sharedPreferences.getBoolean(TrackManager.TRACK_PLAYING_STATUS_FIELD, false)
        )
    }

    // Listener to respond to changes in SharedPreferences
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            TrackManager.TRACK_ARTIST_FIELD, TrackManager.TRACK_NAME_FIELD, TrackManager.TRACK_ALBUM_ART_URL_FIELD, TrackManager.TRACK_PLAYING_STATUS_FIELD -> {
                _trackInfoFlow.value =
                    getInitialTrackInfo() // Update the StateFlow with the new data
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Unregister the listener when the ViewModel is cleared
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}

data class TrackInfo(
    val artist: String,
    val name: String,
    val albumArtUrl: String,
    val playing: Boolean
)