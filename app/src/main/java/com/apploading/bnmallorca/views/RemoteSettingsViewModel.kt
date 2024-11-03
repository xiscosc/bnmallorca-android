package com.apploading.bnmallorca.views

import androidx.lifecycle.ViewModel
import com.apploading.bnmallorca.bncore.RemoteSettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RemoteSettingsViewModel @Inject constructor(val remoteSettingsManager: RemoteSettingsManager) :
    ViewModel() {
}