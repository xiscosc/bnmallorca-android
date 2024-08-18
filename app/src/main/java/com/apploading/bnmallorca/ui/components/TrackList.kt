import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.apploading.bnmallorca.R
import com.apploading.bnmallorca.bncore.RemoteSettingsManager
import com.apploading.bnmallorca.bncore.Track
import com.apploading.bnmallorca.bncore.TrackManager
import com.apploading.bnmallorca.ui.components.ListElement
import com.apploading.bnmallorca.ui.components.Loading
import com.apploading.bnmallorca.views.TrackListViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.DurationUnit


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TrackListScreen(onBannerClick: () -> Unit, viewModel: TrackListViewModel = hiltViewModel()) {
    val trackList by viewModel.trackList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isPolling by viewModel.isPolling.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }

    // Set up the PullRefresh state
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.resetLastTrack()
            viewModel.loadTracks()
            isRefreshing = false
        }
    )

    // Track the scroll position
    val listState = rememberLazyListState()

    LaunchedEffect(true) {
        viewModel.resetLastTrack()
        viewModel.loadTracks()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetTracks()
        }
    }

    // Detect when the user reaches the end of the list
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { index ->
                if (index == trackList.size - 1 && !isLoading) {
                    viewModel.pollTracks()
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .pullRefresh(pullRefreshState)
    ) {

        Column(modifier = Modifier.fillMaxSize()) {
            if (isLoading && !isRefreshing) {
                Loading()
            } else if (errorMessage != null) {
                Text(text = "Error: $errorMessage")
            } else {
                TrackList(tracks = trackList, listState, onBannerClick)
            }
        }
        // Add the PullRefreshIndicator as the last child to overlay on top
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        GradientOverlay(
            modifier = Modifier.align(Alignment.BottomCenter), // Align the gradient at the bottom
            isPolling = isPolling
        )

        if (isPolling) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 30.dp)
            )
        }
    }
}

@Composable
fun TrackList(
    tracks: List<Track>,
    listState: LazyListState,
    onBannerClick: () -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (tracks.isNotEmpty()) {
            item {
                Banner(onBannerClick = onBannerClick)
            }
        }
        items(tracks.size) { index ->
            TrackItem(track = tracks[index])
        }
    }
}

@Composable
fun Banner(onBannerClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onBannerClick() // Navigate to the "services" screen
            }
            .padding(4.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF1E1E1E)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.album_placeholder)
                    .crossfade(true)
                    .build(),
                contentScale = ContentScale.Crop
            ),
            contentDescription = "Album Art Placeholder",
            modifier = Modifier
                .padding(8.dp)
                .size(56.dp) // Adjusted to look proportional
                .clip(RoundedCornerShape(8.dp)) // Apply rounded corners here
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "BANNER SERVICIOS",
                color = Color.White,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "BANNER SERVICIOS",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun TrackItem(track: Track) {
    val context = LocalContext.current
    ListElement(albumArtUrl = TrackManager.getAlbumArtUrl(track, false), content = {
        Text(
            text = track.name,
            color = Color.White,
            fontSize = 16.sp,
            maxLines = 1, // Limit to one line
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${track.artist} · ${getAgoString(track)}",
            color = Color.Gray,
            fontSize = 12.sp,
            maxLines = 1, // Limit to one line
            overflow = TextOverflow.Ellipsis
        )
    }, icon = {
        Icon(
            imageVector = Icons.Filled.IosShare,
            contentDescription = "Share",
            tint = Color.White,
            modifier = Modifier.size(30.dp)
        )
    }, onIconClick = { shareText(context, track) }, imageModifier = Modifier.size(72.dp))
}

@Composable
fun GradientOverlay(modifier: Modifier = Modifier, isPolling: Boolean) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isPolling) 300.dp else 30.dp) // Adjust height as needed
            .graphicsLayer { alpha = 0.99f } // To avoid a potential banding issue
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    )
}

fun getAgoString(track: Track): String {
    val minutes = TrackManager.getTimeAgo(track, DurationUnit.MINUTES).toInt()
    if (minutes < 1) {
        return "Ahora"
    }

    if (minutes < 60) {
        return "hace $minutes min" + if (minutes > 1) "s" else ""
    }

    val hours = TrackManager.getTimeAgo(track, DurationUnit.HOURS).toInt()
    if (hours < 24) {
        return "hace $hours hora" + if (hours > 1) "s" else ""
    }

    val days = TrackManager.getTimeAgo(track, DurationUnit.DAYS).toInt()
    if (days < 30) {
        return "hace $days día" + if (days > 1) "s" else ""
    }

    val instant = Instant.ofEpochMilli(track.timestamp.toLong() * 1000)
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}

fun shareText(context: Context, track: Track) {
    val message =
        "He escuchado ${track.name} de ${track.artist} en la app de BN Mallorca, bájatela aquí ${RemoteSettingsManager.getSettings().appDownloadUrl}"
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, message)
        type = "text/plain"
    }

    context.startActivity(Intent.createChooser(intent, null))
}