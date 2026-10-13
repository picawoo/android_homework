package com.example.androidhomework.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.androidhomework.data.repository.BookRepositoryImpl
import com.example.androidhomework.di.AppContainer
import com.example.androidhomework.ui.navigation.ARG_AUTHOR
import com.example.androidhomework.ui.navigation.ARG_BOOK_ID
import com.example.androidhomework.ui.state.BookDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

private const val DEFAULT_ERROR_MESSAGE = "Failed to load book details."

class BookDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: BookRepositoryImpl
) : ViewModel() {

    private val bookId: String = checkNotNull(savedStateHandle.get<String>(ARG_BOOK_ID))
    private val authorName: String = checkNotNull(savedStateHandle.get<String>(ARG_AUTHOR))
    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    init {
        loadBookDetails()
    }

    private fun loadBookDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            repository.getBookDetails(bookId, authorName)
                .onSuccess { book ->
                    _uiState.update { it.copy(book = book, isLoading = false) }
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
            initializer {
                BookDetailViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = AppContainer.bookRepository
                )
            }
        }
    }
}
