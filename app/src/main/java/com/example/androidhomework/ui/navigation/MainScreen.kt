package com.example.androidhomework.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.androidhomework.ui.screen.BookDetailScreen
import com.example.androidhomework.ui.screen.BookListScreen
import com.example.androidhomework.ui.viewmodel.BookDetailViewModel
import com.example.androidhomework.ui.viewmodel.BookListViewModel

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination

    val showBottomBar = Screen.bottomNavItems.any { it.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    items = Screen.bottomNavItems,
                    currentDestination = currentDestination,
                    onItemClick = navController::navigateToTopLevel
                )
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            innerPadding = innerPadding
        )
    }
}

@Composable
private fun AppBottomBar(
    items: List<Screen.TopLevel>,
    currentDestination: NavDestination?,
    onItemClick: (String) -> Unit
) {
    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = { onItemClick(screen.route) }
            )
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val bookListViewModel: BookListViewModel = viewModel(factory = BookListViewModel.Factory)

    NavHost(
        navController = navController,
        startDestination = Screen.BookList.route,
        modifier = modifier.padding(bottom = innerPadding.calculateBottomPadding())
    ) {
        composable(Screen.BookList.route) {
            BookListScreen(
                viewModel = bookListViewModel,
                onBookClick = { bookId, author ->
                    navController.navigate(Screen.BookDetail.createRoute(bookId, author))
                }
            )
        }

        composable(Screen.Favorites.route) { PlaceholderScreen("Favorites Screen") }
        composable(Screen.Profile.route) { PlaceholderScreen("Profile Screen") }

        composable(
            route = Screen.BookDetail.route,
            arguments = listOf(
                navArgument(ARG_BOOK_ID) { type = NavType.StringType },
                navArgument(ARG_AUTHOR) { type = NavType.StringType }
            )
        ) {
            BookDetailRoute(onBackClick = navController::popBackStack)
        }
    }
}

@Composable
private fun BookDetailRoute(onBackClick: () -> Unit) {
    val viewModel: BookDetailViewModel = viewModel(factory = BookDetailViewModel.Factory)
    val state by viewModel.uiState.collectAsState()

    BookDetailScreen(
        book = state.book,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onBackClick = onBackClick
    )
}

@Composable
private fun PlaceholderScreen(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

private fun NavHostController.navigateToTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
