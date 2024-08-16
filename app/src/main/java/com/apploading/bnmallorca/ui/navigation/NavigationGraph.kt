package com.apploading.bnmallorca.ui.navigation

import androidx.compose.foundation.background
import com.apploading.bnmallorca.ui.screens.HomeScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.media3.session.MediaController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.apploading.bnmallorca.ui.screens.ContactScreen
import com.apploading.bnmallorca.ui.screens.ScheduleScreen
import com.apploading.bnmallorca.ui.screens.ServicesScreen
import com.google.common.util.concurrent.ListenableFuture

@Composable
fun NavigationGraph(navController: NavHostController, mediaController: ListenableFuture<MediaController>,
                    modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = BottomNavItem.Home.route, modifier = modifier.background(
        Color.Black
    )) {
        composable(BottomNavItem.Home.route) {
            HomeScreen(mediaController, navController)
        }
        composable(BottomNavItem.Schedule.route) {
            ScheduleScreen()
        }
        composable(BottomNavItem.Contact.route) {
            ContactScreen()
        }
        composable(BottomNavItem.Services.route) {
            ServicesScreen()
        }
    }
}