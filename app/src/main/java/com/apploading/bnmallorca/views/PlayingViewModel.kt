package com.apploading.bnmallorca.views

import androidx.lifecycle.ViewModel
import com.apploading.bnmallorca.bncore.PlayManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PlayingViewModel @Inject constructor(private val playManager: PlayManager) : ViewModel() {

    private val listenerKey = "playingViewModel"
    private val _playingStatusFlow = MutableStateFlow(getPlayingStatus())
    val playingStatusFlow: StateFlow<Boolean> get() = _playingStatusFlow

    init {
        playManager.registerOnPlayChangeListener(listenerKey) {
            onPlayingStatusChanged()
        }
    }

    private fun getPlayingStatus() = playManager.isPlaying()

    private fun onPlayingStatusChanged() {
        _playingStatusFlow.value = getPlayingStatus()
    }

    override fun onCleared() {
        super.onCleared()
        playManager.unregisterOnPlayChangeListener(listenerKey)
    }
}