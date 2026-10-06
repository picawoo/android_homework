package com.example.androidhomework.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object BookList : Screen("book_list", "Books", Icons.Default.Info)
    object Favorites : Screen("favorites", "Favorites", Icons.Default.Favorite)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object BookDetail : Screen("book_detail/{bookId}", "Detail") {
        fun createRoute(bookId: String) = "book_detail/$bookId"
    }
}