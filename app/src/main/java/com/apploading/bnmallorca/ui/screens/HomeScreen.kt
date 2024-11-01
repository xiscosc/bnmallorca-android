package com.apploading.bnmallorca.ui.screens

import TrackListScreen
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.session.MediaController
import androidx.navigation.NavController
import com.apploading.bnmallorca.R
import com.apploading.bnmallorca.ui.components.AlbumArt
import com.apploading.bnmallorca.ui.components.PlayPauseWithListIcon
import com.apploading.bnmallorca.ui.helpers.screenSize
import com.apploading.bnmallorca.ui.navigation.BottomNavItem
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

@Composable
fun HomeScreen(
    mediaControllerFuture: ListenableFuture<MediaController>,
    navController: NavController
) {

    var playListShown by remember { mutableStateOf(false) }

    BackHandler(enabled = playListShown) {
        playListShown = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .padding(horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {


            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "BN Logo",
                modifier = Modifier
                    .width(screenSize(318.dp, 273.dp))
            )


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Make this Column take up the remaining space
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center // Center content vertically
            ) {
                if (playListShown) {
                    TrackListScreen(onBannerClick = {
                        navController.navigate(BottomNavItem.Services.route)
                    })
                } else {
                    AlbumArt()
                }
            }

            PlayPauseWithListIcon(
                playListShown,
                onPlayPauseClick = {
                    mediaControllerFuture.addListener({
                        val controller = mediaControllerFuture.get()
                        if (controller.isPlaying) controller.stop() else controller.play()
                    }, MoreExecutors.directExecutor())
                }, onListClick = { playListShown = !playListShown }, showPlayList = true
            )
        }
    }
}