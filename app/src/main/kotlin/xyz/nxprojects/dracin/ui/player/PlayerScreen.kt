package xyz.nxprojects.dracin.ui.player

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import xyz.nxprojects.dracin.data.model.VideoData
import xyz.nxprojects.dracin.ui.components.PlayerTopAppBar

@Composable
fun PlayerScreen(
    videoId: String,
    bookId: String?,
    onBackClick: () -> Unit,
    onNextEpisode: (String) -> Unit,
    context: Context,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    LaunchedEffect(videoId) {
        viewModel.loadStream(videoId, bookId)
    }

    val uiState by viewModel.uiState.collectAsState()
    var isPlaying by remember { mutableStateOf(false) }
    var exoPlayer: ExoPlayer? by remember { mutableStateOf(null) }

    // Initialize ExoPlayer
    LaunchedEffect(context) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
        }
    }

    // Set video URL
    LaunchedEffect(uiState.streamData) {
        if (exoPlayer != null && uiState.streamData != null) {
            val url = uiState.streamData!!.mainUrl.ifEmpty { uiState.streamData!!.backupUrl }
            if (url.isNotEmpty()) {
                val mediaItem = MediaItem.fromUri(url)
                exoPlayer!!.setMediaItem(mediaItem)
                exoPlayer!!.prepare()
                exoPlayer!!.play()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer?.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top App Bar
        PlayerTopAppBar(
            title = uiState.videoData?.seriesTitle ?: "Player",
            episode = if (uiState.videoData != null) {
                val currentIndex = uiState.videoData!!.videoList.indexOfFirst { it.vid == videoId }
                if (currentIndex >= 0) "EP ${currentIndex + 1}" else ""
            } else "",
            onBackClick = onBackClick
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFF43F5E)
                )
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFF43F5E)
                    )
                    Text(
                        text = uiState.error ?: "Error loading video",
                        color = Color.White,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Button(
                        onClick = { viewModel.loadStream(videoId, bookId) },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Retry")
                    }
                }
            } else if (exoPlayer != null) {
                // Video Player
                AndroidView(
                    factory = {
                        PlayerView(context).apply {
                            player = exoPlayer
                            useController = true
                            controllerShowTimeoutMs = 5000
                            controllerHideTimeoutMs = 5000
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                )

                // Bottom Bar with Episode Navigation
                if (uiState.videoData != null) {
                    BottomPlayerBar(
                        videoData = uiState.videoData!!,
                        currentVideoId = videoId,
                        isPlaying = isPlaying,
                        onPlayPause = { 
                            isPlaying = !isPlaying
                            if (isPlaying) exoPlayer?.play() else exoPlayer?.pause() 
                        },
                        onNextEpisode = onNextEpisode,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomPlayerBar(
    videoData: xyz.nxprojects.dracin.data.model.VideoData,
    currentVideoId: String,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNextEpisode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentIndex = videoData.videoList.indexOfFirst { it.vid == currentVideoId }
    val hasPrev = currentIndex > 0
    val hasNext = currentIndex >= 0 && currentIndex < videoData.videoList.size - 1

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF09090B))
            .padding(16.dp)
    ) {
        // Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (hasPrev) onNextEpisode(videoData.videoList[currentIndex - 1].vid)
                },
                enabled = hasPrev
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = if (hasPrev) Color.White else Color.Gray
                )
            }

            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFF43F5E), shape = RoundedCornerShape(50))
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(
                onClick = {
                    if (hasNext) onNextEpisode(videoData.videoList[currentIndex + 1].vid)
                },
                enabled = hasNext
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = if (hasNext) Color.White else Color.Gray
                )
            }
        }

        // Next Episode Preview
        if (hasNext && currentIndex >= 0) {
            val nextEpisode = videoData.videoList[currentIndex + 1]
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNextEpisode(nextEpisode.vid) }
                    .clip(RoundedCornerShape(8.dp)),
                color = Color(0xFF18181B)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = nextEpisode.cover,
                        contentDescription = "Next Episode",
                        modifier = Modifier
                            .size(64.dp, 48.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Up Next",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFF43F5E)
                        )
                        Text(
                            text = "Episode ${nextEpisode.vidIndex}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}