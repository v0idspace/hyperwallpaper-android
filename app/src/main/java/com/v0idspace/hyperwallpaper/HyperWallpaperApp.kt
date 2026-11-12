package com.v0idspace.hyperwallpaper

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.v0idspace.hyperwallpaper.data.repository.FavoritesRepository
import com.v0idspace.hyperwallpaper.data.repository.WallpaperRepository
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

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
                    .connectionPool(ConnectionPool(12, 5, TimeUnit.MINUTES))
                    .connectTimeout(6, TimeUnit.SECONDS)
                    .readTimeout(8, TimeUnit.SECONDS)
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .header("User-Agent", "MiuiTheme/2.2.0 (Linux; U; Android 14; Mobile; en-US)")
                            .build()
                        chain.proceed(request)
                    }
                    .build()
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.30) // 30% memory cache
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(350L * 1024 * 1024) // 350 MB disk cache
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(150)
            .build()
    }

    companion object {
        lateinit var instance: HyperWallpaperApp
            private set
    }
}
