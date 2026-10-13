package com.example.androidhomework.di

import com.example.androidhomework.data.remote.OpenLibraryApiService
import com.example.androidhomework.data.repository.BookRepositoryImpl
import com.example.androidhomework.domain.usecase.GetBooksUseCase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://openlibrary.org/"

/**
 * Простейший ручной DI: один Retrofit и один репозиторий на всё приложение,
 * вместо отдельных экземпляров в каждой ViewModel.
 * При переходе на Hilt/Koin этот объект заменяется модулем.
 */
object AppContainer {

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val apiService: OpenLibraryApiService by lazy {
        retrofit.create(OpenLibraryApiService::class.java)
    }

    val bookRepository: BookRepositoryImpl by lazy {
        BookRepositoryImpl(apiService)
    }

    val getBooksUseCase: GetBooksUseCase by lazy {
        GetBooksUseCase(bookRepository)
    }
}
