package xyz.nxprojects.dracin.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import xyz.nxprojects.dracin.ui.components.DramaCard
import xyz.nxprojects.dracin.ui.components.SearchTopAppBar

@Composable
fun SearchScreen(
    onDramaClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF09090B))
    ) {
        // Top App Bar
        SearchTopAppBar()

        // Search Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF18181B)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 8.dp),
                    tint = Color.Gray
                )

                TextField(
                    value = uiState.query,
                    onValueChange = { viewModel.updateQuery(it) },
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF18181B)),
                    placeholder = {
                        Text("Cari drama...", color = Color.Gray)
                    },
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { keyboardController?.hide() }
                    ),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFF18181B),
                        focusedContainerColor = Color(0xFF18181B),
                        cursorColor = Color(0xFFF43F5E),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                if (uiState.query.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.updateQuery("") },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = Color.Gray
                        )
                    }
                }
            }
        }

        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFFF43F5E)
                    )
                }

                !uiState.hasSearched -> {
                    Text(
                        text = "Mulai cari drama favorit Anda",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                }

                uiState.results.isEmpty() -> {
                    Text(
                        text = "Tidak ada hasil untuk \"${uiState.query}\"",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.results) { drama ->
                            DramaCard(
                                drama = drama,
                                onCardClick = onDramaClick,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}