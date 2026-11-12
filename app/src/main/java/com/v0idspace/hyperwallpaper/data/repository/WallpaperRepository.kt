package com.v0idspace.hyperwallpaper.data.repository

import com.v0idspace.hyperwallpaper.data.api.XiaomiApiClient
import com.v0idspace.hyperwallpaper.data.model.Wallpaper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class WallpaperRepository(
    private val apiClient: XiaomiApiClient = XiaomiApiClient()
) {
    // In-memory cache for fast instant detail access
    private val wallpaperCache = ConcurrentHashMap<String, Wallpaper>()

    // Current active catalog items for horizontal swiping in detail view
    private val _currentCatalogWallpapers = MutableStateFlow<List<Wallpaper>>(emptyList())
    val currentCatalogWallpapers: StateFlow<List<Wallpaper>> = _currentCatalogWallpapers.asStateFlow()

    fun updateCurrentCatalogList(list: List<Wallpaper>) {
        _currentCatalogWallpapers.value = list
        cacheWallpapers(list)
    }

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
        region: String = "EEA",
        randomizeSeed: Boolean = false
    ): List<Wallpaper> {
        val effectiveOffset = if (randomizeSeed && offset == 0) {
            // Pick a random page within the 840-item Lucene window to explore true catalog depth
            Random.nextInt(0, 20) * 12
        } else {
            offset
        }

        val list = apiClient.fetchCatalog(categoryName, subtag, effectiveOffset, count, region)
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
        pageIndex: Int = 0,
        count: Int = 12,
        region: String = "EEA"
    ): List<Wallpaper> {
        val isCn = region.equals("CN", ignoreCase = true)

        // Step 1: Find high-quality recommendation seeds
        // Filter out generic boilerplate tags
        val candidateTags = wallpaper.tags.filter { tag ->
            val t = tag.trim()
            t.isNotBlank() &&
                    !t.equals("VCG", ignoreCase = true) &&
                    !t.equals("Xiaomi", ignoreCase = true) &&
                    !t.equals("Featured", ignoreCase = true) &&
                    !t.equals("壁纸", ignoreCase = true) &&
                    !t.equals("Official", ignoreCase = true)
        }

        // Step 2: Select distinct tag based on pageIndex to ensure diverse endless discovery
        val chosenTag: String = if (candidateTags.isNotEmpty()) {
            candidateTags[pageIndex % candidateTags.size]
        } else {
            // If wallpaper has no semantic tags, extract key words from title or fallback to author
            val words = wallpaper.title.split(" ")
                .filter { it.length > 3 && !it.equals("with", ignoreCase = true) && !it.equals("that", ignoreCase = true) }
            if (words.isNotEmpty()) {
                words[pageIndex % words.size]
            } else if (isCn) {
                listOf("自然", "风景", "暗黑", "星空", "极简", "插画")[pageIndex % 6]
            } else {
                wallpaper.category
            }
        }

        // Step 3: Compute an offset that avoids repeating index 0 of the general category
        // Each pageIndex advances through the tag's Lucene results
        val queryOffset = (pageIndex * count) + (if (pageIndex == 0) (1..5).random() * count else 0)

        val results = apiClient.fetchRelated(chosenTag, queryOffset, count, region)
            .filter { it.id != wallpaper.id } // Strictly exclude the current wallpaper

        cacheWallpapers(results)
        return results
    }
}
