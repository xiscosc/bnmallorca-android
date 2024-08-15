package com.apploading.bnmallorca.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun ProfileScreen() {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF03DAC5)) {
        Text(text = "Profile Screen", color = Color.White)
    }
}

@Composable
fun SettingsScreen() {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFFFC107)) {
        Text(text = "Settings Screen", color = Color.Black)
    }
}