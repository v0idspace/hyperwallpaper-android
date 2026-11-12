package com.v0idspace.hyperwallpaper.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.v0idspace.hyperwallpaper.data.model.Wallpaper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FavoritesRepository(context: Context) {

    private val prefs = context.getSharedPreferences("hyper_favorites", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _favorites = MutableStateFlow<List<Wallpaper>>(loadFavorites())
    val favorites: StateFlow<List<Wallpaper>> = _favorites.asStateFlow()

    private fun loadFavorites(): List<Wallpaper> {
        val json = prefs.getString("favorites_list", null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Wallpaper>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveFavorites(list: List<Wallpaper>) {
        val json = gson.toJson(list)
        prefs.edit().putString("favorites_list", json).apply()
        _favorites.value = list
    }

    fun isFavorite(id: String): Boolean {
        return _favorites.value.any { it.id == id }
    }

    fun toggleFavorite(wallpaper: Wallpaper): Boolean {
        val current = _favorites.value.toMutableList()
        val index = current.indexOfFirst { it.id == wallpaper.id }
        val isNowFavorite = if (index >= 0) {
            current.removeAt(index)
            false
        } else {
            current.add(0, wallpaper)
            true
        }
        saveFavorites(current)
        return isNowFavorite
    }

    fun clearAll() {
        saveFavorites(emptyList())
    }
}
