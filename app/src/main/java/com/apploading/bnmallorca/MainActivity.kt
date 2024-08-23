package com.apploading.bnmallorca

import com.apploading.bnmallorca.ui.navigation.BottomNavigationBar
import com.apploading.bnmallorca.ui.navigation.NavigationGraph
import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.compose.rememberNavController
import com.apploading.bnmallorca.bncore.PlayManager
import com.apploading.bnmallorca.bncore.PushManager
import com.apploading.bnmallorca.bncore.RemoteSettingsManager
import com.apploading.bnmallorca.bncore.TrackManager
import com.apploading.bnmallorca.service.MediaPlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var trackManager: TrackManager

    @Inject
    lateinit var pushManager: PushManager

    @Inject
    lateinit var playManager: PlayManager

    private lateinit var mediaController: ListenableFuture<MediaController>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RemoteSettingsManager.setupSettings()

        val sessionToken = SessionToken(this, ComponentName(this, MediaPlaybackService::class.java))
        mediaController = MediaController.Builder(this, sessionToken).buildAsync()
        lifecycleScope.launch {
            playManager.storePlayingStatus(mediaController.await().isPlaying)
        }

        setContent {
            AppTheme {
                Surface(color = Color.Black) {
                    MainScreen(mediaController)
                }
            }
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.d("LEGACY_PUSH", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("LEGACY_PUSH", "FCM registration token: $token")
            if (!pushManager.pushInfoIsStored()) {
                Log.d("LEGACY_PUSH", "FCM registration was not stored")
                lifecycleScope.launch {
                    pushManager.registerDevice(token)
                }
                return@addOnCompleteListener
            }

            Log.d("LEGACY_PUSH", "FCM registration already stored")
        }

        // Update track for the first time
        lifecycleScope.launch {
            trackManager.updateLastTrackFromApi()
        }
    }
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            background = Color.Black
        ),
        content = content
    )
}

@Composable
fun MainScreen(mediaControllerFuture: ListenableFuture<MediaController>) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        modifier = Modifier.background(Color.Black)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            NavigationGraph(navController, mediaControllerFuture)
        }
    }
}