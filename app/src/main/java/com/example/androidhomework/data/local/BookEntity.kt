package com.example.androidhomework.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.androidhomework.domain.model.Book
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "favorite_books")
data class BookEntity(
    @PrimaryKey
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

class BookTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromList(value: List<String>?): String {
        return gson.toJson(value ?: emptyList<String>())
    }

    @TypeConverter
    fun toList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }
}

fun Book.toEntity(): BookEntity = BookEntity(
    id = id,
    title = title,
    author = author,
    publishYear = publishYear,
    coverUrl = coverUrl,
    editionCount = editionCount,
    description = description,
    subjects = subjects,
    language = language
)

fun BookEntity.toDomain(): Book = Book(
    id = id,
    title = title,
    author = author,
    publishYear = publishYear,
    coverUrl = coverUrl,
    editionCount = editionCount,
    description = description,
    subjects = subjects,
    language = language
)