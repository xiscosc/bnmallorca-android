package com.apploading.bnmallorca

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.compose.rememberNavController
import com.apploading.bnmallorca.bncore.PlayManager
import com.apploading.bnmallorca.bncore.PushManager
import com.apploading.bnmallorca.bncore.RemoteSettingsManager
import com.apploading.bnmallorca.bncore.TrackManager
import com.apploading.bnmallorca.service.MediaPlaybackService
import com.apploading.bnmallorca.ui.navigation.BottomNavigationBar
import com.apploading.bnmallorca.ui.navigation.NavigationGraph
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
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

    @Inject
    lateinit var remoteSettingsManager: RemoteSettingsManager

    private lateinit var appUpdateManager: AppUpdateManager
    private val updateType = AppUpdateType.IMMEDIATE

    private lateinit var mediaControllerFuture: ListenableFuture<MediaController>

    // Register the activity result launcher for handling the update flow
    private val updateRequestLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode != RESULT_OK) {
                Log.d("InAppUpdate", "Update flow failed or was canceled by the user.")
            } else {
                Log.d("InAppUpdate", "Update flow completed successfully.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appUpdateManager = AppUpdateManagerFactory.create(this)

        // Check for updates on app launch
        checkForAppUpdate()
        remoteSettingsManager.setupSettings()
        val sessionToken = SessionToken(this, ComponentName(this, MediaPlaybackService::class.java))
        mediaControllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        mediaControllerFuture.addListener({
            playManager.storePlayingStatus(mediaControllerFuture.get().isPlaying)
        }, MoreExecutors.directExecutor())

        setContent {
            AppTheme {
                Surface(color = Color.Black) {
                    MainScreen(mediaControllerFuture, true)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                ErrorNotifier.errorFlow.collect { _ ->
                    showErrorMessage()
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

    override fun onResume() {
        super.onResume()
        // Check if the update was in progress and resume it if necessary
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // Resume the update
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    updateRequestLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                )
            }
        }
    }

    private fun checkForAppUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(updateType)
            ) {

                // Start the update using the new method
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    updateRequestLauncher,
                    AppUpdateOptions.newBuilder(updateType).build()
                )
            }
        }
    }

    override fun onDestroy() {
        MediaController.releaseFuture(mediaControllerFuture)
        super.onDestroy()
    }

    private fun showErrorMessage() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Problema al iniciar la applicación")
        builder.setMessage("La aplicación no pudo iniciarse correctamente. Si el problema persiste, reinstale la aplicación.")
        builder.setPositiveButton("Cerrar") { dialog, _ ->
            dialog.dismiss()
            finishAffinity()
        }
        builder.show()
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
fun MainScreen(
    mediaControllerFuture: ListenableFuture<MediaController>,
    showBottomBar: Boolean = true
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        },
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