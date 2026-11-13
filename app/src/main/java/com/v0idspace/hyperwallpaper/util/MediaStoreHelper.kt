package com.v0idspace.hyperwallpaper.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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

object MediaStoreHelper {

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

    suspend fun saveWallpaperToGallery(
        context: Context,
        downloadUrl: String,
        title: String,
        hash: String
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(downloadUrl).build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to download master image: HTTP ${response.code}"))
            }

            val body = response.body ?: return@withContext Result.failure(Exception("Image data is empty"))
            val cleanTitle = title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val fileName = "HyperOS_${cleanTitle}_${hash.take(8)}.jpg"

            val contentResolver = context.contentResolver

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/HyperWallpapers")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return@withContext Result.failure(Exception("Could not create MediaStore record"))

                contentResolver.openOutputStream(imageUri)?.use { outStream ->
                    BufferedInputStream(body.byteStream()).use { inStream ->
                        BufferedOutputStream(outStream, 65536).use { bufferedOut ->
                            inStream.copyTo(bufferedOut, bufferSize = 65536)
                            bufferedOut.flush()
                        }
                    }
                }

                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(imageUri, contentValues, null, null)

                Result.success(imageUri)
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val targetDir = File(picturesDir, "HyperWallpapers")
                if (!targetDir.exists()) targetDir.mkdirs()

                val file = File(targetDir, fileName)
                FileOutputStream(file).use { outStream ->
                    BufferedInputStream(body.byteStream()).use { inStream ->
                        BufferedOutputStream(outStream, 65536).use { bufferedOut ->
                            inStream.copyTo(bufferedOut, bufferSize = 65536)
                            bufferedOut.flush()
                        }
                    }
                }

                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DATA, file.absolutePath)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                }
                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: Uri.fromFile(file)

                Result.success(uri)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
