package xyz.nxprojects.dracin.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import xyz.nxprojects.dracin.data.model.VideoData
import xyz.nxprojects.dracin.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val videoData: VideoData? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val errorStackTrace: String? = null
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadDetail(bookId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = DetailUiState(isLoading = true)
                val result = bookRepository.getDetail(bookId)
                
                if (result.isSuccess) {
                    _uiState.value = DetailUiState(
                        videoData = result.getOrNull(),
                        isLoading = false
                    )
                } else {
                    val exception = result.exceptionOrNull()
                    _uiState.value = DetailUiState(
                        isLoading = false,
                        error = exception?.message ?: "Failed to load detail",
                        errorStackTrace = exception?.stackTraceToString()
                    )
                }
            } catch (e: Exception) {
                _uiState.value = DetailUiState(
                    isLoading = false,
                    error = "Crash: ${e.message ?: "Unknown error"}",
                    errorStackTrace = e.stackTraceToString()
                )
            }
        }
    }
}
