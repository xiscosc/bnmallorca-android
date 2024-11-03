package com.apploading.bnmallorca.ui.screens

import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.apploading.bnmallorca.views.RemoteSettingsViewModel

@Composable
fun ServicesScreen(remoteSettingsViewModel: RemoteSettingsViewModel = hiltViewModel()) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(top= 16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        // Contact Header
        Text(
            text = "Servicios",
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.W700,
            modifier = Modifier.padding(bottom = 16.dp).padding(horizontal = 16.dp)
        )

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    // Enable zoom controls and scaling
                    // Enable JavaScript
                    settings.javaScriptEnabled = true

                    // Ensure the WebView scales content to fit the screen
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true

                    // Adjust layout to fit the content within the screen
                    settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING

                    // Force the initial scale to 100% to fit the screen width
                    setInitialScale(1)

                    // Set the WebView client
                    webViewClient = WebViewClient()
                    webViewClient = WebViewClient()
                    loadUrl(remoteSettingsViewModel.remoteSettingsManager.getSettings().servicesUrl)
                }
            }
        )
    }
}