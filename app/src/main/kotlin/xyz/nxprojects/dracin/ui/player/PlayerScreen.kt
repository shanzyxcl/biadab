// PlayerScreen.kt - FULL
package xyz.nxprojects.dracin.ui.player

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import xyz.nxprojects.dracin.ui.components.ErrorDialog
import xyz.nxprojects.dracin.ui.components.PlayerTopAppBar

@Composable
fun PlayerScreen(
    videoId: String,
    onBackClick: () -> Unit,
    context: Context,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    var showErrorDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(videoId) {
        try {
            viewModel.loadVideo(videoId)
        } catch (e: Exception) {
            showErrorDialog = true
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    var exoPlayer: ExoPlayer? by remember { mutableStateOf(null) }

    // Show error dialog jika ada error
    if (showErrorDialog || uiState.error != null) {
        ErrorDialog(
            title = "Error Load Video",
            message = uiState.error ?: "Terjadi kesalahan saat memuat video",
            stackTrace = uiState.errorStackTrace,
            onDismiss = { 
                showErrorDialog = false
                onBackClick()
            },
            onRetry = {
                showErrorDialog = false
                viewModel.loadVideo(videoId)
            }
        )
    }

    // Initialize ExoPlayer
    LaunchedEffect(context) {
        try {
            if (exoPlayer == null) {
                exoPlayer = ExoPlayer.Builder(context).build()
            }
        } catch (e: Exception) {
            showErrorDialog = true
        }
    }

    // Set video URL
    LaunchedEffect(uiState.videoUrl) {
        try {
            if (exoPlayer != null && uiState.videoUrl != null) {
                val mediaItem = MediaItem.fromUri(uiState.videoUrl!!)
                exoPlayer!!.setMediaItem(mediaItem)
                exoPlayer!!.prepare()
                exoPlayer!!.playWhenReady = true
            }
        } catch (e: Exception) {
            showErrorDialog = true
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
            title = "Video Player",
            episode = "",
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
            } else if (exoPlayer != null && uiState.videoUrl != null) {
                // Video Player
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = true
                            controllerAutoShow = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                )
            }
        }
    }
}