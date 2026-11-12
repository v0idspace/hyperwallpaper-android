package com.v0idspace.hyperwallpaper.data.api

import com.google.gson.Gson
import com.v0idspace.hyperwallpaper.data.model.CategoryData
import com.v0idspace.hyperwallpaper.data.model.Wallpaper
import com.v0idspace.hyperwallpaper.data.model.XiaomiApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlin.math.pow

class XiaomiApiClient {

    private val gson = Gson()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectionPool(ConnectionPool(10, 5, TimeUnit.MINUTES))
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .addInterceptor(Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "MiuiTheme/2.2.0 (Linux; U; Android 14; Mobile; en-US)")
                .header("Accept", "application/json")
                .build()

            var response = try {
                chain.proceed(request)
            } catch (e: Exception) {
                null
            }

            // Retry loop with exponential backoff for transient 502/network drops
            var tryCount = 0
            while ((response == null || !response.isSuccessful) && tryCount < 3) {
                tryCount++
                try {
                    val backoffMs = (250 * 2.0.pow(tryCount)).toLong()
                    Thread.sleep(backoffMs)
                    response?.close()
                    response = chain.proceed(request)
                } catch (ignored: Exception) {
                }
            }

            response ?: chain.proceed(request)
        })
        .build()

    suspend fun fetchCatalog(
        categoryName: String,
        subtag: String? = null,
        offset: Int = 0,
        count: Int = 12,
        region: String = "EEA"
    ): List<Wallpaper> = withContext(Dispatchers.IO) {
        val cat = CategoryData.categories.find { 
            it.id.equals(categoryName, ignoreCase = true) || it.name.equals(categoryName, ignoreCase = true) 
        } ?: CategoryData.categories.first()

        val isCn = region.equals("CN", ignoreCase = true)
        val subtags = if (isCn) cat.cnSubtags else cat.subtags

        val activeTag: String
        val activeOffset: Int

        if (!subtag.isNullOrBlank() && !subtag.startsWith("All", ignoreCase = true)) {
            activeTag = subtag
            activeOffset = offset
        } else {
            // Apply taxonomy chaining to bypass Lucene 840 item ceiling
            val tagIndex = (offset / 840) % subtags.size
            activeTag = subtags[tagIndex]
            activeOffset = offset % 840
        }

        val encodedTag = URLEncoder.encode(activeTag, "UTF-8")
        val url = "https://api.zhuti.xiaomi.com/app/v9/uipages/search/tag" +
                "?cardCount=$count&type=WALLPAPER&tag=$encodedTag&cardStart=$activeOffset&region=$region"

        executeAndParse(url, cat.name)
    }

    suspend fun searchWallpapers(
        query: String,
        offset: Int = 0,
        count: Int = 12,
        region: String = "EEA"
    ): List<Wallpaper> = withContext(Dispatchers.IO) {
        val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
        val url = "https://api.zhuti.xiaomi.com/app/v9/uipages/search/tag" +
                "?cardCount=$count&type=WALLPAPER&tag=$encodedQuery&cardStart=$offset&region=$region"

        executeAndParse(url, query)
    }

    suspend fun fetchAuthorPortfolio(
        authorName: String,
        offset: Int = 0,
        count: Int = 12,
        region: String = "EEA"
    ): List<Wallpaper> = withContext(Dispatchers.IO) {
        val encodedAuthor = URLEncoder.encode(authorName.trim(), "UTF-8")
        val url = "https://api.zhuti.xiaomi.com/app/v9/uipages/search/tag" +
                "?cardCount=$count&type=WALLPAPER&tag=$encodedAuthor&cardStart=$offset&region=$region"

        executeAndParse(url, "Portfolio: $authorName")
    }

    suspend fun fetchRelated(
        tag: String,
        offset: Int = 0,
        count: Int = 12,
        region: String = "EEA"
    ): List<Wallpaper> = withContext(Dispatchers.IO) {
        val encodedTag = URLEncoder.encode(tag.trim(), "UTF-8")
        val url = "https://api.zhuti.xiaomi.com/app/v9/uipages/search/tag" +
                "?cardCount=$count&type=WALLPAPER&tag=$encodedTag&cardStart=$offset&region=$region"

        executeAndParse(url, tag)
    }

    private fun executeAndParse(url: String, defaultCategory: String): List<Wallpaper> {
        return try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val parsed = gson.fromJson(body, XiaomiApiResponse::class.java)

                val products = parsed.apiData?.cards?.firstOrNull()?.products ?: return emptyList()

                products.mapNotNull { p ->
                    val imgUrl = p.imageUrl.orEmpty()
                    val origUrl = p.originalImageUrl?.takeIf { it.isNotBlank() } ?: imgUrl
                    if (imgUrl.isBlank() && origUrl.isBlank()) return@mapNotNull null

                    val hash = extractHash(origUrl.ifBlank { imgUrl })
                    
                    // True wallpaper title and author extraction
                    val rawTitle = p.name?.takeIf { it.isNotBlank() } ?: p.title?.takeIf { it.isNotBlank() } ?: "HyperOS Wallpaper"
                    val rawAuthor = p.authorName?.takeIf { it.isNotBlank() } ?: p.author?.takeIf { it.isNotBlank() } ?: p.designer?.takeIf { it.isNotBlank() } ?: "Xiaomi Creator"
                    val wallpaperId = p.uuid?.takeIf { it.isNotBlank() } ?: p.productUuid?.takeIf { it.isNotBlank() } ?: p.id ?: hash.ifBlank { java.util.UUID.randomUUID().toString() }

                    val thumb = if (hash.isNotBlank()) {
                        "https://t17.market.mi-img.com/thumbnail/webp/w480q80/ThemeMarket/$hash"
                    } else imgUrl

                    val prev = if (hash.isNotBlank()) {
                        "https://t17.market.mi-img.com/thumbnail/webp/w1080/ThemeMarket/$hash"
                    } else origUrl

                    val download = if (hash.isNotBlank()) {
                        "https://t17.market.mi-img.com/download/ThemeMarket/$hash"
                    } else origUrl

                    // Merge semantic metaTags, innerTags, and tags
                    val combinedTags = mutableListOf<String>()
                    p.metaTags?.forEach { if (it.isNotBlank() && !combinedTags.contains(it)) combinedTags.add(it) }
                    p.innerTags?.forEach { if (it.isNotBlank() && !combinedTags.contains(it)) combinedTags.add(it) }
                    p.tags?.forEach { if (it.isNotBlank() && !combinedTags.contains(it)) combinedTags.add(it) }
                    if (combinedTags.isEmpty()) combinedTags.add(defaultCategory)

                    Wallpaper(
                        id = wallpaperId,
                        title = rawTitle,
                        author = rawAuthor,
                        thumbnailUrl = thumb,
                        previewUrl = prev,
                        downloadUrl = download,
                        hash = hash,
                        tags = combinedTags,
                        category = defaultCategory,
                        downloadCount = p.downloadCount ?: 0L,
                        width = 1080,
                        height = 2400,
                        verifiedStudio = rawAuthor.contains("VCG", ignoreCase = true) ||
                                rawAuthor.contains("Xiaomi", ignoreCase = true) ||
                                rawAuthor.contains("Studio", ignoreCase = true) ||
                                rawAuthor.contains("Design", ignoreCase = true)
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun extractHash(url: String): String {
        return when {
            "/ThemeMarket/" in url -> url.substringAfter("/ThemeMarket/").substringBefore("?")
            "/" in url -> url.substringAfterLast("/").substringBefore("?")
            else -> ""
        }
    }
}
