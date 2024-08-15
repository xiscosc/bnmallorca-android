package com.apploading.bnmallorca.ui.navigation

import com.apploading.bnmallorca.ui.screens.HomeScreen
import com.apploading.bnmallorca.ui.screens.ProfileScreen
import com.apploading.bnmallorca.ui.screens.SettingsScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.media3.session.MediaController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.apploading.bnmallorca.ui.screens.ServicesScreen
import com.google.common.util.concurrent.ListenableFuture

@Composable
fun NavigationGraph(navController: NavHostController, mediaController: ListenableFuture<MediaController>,
                    modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = BottomNavItem.Home.route) {
        composable(BottomNavItem.Home.route) {
            HomeScreen( mediaController)
        }
        composable(BottomNavItem.Schedule.route) {
            ProfileScreen()
        }
        composable(BottomNavItem.Contact.route) {
            SettingsScreen()
        }
        composable(BottomNavItem.Services.route) {
            ServicesScreen()
        }
    }
}