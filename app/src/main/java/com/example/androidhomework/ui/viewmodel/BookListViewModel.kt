package com.example.androidhomework.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.androidhomework.di.AppContainer
import com.example.androidhomework.domain.usecase.GetBooksUseCase
import com.example.androidhomework.ui.state.BookListUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

private const val DEFAULT_ERROR_MESSAGE = "Network error. Please try again."

class BookListViewModel(
    private val getBooksUseCase: GetBooksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookListUiState())
    val uiState: StateFlow<BookListUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadBooks(_uiState.value.searchQuery)
    }

    fun loadBooks(query: String) {
        if (query.isBlank()) return

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, searchQuery = query) }

            getBooksUseCase(query)
                .onSuccess { books ->
                    _uiState.update { it.copy(books = books, isLoading = false) }
                }
                .onFailure { exception ->
                    if (exception is CancellationException) throw exception

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.localizedMessage ?: DEFAULT_ERROR_MESSAGE
                        )
                    }
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { BookListViewModel(AppContainer.getBooksUseCase) }
        }
    }
}
