package com.example.androidhomework.domain.repository

import com.example.androidhomework.data.preferences.FilterSettings
import com.example.androidhomework.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    suspend fun getBooks(query: String, filters: FilterSettings): Result<List<Book>>
    suspend fun getBookDetails(id: String, authorName: String): Result<Book>
    fun getFavoriteBooks(): Flow<List<Book>>
    fun isFavorite(bookId: String): Flow<Boolean>
    suspend fun toggleFavorite(book: Book)
}