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
    val error: String? = null,
    val errorStackTrace: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        
        searchJob?.cancel()
        
        if (query.length >= 2) {
            searchJob = viewModelScope.launch {
                delay(500)
                search(query)
            }
        } else {
            _uiState.value = _uiState.value.copy(results = emptyList(), error = null)
        }
    }

    private suspend fun search(query: String) {
        try {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = bookRepository.searchBooks(query)
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    results = result.getOrNull() ?: emptyList(),
                    isLoading = false
                )
            } else {
                val exception = result.exceptionOrNull()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception?.message ?: "Failed to search",
                    errorStackTrace = exception?.stackTraceToString()
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Crash: ${e.message ?: "Unknown error"}",
                errorStackTrace = e.stackTraceToString()
            )
        }
    }
}