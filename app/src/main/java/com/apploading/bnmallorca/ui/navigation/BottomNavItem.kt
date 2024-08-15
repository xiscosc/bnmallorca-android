package com.apploading.bnmallorca.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.StackedLineChart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val title: String, val icon: ImageVector, val route: String) {
    object Home : BottomNavItem("Radio", Icons.Filled.Radio, "radio")
    object Schedule : BottomNavItem("Programación", Icons.AutoMirrored.Filled.Sort, "schedule")
    object Contact : BottomNavItem("Contacto", Icons.Filled.AccountCircle, "contact")
    object Services : BottomNavItem("Servicios", Icons.Filled.StackedLineChart, "services")
}