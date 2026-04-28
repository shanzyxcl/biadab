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
    val home: List<Book> = emptyList(),
    val drama18: List<Book> = emptyList(),
    val komik: List<Book> = emptyList(),
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
                
                val homeResult = bookRepository.getHome()
                val drama18Result = bookRepository.getDrama18()
                val komikResult = bookRepository.getKomik()

                if (homeResult.isSuccess && drama18Result.isSuccess && komikResult.isSuccess) {
                    _uiState.value = HomeUiState(
                        home = homeResult.getOrNull() ?: emptyList(),
                        drama18 = drama18Result.getOrNull() ?: emptyList(),
                        komik = komikResult.getOrNull() ?: emptyList(),
                        isLoading = false
                    )
                } else {
                    val error = homeResult.exceptionOrNull() 
                        ?: drama18Result.exceptionOrNull() 
                        ?: komikResult.exceptionOrNull()
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