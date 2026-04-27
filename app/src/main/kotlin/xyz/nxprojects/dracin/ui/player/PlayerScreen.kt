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
    
    // Load initial video list (you might want to load multiple videos)
    LaunchedEffect(videoId) {
        viewModel.loadVideo(videoId)
    }

    // Create a list of videos (for now just one, but you can expand this)
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
    
    // Store ExoPlayers for each video
    val exoPlayers = remember {
        mutableStateMapOf<Int, ExoPlayer>()
    }
    
    // Track current playing state
    var isPlaying by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(false) }

    // Auto-hide controls
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(3000)
            showControls = false
        }
    }

    // Handle page changes - play current, pause others
    LaunchedEffect(pagerState.currentPage) {
        exoPlayers.forEach { (index, player) ->
            if (index == pagerState.currentPage) {
                player.playWhenReady = isPlaying
            } else {
                player.playWhenReady = false
            }
        }
    }

    // Cleanup players when component is disposed
    DisposableEffect(Unit) {
        onDispose {
            exoPlayers.values.forEach { it.release() }
            exoPlayers.clear()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondBoundsPageCount = 1 // Preload 1 page (+ gunakan versi Accompanist terbaru)
        ) { page ->
            VideoPlayerPage(
                videoUrl = videos[page],
                isCurrentPage = page == pagerState.currentPage,
                isPlaying = isPlaying,
                context = context,
                onPlayerReady = { player ->
                    exoPlayers[page] = player
                },
                onTap = {
                    showControls = !showControls
                }
            )
        }

        // Top App Bar (always visible)
        PlayerTopAppBar(
            title = "Video ${pagerState.currentPage + 1}/${videos.size}",
            episode = "",
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Play/Pause button overlay (shows on tap)
        if (showControls) {
            IconButton(
                onClick = {
                    isPlaying = !isPlaying
                    exoPlayers[pagerState.currentPage]?.playWhenReady = isPlaying
                    showControls = true
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

        // Video progress indicator (optional)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(videos.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(if (index == pagerState.currentPage) 32.dp else 8.dp)
                        .height(4.dp)
                        .background(
                            color = if (index == pagerState.currentPage) 
                                Color(0xFFF43F5E) 
                            else 
                                Color.White.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.small
                        )
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
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var isPlayerReady by remember { mutableStateOf(false) }

    // Initialize player
    LaunchedEffect(videoUrl) {
        if (exoPlayer == null) {
            val player = ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(videoUrl)
                setMediaItem(mediaItem)
                prepare()
                repeatMode = Player.REPEAT_MODE_ONE // Loop video
                
                // Add listener for player state
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            isPlayerReady = true
                        }
                    }
                })
            }
            exoPlayer = player
            onPlayerReady(player)
        }
    }

    // Control playback based on page visibility
    LaunchedEffect(isCurrentPage, isPlaying) {
        exoPlayer?.playWhenReady = isCurrentPage && isPlaying
    }

    // Cleanup when page is no longer visible
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer?.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() }
                )
            }
    ) {
        if (exoPlayer != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false // Hide default controls
                        controllerAutoShow = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Loading indicator
        if (!isPlayerReady) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFFF43F5E)
            )
        }
    }
}
