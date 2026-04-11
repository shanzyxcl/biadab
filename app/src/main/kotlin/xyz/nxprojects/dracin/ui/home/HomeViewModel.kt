package xyz.nxprojects.dracin.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import xyz.nxprojects.dracin.data.model.Book
import xyz.nxprojects.dracin.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val latest: List<Book> = emptyList(),
    val trending: List<Book> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
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
            _uiState.value = HomeUiState(isLoading = true)
            try {
                val latestDeferred = async { bookRepository.getLatest() }
                val trendingDeferred = async { bookRepository.getTrending() }

                val latestResult = latestDeferred.await()
                val trendingResult = trendingDeferred.await()

                if (latestResult.isSuccess && trendingResult.isSuccess) {
                    _uiState.value = HomeUiState(
                        latest = latestResult.getOrNull() ?: emptyList(),
                        trending = trendingResult.getOrNull() ?: emptyList(),
                        isLoading = false
                    )
                } else {
                    _uiState.value = HomeUiState(
                        isLoading = false,
                        error = latestResult.exceptionOrNull()?.message 
                            ?: trendingResult.exceptionOrNull()?.message 
                            ?: "Failed to load"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}