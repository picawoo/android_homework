package com.example.androidhomework.data.remote

import com.example.androidhomework.data.remote.model.BookDetailDto
import com.example.androidhomework.data.remote.model.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenLibraryApiService {
    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String
    ): SearchResponse

    @GET("works/{id}.json")
    suspend fun getBookDetails(
        @Path("id") id: String
    ): BookDetailDto
}