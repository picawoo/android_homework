package com.example.androidhomework.di

import android.content.Context
import com.example.androidhomework.data.local.AppDatabase
import com.example.androidhomework.data.preferences.FilterBadgeCache
import com.example.androidhomework.data.preferences.FilterPreferences
import com.example.androidhomework.data.remote.OpenLibraryApiService
import com.example.androidhomework.data.repository.BookRepositoryImpl
import com.example.androidhomework.domain.usecase.GetBooksUseCase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://openlibrary.org/"

object AppContainer {

    private lateinit var applicationContext: Context

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val apiService: OpenLibraryApiService by lazy {
        retrofit.create(OpenLibraryApiService::class.java)
    }

    val database: AppDatabase by lazy {
        androidx.room.Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "books_database"
        ).build()
    }

    val bookRepository: BookRepositoryImpl by lazy {
        BookRepositoryImpl(apiService, database.bookDao())
    }

    val getBooksUseCase: GetBooksUseCase by lazy {
        GetBooksUseCase(bookRepository)
    }

    val filterPreferences: FilterPreferences by lazy {
        FilterPreferences(applicationContext)
    }

    val filterBadgeCache: FilterBadgeCache by lazy {
        FilterBadgeCache()
    }
}