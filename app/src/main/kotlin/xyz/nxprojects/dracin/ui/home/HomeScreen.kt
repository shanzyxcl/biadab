package xyz.nxprojects.dracin.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import xyz.nxprojects.dracin.ui.components.DramaCard
import xyz.nxprojects.dracin.ui.components.ErrorDialog

@Composable
fun HomeScreen(
    onDramaClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showErrorDialog by remember { mutableStateOf(false) }

    // Show error dialog jika ada error
    if (showErrorDialog || (uiState.error != null && !uiState.isLoading)) {
        ErrorDialog(
            title = "Error Load Data",
            message = uiState.error ?: "Terjadi kesalahan saat memuat data",
            stackTrace = uiState.errorStackTrace,
            onDismiss = { showErrorDialog = false },
            onRetry = {
                showErrorDialog = false
                viewModel.refresh()
            }
        )
    }

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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Welcome Text
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Nonton Drama Pendek Terbaik",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Home Section
                if (uiState.home.isNotEmpty()) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Home",
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 8.dp),
                                tint = Color(0xFFF43F5E)
                            )
                            Text(
                                text = "Drama Populer",
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
                            items(uiState.home) { drama ->
                                DramaCard(
                                    drama = drama,
                                    onCardClick = { bookId ->
                                        try {
                                            onDramaClick(bookId)
                                        } catch (e: Exception) {
                                            showErrorDialog = true
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Drama 18+ Section
                if (uiState.drama18.isNotEmpty()) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = "Drama 18+",
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 8.dp),
                                tint = Color(0xFFEF4444)
                            )
                            Text(
                                text = "Drama 18+",
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
                            items(uiState.drama18) { drama ->
                                DramaCard(
                                    drama = drama,
                                    onCardClick = { bookId ->
                                        try {
                                            onDramaClick(bookId)
                                        } catch (e: Exception) {
                                            showErrorDialog = true
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Komik Section
                if (uiState.komik.isNotEmpty()) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = "Komik",
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 8.dp),
                                tint = Color(0xFFEAB308)
                            )
                            Text(
                                text = "Komik",
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
                            items(uiState.komik) { drama ->
                                DramaCard(
                                    drama = drama,
                                    onCardClick = { bookId ->
                                        try {
                                            onDramaClick(bookId)
                                        } catch (e: Exception) {
                                            showErrorDialog = true
                                        }
                                    }
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