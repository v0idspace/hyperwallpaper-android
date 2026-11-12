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
                onWallpaperClick = { wp ->
                    navController.navigate("detail/${wp.id}")
                },
                onOpenFavorites = {
                    navController.navigate("favorites")
                }
            )
        }

        composable(
            route = "detail/{wallpaperId}",
            arguments = listOf(navArgument("wallpaperId") { type = NavType.StringType })
        ) { backStackEntry ->
            val wallpaperId = backStackEntry.arguments?.getString("wallpaperId").orEmpty()
            val detailViewModel: DetailViewModel = viewModel()
            DetailScreen(
                wallpaperId = wallpaperId,
                viewModel = detailViewModel,
                onBack = { navController.popBackStack() },
                onOpenAuthor = { authorName ->
                    val encoded = URLEncoder.encode(authorName, "UTF-8")
                    navController.navigate("author/$encoded")
                },
                onOpenRelated = { rel ->
                    navController.navigate("detail/${rel.id}")
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
                    navController.navigate("detail/${wp.id}")
                }
            )
        }

        composable("favorites") {
            val favoritesViewModel: FavoritesViewModel = viewModel()
            FavoritesScreen(
                viewModel = favoritesViewModel,
                onBack = { navController.popBackStack() },
                onWallpaperClick = { wp ->
                    navController.navigate("detail/${wp.id}")
                }
            )
        }
    }
}
