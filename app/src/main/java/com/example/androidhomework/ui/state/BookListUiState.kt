package com.example.androidhomework.ui.state

import com.example.androidhomework.data.model.Book

data class BookListUiState(
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = false
)