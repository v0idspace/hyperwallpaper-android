package com.v0idspace.hyperwallpaper.data.api

import com.google.gson.Gson
import com.v0idspace.hyperwallpaper.data.model.CategoryData
import com.v0idspace.hyperwallpaper.data.model.ProductDetail
import com.v0idspace.hyperwallpaper.data.model.Wallpaper
import com.v0idspace.hyperwallpaper.data.model.XiaomiApiResponse
import com.v0idspace.hyperwallpaper.data.model.XiaomiProductDetailResponse
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
        .connectionPool(ConnectionPool(24, 5, TimeUnit.MINUTES))
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(6, TimeUnit.SECONDS)
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

            var tryCount = 0
            while ((response == null || !response.isSuccessful) && tryCount < 3) {
                tryCount++
                try {
                    val backoffMs = (150 * 2.0.pow(tryCount)).toLong()
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

        val isAll = subtag.isNullOrBlank() || 
                    subtag.startsWith("All", ignoreCase = true) || 
                    subtag.equals("全部", ignoreCase = true) || 
                    subtag.startsWith("全部")

        val activeTag: String
        val activeOffset: Int

        if (!isAll) {
            val rawTag = subtag!!
            activeTag = if (isCn) mapToChineseTag(rawTag) else rawTag
            activeOffset = offset
        } else {
            // Apply taxonomy chaining across verified subtags to bypass Lucene 840 item ceiling
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
        val trimmed = query.trim()
        val isCn = region.equals("CN", ignoreCase = true)
        val effectiveQuery = if (isCn) mapToChineseTag(trimmed) else trimmed

        val encodedQuery = URLEncoder.encode(effectiveQuery, "UTF-8")
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
        val isCn = region.equals("CN", ignoreCase = true)
        val effectiveTag = if (isCn) mapToChineseTag(tag.trim()) else tag.trim()

        val encodedTag = URLEncoder.encode(effectiveTag, "UTF-8")
        val url = "https://api.zhuti.xiaomi.com/app/v9/uipages/search/tag" +
                "?cardCount=$count&type=WALLPAPER&tag=$encodedTag&cardStart=$offset&region=$region"

        executeAndParse(url, tag)
    }

    suspend fun fetchProductDetail(uuid: String): ProductDetail? = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.zhuti.xiaomi.com/app/v9/products/detail/$uuid"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val parsed = gson.fromJson(body, XiaomiProductDetailResponse::class.java)
                val ad = parsed.apiData ?: return@withContext null

                val title = ad.name?.takeIf { it.isNotBlank() } ?: "Xiaomi HyperOS Wallpaper"
                val designer = ad.designerName?.takeIf { it.isNotBlank() } ?: "Xiaomi Official"
                val rawTags = ad.tags.orEmpty()
                val parsedTags = rawTags.split(";").map { it.trim() }.filter { it.isNotBlank() }
                val rawBytes = ad.fileSize ?: 0L
                val count = ad.productCount ?: 0

                val primaryTheme = parsedTags.firstOrNull() ?: "Mobile Art"
                val secondaryThemes = if (parsedTags.size > 1) parsedTags.drop(1).take(3).joinToString(", ") else "minimalist styling"

                val story = "Officially cataloged in the Xiaomi Global Theme Store, \"$title\" is an authentic wallpaper created by $designer" +
                        (if (count > 0) " ($count wallpapers published in official store portfolio)." else ".") +
                        " The aesthetic tone centers around $primaryTheme with accents of $secondaryThemes. " +
                        "Tailored with a 20:9 portrait aspect ratio, dynamic contrast is engineered specifically for lockscreen clocks, Always-On-Display (AOD), and homescreen app grids."

                val formattedSize = if (rawBytes > 1024 * 1024) {
                    String.format("%.2f MB Master", rawBytes / (1024.0 * 1024.0))
                } else if (rawBytes > 1024) {
                    String.format("%.1f KB Master", rawBytes / 1024.0)
                } else {
                    "HQ Master"
                }

                ProductDetail(
                    id = uuid,
                    name = title,
                    designerName = designer,
                    designerIcon = ad.designerIcon.orEmpty(),
                    designerMiId = ad.designerMiId.orEmpty(),
                    productCount = count,
                    tags = parsedTags,
                    fileSizeBytes = rawBytes,
                    formattedFileSize = formattedSize,
                    editorialStory = story
                )
            }
        } catch (e: Exception) {
            null
        }
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
                    
                    val rawTitle = p.name?.takeIf { it.isNotBlank() } ?: p.title?.takeIf { it.isNotBlank() } ?: "HyperOS Wallpaper"
                    val rawAuthor = p.authorName?.takeIf { it.isNotBlank() } ?: p.author?.takeIf { it.isNotBlank() } ?: p.designer?.takeIf { it.isNotBlank() } ?: "Xiaomi Official"
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

                    val fileSize = p.fileSizeInKB ?: 0L

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
                        fileSizeBytes = fileSize,
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

    private fun mapToChineseTag(englishTag: String): String {
        val lower = englishTag.lowercase().trim()
        return when {
            "nature" in lower || "forest" in lower || "mountain" in lower -> "自然"
            "landscape" in lower || "scenery" in lower || "sunset" in lower -> "风景"
            "dark" in lower || "amoled" in lower || "black" in lower || "night" in lower -> "暗黑"
            "space" in lower || "galaxy" in lower || "star" in lower || "cosmic" in lower -> "星空"
            "minimal" in lower || "simple" in lower || "clean" in lower -> "极简"
            "city" in lower || "urban" in lower || "architecture" in lower -> "城市"
            "anime" in lower || "art" in lower || "illustration" in lower || "drawing" in lower -> "动漫"
            "abstract" in lower || "3d" in lower || "fluid" in lower || "gradient" in lower -> "抽象"
            "cyberpunk" in lower || "tech" in lower || "future" in lower -> "机械"
            "car" in lower || "auto" in lower || "vehicle" in lower -> "汽车"
            "ocean" in lower || "sea" in lower || "water" in lower || "beach" in lower -> "海洋"
            else -> englishTag
        }
    }
}
