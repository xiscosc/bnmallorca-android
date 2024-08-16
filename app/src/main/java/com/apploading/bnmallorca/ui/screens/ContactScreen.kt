package com.apploading.bnmallorca.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apploading.bnmallorca.R
import com.apploading.bnmallorca.bncore.RemoteSettingsManager

@Composable
fun ContactScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        // Contact Header
        Text(
            text = "Contacto",
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.W700,
            modifier = Modifier.padding(bottom = 65.dp)
        )

        // Phone Row
        ContactItem(
            icon = Icons.Default.Phone,
            text = "+34 971 72 51 50",
            contentDescription = "Phone",
            onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+34971725150"))
                context.startActivity(intent)
            }
        )

        // Email Row
        ContactItem(
            icon = Icons.Default.Email,
            text = "info@bnmallorca.com",
            contentDescription = "Email",
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:info@bnmallorca.com")
                    putExtra(Intent.EXTRA_SUBJECT, "Inquiry")
                }
                context.startActivity(intent)
            }
        )

        // Address Row
        ContactItem(
            icon = Icons.Default.Map,
            text = "C/ Posada de Lluc 8\nLocal 3\n07001\nPalma\nBaleares",
            contentDescription = "Address",
            onClick = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q=C%2F+Posada+de+Lluc+8+%2C+07001%2C+Palma+de+Mallorca%2C+Baleares")
                )
                context.startActivity(intent)
            }
        )

        Spacer(
            modifier = Modifier
                .padding(top = 35.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Gray)
        )

        // Social Media Icons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            SocialMediaIcon(
                imageResource = R.drawable.instagram_glyph_white_1, // Your Instagram icon resource
                contentDescription = "Instagram",
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(RemoteSettingsManager.getSettings().instagramWebUrl)
                    )
                    context.startActivity(intent)
                }
            )
            Spacer(modifier = Modifier.width(48.dp))
            SocialMediaIcon(
                imageResource = R.drawable.facebook_logo_secondary_2, // Your Facebook icon resource
                contentDescription = "Facebook",
                onClick = {
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

@Composable
fun ContactItem(icon: ImageVector, text: String, contentDescription: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(bottom = 24.dp)
            .clickable(onClick = onClick) // Make the row clickable
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(24.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.W400
        )
    }
}

@Composable
fun SocialMediaIcon(imageResource: Int, contentDescription: String, onClick: () -> Unit) {
    Image(
        painter = painterResource(id = imageResource),
        contentDescription = contentDescription,
        modifier = Modifier
            .size(85.dp)
            .clip(CircleShape)
            .background(Color.Black)
            .padding(12.dp)
            .clickable(onClick = onClick), // Make the icon clickable
        contentScale = ContentScale.Crop
    )
}