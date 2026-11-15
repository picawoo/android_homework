package com.example.androidhomework.ui.state

import com.example.androidhomework.domain.model.Book

data class BookDetailUiState(
    val book: Book? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)