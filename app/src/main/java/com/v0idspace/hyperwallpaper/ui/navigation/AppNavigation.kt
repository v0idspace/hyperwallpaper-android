package com.v0idspace.hyperwallpaper.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.v0idspace.hyperwallpaper.ui.screens.author.AuthorScreen
import com.v0idspace.hyperwallpaper.ui.screens.author.AuthorViewModel
import com.v0idspace.hyperwallpaper.ui.screens.catalog.CatalogScreen
import com.v0idspace.hyperwallpaper.ui.screens.catalog.CatalogViewModel
import com.v0idspace.hyperwallpaper.ui.screens.detail.DetailScreen
import com.v0idspace.hyperwallpaper.ui.screens.detail.DetailViewModel
import com.v0idspace.hyperwallpaper.ui.screens.favorites.FavoritesScreen
import com.v0idspace.hyperwallpaper.ui.screens.favorites.FavoritesViewModel
import com.v0idspace.hyperwallpaper.util.DetailNavigationState
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "catalog"
    ) {
        composable("catalog") {
            val catalogViewModel: CatalogViewModel = viewModel()
            CatalogScreen(
                viewModel = catalogViewModel,
                onWallpaperClick = { wp, index ->
                    val wallpapers = catalogViewModel.uiState.value.wallpapers
                    DetailNavigationState.saveSession("catalog", wallpapers)
                    navController.navigate("detail/catalog/${wp.id}/$index")
                },
                onOpenFavorites = {
                    navController.navigate("favorites")
                }
            )
        }

        composable(
            route = "detail/{sessionId}/{wallpaperId}/{initialIndex}",
            arguments = listOf(
                navArgument("sessionId") { type = NavType.StringType },
                navArgument("wallpaperId") { type = NavType.StringType },
                navArgument("initialIndex") { type = NavType.IntType; defaultValue = 0 }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId").orEmpty()
            val wallpaperId = backStackEntry.arguments?.getString("wallpaperId").orEmpty()
            val initialIndex = backStackEntry.arguments?.getInt("initialIndex") ?: 0

            // Scope ViewModel strictly to this unique backStackEntry so each depth level has its own state
            val detailViewModel: DetailViewModel = viewModel(viewModelStoreOwner = backStackEntry)

            DetailScreen(
                sessionId = sessionId,
                initialWallpaperId = wallpaperId,
                initialIndex = initialIndex,
                viewModel = detailViewModel,
                onBack = { navController.popBackStack() },
                onOpenAuthor = { authorName ->
                    val encoded = URLEncoder.encode(authorName, "UTF-8")
                    navController.navigate("author/$encoded")
                },
                onOpenRelated = { nextSessionId, rel, relIndex ->
                    // Navigate to Depth N+1 with its own session list
                    navController.navigate("detail/$nextSessionId/${rel.id}/$relIndex")
                }
            )
        }

        composable(
            route = "author/{authorName}",
            arguments = listOf(navArgument("authorName") { type = NavType.StringType })
        ) { backStackEntry ->
            val rawName = backStackEntry.arguments?.getString("authorName").orEmpty()
            val authorName = URLDecoder.decode(rawName, "UTF-8")
            val authorViewModel: AuthorViewModel = viewModel()
            AuthorScreen(
                authorName = authorName,
                viewModel = authorViewModel,
                onBack = { navController.popBackStack() },
                onWallpaperClick = { wp ->
                    val authorWallpapers = authorViewModel.uiState.value.wallpapers
                    val index = authorWallpapers.indexOfFirst { it.id == wp.id }.coerceAtLeast(0)
                    val sessionId = "author_${System.currentTimeMillis()}"
                    DetailNavigationState.saveSession(sessionId, authorWallpapers)
                    navController.navigate("detail/$sessionId/${wp.id}/$index")
                }
            )
        }

        composable("favorites") {
            val favoritesViewModel: FavoritesViewModel = viewModel()
            FavoritesScreen(
                viewModel = favoritesViewModel,
                onBack = { navController.popBackStack() },
                onWallpaperClick = { wp ->
                    val favWallpapers = favoritesViewModel.favorites.value
                    val index = favWallpapers.indexOfFirst { it.id == wp.id }.coerceAtLeast(0)
                    val sessionId = "favorites_${System.currentTimeMillis()}"
                    DetailNavigationState.saveSession(sessionId, favWallpapers)
                    navController.navigate("detail/$sessionId/${wp.id}/$index")
                }
            )
        }
    }
}
