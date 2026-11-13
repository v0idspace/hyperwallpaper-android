package com.v0idspace.hyperwallpaper.util

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import kotlin.math.pow

enum class WallpaperTarget {
    HOME,
    LOCK,
    BOTH
}

object WallpaperManagerHelper {

    private val httpClient = OkHttpClient.Builder()
        .connectionPool(ConnectionPool(10, 5, TimeUnit.MINUTES))
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "MiuiTheme/2.2.0 (Linux; U; Android 14; Mobile; en-US)")
                .header("Accept", "*/*")
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
                    val backoffMs = (200 * 2.0.pow(tryCount)).toLong()
                    Thread.sleep(backoffMs)
                    response?.close()
                    response = chain.proceed(request)
                } catch (ignored: Exception) {
                }
            }

            response ?: chain.proceed(request)
        })
        .build()

    suspend fun setWallpaperDirect(
        context: Context,
        imageUrl: String,
        target: WallpaperTarget
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(imageUrl).build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${response.code} downloading image"))
            }

            val inputStream = response.body?.byteStream()
                ?: return@withContext Result.failure(Exception("Empty image body"))

            val bitmap = BitmapFactory.decodeStream(BufferedInputStream(inputStream, 65536))
                ?: return@withContext Result.failure(Exception("Failed to decode image bitmap"))

            val wallpaperManager = WallpaperManager.getInstance(context)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val which = when (target) {
                    WallpaperTarget.HOME -> WallpaperManager.FLAG_SYSTEM
                    WallpaperTarget.LOCK -> WallpaperManager.FLAG_LOCK
                    WallpaperTarget.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                }
                wallpaperManager.setBitmap(bitmap, null, true, which)
            } else {
                wallpaperManager.setBitmap(bitmap)
            }

            bitmap.recycle()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun launchSystemCropPicker(
        context: Context,
        imageUrl: String
    ): Result<Intent> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(imageUrl).build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext Result.failure(Exception("Download failed"))

            val cacheFile = File(context.cacheDir, "crop_preview_${System.currentTimeMillis()}.jpg")
            response.body?.byteStream()?.use { input ->
                BufferedOutputStream(FileOutputStream(cacheFile), 65536).use { output ->
                    input.copyTo(output, bufferSize = 65536)
                    output.flush()
                }
            }

            // Secure URI via FileProvider to avoid FileUriExposedException on Android 7+
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val intent = Intent(WallpaperManager.ACTION_CROP_AND_SET_WALLPAPER).apply {
                setDataAndType(uri, "image/jpeg")
                putExtra("mimeType", "image/jpeg")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            Result.success(intent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
