package com.v0idspace.hyperwallpaper.data.repository

import com.v0idspace.hyperwallpaper.data.api.XiaomiApiClient
import com.v0idspace.hyperwallpaper.data.model.Wallpaper
import java.util.concurrent.ConcurrentHashMap

class WallpaperRepository(
    private val apiClient: XiaomiApiClient = XiaomiApiClient()
) {
    // In-memory cache for fast instant detail access
    private val wallpaperCache = ConcurrentHashMap<String, Wallpaper>()

    fun cacheWallpapers(wallpapers: List<Wallpaper>) {
        wallpapers.forEach { wallpaperCache[it.id] = it }
    }

    fun getCachedWallpaper(id: String): Wallpaper? {
        return wallpaperCache[id]
    }

    suspend fun getCatalog(
        categoryName: String,
        subtag: String? = null,
        offset: Int = 0,
        count: Int = 12,
        region: String = "EEA"
    ): List<Wallpaper> {
        val list = apiClient.fetchCatalog(categoryName, subtag, offset, count, region)
        cacheWallpapers(list)
        return list
    }

    suspend fun search(
        query: String,
        offset: Int = 0,
        count: Int = 12,
        region: String = "EEA"
    ): List<Wallpaper> {
        val list = apiClient.searchWallpapers(query, offset, count, region)
        cacheWallpapers(list)
        return list
    }

    suspend fun getAuthorPortfolio(
        authorName: String,
        offset: Int = 0,
        count: Int = 12,
        region: String = "EEA"
    ): List<Wallpaper> {
        val list = apiClient.fetchAuthorPortfolio(authorName, offset, count, region)
        cacheWallpapers(list)
        return list
    }

    suspend fun getRelatedWallpapers(
        wallpaper: Wallpaper,
        offset: Int = 0,
        count: Int = 12,
        region: String = "EEA"
    ): List<Wallpaper> {
        // Find best recommendation tag: first non-generic tag, or fallback to author or category
        val recommendationTag = wallpaper.tags.firstOrNull { it != "VCG" && it != "Xiaomi" && it.isNotBlank() }
            ?: wallpaper.tags.firstOrNull { it.isNotBlank() }
            ?: wallpaper.category

        val list = apiClient.fetchRelated(recommendationTag, offset, count, region)
            .filter { it.id != wallpaper.id }
        cacheWallpapers(list)
        return list
    }
}
