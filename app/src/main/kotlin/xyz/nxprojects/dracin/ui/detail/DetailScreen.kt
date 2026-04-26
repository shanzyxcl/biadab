// DetailScreen.kt - FULL
package xyz.nxprojects.dracin.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.serialization.json.Json
import xyz.nxprojects.dracin.data.model.Category
import xyz.nxprojects.dracin.data.model.VideoInfo
import xyz.nxprojects.dracin.ui.components.ErrorDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    bookId: String,
    onVideoClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    var showErrorDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(bookId) {
        try {
            viewModel.loadDetail(bookId)
        } catch (e: Exception) {
            showErrorDialog = true
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    // Show error dialog jika ada error
    if (showErrorDialog || uiState.error != null) {
        ErrorDialog(
            title = "Error Load Detail",
            message = uiState.error ?: "Terjadi kesalahan saat memuat detail drama",
            stackTrace = uiState.errorStackTrace,
            onDismiss = { 
                showErrorDialog = false
                onBackClick()
            },
            onRetry = {
                showErrorDialog = false
                viewModel.loadDetail(bookId)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.videoData?.seriesTitle ?: "Detail Drama",
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
    contentDescription = "Back",
    tint = Color.White
)
                    }
                },
                actions = {
                    if (uiState.videoData != null) {
                        IconButton(onClick = { /* Handle share */ }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF09090B),
                    scrolledContainerColor = Color(0xFF09090B)
                )
            )
        },
        containerColor = Color(0xFF09090B)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF09090B))
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFF43F5E)
                )
            } else if (uiState.videoData != null) {
                val videoData = uiState.videoData!!
                val categories = remember {
                    try {
                        Json.decodeFromString<List<Category>>(videoData.categorySchema)
                    } catch (e: Exception) {
                        emptyList()
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Hero Image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(Color.Black)
                    ) {
                        AsyncImage(
                            model = videoData.seriesCover,
                            contentDescription = videoData.seriesTitle,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF18181B)),
                            contentScale = ContentScale.Crop,
                            alpha = 0.6f
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f))
                                    )
                                )
                        )

                        // Play Button
                        if (videoData.videoList.isNotEmpty()) {
                            Button(
                                onClick = { 
                                    try {
                                        onVideoClick(videoData.videoList[0].vid)
                                    } catch (e: Exception) {
                                        showErrorDialog = true
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(56.dp),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF43F5E)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Title & Info
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF27272A)
                            ) {
                                Text(
                                    text = "${videoData.episodeCount} Episodes",
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }

                            categories.take(3).forEach { category ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF27272A)
                                ) {
                                    Text(
                                        text = category.name,
                                        modifier = Modifier.padding(8.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFA1A1AA)
                                    )
                                }
                            }
                        }
                    }

                    // Sinopsis
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Sinopsis",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )

                        Text(
                            text = videoData.seriesIntro.ifEmpty { "Tidak ada sinopsis tersedia" },
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF999999)
                        )
                    }

                    // Episodes
                    if (videoData.videoList.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "Daftar Episode",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                videoData.videoList.chunked(4).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowItems.forEach { episode ->
                                            Box(modifier = Modifier.weight(1f)) {
                                                EpisodeCard(
                                                    episode = episode,
                                                    onEpisodeClick = { 
                                                        try {
                                                            onVideoClick(episode.vid)
                                                        } catch (e: Exception) {
                                                            showErrorDialog = true
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                        // Fill remaining space jika kurang dari 4
                                        repeat(4 - rowItems.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun EpisodeCard(
    episode: VideoInfo,
    onEpisodeClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF18181B))
            .clickable { onEpisodeClick() }
    ) {
        AsyncImage(
            model = episode.cover,
            contentDescription = "Episode ${episode.vidIndex}",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play",
            modifier = Modifier
                .align(Alignment.Center)
                .size(32.dp),
            tint = Color.White
        )

        Text(
            text = "EP ${episode.vidIndex}",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}
