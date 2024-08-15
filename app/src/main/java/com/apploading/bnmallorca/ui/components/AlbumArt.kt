package com.apploading.bnmallorca.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.apploading.bnmallorca.R
import com.apploading.bnmallorca.views.TrackViewModel

@Composable
fun AlbumArt(trackViewModel: TrackViewModel = hiltViewModel()) {
    val trackInfo by trackViewModel.trackInfoFlow.collectAsState()

    // Album Art Placeholder and Track Info in the Middle
    val albumArtSize = 350.dp
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 5.dp)
    ) {
        // Album Art Placeholder with rounded corners and a placeholder while loading
        Image(
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(trackInfo.albumArtUrl.ifEmpty { R.drawable.album_placeholder })
                    .placeholder(R.drawable.album_placeholder) // Show this while loading
                    .error(R.drawable.album_placeholder) // Show this if there's an error
                    .crossfade(true)
                    .build(),
                contentScale = ContentScale.Crop
            ),
            contentDescription = "Album Art Placeholder",
            modifier = Modifier
                .size(albumArtSize) // Adjusted to look proportional
                .clip(RoundedCornerShape(10.dp)) // Apply rounded corners here
        )

        // Track Name and Artist Name - Aligned with Album Art
        Column(
            modifier = Modifier
                .width(albumArtSize) // Match the width of the image
                .padding(top = 10.dp)
        ) {
            Text(
                text = trackInfo.name, // Observed track name
                fontSize = 22.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                text = trackInfo.artist, // Observed artist name
                fontSize = 18.sp,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 4.dp)
            )
        }
    }

}