package xyz.nxprojects.dracin.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import xyz.nxprojects.dracin.data.model.ErrorType
import xyz.nxprojects.dracin.ui.components.DramaCard
import xyz.nxprojects.dracin.ui.components.HomeTopAppBar

@Composable
fun HomeScreen(
    onDramaClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF09090B))
    ) {
        // Top App Bar
        HomeTopAppBar()

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
                // Enhanced Error Display
                ErrorScreen(
                    error = uiState.error ?: "Unknown error",
                    errorDetail = uiState.errorDetail,
                    onRetry = { viewModel.refresh() }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = "Nonton Drama Pendek Terbaik",
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Trending Section
                    if (uiState.trending.isNotEmpty()) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Trending",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .padding(end = 8.dp),
                                    tint = Color(0xFFF43F5E)
                                )
                                Text(
                                    text = "Sedang Trending",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(uiState.trending) { drama ->
                                    DramaCard(
                                        drama = drama,
                                        onCardClick = onDramaClick
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // Latest Section
                    if (uiState.latest.isNotEmpty()) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Whatshot,
                                    contentDescription = "Latest",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .padding(end = 8.dp),
                                    tint = Color(0xFFEAB308)
                                )
                                Text(
                                    text = "Drama Terbaru",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(uiState.latest) { drama ->
                                    DramaCard(
                                        drama = drama,
                                        onCardClick = onDramaClick
                                    )
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
fun ErrorScreen(
    error: String,
    errorDetail: xyz.nxprojects.dracin.data.model.ApiError?,
    onRetry: () -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Error Icon
        Icon(
            imageVector = when (errorDetail?.errorType) {
                ErrorType.NETWORK -> Icons.Default.WifiOff
                ErrorType.HTTP_403 -> Icons.Default.Block
                ErrorType.HTTP_404 -> Icons.Default.SearchOff
                ErrorType.TIMEOUT -> Icons.Default.HourglassEmpty
                else -> Icons.Default.ErrorOutline
            },
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFF43F5E)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // HTTP Code (jika ada)
        if (errorDetail?.httpCode != 0 && errorDetail?.httpCode != null) {
            Text(
                text = "HTTP ${errorDetail.httpCode}",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )
            
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Error Type
        errorDetail?.errorType?.let { type ->
            Text(
                text = when (type) {
                    ErrorType.HTTP_403 -> "FORBIDDEN"
                    ErrorType.HTTP_404 -> "NOT FOUND"
                    ErrorType.HTTP_500 -> "SERVER ERROR"
                    ErrorType.NETWORK -> "NETWORK ERROR"
                    ErrorType.TIMEOUT -> "TIMEOUT"
                    ErrorType.PARSE_ERROR -> "PARSE ERROR"
                    ErrorType.UNKNOWN -> "ERROR"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFF43F5E),
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Main Error Message
        Text(
            text = error,
            color = Color.White,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // JSON Message dari server
        if (!errorDetail?.jsonMessage.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1C1C1E)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Response dari Server:",
                            color = Color(0xFFEAB308),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        IconButton(
                            onClick = { showDetails = !showDetails },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (showDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle details",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    if (showDetails) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = errorDetail.jsonMessage ?: "",
                            color = Color(0xFFFF6B6B),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Retry Button
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF43F5E)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Retry",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Retry",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Troubleshooting Tips
        if (errorDetail?.errorType == ErrorType.HTTP_403) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1C1C1E)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "💡 Tips:",
                        color = Color(0xFFEAB308),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Periksa koneksi internet\n• Coba gunakan VPN\n• Server mungkin sedang maintenance\n• API memerlukan authentication",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}