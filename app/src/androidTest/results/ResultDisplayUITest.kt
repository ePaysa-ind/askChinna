/**
 * File: app/src/androidTest/java/com/example/askchinna/results/ResultDisplayUITest.kt
 * Copyright (c) 2025 askChinna
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.results

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.askchinna.R
import com.example.askchinna.data.model.Action
import com.example.askchinna.data.model.IdentificationResult
import com.example.askchinna.data.repository.IdentificationRepository
import com.example.askchinna.ui.results.ResultActivity
import com.example.askchinna.util.Constants
import com.example.askchinna.util.SessionManager
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.containsString
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

/**
 * UI tests for the ResultActivity
 * Covers display of identification results and user interactions
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ResultDisplayUITest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var identificationRepository: IdentificationRepository

    private lateinit var testResult: IdentificationResult
    private val testResultId = UUID.randomUUID().toString()

    @Before
    fun setup() {
        hiltRule.inject()

        // Create test data
        setupTestResult()

        // Set up session for tests
        setupTestSession()
    }

    private fun setupTestResult() = runBlocking {
        // Create a mock result
        testResult = IdentificationResult(
            id = testResultId,
            cropId = "test_crop_id",
            cropName = "Tomato",
            imageUrl = "https://example.com/test-image.jpg",
            problemName = "Early Blight",
            description = "Early blight is a common fungal disease that affects tomato plants. " +
                    "It causes dark spots on leaves with concentric rings, leading to leaf yellowing and drop.",
            severity = 2, // Medium severity
            confidence = 85.5f,
            actions = listOf(
                Action(
                    actionType = "spray",
                    description = "Apply copper-based fungicide every 7-10 days"
                ),
                Action(
                    actionType = "remove",
                    description = "Remove and destroy infected leaves"
                ),
                Action(
                    actionType = "monitor",
                    description = "Monitor plants regularly for new infections"
                )
            ),
            timestamp = Date(),
            userId = "test_user_id"
        )

        // Mock the repository to return our test result
        val mockRepo = mock(IdentificationRepository::class.java)
        `when`(mockRepo.getIdentificationResultById(testResultId)).thenReturn(testResult)

        // Save the test result to the real repository for testing
        identificationRepository.saveIdentificationResult(testResult)
    }

    private fun setupTestSession() {
        // Set up a test session
        sessionManager.startSession(
            user = com.example.askchinna.data.model.User(
                id = "test_user_id",
                name = "Test User",
                phoneNumber = "+911234567890",
                registrationDate = Date()
            ),
            usageLimit = com.example.askchinna.data.model.UsageLimit(
                usageCount = 2,
                lastUpdated = Date()
            )
        )
    }

    /**
     * Test activity initialization and display of key UI elements
     */
    @Test
    fun testResultDisplayInitialization() {
        // Launch activity with intent data
        val intent = Intent(ApplicationProvider.getApplicationContext(), ResultActivity::class.java).apply {
            putExtra(Constants.EXTRA_RESULT_ID, testResultId)
        }

        ActivityScenario.launch<ResultActivity>(intent).use {
            // Check toolbar title
            onView(withText(R.string.result_title)).check(matches(isDisplayed()))

            // Check problem name display
            onView(withId(R.id.problemNameText)).check(matches(isDisplayed()))
            onView(withId(R.id.problemNameText)).check(matches(withText(containsString("Early Blight"))))

            // Check severity display
            onView(withId(R.id.severityText)).check(matches(isDisplayed()))
            onView(withId(R.id.severityText)).check(matches(withText(containsString("Medium"))))

            // Check confidence display
            onView(withId(R.id.confidenceText)).check(matches(isDisplayed()))
            onView(withId(R.id.confidenceText)).check(matches(withText(containsString("85"))))

            // Check description display
            onView(withId(R.id.descriptionText)).check(matches(isDisplayed()))
            onView(withId(R.id.descriptionText)).check(matches(withText(containsString("fungal disease"))))

            // Check action plan is displayed with at least one action
            onView(withId(R.id.actionPlanView)).check(matches(isDisplayed()))
        }
    }

    /**
     * Test expanding and collapsing detail view
     */
    @Test
    fun testExpandCollapseDetails() {
        // Launch activity
        val intent = Intent(ApplicationProvider.getApplicationContext(), ResultActivity::class.java).apply {
            putExtra(Constants.EXTRA_RESULT_ID, testResultId)
        }

        ActivityScenario.launch<ResultActivity>(intent).use {
            // Check detail expandable view is displayed
            onView(withId(R.id.detailExpandableView)).check(matches(isDisplayed()))

            // Detail content should initially be collapsed
            onView(withId(R.id.detailContent)).check(matches(not(isDisplayed())))

            // Click to expand
            onView(withId(R.id.expandCollapseButton)).perform(click())

            // Detail content should now be visible
            onView(withId(R.id.detailContent)).check(matches(isDisplayed()))

            // Click to collapse
            onView(withId(R.id.expandCollapseButton)).perform(click())

            // Detail content should be hidden again
            onView(withId(R.id.detailContent)).check(matches(not(isDisplayed())))
        }
    }

    /**
     * Test PDF export button click
     */
    @Test
    fun testPdfExportButton() {
        // Launch activity
        val intent = Intent(ApplicationProvider.getApplicationContext(), ResultActivity::class.java).apply {
            putExtra(Constants.EXTRA_RESULT_ID, testResultId)
        }

        ActivityScenario.launch<ResultActivity>(intent).use {
            // Check PDF button is displayed
            onView(withId(R.id.exportPdfButton)).check(matches(isDisplayed()))

            // Click PDF button
            onView(withId(R.id.exportPdfButton)).perform(click())

            // Check for PDF generation progress indicator
            onView(withId(R.id.pdfProgressBar)).check(matches(isDisplayed()))

            // In a real test, we would wait for PDF generation using IdlingResource
            // and verify the share intent is launched
        }
    }

    /**
     * Test feedback submission
     */
    @Test
    fun testFeedbackSubmission() {
        // Launch activity
        val intent = Intent(ApplicationProvider.getApplicationContext(), ResultActivity::class.java).apply {
            putExtra(Constants.EXTRA_RESULT_ID, testResultId)
        }

        ActivityScenario.launch<ResultActivity>(intent).use {
            // Check feedback view is displayed
            onView(withId(R.id.feedbackView)).check(matches(isDisplayed()))

            // Click a feedback option (thumbs up)
            onView(withId(R.id.feedbackThumbsUp)).perform(click())

            // Verify thank you message is displayed
            onView(withId(R.id.feedbackThankYouText)).check(matches(isDisplayed()))

            // Check that feedback buttons are disabled after submission
            onView(withId(R.id.feedbackThumbsUp)).check(matches(not(isEnabled())))
            onView(withId(R.id.feedbackThumbsDown)).check(matches(not(isEnabled())))
        }
    }

    /**
     * Test activity behavior when invalid result ID is provided
     */
    @Test
    fun testInvalidResultId() {
        // Launch activity with invalid result ID
        val intent = Intent(ApplicationProvider.getApplicationContext(), ResultActivity::class.java).apply {
            putExtra(Constants.EXTRA_RESULT_ID, "invalid_id")
        }

        ActivityScenario.launch<ResultActivity>(intent).use {
            // Check error message is displayed
            onView(withId(R.id.errorView)).check(matches(isDisplayed()))

            // Error message should indicate result not found
            onView(withId(R.id.errorMessage)).check(matches(withText(containsString("not found"))))
        }
    }

    /**
     * Test activity behavior when no result ID is provided
     */
    @Test
    fun testNoResultId() {
        // Launch activity without a result ID
        val intent = Intent(ApplicationProvider.getApplicationContext(), ResultActivity::class.java)

        ActivityScenario.launch<ResultActivity>(intent).use {
            // Activity should finish immediately, so we expect the scenario to be in a finished state
            it.onActivity { activity ->
                assert(activity.isFinishing)
            }
        }
    }
}