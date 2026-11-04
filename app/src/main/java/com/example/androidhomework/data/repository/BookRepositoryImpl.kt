package com.example.androidhomework.data.repository

import com.example.androidhomework.data.local.BookDao
import com.example.androidhomework.data.local.toDomain
import com.example.androidhomework.data.local.toEntity
import com.example.androidhomework.data.preferences.FilterSettings
import com.example.androidhomework.data.preferences.SortOption
import com.example.androidhomework.data.remote.OpenLibraryApiService
import com.example.androidhomework.domain.model.Book
import com.example.androidhomework.domain.repository.BookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException

class BookRepositoryImpl(
    private val apiService: OpenLibraryApiService,
    private val bookDao: BookDao
) : BookRepository {

    companion object {
        private val bookCache = ConcurrentHashMap<String, Book>()
    }

    override suspend fun getBooks(query: String, filters: FilterSettings): Result<List<Book>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchBooks(query)
            var books = response.docs.map { dto ->
                val coverUrl = dto.coverI?.let {
                    "https://covers.openlibrary.org/b/id/$it-M.jpg"
                } ?: ""

                val book = Book(
                    id = dto.key.replace("/works/", ""),
                    title = dto.title ?: "Unknown Title",
                    author = dto.authorName?.joinToString(", ") ?: "Unknown Author",
                    publishYear = dto.firstPublishYear ?: 0,
                    coverUrl = coverUrl,
                    editionCount = dto.editionCount ?: 0,
                    description = "",
                    subjects = dto.subject?.take(5) ?: emptyList(),
                    language = dto.language?.firstOrNull()?.uppercase() ?: "EN"
                )

                bookCache[book.id] = book
                book
            }

            if (filters.onlyWithCover) {
                books = books.filter { it.coverUrl.isNotBlank() }
            }

            if (filters.selectedLanguage.isNotBlank()) {
                books = books.filter { book ->
                    book.language.equals(filters.selectedLanguage, ignoreCase = true)
                }
            }

            books = when (filters.sortBy) {
                SortOption.NEWEST -> books.sortedByDescending { it.publishYear }
                SortOption.MOST_EDITIONS -> books.sortedByDescending { it.editionCount }
                SortOption.DEFAULT -> books
            }

            Result.success(books)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBookDetails(id: String, authorName: String): Result<Book> = withContext(Dispatchers.IO) {
        try {
            val dto = apiService.getBookDetails(id)

            val localBook = bookCache[id] ?: bookDao.getBookById(id)?.toDomain()

            val coverUrl = if (!dto.covers.isNullOrEmpty()) {
                "https://covers.openlibrary.org/b/id/${dto.covers.first()}-L.jpg"
            } else {
                localBook?.coverUrl ?: ""
            }

            val book = Book(
                id = id,
                title = dto.title ?: localBook?.title ?: "Unknown Title",
                author = authorName.ifBlank { localBook?.author ?: "Unknown Author" },
                publishYear = if (dto.publishYear > 0) dto.publishYear else (localBook?.publishYear ?: 0),
                coverUrl = coverUrl,
                editionCount = localBook?.editionCount ?: 0,
                description = dto.description.ifBlank { localBook?.description.orEmpty() },
                subjects = if (!dto.subjects.isNullOrEmpty()) dto.subjects.take(5) else (localBook?.subjects ?: emptyList()),
                language = localBook?.language ?: "EN"
            )

            bookDao.updateFavorite(book.toEntity())

            Result.success(book)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            bookDao.getBookById(id)?.let { Result.success(it.toDomain()) } ?: Result.failure(e)
        }
    }

    override fun getFavoriteBooks(): Flow<List<Book>> {
        return bookDao.getFavoriteBooks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun isFavorite(bookId: String): Flow<Boolean> {
        return bookDao.isFavorite(bookId)
    }

    override suspend fun toggleFavorite(book: Book) = withContext(Dispatchers.IO) {
        val existing = bookDao.getBookById(book.id)
        if (existing != null) {
            bookDao.removeFavorite(book.id)
        } else {
            addFavorite(book)
        }
    }

    private suspend fun addFavorite(book: Book) {
        bookDao.insertFavorite(book.toEntity())

        if (book.description.isBlank()) {
            getBookDetails(book.id, book.author)
        }
    }
}