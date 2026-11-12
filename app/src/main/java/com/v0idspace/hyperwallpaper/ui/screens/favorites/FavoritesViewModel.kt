package com.v0idspace.hyperwallpaper.ui.screens.favorites

import androidx.lifecycle.ViewModel
import com.v0idspace.hyperwallpaper.HyperWallpaperApp
import com.v0idspace.hyperwallpaper.data.model.Wallpaper
import com.v0idspace.hyperwallpaper.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.StateFlow

class FavoritesViewModel(
    private val favoritesRepository: FavoritesRepository = HyperWallpaperApp.instance.favoritesRepository
) : ViewModel() {

    val favorites: StateFlow<List<Wallpaper>> = favoritesRepository.favorites

    fun removeFavorite(wallpaper: Wallpaper) {
        favoritesRepository.toggleFavorite(wallpaper)
    }

    fun clearAll() {
        favoritesRepository.clearAll()
    }
}
