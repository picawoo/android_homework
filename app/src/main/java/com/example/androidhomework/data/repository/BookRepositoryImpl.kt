package com.example.androidhomework.data.repository

import com.example.androidhomework.data.remote.OpenLibraryApiService
import com.example.androidhomework.domain.model.Book
import com.example.androidhomework.domain.repository.BookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookRepositoryImpl(
    private val apiService: OpenLibraryApiService
) : BookRepository {

    companion object {
        private val bookCache = mutableMapOf<String, Book>()
    }

    override suspend fun getBooks(query: String): Result<List<Book>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchBooks(query)
            val books = response.docs.map { dto ->
                val coverUrl = dto.coverI?.let {
                    "https://covers.openlibrary.org/b/id/$it-M.jpg"
                } ?: ""

                val book = Book(
                    id = dto.key.replace("/works/", ""),
                    title = dto.title,
                    author = dto.authorName?.joinToString(", ") ?: "Unknown Author",
                    publishYear = dto.firstPublishYear ?: 0,
                    coverUrl = coverUrl,
                    editionCount = dto.editionCount ?: 0,
                    description = "",
                    subjects = dto.subject?.take(5) ?: emptyList(),
                    language = dto.language?.firstOrNull()?.uppercase() ?: "EN"
                )

                // Сохраняем в кэш при поиске
                bookCache[book.id] = book
                book
            }
            Result.success(books)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBookDetails(id: String, authorName: String): Result<Book> = withContext(Dispatchers.IO) {
        try {
            val dto = apiService.getBookDetails(id)
            val cachedBook = bookCache[id]

            val coverUrl = if (!dto.covers.isNullOrEmpty()) {
                "https://covers.openlibrary.org/b/id/${dto.covers.first()}-L.jpg"
            } else {
                cachedBook?.coverUrl ?: ""
            }

            val book = Book(
                id = id,
                title = dto.title ?: cachedBook?.title ?: "Unknown Title",
                author = authorName.ifBlank { cachedBook?.author ?: "Unknown Author" },
                publishYear = if (dto.publishYear > 0) dto.publishYear else (cachedBook?.publishYear ?: 0),
                coverUrl = coverUrl,
                editionCount = cachedBook?.editionCount ?: 0, // Забираем из кэша
                description = dto.description, // Подробное описание из Works API
                subjects = if (!dto.subjects.isNullOrEmpty()) dto.subjects.take(5) else (cachedBook?.subjects ?: emptyList()),
                language = cachedBook?.language ?: "EN" // Язык из поиска
            )
            Result.success(book)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}