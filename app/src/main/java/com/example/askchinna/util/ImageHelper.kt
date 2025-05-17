/**
 * File: app/src/main/java/com/example/askchinna/util/ImageHelper.kt
 * Copyright (c) 2025 askChinna
 * Created: April 29, 2025
 * Updated: May 4, 2025
 * Version: 1.1
 */

package com.example.askchinna.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.util.Log
import android.util.LruCache
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import androidx.core.graphics.scale
import androidx.core.graphics.createBitmap


/**
 * Helper class for image processing operations
 * Optimized for low-end Android devices with minimal memory usage
 */
@Singleton
class ImageHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "ImageHelper"
        private const val TEMP_IMG_PREFIX = "ASKCHINNA_IMG_"
        private const val TEMP_IMG_SUFFIX = ".jpg"
        private const val DATE_FORMAT = "yyyyMMdd_HHmmss"
        private const val MEMORY_CACHE_SIZE = 1024 * 1024 * 10 // 10MB
        private const val DISK_CACHE_SIZE = 1024 * 1024 * 50L // 50MB
        private const val DISK_CACHE_DIR = "image_cache"
        private const val MAX_IMAGE_SIZE = 1920 // Max width or height for processed images
    }

    // Memory cache for bitmap images
    private val memoryCache: LruCache<String, Bitmap> = object : LruCache<String, Bitmap>(MEMORY_CACHE_SIZE) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
        
        override fun entryRemoved(evicted: Boolean, key: String, oldValue: Bitmap, newValue: Bitmap?) {
            if (evicted) {
                // When bitmap is evicted from memory cache, recycle it
                if (!oldValue.isRecycled) {
                    oldValue.recycle()
                }
            }
        }
    }
    
    // Disk cache directory
    private val diskCacheDir: File by lazy {
        File(context.cacheDir, DISK_CACHE_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    // Cache management methods
    private fun getBitmapFromMemCache(key: String): Bitmap? {
        return memoryCache.get(key)
    }
    
    private fun addBitmapToMemCache(key: String, bitmap: Bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap)
        }
    }
    
    private fun getBitmapFromDiskCache(key: String): File? {
        val filename = key.hashCode().toString()
        val file = File(diskCacheDir, filename)
        return if (file.exists()) file else null
    }
    
    private fun addBitmapToDiskCache(key: String, bitmap: Bitmap) {
        val filename = key.hashCode().toString()
        val file = File(diskCacheDir, filename)
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving to disk cache", e)
        }
    }
    
    private fun saveBitmapToFile(bitmap: Bitmap, quality: Int): File? {
        return try {
            val file = createTempImageFile()
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error saving bitmap to file", e)
            null
        }
    }
    
    // Clear caches when memory is low
    fun clearCaches() {
        memoryCache.evictAll()
        diskCacheDir.listFiles()?.forEach { it.delete() }
    }
    
    /**
     * Loads image progressively - first low quality, then high quality
     * @param imageUri URI of the image to load
     * @param callback Callback for progressive loading
     */
    fun loadImageProgressively(imageUri: Uri, callback: (Bitmap?, Boolean) -> Unit) {
        // Load low quality version first
        try {
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 8 // Very low quality for quick preview
                    inPreferredConfig = Bitmap.Config.RGB_565
                }
                val lowQualityBitmap = BitmapFactory.decodeStream(inputStream, null, options)
                callback(lowQualityBitmap, false) // false = not final quality
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading low quality image", e)
        }
        
        // Load high quality version
        try {
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                val bitmap = decodeSampledBitmapFromStream(inputStream)
                val rotatedBitmap = rotateImageIfRequired(bitmap, imageUri)
                callback(rotatedBitmap, true) // true = final quality
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading high quality image", e)
            callback(null, true)
        }
    }

    /**
     * Creates a temporary file for storing camera images
     * @return File object for the temporary file
     */
    fun createTempImageFile(): File {
        val timeStamp = SimpleDateFormat(DATE_FORMAT, Locale.US).format(Date())
        val fileName = TEMP_IMG_PREFIX + timeStamp + TEMP_IMG_SUFFIX
        val storageDir = File(context.filesDir, Constants.TEMP_IMAGE_PATH)

        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        return File(storageDir, fileName)
    }

    /**
     * Compress image for upload, optimized for low-end devices
     * @param imageUri URI of the image to compress
     * @param quality Compression quality (0-100)
     * @return File containing the compressed image
     */
    fun compressImage(imageUri: Uri, quality: Int = Constants.IMAGE_QUALITY_COMPRESSION): File? {
        return try {
            val cacheKey = imageUri.toString()
            
            // Check memory cache first
            val cachedBitmap = getBitmapFromMemCache(cacheKey)
            if (cachedBitmap != null) {
                return saveBitmapToFile(cachedBitmap, quality)
            }
            
            // Check disk cache
            val diskCachedFile = getBitmapFromDiskCache(cacheKey)
            if (diskCachedFile?.exists() == true) {
                return diskCachedFile
            }
            
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return null
            val bitmap = decodeSampledBitmapFromStream(inputStream)
            inputStream.close()

            // Rotate bitmap if needed based on EXIF data
            val rotatedBitmap = rotateImageIfRequired(bitmap, imageUri)
            
            // Add to memory cache
            addBitmapToMemCache(cacheKey, rotatedBitmap)

            // Create compressed file
            val compressedFile = createTempImageFile()
            val outputStream = FileOutputStream(compressedFile)

            // Use ByteArrayOutputStream first to avoid OOM on low-end devices
            val byteArrayOutputStream = ByteArrayOutputStream()
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
            outputStream.write(byteArrayOutputStream.toByteArray())

            // Clean up
            outputStream.flush()
            outputStream.close()
            rotatedBitmap.recycle()
            byteArrayOutputStream.close()

            compressedFile
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image: ${e.message}")
            null
        }
    }

    /**
     * Calculates optimal sample size for loading large bitmaps efficiently
     * @param options BitmapFactory.Options with outHeight and outWidth set
     * @param reqWidth Required width
     * @param reqHeight Required height
     * @return Calculated sample size for decoding
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int = Constants.MIN_IMAGE_RESOLUTION_WIDTH,
        reqHeight: Int = Constants.MIN_IMAGE_RESOLUTION_HEIGHT
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Decode a sampled bitmap from input stream to prevent OOM errors
     * @param inputStream Input stream from image file
     * @return Decoded bitmap at appropriate sample size
     */
    private fun decodeSampledBitmapFromStream(inputStream: InputStream): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        // Reset the stream position
        inputStream.mark(inputStream.available())
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.reset()

        // Calculate inSampleSize
        options.apply {
            inSampleSize = calculateInSampleSize(this)
            inJustDecodeBounds = false
            inPreferredConfig = Bitmap.Config.RGB_565 // Use lower quality config for memory efficiency
        }

        // Decode bitmap with inSampleSize set
        return BitmapFactory.decodeStream(inputStream, null, options) ?: createBitmap(
            Constants.MIN_IMAGE_RESOLUTION_WIDTH,
            Constants.MIN_IMAGE_RESOLUTION_HEIGHT,
            Bitmap.Config.RGB_565
        )
    }

    /**
     * Rotates an image if required according to its EXIF orientation
     * @param bitmap Bitmap to rotate
     * @param uri URI of the original image
     * @return Rotated bitmap if needed, or original bitmap
     */
    private fun rotateImageIfRequired(bitmap: Bitmap, uri: Uri): Bitmap {
        try {
            val input = context.contentResolver.openInputStream(uri) ?: return bitmap

            val ei =
                ExifInterface(input)

            input.close()

            val orientation = ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error rotating image: ${e.message}")
            return bitmap
        }
    }

    /**
     * Rotates a bitmap by specified degrees
     * @param source Source bitmap
     * @param angle Rotation angle in degrees
     * @return Rotated bitmap
     */
    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        val rotatedBitmap = Bitmap.createBitmap(
            source, 0, 0, source.width, source.height, matrix, true
        )
        // Recycle source bitmap if it's different from rotated
        if (source != rotatedBitmap && !source.isRecycled) {
            source.recycle()
        }
        return rotatedBitmap
    }

    /**
     * Calculates image quality score based on brightness, contrast and blur detection
     * Optimized for low-end devices, simplified algorithms
     * @param bitmap Input bitmap to analyze
     * @return Quality score from 0-100
     */
    private fun calculateImageQuality(bitmap: Bitmap): Int {
        // Create smaller bitmap for analysis to save memory
        val scaledBitmap = bitmap.scale(bitmap.width / 4, bitmap.height / 4, false)

        val brightnessScore = calculateBrightnessScore(scaledBitmap)
        val contrastScore = calculateContrastScore(scaledBitmap)
        val blurScore = detectBlur(scaledBitmap)

        // Clean up
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }

        // Weighted average of scores
        return ((brightnessScore * 0.4) + (contrastScore * 0.3) + (blurScore * 0.3)).toInt()
    }

    /**
     * Calculate brightness score (0-100)
     * @param bitmap Input bitmap
     * @return Brightness score
     */
    private fun calculateBrightnessScore(bitmap: Bitmap): Int {
        var totalBrightness = 0L
        val pixels = IntArray(bitmap.width * bitmap.height)

        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        // Sample only a portion of pixels to save processing time
        val samplingRate = 5
        var sampledPixels = 0

        for (i in pixels.indices step samplingRate) {
            val pixel: Int = pixels[i]
            val r = (pixel shr 16) and 0xff
            val g = (pixel shr 8) and 0xff
            val b = pixel and 0xff

            // Perceived brightness using relative luminance formula
            val brightness = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            totalBrightness += brightness
            sampledPixels++
        }

        val avgBrightness = totalBrightness / sampledPixels

        // Score based on distance from ideal brightness range (80-160)
        return when {
            avgBrightness < 40 -> (avgBrightness * 1.25).toInt() // Too dark
            avgBrightness < 80 -> 50 + ((avgBrightness - 40) * 1.25).toInt()
            avgBrightness <= 160 -> 100 // Ideal range
            avgBrightness <= 200 -> 100 - ((avgBrightness - 160) * 1.25).toInt()
            else -> ((255 - avgBrightness) * 0.5).toInt() // Too bright
        }
    }

    /**
     * Calculate contrast score (0-100)
     * @param bitmap Input bitmap
     * @return Contrast score
     */
    private fun calculateContrastScore(bitmap: Bitmap): Int {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var minBrightness = 255
        var maxBrightness = 0

        // Sample pixels to find min and max brightness
        val samplingRate = 5

        for (i in pixels.indices step samplingRate) {
            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xff
            val g = (pixel shr 8) and 0xff
            val b = pixel and 0xff

            val brightness = (0.299 * r + 0.587 * g + 0.114 * b).toInt()

            if (brightness < minBrightness) minBrightness = brightness
            if (brightness > maxBrightness) maxBrightness = brightness
        }

        val contrast = maxBrightness - minBrightness

        // Score based on contrast level
        return when {
            contrast < 30 -> (contrast * 1.5).toInt() // Very low contrast
            contrast < 60 -> 45 + ((contrast - 30) * 1.0).toInt()
            contrast < 120 -> 75 + ((contrast - 60) * 0.42).toInt() // Good contrast
            contrast < 180 -> 100
            else -> 100 - ((contrast - 180) * 0.5).toInt() // Too much contrast
        }.coerceIn(0, 100)
    }

    /**
     * Detect blur in image using Laplacian variance
     * Simplified algorithm optimized for performance
     * @param bitmap Input bitmap
     * @return Blur score (0-100, where 100 means not blurry)
     */
    private fun detectBlur(bitmap: Bitmap): Int {
        // Convert to grayscale to simplify processing
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Sampling rate to improve performance
        val samplingRate = 3

        var sumLaplacian = 0.0
        var sampleCount = 0

        // Apply simplified Laplacian filter to detect edges
        for (y in 1 until height - 1 step samplingRate) {
            for (x in 1 until width - 1 step samplingRate) {
                val index = y * width + x

                // Get surrounding pixel indices
                val top = index - width
                val bottom = index + width
                val left = index - 1
                val right = index + 1

                // Convert to grayscale
                val grayCenter = getGrayScale(pixels[index])
                val grayTop = getGrayScale(pixels[top])
                val grayBottom = getGrayScale(pixels[bottom])
                val grayLeft = getGrayScale(pixels[left])
                val grayRight = getGrayScale(pixels[right])

                // Simplified Laplacian calculation (4-neighborhood)
                val laplacian = abs(4 * grayCenter - grayTop - grayBottom - grayLeft - grayRight)

                sumLaplacian += laplacian
                sampleCount++
            }
        }

        // Average Laplacian value - higher means sharper image
        val avgLaplacian = if (sampleCount > 0) sumLaplacian / sampleCount else 0.0

        // Convert to score (0-100)
        return when {
            avgLaplacian < 5 -> (avgLaplacian * 10).toInt() // Very blurry
            avgLaplacian < 10 -> 50 + ((avgLaplacian - 5) * 6).toInt()
            avgLaplacian < 20 -> 80 + ((avgLaplacian - 10)).toInt()
            else -> 100 // Sharp image
        }.coerceIn(0, 100)
    }

    /**
     * Convert RGB to grayscale value
     * @param pixel RGB pixel value
     * @return Grayscale value (0-255)
     */
    private fun getGrayScale(pixel: Int): Int {
        val r = (pixel shr 16) and 0xff
        val g = (pixel shr 8) and 0xff
        val b = pixel and 0xff
        return (0.299 * r + 0.587 * g + 0.114 * b).toInt()
    }

    /**
     * Delete temporary images to free up space
     * Should be called periodically to clean up storage
     */
    fun cleanupTempImages() {
        try {
            val storageDir = File(context.filesDir, Constants.TEMP_IMAGE_PATH)
            if (storageDir.exists()) {
                val files = storageDir.listFiles()
                files?.forEach { file ->
                    // Delete files older than 24 hours
                    if (System.currentTimeMillis() - file.lastModified() > 24 * 60 * 60 * 1000) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up temp images: ${e.message}")
        }
    }

    /**
     * Clear in-memory image cache to free up memory
     * Should be called when app needs to reduce memory usage
     */
    fun clearImageCache() {
        try {
            memoryCache.evictAll()
            Log.d(TAG, "Image cache cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing image cache: ${e.message}")
        }
    }

    /**
     * Compresses a bitmap with specified quality
     * @param bitmap Bitmap to compress
     * @param quality Compression quality (0-100)
     * @return Compressed bitmap
     */
    fun compressBitmap(bitmap: Bitmap, quality: Int): Bitmap {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        val compressedData = byteArrayOutputStream.toByteArray()

        return BitmapFactory.decodeByteArray(compressedData, 0, compressedData.size) ?: bitmap
    }

    /**
     * Resizes a bitmap to specified dimensions
     * @param bitmap Bitmap to resize
     * @param width Target width
     * @param height Target height
     * @return Resized bitmap
     */
    fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return bitmap.scale(width, height)
    }

    /**
     * Gets a bitmap from URI
     * @param uri URI to get bitmap from
     * @return Bitmap from URI
     */
    fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error getting bitmap from URI: ${e.message}")
            null
        }
    }

    /**
     * Assess image quality
     * @param bitmap Bitmap to assess
     * @return Quality score (0-100)
     */
    fun assessImageQuality(bitmap: Bitmap): Int {
        return calculateImageQuality(bitmap)
    }

    /**
     * Save image to cache directory
     * @param bitmap Bitmap to save
     * @param fileName Name for the file
     * @return File where image was saved
     */
    fun saveImageToCache(bitmap: Bitmap, fileName: String): File {
        val cacheFile = File(context.cacheDir, fileName)
        val outputStream = FileOutputStream(cacheFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        outputStream.flush()
        outputStream.close()
        return cacheFile
    }

    /**
     * Optimize image for upload, ensuring it's under specified size limit
     * @param bitmap Bitmap to optimize
     * @param maxSizeBytes Maximum size in bytes
     * @return Optimized bitmap
     */
    fun optimizeImageForUpload(bitmap: Bitmap, maxSizeBytes: Int): Bitmap {
        var quality = 100
        var outputStream = ByteArrayOutputStream()
        var optimizedBitmap = bitmap

        // First try just compressing with reducing quality
        while (quality > 10) {
            outputStream = ByteArrayOutputStream()
            optimizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            if (outputStream.size() <= maxSizeBytes) {
                break
            }
            quality -= 10
        }

        // If still too large, reduce dimensions
        if (outputStream.size() > maxSizeBytes) {
            var scale = 0.9f
            while (scale > 0.1f) {
                val width = (bitmap.width * scale).toInt()
                val height = (bitmap.height * scale).toInt()
                optimizedBitmap = bitmap.scale(width, height)

                outputStream = ByteArrayOutputStream()
                optimizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

                if (outputStream.size() <= maxSizeBytes) {
                    break
                }
                scale -= 0.1f
            }
        }

        return optimizedBitmap
    }

    /**
     * Get image orientation in degrees
     * @param imagePath Path to image file
     * @return Orientation in degrees (0, 90, 180, 270)
     */
    fun getImageOrientation(imagePath: String): Int {
        try {
            val exif = ExifInterface(imagePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting image orientation: ${e.message}")
            return 0
        }
    }

}