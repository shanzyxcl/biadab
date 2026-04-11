package xyz.nxprojects.dracin.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import xyz.nxprojects.dracin.data.model.Book
import xyz.nxprojects.dracin.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    private var searchJob: Job? = null

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            query = query,
            hasSearched = if (query.isEmpty()) false else _uiState.value.hasSearched
        )
        
        searchJob?.cancel()
        
        if (query.isNotEmpty()) {
            searchJob = viewModelScope.launch {
                delay(500)
                search(query)
            }
        } else {
            _uiState.value = _uiState.value.copy(
                results = emptyList(),
                isLoading = false,
                hasSearched = false
            )
        }
    }

    private suspend fun search(query: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        try {
            val result = bookRepository.searchBooks(query)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    results = result.getOrNull() ?: emptyList(),
                    isLoading = false,
                    hasSearched = true
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasSearched = true,
                    error = result.exceptionOrNull()?.message
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                hasSearched = true,
                error = e.message
            )
        }
    }
}