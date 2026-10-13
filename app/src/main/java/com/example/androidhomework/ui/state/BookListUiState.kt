package com.example.androidhomework.ui.state

import com.example.androidhomework.domain.model.Book

data class BookListUiState(
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = ""
)