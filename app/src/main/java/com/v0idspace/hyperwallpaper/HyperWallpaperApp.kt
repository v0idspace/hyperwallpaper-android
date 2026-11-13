package com.v0idspace.hyperwallpaper

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.v0idspace.hyperwallpaper.data.repository.FavoritesRepository
import com.v0idspace.hyperwallpaper.data.repository.WallpaperRepository
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import kotlin.math.pow

class HyperWallpaperApp : Application(), ImageLoaderFactory {

    lateinit var wallpaperRepository: WallpaperRepository
        private set

    lateinit var favoritesRepository: FavoritesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        wallpaperRepository = WallpaperRepository()
        favoritesRepository = FavoritesRepository(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient {
                OkHttpClient.Builder()
                    .connectionPool(ConnectionPool(32, 5, TimeUnit.MINUTES))
                    .connectTimeout(6, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(6, TimeUnit.SECONDS)
                    .addInterceptor(Interceptor { chain ->
                        val request = chain.request().newBuilder()
                            .header("User-Agent", "MiuiTheme/2.2.0 (Linux; U; Android 14; Mobile; en-US)")
                            .header("Accept", "image/webp,image/*,*/*")
                            .build()

                        var response = try {
                            chain.proceed(request)
                        } catch (e: Exception) {
                            null
                        }

                        // Exponential backoff retry loop for high-speed CDN streaming
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
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.35) // 35% RAM cache for immediate swipe hits
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(500L * 1024 * 1024) // 500 MB persistent disk cache
                    .build()
            }
            .respectCacheHeaders(false) // Zero CDN revalidation latency
            .crossfade(200)
            .build()
    }

    companion object {
        lateinit var instance: HyperWallpaperApp
            private set
    }
}
