// PlayerViewModel.kt - FULL
package xyz.nxprojects.dracin.ui.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import xyz.nxprojects.dracin.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val videoUrl: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val errorStackTrace: String? = null
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val videoId: String = checkNotNull(savedStateHandle["videoId"])

    init {
        loadVideo(videoId)
    }

    fun loadVideo(vid: String) {
        viewModelScope.launch {
            try {
                _uiState.value = PlayerUiState(isLoading = true)
                val result = bookRepository.getStream(vid)
                
                if (result.isSuccess) {
                    val streamData = result.getOrNull()
                    _uiState.value = PlayerUiState(
                        videoUrl = streamData?.videoUrl,
                        isLoading = false
                    )
                } else {
                    val exception = result.exceptionOrNull()
                    _uiState.value = PlayerUiState(
                        isLoading = false,
                        error = exception?.message ?: "Failed to load video",
                        errorStackTrace = exception?.stackTraceToString()
                    )
                }
            } catch (e: Exception) {
                _uiState.value = PlayerUiState(
                    isLoading = false,
                    error = "Crash: ${e.message ?: "Unknown error"}",
                    errorStackTrace = e.stackTraceToString()
                )
            }
        }
    }
}