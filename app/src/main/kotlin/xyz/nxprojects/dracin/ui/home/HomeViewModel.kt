package xyz.nxprojects.dracin.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import xyz.nxprojects.dracin.data.model.Book
import xyz.nxprojects.dracin.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val trending: List<Book> = emptyList(),
    val latest: List<Book> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val errorStackTrace: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun refresh() {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState(isLoading = true)
                
                val trendingResult = bookRepository.getTrending()
                val latestResult = bookRepository.getLatest()

                if (trendingResult.isSuccess && latestResult.isSuccess) {
                    _uiState.value = HomeUiState(
                        trending = trendingResult.getOrNull() ?: emptyList(),
                        latest = latestResult.getOrNull() ?: emptyList(),
                        isLoading = false
                    )
                } else {
                    val error = trendingResult.exceptionOrNull() ?: latestResult.exceptionOrNull()
                    _uiState.value = HomeUiState(
                        isLoading = false,
                        error = error?.message ?: "Failed to load data",
                        errorStackTrace = error?.stackTraceToString()
                    )
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState(
                    isLoading = false,
                    error = "Crash: ${e.message ?: "Unknown error"}",
                    errorStackTrace = e.stackTraceToString()
                )
            }
        }
    }
}
