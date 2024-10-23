package com.apploading.bnmallorca.ui.helpers

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

@Composable
fun screenSize(baseSize: Dp): Dp {
    val context = LocalContext.current
    val configuration = context.resources.configuration
    val fontScale = configuration.fontScale
    val densityDpi = configuration.densityDpi

    // Adjust the base size according to the font scale or density
    val scaledSize = when {
        fontScale > 1.3f -> baseSize * 0.8f
        densityDpi < 365 -> baseSize * 0.65f
        densityDpi < 480 -> baseSize * 0.8f
        else -> baseSize
    }

    return scaledSize
}

@Composable
fun textSize(baseSize: TextUnit): TextUnit {
    val context = LocalContext.current
    val configuration = context.resources.configuration
    val fontScale = configuration.fontScale
    val densityDpi = configuration.densityDpi

    // Adjust the base size according to the font scale or density
    val scaledSize = when {
        fontScale > 1.3f -> baseSize * 0.8f // Reduce size if font scale is large
        densityDpi > 480 -> baseSize * 0.8f // Reduce size if density is very high
        else -> baseSize
    }

    return scaledSize
}