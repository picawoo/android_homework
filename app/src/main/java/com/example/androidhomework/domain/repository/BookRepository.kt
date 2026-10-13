package com.example.androidhomework.domain.repository

import com.example.androidhomework.domain.model.Book

interface BookRepository {
    suspend fun getBooks(query: String): Result<List<Book>>
    suspend fun getBookDetails(id: String, authorName: String): Result<Book>
}