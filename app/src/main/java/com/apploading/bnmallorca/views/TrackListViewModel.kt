package com.apploading.bnmallorca.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apploading.bnmallorca.bncore.Track
import com.apploading.bnmallorca.bncore.TrackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TrackListViewModel @Inject constructor() : ViewModel() {

    private val _trackList = MutableStateFlow<List<Track>>(emptyList())
    val trackList: StateFlow<List<Track>> get() = _trackList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _isPolling = MutableStateFlow(false)
    val isPolling: StateFlow<Boolean> get() = _isPolling

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private var lastTrack: Number? = null // Store the lastTrack value

    fun loadTracks() {
        viewModelScope.launch {
            _errorMessage.value = null
            try {
                _isLoading.value = true
                val response = TrackManager.getTrackList(lastTrack = lastTrack)
                _trackList.value = response.tracks
                lastTrack = response.lastTrack // Store the lastTrack for subsequent calls
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun pollTracks() {
        viewModelScope.launch {
            _errorMessage.value = null
            try {
                _isPolling.value = true
                val response = TrackManager.getTrackList(lastTrack = lastTrack)
                _trackList.value += response.tracks
                lastTrack = response.lastTrack // Store the lastTrack for subsequent calls
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isPolling.value = false
            }
        }
    }

    fun resetLastTrack() {
        lastTrack = null
    }

    fun resetTracks() {
        _trackList.value = emptyList()
    }
}