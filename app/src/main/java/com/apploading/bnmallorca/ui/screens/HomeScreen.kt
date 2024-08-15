package com.apploading.bnmallorca.ui.screens

import TrackListScreen
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.session.MediaController
import com.apploading.bnmallorca.R
import com.apploading.bnmallorca.ui.components.AlbumArt
import com.apploading.bnmallorca.ui.components.PlayPauseWithListIcon
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.guava.await

@Composable
fun HomeScreen(mediaControllerFuture: ListenableFuture<MediaController>) {

    var mediaController by remember { mutableStateOf<MediaController?>(null) }
    var showPlatList by remember { mutableStateOf(false) }

    LaunchedEffect(mediaControllerFuture) {
        mediaController = mediaControllerFuture.await()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // BN Logo at the Top
            Image(
                painter = painterResource(id = R.drawable.bn_logo),
                contentDescription = "BN Logo",
                modifier = Modifier
                    .width(900.dp)
                    .height(75.dp)
                    .padding(top = 15.dp, bottom = 10.dp)
            )


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Make this Column take up the remaining space
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center // Center content vertically
            ) {
                if (showPlatList) {
                    TrackListScreen()
                } else {
                    AlbumArt()
                }
            }



            PlayPauseWithListIcon(
                onPlayPauseClick = {
                mediaController?.let { controller ->
                    if (controller.isPlaying) controller.pause() else controller.play()
                }
            }, onListClick = { showPlatList = !showPlatList })
        }
    }
}