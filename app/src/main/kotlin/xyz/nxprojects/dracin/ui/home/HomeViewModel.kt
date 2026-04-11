package xyz.nxprojects.dracin.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.nxprojects.dracin.data.model.ApiError
import xyz.nxprojects.dracin.data.model.Book
import xyz.nxprojects.dracin.data.repository.ApiErrorException
import xyz.nxprojects.dracin.data.repository.BookRepository
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val trending: List<Book> = emptyList(),
    val latest: List<Book> = emptyList(),
    val error: String? = null,
    val errorDetail: ApiError? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: BookRepository
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

            // Load trending
            val trendingResult = repository.getTrending()
            
            if (trendingResult.isFailure) {
                val exception = trendingResult.exceptionOrNull()
                val apiError = if (exception is ApiErrorException) {
                    exception.apiError
                } else {
                    ApiError(message = exception?.message ?: "Unknown error")
                }
                
                _uiState.value = HomeUiState(
                    isLoading = false,
                    error = apiError.message,
                    errorDetail = apiError
                )
                return@launch
            }

            // Load latest
            val latestResult = repository.getLatest()
            
            if (latestResult.isFailure) {
                val exception = latestResult.exceptionOrNull()
                val apiError = if (exception is ApiErrorException) {
                    exception.apiError
                } else {
                    ApiError(message = exception?.message ?: "Unknown error")
                }
                
                _uiState.value = HomeUiState(
                    isLoading = false,
                    trending = trendingResult.getOrDefault(emptyList()),
                    error = apiError.message,
                    errorDetail = apiError
                )
                return@launch
            }

            _uiState.value = HomeUiState(
                isLoading = false,
                trending = trendingResult.getOrDefault(emptyList()),
                latest = latestResult.getOrDefault(emptyList())
            )
        }
    }
}