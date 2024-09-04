package com.apploading.bnmallorca.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.apploading.bnmallorca.R
import com.apploading.bnmallorca.bncore.RemoteSettingsManager

@Composable
fun LegacyHeader() {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(7.dp),
        horizontalArrangement = Arrangement.SpaceBetween, // Align elements with space between
        verticalAlignment = Alignment.CenterVertically // Align elements vertically centered
    ) {
        // BN Logo at the Top
        Image(
            painter = painterResource(id = R.drawable.bn_logo_fm_2_g),
            contentDescription = "BN Logo",
            modifier = Modifier.weight(1f) // Allocate space for the logo
        )

        Spacer(modifier = Modifier.width(40.dp)) // Add some space between the logo and the icons

        // Social Media Icons
        Row(
            verticalAlignment = Alignment.CenterVertically // Align elements vertically centered
        ) {
            // Instagram Icon
            Image(
                painter = painterResource(id = R.drawable.instagram_glyph_white_1),
                contentDescription = "Instagram",
                modifier = Modifier.size(30.dp) // Set the size of the icon
                    .align(Alignment.CenterVertically)
                    .clickable {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(RemoteSettingsManager.getSettings().instagramWebUrl)
                        )
                        context.startActivity(intent)
                    }
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Facebook Icon
            Image(
                painter = painterResource(id = R.drawable.facebook_logo_secondary_2),
                contentDescription = "Facebook",
                modifier = Modifier.size(30.dp) // Set the size of the icon
                    .align(Alignment.CenterVertically)
                    .clickable {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(RemoteSettingsManager.getSettings().facebookWebUrl)
                        )
                        context.startActivity(intent)
                    }
            )
        }
    }
}