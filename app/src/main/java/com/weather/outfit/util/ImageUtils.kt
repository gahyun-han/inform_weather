package com.weather.outfit.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageUtils {

    private const val MAX_IMAGE_SIZE = 1024  // max dimension in pixels
    private const val JPEG_QUALITY = 85

    /**
     * Creates a temporary file for camera capture.
     */
    fun createImageFile(context: Context): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("CLOTHING_${timestamp}_", ".jpg", storageDir)
    }

    /**
     * Gets the content URI for a file using FileProvider.
     */
    fun getFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Saves a bitmap to the app's private storage and returns the file path.
     */
    fun saveBitmapToStorage(context: Context, bitmap: Bitmap, fileName: String): String {
        val scaled = scaleBitmapIfNeeded(bitmap)
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: context.filesDir
        val file = File(directory, "$fileName.jpg")
        FileOutputStream(file).use { stream ->
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)
        }
        return file.absolutePath
    }

    /**
     * Loads and decodes a bitmap from a URI, downsampling if necessary.
     */
    fun decodeBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                // First, get dimensions without loading full bitmap
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(stream, null, options)

                // Calculate sample size
                val sampleSize = calculateSampleSize(options.outWidth, options.outHeight)

                // Load with sample size
                context.contentResolver.openInputStream(uri)?.use { stream2 ->
                    val decodeOptions = BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                    }
                    BitmapFactory.decodeStream(stream2, null, decodeOptions)
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Copies an image from a URI to app's private storage.
     * Returns the new file path.
     */
    fun copyImageToStorage(context: Context, uri: Uri, fileName: String): String? {
        val bitmap = decodeBitmapFromUri(context, uri) ?: return null
        return saveBitmapToStorage(context, bitmap, fileName)
    }

    private fun scaleBitmapIfNeeded(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= MAX_IMAGE_SIZE && height <= MAX_IMAGE_SIZE) return bitmap

        val ratio = minOf(
            MAX_IMAGE_SIZE.toFloat() / width,
            MAX_IMAGE_SIZE.toFloat() / height
        )
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun calculateSampleSize(width: Int, height: Int): Int {
        var sampleSize = 1
        while (width / sampleSize > MAX_IMAGE_SIZE || height / sampleSize > MAX_IMAGE_SIZE) {
            sampleSize *= 2
        }
        return sampleSize
    }

    /**
     * Downloads an image from a URL and saves it to app storage. Returns the file path.
     * Must be called from a background coroutine (does network I/O).
     */
    fun downloadImageFromUrl(context: Context, url: String, fileName: String): String? {
        return try {
            val bitmap = URL(url).openStream().use { BitmapFactory.decodeStream(it) }
                ?: return null
            saveBitmapToStorage(context, bitmap, fileName)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Deletes an image file from storage.
     */
    fun deleteImage(path: String) {
        try {
            File(path).delete()
        } catch (e: Exception) {
            // Ignore
        }
    }
}
