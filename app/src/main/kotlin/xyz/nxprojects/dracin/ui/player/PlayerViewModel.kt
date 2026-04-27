// PlayerViewModel.kt - Support Multiple Videos
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

data class VideoItem(
    val id: String,
    val url: String,
    val title: String? = null,
    val episode: String? = null
)

data class PlayerUiState(
    val currentVideoIndex: Int = 0,
    val videos: List<VideoItem> = emptyList(),
    val videoUrl: String? = null, // For backward compatibility
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
                    val videoUrl = streamData?.mainUrl?.ifEmpty { 
                        streamData.backupUrl 
                    } ?: streamData?.videoModel?.videoList?.values?.firstOrNull()?.mainUrlDecoded
                    
                    _uiState.value = PlayerUiState(
                        videoUrl = videoUrl,
                        videos = listOf(
                            VideoItem(
                                id = vid,
                                url = videoUrl ?: "",
                                title = "Video",
                                episode = ""
                            )
                        ),
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

    // Function to load multiple videos (for future expansion)
    fun loadVideoPlaylist(videoIds: List<String>) {
        viewModelScope.launch {
            try {
                _uiState.value = PlayerUiState(isLoading = true)
                val videoItems = mutableListOf<VideoItem>()
                
                videoIds.forEach { vid ->
                    val result = bookRepository.getStream(vid)
                    if (result.isSuccess) {
                        val streamData = result.getOrNull()
                        val videoUrl = streamData?.mainUrl?.ifEmpty { 
                            streamData.backupUrl 
                        } ?: streamData?.videoModel?.videoList?.values?.firstOrNull()?.mainUrlDecoded
                        
                        if (videoUrl != null) {
                            videoItems.add(
                                VideoItem(
                                    id = vid,
                                    url = videoUrl,
                                    title = "Video ${videoItems.size + 1}",
                                    episode = ""
                                )
                            )
                        }
                    }
                }
                
                if (videoItems.isNotEmpty()) {
                    _uiState.value = PlayerUiState(
                        videos = videoItems,
                        videoUrl = videoItems.firstOrNull()?.url,
                        isLoading = false
                    )
                } else {
                    _uiState.value = PlayerUiState(
                        isLoading = false,
                        error = "No videos could be loaded"
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

    fun setCurrentVideoIndex(index: Int) {
        _uiState.value = _uiState.value.copy(currentVideoIndex = index)
    }
}
