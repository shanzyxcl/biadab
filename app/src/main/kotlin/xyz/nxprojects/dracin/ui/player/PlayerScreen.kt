// PlayerScreen.kt - TikTok Style Vertical Scroll
package xyz.nxprojects.dracin.ui.player

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import xyz.nxprojects.dracin.ui.components.ErrorDialog
import xyz.nxprojects.dracin.ui.components.PlayerTopAppBar

@Composable
fun PlayerScreen(
    videoId: String,
    onBackClick: () -> Unit,
    context: Context = LocalContext.current,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(videoId) {
        viewModel.loadVideo(videoId)
    }

    val videoList = remember(uiState.videoUrl) {
        if (uiState.videoUrl != null) listOf(uiState.videoUrl!!) else emptyList()
    }

    if (uiState.error != null) {
        ErrorDialog(
            title = "Error Load Video",
            message = uiState.error ?: "Terjadi kesalahan saat memuat video",
            stackTrace = uiState.errorStackTrace,
            onDismiss = onBackClick,
            onRetry = { viewModel.loadVideo(videoId) }
        )
    }

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
        } else if (videoList.isNotEmpty()) {
            TikTokStyleVideoPlayer(
                videos = videoList,
                onBackClick = onBackClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TikTokStyleVideoPlayer(
    videos: List<String>,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { videos.size })
    val exoPlayers = remember { mutableStateMapOf<Int, ExoPlayer>() }
    var isPlaying by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(false) }

    val currentShowControls by rememberUpdatedState(newValue = showControls)

    // Auto-hide control logic
    LaunchedEffect(showControls) {
        if (currentShowControls) {
            delay(3000)
            showControls = false
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        exoPlayers.forEach { (index, player) ->
            player.playWhenReady = index == pagerState.currentPage && isPlaying
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayers.values.forEach { it.release() }
            exoPlayers.clear()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            VideoPlayerPage(
                videoUrl = videos[page],
                isCurrentPage = page == pagerState.currentPage,
                isPlaying = isPlaying,
                context = context,
                onPlayerReady = { player -> exoPlayers[page] = player },
                onTap = { showControls = !showControls }
            )
        }

        PlayerTopAppBar(
            title = "Video ${pagerState.currentPage + 1}/${videos.size}",
            episode = "",
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        if (showControls) {
            IconButton(
                onClick = {
                    isPlaying = !isPlaying
                    exoPlayers[pagerState.currentPage]?.playWhenReady = isPlaying
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.medium
                    )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Composable
fun VideoPlayerPage(
    videoUrl: String,
    isCurrentPage: Boolean,
    isPlaying: Boolean,
    context: Context,
    onPlayerReady: (ExoPlayer) -> Unit,
    onTap: () -> Unit
) {
    val exoPlayer = remember { ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Box(
        Modifier.pointerInput(Unit) { detectTapGestures { onTap.invoke() }}
    ) {
        AndroidView(factory = { ctx -> PlayerView(ctx).apply {
            player = exoPlayer
            useController = false
        }})
    }
}