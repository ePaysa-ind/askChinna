package com.example.askchinna.memory

import android.graphics.Bitmap
import android.net.Uri
import com.example.askchinna.util.ImageHelper
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.io.File
import java.lang.ref.WeakReference

class MemoryManagementTest {
    
    @Mock
    private lateinit var mockUri: Uri
    
    private lateinit var imageHelper: ImageHelper
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        // Initialize ImageHelper with mock context
    }
    
    @Test
    fun testBitmapRecyclingInCache() {
        // Create multiple bitmaps
        val bitmaps = mutableListOf<WeakReference<Bitmap>>()
        
        repeat(20) { index ->
            val bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)
            bitmaps.add(WeakReference(bitmap))
            
            // Add to cache (should evict old ones)
            imageHelper.addBitmapToMemCache("key_$index", bitmap)
        }
        
        // Force garbage collection
        System.gc()
        Thread.sleep(100)
        
        // Check that early bitmaps are recycled
        val recycledCount = bitmaps.take(10).count { it.get() == null || it.get()?.isRecycled == true }
        assertTrue("At least 8 out of first 10 bitmaps should be recycled", recycledCount >= 8)
    }
    
    @Test
    fun testDiskCacheSize() {
        // Add multiple large files to disk cache
        repeat(10) { index ->
            val largebitmap = Bitmap.createBitmap(2000, 2000, Bitmap.Config.ARGB_8888)
            imageHelper.addBitmapToDiskCache("disk_key_$index", largebitmap)
            largebitmap.recycle()
        }
        
        // Check disk cache size
        val cacheDir = File(context.cacheDir, "image_cache")
        val totalSize = cacheDir.listFiles()?.sumOf { it.length() } ?: 0L
        
        // Should not exceed 50MB
        assertTrue("Disk cache should not exceed 50MB", totalSize < 50 * 1024 * 1024)
    }
    
    @Test
    fun testProgressiveLoading() {
        var lowQualityLoaded = false
        var highQualityLoaded = false
        
        imageHelper.loadImageProgressively(mockUri) { bitmap, isFinal ->
            if (!isFinal) {
                assertNotNull("Low quality bitmap should not be null", bitmap)
                lowQualityLoaded = true
            } else {
                highQualityLoaded = true
            }
        }
        
        // Wait for async operations
        Thread.sleep(1000)
        
        assertTrue("Low quality should load first", lowQualityLoaded)
        assertTrue("High quality should load second", highQualityLoaded)
    }
    
    @Test
    fun testMemoryLeakInActivities() {
        // Create weak references to activities
        val activityRefs = mutableListOf<WeakReference<Any>>()
        
        // Simulate activity lifecycle
        repeat(5) {
            val activity = createMockActivity()
            activityRefs.add(WeakReference(activity))
            
            // Simulate onDestroy
            activity.onDestroy()
        }
        
        // Force garbage collection
        System.gc()
        Thread.sleep(500)
        
        // All activities should be garbage collected
        val leakedCount = activityRefs.count { it.get() != null }
        assertEquals("No activities should be leaked", 0, leakedCount)
    }
    
    @Test
    fun testCacheClearingOnLowMemory() {
        // Fill caches
        repeat(10) { index ->
            val bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)
            imageHelper.addBitmapToMemCache("mem_key_$index", bitmap)
        }
        
        // Clear caches
        imageHelper.clearCaches()
        
        // Verify caches are cleared
        val cachedBitmap = imageHelper.getBitmapFromMemCache("mem_key_0")
        assertNull("Memory cache should be cleared", cachedBitmap)
        
        val cacheDir = File(context.cacheDir, "image_cache")
        val remainingFiles = cacheDir.listFiles()?.size ?: 0
        assertEquals("Disk cache should be cleared", 0, remainingFiles)
    }
    
    private fun createMockActivity(): Any {
        // Mock activity creation
        return Object()
    }
}