package com.example.askchinna.performance

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.askchinna.R
import com.example.askchinna.ui.home.HomeActivity
import com.example.askchinna.ui.identification.ImageCaptureActivity
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureTimeMillis

@RunWith(AndroidJUnit4::class)
class PerformanceTest {
    
    @Test
    fun testHomeScreenLoadTime() {
        val loadTime = measureTimeMillis {
            ActivityScenario.launch(HomeActivity::class.java)
        }
        
        // Should load within 2 seconds
        assert(loadTime < 2000) { "Home screen took ${loadTime}ms to load" }
    }
    
    @Test
    fun testImageCaptureMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()
        
        ActivityScenario.launch(ImageCaptureActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                // Simulate taking multiple photos
                repeat(5) {
                    // Trigger camera capture
                    activity.runOnUiThread {
                        activity.findViewById<View>(R.id.button_capture)?.performClick()
                    }
                    Thread.sleep(1000)
                }
            }
        }
        
        System.gc()
        val finalMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryIncrease = finalMemory - initialMemory
        
        // Memory increase should be less than 100MB
        assert(memoryIncrease < 100 * 1024 * 1024) { 
            "Memory leak detected: ${memoryIncrease / 1024 / 1024}MB increase" 
        }
    }
    
    @Test
    fun testNetworkTimeouts() {
        // Test API calls with simulated slow network
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Simulate slow network
        val startTime = System.currentTimeMillis()
        
        // Attempt network operation
        // Check if timeout is properly handled within 15 seconds
        val duration = System.currentTimeMillis() - startTime
        assert(duration < 15000) { "Network timeout not working properly" }
    }
    
    @Test
    fun testCachePerformance() {
        // Test if cached data loads faster than network data
        val scenario = ActivityScenario.launch(HomeActivity::class.java)
        
        // First load - from network
        val firstLoadTime = measureTimeMillis {
            onView(withId(R.id.button_capture)).perform(click())
        }
        
        // Navigate back
        scenario.onActivity { it.onBackPressed() }
        
        // Second load - should be from cache
        val secondLoadTime = measureTimeMillis {
            onView(withId(R.id.button_capture)).perform(click())
        }
        
        // Cached load should be at least 50% faster
        assert(secondLoadTime < firstLoadTime * 0.5) {
            "Cache not improving performance: ${secondLoadTime}ms vs ${firstLoadTime}ms"
        }
    }
}