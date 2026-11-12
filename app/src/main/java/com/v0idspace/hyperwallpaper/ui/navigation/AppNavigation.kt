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
                onWallpaperClick = { wp, index ->
                    navController.navigate("detail/${wp.id}/$index")
                },
                onOpenFavorites = {
                    navController.navigate("favorites")
                }
            )
        }

        composable(
            route = "detail/{wallpaperId}/{initialIndex}",
            arguments = listOf(
                navArgument("wallpaperId") { type = NavType.StringType },
                navArgument("initialIndex") { type = NavType.IntType; defaultValue = 0 }
            )
        ) { backStackEntry ->
            val wallpaperId = backStackEntry.arguments?.getString("wallpaperId").orEmpty()
            val initialIndex = backStackEntry.arguments?.getInt("initialIndex") ?: 0
            val detailViewModel: DetailViewModel = viewModel()
            DetailScreen(
                initialWallpaperId = wallpaperId,
                initialIndex = initialIndex,
                viewModel = detailViewModel,
                onBack = { navController.popBackStack() },
                onOpenAuthor = { authorName ->
                    val encoded = URLEncoder.encode(authorName, "UTF-8")
                    navController.navigate("author/$encoded")
                },
                onOpenRelated = { rel ->
                    navController.navigate("detail/${rel.id}/-1")
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
                    navController.navigate("detail/${wp.id}/-1")
                }
            )
        }

        composable("favorites") {
            val favoritesViewModel: FavoritesViewModel = viewModel()
            FavoritesScreen(
                viewModel = favoritesViewModel,
                onBack = { navController.popBackStack() },
                onWallpaperClick = { wp ->
                    navController.navigate("detail/${wp.id}/-1")
                }
            )
        }
    }
}
