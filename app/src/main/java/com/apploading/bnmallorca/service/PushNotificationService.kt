package com.apploading.bnmallorca.service

import android.content.Intent
import android.util.Log
import com.apploading.bnmallorca.bncore.PushManager
import com.google.firebase.messaging.Constants.TAG
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.apploading.bnmallorca.bncore.TrackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class PushNotificationService : FirebaseMessagingService() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        addTrackToPreferences(remoteMessage.data)
        Log.d(TAG, "Message received: ${remoteMessage.data}")
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        scope.launch {
            try {
                PushManager.registerDevice(this@PushNotificationService, token)
            } catch (e: Exception) {
                Log.e(TAG, "Error registering token in Grupo Fantome API")
                return@launch
            }
        }
    }

    private fun addTrackToPreferences(data: Map<String, String>) {
        val track = TrackManager.buildTrackFromNotification(Gson().toJson(data))
        TrackManager.storeTrackInSharedPreferences(track, this)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}