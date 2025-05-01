/*
 * File: com/example/askchinna/util/ImageHelperTest.kt
 * Copyright (c) 2025 askChinna
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.askchinna.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Unit tests for the ImageHelper utility class.
 * Tests image processing, compression, and quality assessment functions.
 */
@RunWith(AndroidJUnit4::class)
class ImageHelperTest {

    private lateinit var context: Context
    private lateinit var imageHelper: ImageHelper
    private lateinit var testBitmap: Bitmap
    private lateinit var testImageFile: File

    companion object {
        private const val TEST_IMAGE_WIDTH = 800
        private const val TEST_IMAGE_HEIGHT = 600
        private const val TEST_IMAGE_QUALITY = 80
    }

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        imageHelper = ImageHelper(context)

        // Create a test bitmap
        testBitmap = Bitmap.createBitmap(
            TEST_IMAGE_WIDTH,
            TEST_IMAGE_HEIGHT,
            Bitmap.Config.ARGB_8888
        )

        // Create a test image file
        testImageFile = File(context.cacheDir, "test_image.jpg")
        if (!testImageFile.exists()) {
            testImageFile.createNewFile()
            val outputStream = FileOutputStream(testImageFile)
            testBitmap.compress(Bitmap.CompressFormat.JPEG, TEST_IMAGE_QUALITY, outputStream)
            outputStream.flush()
            outputStream.close()
        }
    }

    @Test
    fun compressBitmap_reducesFileSize() = runBlocking {
        // Get original size
        val outputStreamOriginal = ByteArrayOutputStream()
        testBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStreamOriginal)
        val originalSize = outputStreamOriginal.toByteArray().size

        // Compress with our utility
        val compressedBitmap = imageHelper.compressBitmap(testBitmap, 70)

        // Get compressed size
        val outputStreamCompressed = ByteArrayOutputStream()
        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStreamCompressed)
        val compressedSize = outputStreamCompressed.toByteArray().size

        // Verify compression reduced size
        assertTrue("Compressed image should be smaller than original", compressedSize < originalSize)
    }

    @Test
    fun resizeBitmap_correctDimensions() = runBlocking {
        // Target dimensions
        val targetWidth = 400
        val targetHeight = 300

        // Resize bitmap
        val resizedBitmap = imageHelper.resizeBitmap(testBitmap, targetWidth, targetHeight)

        // Check dimensions
        assertEquals("Resized bitmap width should match target", targetWidth, resizedBitmap.width)
        assertEquals("Resized bitmap height should match target", targetHeight, resizedBitmap.height)
    }

    @Test
    fun getBitmapFromUri_returnsValidBitmap() = runBlocking {
        // Mock Uri and ContentResolver behavior
        val mockUri = mockk<Uri>()
        val mockInputStream = testImageFile.inputStream()

        mockkStatic(BitmapFactory::class)
        every { BitmapFactory.decodeStream(any()) } returns testBitmap

        // Mock ContentResolver behavior
        every { context.contentResolver.openInputStream(mockUri) } returns mockInputStream

        // Call method under test
        val resultBitmap = imageHelper.getBitmapFromUri(mockUri)

        // Verify result
        assertNotNull("Bitmap should not be null", resultBitmap)
        verify { context.contentResolver.openInputStream(mockUri) }
    }

    @Test
    fun assessImageQuality_highQualityImage_returnsHighScore() = runBlocking {
        // Create a high-quality test image (clear, well-lit)
        val highQualityBitmap = testBitmap // In a real test, this would be a known high-quality image

        // Assess quality
        val qualityScore = imageHelper.assessImageQuality(highQualityBitmap)

        // Verify high score for good quality
        assertTrue("High quality image should have score > 70", qualityScore > 70)
    }

    @Test
    fun saveImageToCache_savesSuccessfully() = runBlocking {
        // Delete any existing file to ensure clean test
        val cacheFile = File(context.cacheDir, "test_save.jpg")
        if (cacheFile.exists()) {
            cacheFile.delete()
        }

        // Save image to cache
        val savedFile = imageHelper.saveImageToCache(testBitmap, "test_save.jpg")

        // Verify file exists and has content
        assertTrue("File should exist after saving", savedFile.exists())
        assertTrue("File should have content", savedFile.length() > 0)
    }

    @Test
    fun optimizeImageForUpload_reducesFileSizeWithinLimit() = runBlocking {
        // Define max upload size (500KB)
        val maxUploadSizeBytes = 500 * 1024

        // Optimize image
        val optimizedBitmap = imageHelper.optimizeImageForUpload(testBitmap, maxUploadSizeBytes)

        // Check size is within limit
        val outputStream = ByteArrayOutputStream()
        optimizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        val optimizedSize = outputStream.toByteArray().size

        assertTrue("Optimized image should be under size limit",
            optimizedSize <= maxUploadSizeBytes)
    }

    @Test
    fun getImageOrientation_portrait_returnsCorrectOrientation() {
        // Mock ExifInterface to return portrait orientation
        mockkStatic(android.media.ExifInterface::class)
        every {
            any<android.media.ExifInterface>().getAttributeInt(
                android.media.ExifInterface.TAG_ORIENTATION,
                android.media.ExifInterface.ORIENTATION_NORMAL
            )
        } returns android.media.ExifInterface.ORIENTATION_ROTATE_90

        // Test orientation detection
        val orientation = imageHelper.getImageOrientation(testImageFile.absolutePath)

        // Verify portrait orientation detected
        assertEquals("Should detect portrait orientation (90 degrees)",
            90, orientation)
    }
}