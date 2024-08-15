package com.apploading.bnmallorca.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apploading.bnmallorca.bncore.BnApi
import com.apploading.bnmallorca.bncore.Track
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

    private val _isFetching = MutableStateFlow(false)
    val isFetching: StateFlow<Boolean> get() = _isFetching

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private var lastTrack: Number? = null // Store the lastTrack value

    fun fetchTracks() {
        viewModelScope.launch {
            _errorMessage.value = null
            try {
                if (lastTrack == null) {
                    _isLoading.value = true
                } else {
                    _isFetching.value = true
                }
                val api = BnApi.build()
                val response = api.getLastTracks(lastTrack = lastTrack)
                _trackList.value += response.tracks
                lastTrack = response.lastTrack // Store the lastTrack for subsequent calls
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isFetching.value = false
                _isLoading.value = false
            }
        }
    }

    fun loadMoreTracks() {
        if (lastTrack != null) {
            fetchTracks() // Fetch the next set of tracks using the stored lastTrack
        }
    }

    fun resetLastTrack() {
        lastTrack = null
        _trackList.value = emptyList()
    }
}