package com.apploading.bnmallorca.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.apploading.bnmallorca.R
import com.apploading.bnmallorca.bncore.Day
import com.apploading.bnmallorca.bncore.Show
import com.apploading.bnmallorca.bncore.TrackManager
import com.apploading.bnmallorca.ui.components.ListElement
import com.apploading.bnmallorca.ui.components.Loading
import com.apploading.bnmallorca.views.ScheduleViewModel

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = hiltViewModel()) {
    val schedule by viewModel.schedule.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSchedule()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {

        Text(
            text = "Programación",
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.W700,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            Loading()
        } else if (errorMessage != null) {
            Text(text = "No se ha podido cargar la programación, inténtelo más tarde.", color = Color.White)
        } else {
            LazyColumn {
                items(schedule) { day ->
                    DaySchedule(day)
                }
            }
        }
    }
}

@Composable
fun DaySchedule(day: Day) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = when (day.numberOfTheWeek.toInt()) {
                1 -> "Lunes"
                2 -> "Martes"
                3 -> "Miércoles"
                4 -> "Jueves"
                5 -> "Viernes"
                6 -> "Sábado"
                7 -> "Domingo"
                else -> "Día Desconocido"
            },
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.W400,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        day.shows.forEach { show ->
            ShowItem(show)
        }
    }
}

@Composable
fun ShowItem(show: Show) {
    val context = LocalContext.current
    ListElement(albumArtUrl = TrackManager.getAlbumArtUrl(show.albumArt), content = {
        Text(
            text = show.name,
            color = Color.White,
            fontSize = 16.sp,
            maxLines = 1, // Limit to one line
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "${show.time} • ${show.artist}",
            color = Color.Gray,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = if (show.online) "Online" else "Offline",
            color = if (show.online) Color.Green else Color.Red,
            fontSize = 12.sp
        )
    }, icon = {
        Image(
            painter = painterResource(id = R.drawable.play_button),
            contentDescription = "Play/Pause Button",
            modifier = Modifier
                .size(50.dp)
                .padding(8.dp)
        )
    }, onIconClick = {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(show.podcastLink)
        }
        context.startActivity(intent)
    }, imageModifier = Modifier.size(86.dp))
}