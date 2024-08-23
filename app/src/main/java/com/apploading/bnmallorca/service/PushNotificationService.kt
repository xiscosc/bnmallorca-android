package com.apploading.bnmallorca.service

import android.content.Intent
import android.util.Log
import com.apploading.bnmallorca.bncore.PushManager
import com.google.firebase.messaging.Constants.TAG
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.apploading.bnmallorca.bncore.TrackManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PushNotificationService : FirebaseMessagingService() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    @Inject
    lateinit var pushManager: PushManager

    @Inject
    lateinit var trackManager: TrackManager

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        trackManager.storeTrackFromPushNotification(Gson().toJson(remoteMessage.data))
        Log.d(TAG, "Message received: ${remoteMessage.data}")
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        scope.launch {
            try {
                pushManager.registerDevice(token)
            } catch (e: Exception) {
                Log.e(TAG, "Error registering token in Grupo Fantome API")
                return@launch
            }
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        scope.launch {
            try {
                pushManager.unregisterDevice()
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering token in Grupo Fantome API")
                return@launch
            }
        }
    }
}