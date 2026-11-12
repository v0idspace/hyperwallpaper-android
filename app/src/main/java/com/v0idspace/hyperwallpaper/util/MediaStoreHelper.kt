package com.v0idspace.hyperwallpaper.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

object MediaStoreHelper {

    private val httpClient = OkHttpClient()

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
                return@withContext Result.failure(Exception("Failed to download master image: ${response.code}"))
            }

            val inputStream = response.body?.byteStream()
                ?: return@withContext Result.failure(Exception("Image data is empty"))

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

                contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                    inputStream.copyTo(outputStream)
                }

                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(imageUri, contentValues, null, null)

                Result.success(imageUri)
            } else {
                // Android 9 and below fallback
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val targetDir = File(picturesDir, "HyperWallpapers")
                if (!targetDir.exists()) targetDir.mkdirs()

                val file = File(targetDir, fileName)
                FileOutputStream(file).use { output ->
                    inputStream.copyTo(output)
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
