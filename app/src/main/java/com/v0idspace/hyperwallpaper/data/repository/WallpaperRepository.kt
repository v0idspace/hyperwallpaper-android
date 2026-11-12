package com.v0idspace.hyperwallpaper.data.repository

import com.v0idspace.hyperwallpaper.data.api.XiaomiApiClient
import com.v0idspace.hyperwallpaper.data.model.Wallpaper
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

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

        // 1. Gather all candidate semantic tags, filtering out boilerplate
        val meaningfulTags = wallpaper.tags.filter { tag ->
            val t = tag.trim()
            t.isNotBlank() &&
                    !t.equals("VCG", ignoreCase = true) &&
                    !t.equals("Xiaomi", ignoreCase = true) &&
                    !t.equals("Featured", ignoreCase = true) &&
                    !t.equals("壁纸", ignoreCase = true) &&
                    !t.equals("Official", ignoreCase = true) &&
                    !t.equals("Nature", ignoreCase = true) && // Avoid repeating default root
                    !t.equals("自然", ignoreCase = true)
        }

        // 2. Extract meaningful keywords from the title if available
        val titleWords = wallpaper.title.split(Regex("[\\s,._-]+"))
            .filter { word ->
                word.length > 3 &&
                        !word.equals("wallpaper", ignoreCase = true) &&
                        !word.equals("hyperos", ignoreCase = true) &&
                        !word.equals("miui", ignoreCase = true) &&
                        !word.equals("that", ignoreCase = true) &&
                        !word.equals("with", ignoreCase = true) &&
                        !word.equals("from", ignoreCase = true)
            }

        // 3. Build prioritized list of recommendation query seeds
        val querySeeds = mutableListOf<String>()
        querySeeds.addAll(meaningfulTags)
        querySeeds.addAll(titleWords)
        if (querySeeds.isEmpty()) {
            if (isCn) {
                querySeeds.addAll(listOf("风景", "暗黑", "星空", "极简", "插画", "动漫", "治愈", "壁纸"))
            } else {
                querySeeds.addAll(listOf("Landscape", "Scenery", "Forest", "Mountain", "Sky", "Dark", "Space", "Art"))
            }
        }

        // 4. Select query tag for this page
        val targetTag = querySeeds[pageIndex % querySeeds.size]

        // 5. Calculate query offset so we don't always pull the same top 12 items
        val seed = (wallpaper.id.hashCode() and 0x7FFFFFFF) % 10
        val queryOffset = (pageIndex * count) + (seed * 6)

        var results = apiClient.fetchRelated(targetTag, queryOffset, count, region)
            .filter { it.id != wallpaper.id }

        // Fallback: If tag yielded too few items, query author's works or next seed
        if (results.size < 4 && wallpaper.author.isNotBlank() && !wallpaper.author.contains("Xiaomi", ignoreCase = true)) {
            val authorResults = apiClient.fetchAuthorPortfolio(wallpaper.author, pageIndex * count, count, region)
                .filter { it.id != wallpaper.id }
            results = (results + authorResults).distinctBy { it.id }
        }

        cacheWallpapers(results)
        return results
    }
}
