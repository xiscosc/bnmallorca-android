package com.apploading.bnmallorca.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.apploading.bnmallorca.R
import com.apploading.bnmallorca.ui.helpers.screenSize
import com.apploading.bnmallorca.ui.helpers.textSize
import com.apploading.bnmallorca.views.PlayingViewModel
import com.apploading.bnmallorca.views.TrackViewModel
import com.valentinilk.shimmer.shimmer

@Composable
fun AlbumArt(
    trackViewModel: TrackViewModel = hiltViewModel(),
    playingViewModel: PlayingViewModel = hiltViewModel()
) {
    val trackInfo by trackViewModel.trackInfoFlow.collectAsState()
    val playingStatus by playingViewModel.playingStatusFlow.collectAsState()

    val albumArtSize = screenSize(318.dp, 273.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 5.dp)
    ) {
        // Album Art Placeholder with rounded corners and a placeholder while loading
        Box(
            contentAlignment = Alignment.Center, // Center the spinner on top of the image
            modifier = Modifier
                .size(albumArtSize) // Make the Box the same size as the Image
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(
                            if (playingStatus) {
                                trackInfo.albumArtUrl.ifEmpty { R.drawable.new_album_placeholder_600 }
                            } else {
                                R.drawable.new_album_placeholder_600
                            }
                        )
                        .placeholder(R.drawable.new_album_placeholder_600) // Show this while loading
                        .error(R.drawable.new_album_placeholder_600) // Show this if there's an error
                        .crossfade(true)
                        .build(),
                    contentScale = ContentScale.Crop
                ),
                contentDescription = "Album Art Placeholder",
                modifier = Modifier
                    .matchParentSize() // Ensure the Image fills the Box
                    .clip(RoundedCornerShape(10.dp)) // Apply rounded corners
            )

            if (trackInfo.isLoading) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                )
                CircularProgressIndicator(
                    color = Color.Red,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .zIndex(1f)
                )
            }
        }

        Column(
            modifier = Modifier
                .width(albumArtSize) // Match the width of the image
                .padding(top = 10.dp)
                .height(75.dp)
        ) {

            if (trackInfo.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .shimmer()
                        .background(Color.LightGray),
                )
                Box(
                    modifier = Modifier
                        .size(145.dp, 24.dp)
                        .padding(top = 4.dp)
                        .shimmer()
                        .background(Color.LightGray),
                )
            } else {
                Text(
                    text = if (playingStatus) trackInfo.name else "",
                    fontSize = textSize(22.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start),
                    maxLines = 1, // Limit to one line
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (playingStatus) trackInfo.artist else "",
                    fontSize = textSize(18.sp),
                    color = Color.White,
                    maxLines = 1, // Limit to one line
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 4.dp)
                )

            }


        }
    }
}