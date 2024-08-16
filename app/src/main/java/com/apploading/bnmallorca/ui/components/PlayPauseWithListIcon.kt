package com.apploading.bnmallorca.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apploading.bnmallorca.R
import com.apploading.bnmallorca.views.TrackViewModel

@Composable
fun PlayPauseWithListIcon(
    showPlayList: Boolean,
    onPlayPauseClick: () -> Unit,
    onListClick: () -> Unit,
    trackViewModel: TrackViewModel = hiltViewModel()
) {
    val trackInfo by trackViewModel.trackInfoFlow.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        val icon = if (trackInfo.playing) R.drawable.pause_button else R.drawable.play_button
        Image(
            painter = painterResource(id = icon),
            contentDescription = "Play/Pause Button",
            modifier = Modifier
                .size(125.dp) // Size adjusted for prominence
                .clickable {
                    onPlayPauseClick()
                }
        )

        // List Icon
        Icon(
            imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
            contentDescription = "Playlist",
            tint = if (showPlayList) Color.DarkGray else Color.White,
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.CenterStart)
                .offset(x = (300).dp) // Space between the list icon and the play/pause button
             .clickable { onListClick() }
        )
    }

}