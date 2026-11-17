package com.example.androidhomework.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM favorite_books")
    fun getFavoriteBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM favorite_books WHERE id = :bookId")
    suspend fun getBookById(bookId: String): BookEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_books WHERE id = :bookId)")
    fun isFavorite(bookId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(book: BookEntity)

    @Update
    suspend fun updateFavorite(book: BookEntity)

    @Query("DELETE FROM favorite_books WHERE id = :bookId")
    suspend fun removeFavorite(bookId: String)
}