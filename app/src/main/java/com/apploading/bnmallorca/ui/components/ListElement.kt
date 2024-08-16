package com.apploading.bnmallorca.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.apploading.bnmallorca.R

@Composable
fun ListElement(
    albumArtUrl: String?,
    content: @Composable ColumnScope.() -> Unit, // Content to be placed inside the Column
    onIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null, // Composable lambda for the icon
    imageModifier: Modifier = Modifier, // Modifier for the image size and styling
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF1E1E1E)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(albumArtUrl?.ifEmpty { R.drawable.album_placeholder })
                    .placeholder(R.drawable.album_placeholder) // Show this while loading
                    .error(R.drawable.album_placeholder) // Show this if there's an error
                    .crossfade(true)
                    .build(),
                contentScale = ContentScale.Crop
            ),
            contentDescription = "Album Art Placeholder",
            modifier = imageModifier
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp)) // Apply rounded corners here
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(2f).padding(vertical = 2.dp)) {
            content() // Display custom content passed to the composable
        }

        icon?.let {
            IconButton(onClick = { onIconClick() }, modifier = Modifier.size(60.dp)) {
                it() // Invoke the composable lambda to display the icon
            }
        }

        Spacer(modifier = Modifier.width(18.dp))
    }
}