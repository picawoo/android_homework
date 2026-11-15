package com.example.androidhomework.domain.model

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val publishYear: Int,
    val coverUrl: String,
    val editionCount: Int,
    val description: String,
    val subjects: List<String>,
    val language: String
)