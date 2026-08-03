package com.woowacourse.runpamine.presentation.share

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal suspend fun saveRunShareBitmap(
    context: Context,
    bitmap: Bitmap,
): Uri =
    withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val displayName = "runpamine_${FILE_TIME_FORMATTER.format(LocalDateTime.now())}.jpg"
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
        val values =
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.MIME_TYPE, JPEG_MIME_TYPE)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Runpamine")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
        val uri = resolver.insert(collection, values) ?: error("Failed to create image entry")

        try {
            resolver.openOutputStream(uri)?.use { stream ->
                check(bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)) {
                    "Failed to encode image"
                }
            } ?: error("Failed to open image stream")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            uri
        } catch (error: Throwable) {
            resolver.delete(uri, null, null)
            throw error
        }
    }

internal fun shareRunShareImage(
    context: Context,
    uri: Uri,
) {
    val shareIntent =
        Intent(Intent.ACTION_SEND).apply {
            type = JPEG_MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    context.startActivity(Intent.createChooser(shareIntent, "러닝 사진 공유"))
}

internal suspend fun cacheRunShareBitmap(
    context: Context,
    bitmap: Bitmap,
): Uri =
    withContext(Dispatchers.IO) {
        val shareDirectory = File(context.cacheDir, SHARE_CACHE_DIRECTORY).apply { mkdirs() }
        shareDirectory.listFiles()?.forEach(File::delete)
        val file = File(shareDirectory, SHARE_CACHE_FILE_NAME)
        file.outputStream().use { stream ->
            check(bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)) {
                "Failed to encode shared image"
            }
        }
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }

private const val JPEG_MIME_TYPE = "image/jpeg"
private const val JPEG_QUALITY = 95
private const val SHARE_CACHE_DIRECTORY = "run_share"
private const val SHARE_CACHE_FILE_NAME = "runpamine_share.jpg"
private val FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
