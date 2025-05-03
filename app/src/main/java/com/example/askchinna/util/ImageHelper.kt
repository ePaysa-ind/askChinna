/**
 * File: app/src/main/java/com/example/askchinna/util/ImageHelper.kt
 * Copyright (c) 2025 askChinna
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
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
     * Gets content URI for a file using FileProvider
     * @param file File to get URI for
     * @return Content URI for the file
     */
    fun getUriForFile(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Compress image for upload, optimized for low-end devices
     * @param imageUri URI of the image to compress
     * @param quality Compression quality (0-100)
     * @return File containing the compressed image
     */
    fun compressImage(imageUri: Uri, quality: Int = Constants.IMAGE_QUALITY_COMPRESSION): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return null
            val bitmap = decodeSampledBitmapFromStream(inputStream)
            inputStream.close()

            // Rotate bitmap if needed based on EXIF data
            val rotatedBitmap = rotateImageIfRequired(bitmap, imageUri)

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
        return BitmapFactory.decodeStream(inputStream, null, options) ?:
        Bitmap.createBitmap(Constants.MIN_IMAGE_RESOLUTION_WIDTH, Constants.MIN_IMAGE_RESOLUTION_HEIGHT, Bitmap.Config.RGB_565)
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

            val ei = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ExifInterface(input)
            } else {
                // For older devices, try to get file path
                val path = getPathFromUri(uri)
                if (path != null) {
                    ExifInterface(path)
                } else {
                    input.close()
                    return bitmap
                }
            }

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
        // Don't recycle source here as it might be used elsewhere
        return rotatedBitmap
    }

    /**
     * Attempts to get file path from URI
     * @param uri URI to get path for
     * @return File path string or null
     */
    private fun getPathFromUri(uri: Uri): String? {
        try {
            // For MediaStore content URIs
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow("_data")
                    return cursor.getString(columnIndex)
                }
            }

            // For file URI
            if (uri.scheme == "file") {
                return uri.path
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting path from URI: ${e.message}")
        }
        return null
    }

    /**
     * Calculates image quality score based on brightness, contrast and blur detection
     * Optimized for low-end devices, simplified algorithms
     * @param bitmap Input bitmap to analyze
     * @return Quality score from 0-100
     */
    fun calculateImageQuality(bitmap: Bitmap): Int {
        // Create smaller bitmap for analysis to save memory
        val scaledBitmap = Bitmap.createScaledBitmap(
            bitmap,
            bitmap.width / 4,
            bitmap.height / 4,
            false
        )

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

        for (i in 0 until pixels.size step samplingRate) {
            val pixel = pixels[i]
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

        for (i in 0 until pixels.size step samplingRate) {
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
                val laplacian = Math.abs(4 * grayCenter - grayTop - grayBottom - grayLeft - grayRight)

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
     * Check if image size is within allowed limit
     * @param uri URI of the image
     * @return true if within limit, false otherwise
     */
    fun isImageSizeWithinLimit(uri: Uri): Boolean {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            val fileSize = inputStream.available()
            inputStream.close()

            val fileSizeMB = fileSize / (1024f * 1024f)
            return fileSizeMB <= Constants.MAX_IMAGE_SIZE_MB
        } catch (e: Exception) {
            Log.e(TAG, "Error checking image size: ${e.message}")
            return false
        }
    }

    /**
     * Check if image resolution meets minimum requirements
     * @param uri URI of the image
     * @return true if resolution is sufficient, false otherwise
     */
    fun isImageResolutionSufficient(uri: Uri): Boolean {
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            return options.outWidth >= Constants.MIN_IMAGE_RESOLUTION_WIDTH &&
                    options.outHeight >= Constants.MIN_IMAGE_RESOLUTION_HEIGHT
        } catch (e: Exception) {
            Log.e(TAG, "Error checking image resolution: ${e.message}")
            return false
        }
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
}