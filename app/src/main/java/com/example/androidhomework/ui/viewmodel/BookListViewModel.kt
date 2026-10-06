package com.example.androidhomework.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.androidhomework.data.model.Book
import com.example.androidhomework.ui.state.BookListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BookListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BookListUiState())
    val uiState: StateFlow<BookListUiState> = _uiState.asStateFlow()

    init {
        loadMockBooks()
    }

    private fun loadMockBooks() {
        val mockData = listOf(
            Book(
                id = "OL27448W",
                title = "The Hobbit",
                author = "J.R.R. Tolkien",
                publishYear = 1937,
                coverUrl = "https://covers.openlibrary.org/b/id/8406786-L.jpg",
                rating = 4.8f,
                ratingsCount = 12450,
                description = "Bilbo Baggins is a hobbit who enjoys a comfortable, unambitious life, rarely traveling any further than his pantry or cellar. But his contentment is disturbed when Gandalf and thirteen dwarves arrive on his doorstep...",
                subjects = listOf("Fantasy", "Adventure", "Classics"),
                pageCount = 310,
                language = "English"
            ),
            Book(
                id = "OL2163653W",
                title = "Dune",
                author = "Frank Herbert",
                publishYear = 1965,
                coverUrl = "https://covers.openlibrary.org/b/id/10530386-L.jpg",
                rating = 4.7f,
                ratingsCount = 9820,
                description = "Set on the desert planet Arrakis, Dune is the story of the boy Paul Atreides, heir to a noble family tasked with ruling an inhospitable world where the only thing of value is the 'spice' melange...",
                subjects = listOf("Science Fiction", "Space Opera", "Dystopian"),
                pageCount = 412,
                language = "English"
            ),
            Book(
                id = "OL82586W",
                title = "1984",
                author = "George Orwell",
                publishYear = 1949,
                coverUrl = "https://covers.openlibrary.org/b/id/7222246-L.jpg",
                rating = 4.6f,
                ratingsCount = 15300,
                description = "Winston Smith rewrites history for the Ministry of Truth, but secretly hates the Party and dreams of rebellion in a totalitarian world ruled by Big Brother.",
                subjects = listOf("Dystopian", "Political Fiction", "Classics"),
                pageCount = 328,
                language = "English"
            ),
            Book(
                id = "OL262419W",
                title = "Pride and Prejudice",
                author = "Jane Austen",
                publishYear = 1813,
                coverUrl = "https://covers.openlibrary.org/b/id/8231856-L.jpg",
                rating = 4.5f,
                ratingsCount = 11200,
                description = "A classic romance novel following the turbulent relationship between Elizabeth Bennet, the daughter of a country gentleman, and Fitzwilliam Darcy, a rich aristocratic landowner.",
                subjects = listOf("Romance", "Classics", "Literature"),
                pageCount = 279,
                language = "English"
            ),
            Book(
                id = "OL27479W",
                title = "The Great Gatsby",
                author = "F. Scott Fitzgerald",
                publishYear = 1925,
                coverUrl = "https://covers.openlibrary.org/b/id/8313217-L.jpg",
                rating = 4.4f,
                ratingsCount = 8900,
                description = "The story primarily concerns the mysterious millionaire Jay Gatsby and his passion and obsession for the beautiful former debutante Daisy Buchanan.",
                subjects = listOf("Classics", "American Literature", "Fiction"),
                pageCount = 180,
                language = "English"
            ),
            Book(
                id = "OL23919W",
                title = "To Kill a Mockingbird",
                author = "Harper Lee",
                publishYear = 1960,
                coverUrl = "https://covers.openlibrary.org/b/id/8225266-L.jpg",
                rating = 4.9f,
                ratingsCount = 18400,
                description = "The unforgettable novel of a childhood in a sleepy Southern town and the crisis of conscience that rocked it, taking a view on racial injustice and innocence.",
                subjects = listOf("Classics", "Historical Fiction", "Drama"),
                pageCount = 281,
                language = "English"
            )
        )
        _uiState.value = BookListUiState(books = mockData)
    }
}