package xyz.nxprojects.dracin.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import xyz.nxprojects.dracin.data.model.StreamData
import xyz.nxprojects.dracin.data.model.VideoData
import xyz.nxprojects.dracin.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val streamData: StreamData? = null,
    val videoData: VideoData? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    fun loadStream(videoId: String, bookId: String? = null) {
        viewModelScope.launch {
            _uiState.value = PlayerUiState(isLoading = true)
            try {
                val streamDeferred = async { bookRepository.getStream(videoId) }
                val detailDeferred = if (bookId != null) {
                    async { bookRepository.getDetail(bookId) }
                } else null

                val streamResult = streamDeferred.await()
                val detailResult = detailDeferred?.await()

                if (streamResult.isSuccess) {
                    _uiState.value = PlayerUiState(
                        streamData = streamResult.getOrNull(),
                        videoData = detailResult?.getOrNull(),
                        isLoading = false
                    )
                } else {
                    _uiState.value = PlayerUiState(
                        isLoading = false,
                        error = streamResult.exceptionOrNull()?.message ?: "Failed to load stream"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = PlayerUiState(
                    isLoading = false,
                    error = e.message ?: "Error loading stream"
                )
            }
        }
    }
}