package com.example.androidhomework.ui.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

const val ARG_BOOK_ID = "bookId"
const val ARG_AUTHOR = "author"

sealed class Screen(val route: String, val title: String) {

    sealed class TopLevel(
        route: String,
        title: String,
        val icon: ImageVector
    ) : Screen(route, title)

    data object BookList : TopLevel("book_list", "Books", Icons.Default.Info)
    data object Favorites : TopLevel("favorites", "Favorites", Icons.Default.Favorite)
    data object Profile : TopLevel("profile", "Profile", Icons.Default.Person)

    data object BookDetail : Screen("book_detail/{$ARG_BOOK_ID}/{$ARG_AUTHOR}", "Detail") {
        fun createRoute(bookId: String, author: String): String =
            "book_detail/${Uri.encode(bookId)}/${Uri.encode(author)}"
    }

    companion object {
        val bottomNavItems: List<TopLevel> = listOf(BookList, Favorites, Profile)
    }
}
