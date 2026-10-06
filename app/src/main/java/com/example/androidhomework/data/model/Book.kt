package com.example.androidhomework.data.model

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val publishYear: Int,
    val coverUrl: String,
    val rating: Float,
    val ratingsCount: Int,
    val description: String,
    val subjects: List<String>,
    val pageCount: Int,
    val language: String
)