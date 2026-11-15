package com.example.androidhomework.domain.usecase

import com.example.androidhomework.data.preferences.FilterSettings
import com.example.androidhomework.domain.model.Book
import com.example.androidhomework.domain.repository.BookRepository

class GetBooksUseCase(
    private val repository: BookRepository
) {
    suspend operator fun invoke(query: String, filters: FilterSettings): Result<List<Book>> {
        return repository.getBooks(query, filters)
    }
}