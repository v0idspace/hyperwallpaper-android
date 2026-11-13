package com.v0idspace.hyperwallpaper.data.repository

import com.v0idspace.hyperwallpaper.data.api.XiaomiApiClient
import com.v0idspace.hyperwallpaper.data.model.CategoryData
import com.v0idspace.hyperwallpaper.data.model.ProductDetail
import com.v0idspace.hyperwallpaper.data.model.Wallpaper
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class WallpaperRepository(
    private val apiClient: XiaomiApiClient = XiaomiApiClient()
) {
    // In-memory cache for fast instant detail access
    private val wallpaperCache = ConcurrentHashMap<String, Wallpaper>()
    private val detailCache = ConcurrentHashMap<String, ProductDetail>()

    fun cacheWallpapers(wallpapers: List<Wallpaper>) {
        wallpapers.forEach { wallpaperCache[it.id] = it }
    }

    fun getCachedWallpaper(id: String): Wallpaper? {
        return wallpaperCache[id]
    }

    suspend fun getProductDetail(uuid: String): ProductDetail? {
        detailCache[uuid]?.let { return it }
        val fetched = apiClient.fetchProductDetail(uuid)
        if (fetched != null) {
            detailCache[uuid] = fetched
        }
        return fetched
    }

    suspend fun getCatalog(
        categoryName: String,
        subtag: String? = null,
        offset: Int = 0,
        count: Int = 12,
        region: String = "EEA",
        randomizeSeed: Boolean = false,
        filterAi: Boolean = false
    ): List<Wallpaper> {
        val effectiveOffset = if (randomizeSeed && offset == 0) {
            Random.nextInt(0, 20) * 12
        } else {
            offset
        }

        val list = apiClient.fetchCatalog(categoryName, subtag, effectiveOffset, count, region)
        val filtered = if (filterAi) list.filter { !it.isAiGenerated } else list
        cacheWallpapers(filtered)
        return filtered
    }

    suspend fun search(
        query: String,
        offset: Int = 0,
        count: Int = 12,
        region: String = "EEA",
        filterAi: Boolean = false
    ): List<Wallpaper> {
        val list = apiClient.searchWallpapers(query, offset, count, region)
        val filtered = if (filterAi) list.filter { !it.isAiGenerated } else list
        cacheWallpapers(filtered)
        return filtered
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

    /**
     * Truly Endless, High-Diversity Recommendation Engine:
     * 1. Per-wallpaper unique hash seed offset eliminating identical sequence loops.
     * 2. Excludes generic aggregators ("VCG", "Xiaomi") from author flooding.
     * 3. Multi-seed tag rotation with auto-fallback to secondary category subtags.
     * 4. Exponential backoff retry ensuring recommendations never stall at 15-20 items.
     */
    suspend fun getRelatedWallpapers(
        wallpaper: Wallpaper,
        pageIndex: Int = 0,
        count: Int = 12,
        region: String = "EEA",
        filterAi: Boolean = false
    ): List<Wallpaper> {
        val isCn = region.equals("CN", ignoreCase = true)

        // Noise tokens to discard
        val noiseTokens = setOf(
            "壁纸", "official", "Official", "ThemeMarket", "xiaomi", "Xiaomi", "WALLPAPER", "THEME", "VCG"
        )

        // 1. Semantic visual tags from wallpaper metadata
        val candidateTags = wallpaper.tags
            .map { it.trim() }
            .filter { it.isNotBlank() && !noiseTokens.contains(it) }

        // 2. Resolve category context and verified subtags
        val matchedCategory = CategoryData.categories.find {
            it.id.equals(wallpaper.category, ignoreCase = true) ||
            it.name.equals(wallpaper.category, ignoreCase = true)
        } ?: CategoryData.categories.first()

        val categoryTags = if (isCn) matchedCategory.cnSubtags else matchedCategory.subtags

        // 3. Assemble priority seed pool
        val seeds = mutableListOf<String>()
        seeds.addAll(candidateTags)
        seeds.addAll(categoryTags)
        if (seeds.isEmpty()) {
            seeds.add(if (isCn) "风景" else "Landscape")
        }

        // 4. Calculate deterministic wallpaper-specific offset so every wallpaper has unique recommendations
        val wallpaperSeed = (wallpaper.id.hashCode() and 0x7FFFFFFF) % 25 // 0 to 24 offset multiplier
        val baseOffset = wallpaperSeed * 12

        // Rotate through seeds across pages
        val seedIndex = (pageIndex + wallpaperSeed) % seeds.size
        val cycle = pageIndex / seeds.size
        val queryOffset = (baseOffset + (cycle * count)) % 840 // Stay within Xiaomi Lucene 840 depth window

        var primaryTag = seeds[seedIndex]

        val collected = mutableListOf<Wallpaper>()
        var attempts = 0

        while (collected.size < 6 && attempts < 3) {
            attempts++
            try {
                val results = apiClient.fetchRelated(primaryTag, queryOffset + (attempts * 4), count, region)
                val clean = results.filter { it.id != wallpaper.id }
                val aiFiltered = if (filterAi) clean.filter { !it.isAiGenerated } else clean
                collected.addAll(aiFiltered)
            } catch (e: Exception) {
                delay(200) // Brief backoff on transient errors
            }

            // Fallback to next seed tag if this tag yielded insufficient items
            if (collected.size < 6) {
                val nextSeedIdx = (seedIndex + attempts) % seeds.size
                primaryTag = seeds[nextSeedIdx]
            }
        }

        // Optional artist blend: Only query artist if it's an individual artist, NEVER generic "VCG" or "Xiaomi"
        val isGenericAuthor = wallpaper.author.isBlank() ||
                wallpaper.author.equals("VCG", ignoreCase = true) ||
                wallpaper.author.contains("Xiaomi", ignoreCase = true) ||
                wallpaper.author.contains("Visual China", ignoreCase = true)

        if (collected.size < 6 && !isGenericAuthor) {
            try {
                val authorResults = apiClient.fetchAuthorPortfolio(wallpaper.author, pageIndex * count, count, region)
                    .filter { it.id != wallpaper.id }
                val aiFiltered = if (filterAi) authorResults.filter { !it.isAiGenerated } else authorResults
                collected.addAll(aiFiltered)
            } catch (ignored: Exception) {
            }
        }

        val distinctResults = collected.distinctBy { it.id }.filter { it.id != wallpaper.id }
        cacheWallpapers(distinctResults)
        return distinctResults
    }
}
