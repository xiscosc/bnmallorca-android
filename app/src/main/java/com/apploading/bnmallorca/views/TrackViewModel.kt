package com.apploading.bnmallorca.views

import androidx.lifecycle.ViewModel
import com.apploading.bnmallorca.bncore.TrackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class TrackViewModel @Inject constructor(
    private val trackManager: TrackManager,
) : ViewModel() {

    private val listenerKey = "trackViewModel"
    private val _trackInfoFlow = MutableStateFlow(getTrackInfo())
    val trackInfoFlow: StateFlow<TrackInfo> get() = _trackInfoFlow

    init {
        trackManager.registerOnTrackChangeListener(listenerKey) {
            onTrackChanged()
        }
    }

    private fun getTrackInfo(): TrackInfo {
        val track = trackManager.getCurrentTrack()
        return TrackInfo(
            artist = track.artist,
            name = track.name,
            albumArtUrl = TrackManager.getAlbumArtUrl(track) ?: "",
            isLoading = track.id == TrackManager.LOADING_TRACK_ID
        )
    }

    private fun onTrackChanged() {
        _trackInfoFlow.value = getTrackInfo()
    }

    override fun onCleared() {
        super.onCleared()
        trackManager.unregisterOnTrackChangeListener(listenerKey)
    }
}

data class TrackInfo(
    val artist: String,
    val name: String,
    val albumArtUrl: String,
    val isLoading: Boolean
)