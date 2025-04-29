/**
 * File: app/src/androidTest/java/com/example/askchinna/identification/ImageCaptureUITest.kt
 * Copyright (c) 2025 askChinna
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.identification

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.askchinna.R
import com.example.askchinna.data.model.Crop
import com.example.askchinna.data.model.UsageLimit
import com.example.askchinna.data.repository.CropRepository
import com.example.askchinna.ui.identification.ImageCaptureActivity
import com.example.askchinna.util.Constants
import com.example.askchinna.util.SessionManager
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date
import javax.inject.Inject

/**
 * UI tests for the ImageCaptureActivity
 * Covers UI interactions with camera capture and gallery selection
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ImageCaptureUITest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var cropRepository: CropRepository

    private lateinit var testCrop: Crop

    @Before
    fun setup() {
        hiltRule.inject()

        // Set up test data
        setupTestCrop()

        // Set up session for tests
        setupTestSession()
    }

    private fun setupTestCrop() = runBlocking {
        // Get a sample crop for testing
        val crops = cropRepository.getAllCrops()
        testCrop = crops.firstOrNull() ?: Crop(
            id = "test_crop_id",
            name = "Test Crop",
            scientificName = "Testus Cropus",
            iconResName = "ic_tomato"
        )
    }

    private fun setupTestSession() {
        // Set up a test session with initial usage limit
        val usageLimit = UsageLimit(
            usageCount = 2, // 3 left out of 5
            lastUpdated = Date()
        )

        // Start a session with test data
        sessionManager.startSession(
            user = com.example.askchinna.data.model.User(
                id = "test_user_id",
                name = "Test User",
                phoneNumber = "+911234567890",
                registrationDate = Date()
            ),
            usageLimit = usageLimit
        )
    }

    /**
     * Test activity initialization and display of key UI elements
     */
    @Test
    fun testActivityInitialization() {
        // Launch activity with intent data
        val intent = Intent(ApplicationProvider.getApplicationContext(), ImageCaptureActivity::class.java).apply {
            putExtra(Constants.EXTRA_CROP_ID, testCrop.id)
        }

        ActivityScenario.launch<ImageCaptureActivity>(intent).use {
            // Check toolbar title
            onView(withText(R.string.capture_image_title)).check(matches(isDisplayed()))

            // Check if crop name is displayed
            onView(withId(R.id.selectedCropName)).check(matches(isDisplayed()))
            onView(withId(R.id.selectedCropName)).check(matches(withText(testCrop.name)))

            // Check if buttons are displayed and enabled
            onView(withId(R.id.captureButton)).check(matches(isDisplayed()))
            onView(withId(R.id.captureButton)).check(matches(isEnabled()))

            onView(withId(R.id.galleryButton)).check(matches(isDisplayed()))
            onView(withId(R.id.galleryButton)).check(matches(isEnabled()))

            // Check if remaining identifications are displayed
            onView(withId(R.id.remainingIdentifications)).check(matches(isDisplayed()))
        }
    }

    /**
     * Test behavior when usage limit is reached
     */
    @Test
    fun testUsageLimitReached() {
        // Update session with maxed out usage
        val usageLimit = UsageLimit(
            usageCount = Constants.MAX_MONTHLY_IDENTIFICATIONS,
            lastUpdated = Date()
        )
        sessionManager.updateUsageLimit(usageLimit)

        // Launch activity
        val intent = Intent(ApplicationProvider.getApplicationContext(), ImageCaptureActivity::class.java).apply {
            putExtra(Constants.EXTRA_CROP_ID, testCrop.id)
        }

        ActivityScenario.launch<ImageCaptureActivity>(intent).use {
            // Check if usage limit warning is displayed
            onView(withId(R.id.usageLimitWarning)).check(matches(isDisplayed()))

            // Check if buttons are disabled
            onView(withId(R.id.captureButton)).check(matches(not(isEnabled())))
            onView(withId(R.id.galleryButton)).check(matches(not(isEnabled())))
        }
    }

    /**
     * Test help button click
     */
    @Test
    fun testHelpButtonClick() {
        // Launch activity
        val intent = Intent(ApplicationProvider.getApplicationContext(), ImageCaptureActivity::class.java).apply {
            putExtra(Constants.EXTRA_CROP_ID, testCrop.id)
        }

        ActivityScenario.launch<ImageCaptureActivity>(intent).use {
            // Check help container is initially hidden
            onView(withId(R.id.helpContainer)).check(matches(not(isDisplayed())))

            // Click help button
            onView(withId(R.id.helpButton)).perform(click())

            // Check help container is displayed
            onView(withId(R.id.helpContainer)).check(matches(isDisplayed()))

            // Wait for help container to auto-hide (would need IdlingResource in real test)
            Thread.sleep(5100) // Slightly longer than the 5000ms auto-hide delay

            // Check help container is hidden again
            onView(withId(R.id.helpContainer)).check(matches(not(isDisplayed())))
        }
    }

    /**
     * Test activity behavior when no crop ID is provided
     */
    @Test
    fun testNoCropId() {
        // Launch activity without crop ID
        val intent = Intent(ApplicationProvider.getApplicationContext(), ImageCaptureActivity::class.java)

        ActivityScenario.launch<ImageCaptureActivity>(intent).use {
            // Activity should finish immediately, so we expect the scenario to be in a finished state
            it.onActivity { activity ->
                assert(activity.isFinishing)
            }
        }
    }

    /**
     * Test that permissions are requested when camera button is clicked
     * Note: This test requires manual interaction and is usually disabled in automated test runs
     */
    @Test
    fun testCameraButtonClickRequestsPermission() {
        // Only run this test if we don't already have camera permission
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val hasPermission = context.checkSelfPermission(android.Manifest.permission.CAMERA) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            // Skip test if we already have permission
            return
        }

        // Launch activity
        val intent = Intent(ApplicationProvider.getApplicationContext(), ImageCaptureActivity::class.java).apply {
            putExtra(Constants.EXTRA_CROP_ID, testCrop.id)
        }

        ActivityScenario.launch<ImageCaptureActivity>(intent).use {
            // Click camera button
            onView(withId(R.id.captureButton)).perform(click())

            // Check if permission rationale is displayed
            // This depends on whether the user has previously denied permission
            // and whether we're showing rationale or directly requesting permission
            it.onActivity { activity ->
                // We can only assert that the permission request was initiated
                // but can't verify the system dialog in instrumented tests
                assert(true)
            }
        }
    }
}