/*
 * Copyright (c) 2025 askChinna App Development Team
 * File: app/src/test/java/com/example/askchinna/util/SessionManagerTest.kt
 * Created: April 29, 2025
 * Version: 1.0.0
 */

package com.example.askchinna.util

import android.content.Context
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.askchinna.data.local.SharedPreferencesManager
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Unit tests for SessionManager to verify proper session timeout functionality
 * and usage limit tracking, which are critical for the app's business rules.
 */
@ExperimentalCoroutinesApi
class SessionManagerTest {
    // Executes each task synchronously using Architecture Components
    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    // Test dispatcher for coroutines
    private val testDispatcher = StandardTestDispatcher()

    // Mocks
    private lateinit var context: Context
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var sharedPrefsEditor: SharedPreferences.Editor
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var dateTimeUtils: DateTimeUtils
    private lateinit var sessionManager: SessionManager
    private lateinit var isSessionActiveObserver: Observer<Boolean>
    private lateinit var timeRemainingObserver: Observer<Long>
    private lateinit var usageCountObserver: Observer<Int>
    private lateinit var usageRemainingObserver: Observer<Int>

    // Constants for testing
    private val SESSION_TIMEOUT_MINUTES = 10L
    private val USAGE_LIMIT_PERIOD_DAYS = 30
    private val USAGE_LIMIT_COUNT = 5

    @Before
    fun setup() {
        // Initialize Timber for logging
        Timber.plant(object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                // No-op for tests
            }
        })

        // Set main dispatcher for coroutines
        Dispatchers.setMain(testDispatcher)

        // Create mocks
        context = mockk(relaxed = true)
        sharedPrefs = mockk(relaxed = true)
        sharedPrefsEditor = mockk(relaxed = true)
        dateTimeUtils = mockk(relaxed = true)
        isSessionActiveObserver = mockk(relaxed = true)
        timeRemainingObserver = mockk(relaxed = true)
        usageCountObserver = mockk(relaxed = true)
        usageRemainingObserver = mockk(relaxed = true)

        // Setup SharedPreferences
        every { sharedPrefs.edit() } returns sharedPrefsEditor
        every { sharedPrefsEditor.apply() } returns Unit
        every { sharedPrefsEditor.putLong(any(), any()) } returns sharedPrefsEditor
        every { sharedPrefsEditor.putInt(any(), any()) } returns sharedPrefsEditor
        every { sharedPrefsEditor.putString(any(), any()) } returns sharedPrefsEditor
        every { sharedPrefsEditor.clear() } returns sharedPrefsEditor

        // Setup SharedPreferencesManager
        sharedPreferencesManager = SharedPreferencesManager(context)
        every { context.getSharedPreferences(any(), any()) } returns sharedPrefs

        // Create SessionManager
        sessionManager = spyk(
            SessionManager(
                sharedPreferencesManager,
                dateTimeUtils,
                SESSION_TIMEOUT_MINUTES,
                USAGE_LIMIT_COUNT,
                USAGE_LIMIT_PERIOD_DAYS
            )
        )

        // Observe LiveData
        sessionManager.isSessionActive.observeForever(isSessionActiveObserver)
        sessionManager.sessionTimeRemaining.observeForever(timeRemainingObserver)
        sessionManager.identificationUsageCount.observeForever(usageCountObserver)
        sessionManager.identificationUsageRemaining.observeForever(usageRemainingObserver)
    }

    @After
    fun tearDown() {
        // Clean up observers
        sessionManager.isSessionActive.removeObserver(isSessionActiveObserver)
        sessionManager.sessionTimeRemaining.removeObserver(timeRemainingObserver)
        sessionManager.identificationUsageCount.removeObserver(usageCountObserver)
        sessionManager.identificationUsageRemaining.removeObserver(usageRemainingObserver)

        // Reset main dispatcher
        Dispatchers.resetMain()

        // Clear mocks
        clearAllMocks()

        // Remove Timber tree
        Timber.uprootAll()
    }

    @Test
    fun `startSession sets session start time and is active`() {
        // Given
        val currentTime = System.currentTimeMillis()
        every { dateTimeUtils.getCurrentTimeMillis() } returns currentTime
        every { sharedPrefs.getLong(Constants.PREF_SESSION_START_TIME, 0L) } returns 0L
        every { sharedPrefsEditor.putLong(Constants.PREF_SESSION_START_TIME, currentTime) } returns sharedPrefsEditor

        // When
        sessionManager.startSession()

        // Then
        verify { sharedPrefsEditor.putLong(Constants.PREF_SESSION_START_TIME, currentTime) }
        verify { isSessionActiveObserver.onChanged(true) }
    }

    @Test
    fun `endSession clears session data and updates is active`() {
        // Given
        every { sharedPrefsEditor.remove(Constants.PREF_SESSION_START_TIME) } returns sharedPrefsEditor

        // When
        sessionManager.endSession()

        // Then
        verify { sharedPrefsEditor.remove(Constants.PREF_SESSION_START_TIME) }
        verify { isSessionActiveObserver.onChanged(false) }
    }

    @Test
    fun `isSessionExpired returns true when session timeout exceeded`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val sessionStartTime = currentTime - TimeUnit.MINUTES.toMillis(SESSION_TIMEOUT_MINUTES + 1) // Expired

        every { dateTimeUtils.getCurrentTimeMillis() } returns currentTime
        every { sharedPrefs.getLong(Constants.PREF_SESSION_START_TIME, 0L) } returns sessionStartTime

        // When
        val result = sessionManager.isSessionExpired()

        // Then
        assertTrue(result)
    }

    @Test
    fun `isSessionExpired returns false when session timeout not exceeded`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val sessionStartTime = currentTime - TimeUnit.MINUTES.toMillis(SESSION_TIMEOUT_MINUTES - 1) // Not expired

        every { dateTimeUtils.getCurrentTimeMillis() } returns currentTime
        every { sharedPrefs.getLong(Constants.PREF_SESSION_START_TIME, 0L) } returns sessionStartTime

        // When
        val result = sessionManager.isSessionExpired()

        // Then
        assertFalse(result)
    }

    @Test
    fun `getSessionTimeRemaining returns correct remaining minutes`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val minutesRemaining = 5L
        val sessionStartTime = currentTime - TimeUnit.MINUTES.toMillis(SESSION_TIMEOUT_MINUTES - minutesRemaining)

        every { dateTimeUtils.getCurrentTimeMillis() } returns currentTime
        every { sharedPrefs.getLong(Constants.PREF_SESSION_START_TIME, 0L) } returns sessionStartTime

        // When
        val result = sessionManager.getSessionTimeRemaining()

        // Then
        assertEquals(TimeUnit.MINUTES.toMillis(minutesRemaining), result)
        verify { timeRemainingObserver.onChanged(TimeUnit.MINUTES.toMillis(minutesRemaining)) }
    }

    @Test
    fun `getSessionTimeRemaining returns zero when session expired`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val sessionStartTime = currentTime - TimeUnit.MINUTES.toMillis(SESSION_TIMEOUT_MINUTES + 1) // Expired

        every { dateTimeUtils.getCurrentTimeMillis() } returns currentTime
        every { sharedPrefs.getLong(Constants.PREF_SESSION_START_TIME, 0L) } returns sessionStartTime

        // When
        val result = sessionManager.getSessionTimeRemaining()

        // Then
        assertEquals(0L, result)
    }

    @Test
    fun `refreshSession extends session timeout`() {
        // Given
        val currentTime = System.currentTimeMillis()
        every { dateTimeUtils.getCurrentTimeMillis() } returns currentTime
        every { sharedPrefs.getLong(Constants.PREF_SESSION_START_TIME, 0L) } returns currentTime - 60000 // 1 minute old

        // When
        sessionManager.refreshSession()

        // Then
        verify { sharedPrefsEditor.putLong(Constants.PREF_SESSION_START_TIME, currentTime) }
        verify { isSessionActiveObserver.onChanged(true) }
    }

    @Test
    fun `incrementIdentificationUsage increases usage count and updates remaining`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val currentUsage = 2
        val periodStart = currentTime - TimeUnit.DAYS.toMillis(15) // Middle of period

        every { dateTimeUtils.getCurrentTimeMillis() } returns currentTime
        every { sharedPrefs.getInt(Constants.PREF_IDENTIFICATION_COUNT, 0) } returns currentUsage
        every { sharedPrefs.getLong(Constants.PREF_IDENTIFICATION_PERIOD_START, any()) } returns periodStart
        every {
            sharedPrefsEditor.putInt(Constants.PREF_IDENTIFICATION_COUNT, currentUsage + 1)
        } returns sharedPrefsEditor

        // When
        sessionManager.incrementIdentificationUsage()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { sharedPrefsEditor.putInt(Constants.PREF_IDENTIFICATION_COUNT, currentUsage + 1) }
        verify { usageCountObserver.onChanged(currentUsage + 1) }
        verify { usageRemainingObserver.onChanged(USAGE_LIMIT_COUNT - (currentUsage + 1)) }
    }

    @Test
    fun `hasExceededUsageLimit returns true when limit exceeded`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val currentUsage = USAGE_LIMIT_COUNT
        val periodStart = currentTime - TimeUnit.DAYS.toMillis(15) // Middle of period

        every { dateTimeUtils.getCurrentTimeMillis() } returns currentTime
        every { sharedPrefs.getInt(Constants.PREF_IDENTIFICATION_COUNT, 0) } returns currentUsage
        every { sharedPrefs.getLong(Constants.PREF_IDENTIFICATION_PERIOD_START, any()) } returns periodStart

        // When
        val result = sessionManager.hasExceededUsageLimit()

        // Then
        assertTrue(result)
    }

    @Test
    fun `hasExceededUsageLimit returns false when limit not exceeded`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val currentUsage = USAGE_LIMIT_COUNT - 1
        val periodStart = currentTime - TimeUnit.DAYS.toMillis(15) // Middle of period

        every { dateTimeUtils.getCurrentTimeMillis() } returns currentTime
        every { sharedPrefs.getInt(Constants.PREF_IDENTIFICATION_COUNT, 0) } returns currentUsage
        every { sharedPrefs.getLong(Constants.PREF_IDENTIFICATION_PERIOD_START, any()) } returns periodStart

        // When
        val result = sessionManager.hasExceededUsageLimit()

        // Then
        assertFalse(result)
    }

    @Test
    fun `resetUsagePeriod resets count and starts new period`() {
        // Given
        val currentTime = System.currentTimeMillis()

        every { dateTimeUtils.getCurrentTimeMillis() } returns currentTime
        every { sharedPrefsEditor.putInt(Constants.PREF_IDENTIFICATION_COUNT, 0) } returns sharedPrefsEditor
        every { sharedPrefsEditor.putLong(Constants.PREF_IDENTIFICATION_PERIOD_START, currentTime) } returns sharedPrefsEditor

        // When
        sessionManager.resetUsagePeriod()

        // Then
        verify { sharedPrefsEditor.putInt(Constants.PREF_IDENTIFICATION_COUNT, 0) }
        verify { sharedPrefsEditor.putLong(Constants.PREF_IDENTIFICATION_PERIOD_START, currentTime) }
        verify { usageCountObserver.onChanged(0) }
        verify { usageRemainingObserver.onChanged(USAGE_LIMIT_COUNT) }
    }

    @Test
    fun `checkUsagePeriod resets when period expired`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val oldPeriodStart = currentTime - TimeUnit.DAYS.toMillis(USAGE_LIMIT_PERIOD_DAYS + 1) // Expired

        every { dateTimeUtils.getCurrentTimeMillis() } returns currentTime
        every { sharedPrefs.getLong(Constants.PREF_IDENTIFICATION_PERIOD_START, currentTime) } returns oldPeriodStart
        every { sharedPrefsEditor.putInt(Constants.PREF_IDENTIFICATION_COUNT, 0) } returns sharedPrefsEditor
        every { sharedPrefsEditor.putLong(Constants.PREF_IDENTIFICATION_PERIOD_START, currentTime) } returns sharedPrefsEditor

        // When
        sessionManager.checkUsagePeriod()

        // Then
        verify { sharedPrefsEditor.putInt(Constants.PREF_IDENTIFICATION_COUNT, 0) }
        verify { sharedPrefsEditor.putLong(Constants.PREF_IDENTIFICATION_PERIOD_START, currentTime) }
    }

    @Test
    fun `checkUsagePeriod does not reset when period not expired`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val periodStart = currentTime - TimeUnit.DAYS.toMillis(USAGE_LIMIT_PERIOD_DAYS - 1) // Not expired

        every { dateTimeUtils.getCurrentTimeMillis() } returns currentTime
        every { sharedPrefs.getLong(Constants.PREF_IDENTIFICATION_PERIOD_START, currentTime) } returns periodStart

        // When
        sessionManager.checkUsagePeriod()

        // Then
        verify(exactly = 0) { sharedPrefsEditor.putInt(Constants.PREF_IDENTIFICATION_COUNT, any()) }
        verify(exactly = 0) { sharedPrefsEditor.putLong(Constants.PREF_IDENTIFICATION_PERIOD_START, any()) }
    }

    @Test
    fun `getRemainingDaysInPeriod calculates correct days`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val daysElapsed = 10
        val periodStart = currentTime - TimeUnit.DAYS.toMillis(daysElapsed.toLong())
        val expectedRemainingDays = USAGE_LIMIT_PERIOD_DAYS - daysElapsed

        every { dateTimeUtils.getCurrentTimeMillis() } returns currentTime
        every { sharedPrefs.getLong(Constants.PREF_IDENTIFICATION_PERIOD_START, currentTime) } returns periodStart
        every {
            dateTimeUtils.getDaysBetween(periodStart, currentTime)
        } returns daysElapsed

        // When
        val result = sessionManager.getRemainingDaysInPeriod()

        // Then
        assertEquals(expectedRemainingDays, result)
    }

    @Test
    fun `getIdentificationUsageRemaining calculates correct remaining usage`() {
        // Given
        val currentUsage = 3

        every { sharedPrefs.getInt(Constants.PREF_IDENTIFICATION_COUNT, 0) } returns currentUsage

        // When
        val result = sessionManager.getIdentificationUsageRemaining()

        // Then
        assertEquals(USAGE_LIMIT_COUNT - currentUsage, result)
        verify { usageRemainingObserver.onChanged(USAGE_LIMIT_COUNT - currentUsage) }
    }
}
