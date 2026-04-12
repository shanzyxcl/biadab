package xyz.nxprojects.dracin.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
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
import xyz.nxprojects.dracin.ui.components.DetailTopAppBar

@Composable
fun DetailScreen(
    bookId: String,
    onVideoClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    LaunchedEffect(bookId) {
        viewModel.loadDetail(bookId)
    }

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF09090B))
    ) {
        // Top App Bar
        DetailTopAppBar(
            title = uiState.videoData?.seriesTitle ?: "Drama Details",
            onBackClick = onBackClick
        )

        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF09090B))
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
                    Text(
                        text = uiState.error ?: "Unknown error",
                        color = Color.White
                    )
                    Button(
                        onClick = { viewModel.loadDetail(bookId) },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Retry")
                    }
                }
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
                                    brush = androidx.compose.foundation.background(
                                        Color.Black.copy(alpha = 0.3f)
                                    ).brush
                                )
                        )

                        // Play Button
                        if (videoData.videoList.isNotEmpty()) {
                            Button(
                                onClick = { onVideoClick(videoData.videoList[0].vid) },
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(56.dp),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF43F5E)
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
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
                        Text(
                            text = videoData.seriesTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

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

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            userScrollEnabled = false
                        ) {
                            items(videoData.videoList) { episode ->
                                EpisodeCard(
                                    episode = episode,
                                    onEpisodeClick = { onVideoClick(episode.vid) }
                                )
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