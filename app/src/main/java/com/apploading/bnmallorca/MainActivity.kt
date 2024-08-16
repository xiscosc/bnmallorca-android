package com.apploading.bnmallorca

import com.apploading.bnmallorca.ui.navigation.BottomNavigationBar
import com.apploading.bnmallorca.ui.navigation.NavigationGraph
import android.content.ComponentName
import android.graphics.Color as ColorInt
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.compose.material3.Surface
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.apploading.bnmallorca.bncore.RemoteSettingsManager
import com.apploading.bnmallorca.bncore.TrackManager
import com.apploading.bnmallorca.service.MediaPlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var mediaController: ListenableFuture<MediaController>

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.light(
                ColorInt.TRANSPARENT,
                ColorInt.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)
        RemoteSettingsManager.setupSettings()

        val sessionToken = SessionToken(this, ComponentName(this, MediaPlaybackService::class.java))
        mediaController = MediaController.Builder(this, sessionToken).buildAsync()

        setContent {
            AppTheme {
                Surface(color = Color.Black) {
                    MainScreen(mediaController)
                }
            }
        }

        // Update track for the first time
        lifecycleScope.launch {
            TrackManager.updateLastTrackFromApi(this@MainActivity)
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