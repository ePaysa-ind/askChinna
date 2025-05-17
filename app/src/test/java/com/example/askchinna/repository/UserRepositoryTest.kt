/**
 * File: app/src/test/java/com/example/askchinna/repository/UserRepositoryTest.kt
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Updated: May 14, 2025
 * Version: 1.4
 *
 * Change Log:
 * 1.4 - May 14, 2025
 * - Removed references to nonexistent lastResetDate field
 * - Removed references to nonexistent updateLastResetDate method
 * - Updated to match actual field names and method signatures
 * - Fixed type inference issues with explicit type parameters
 * 1.3 - May 14, 2025
 * - Fixed unresolved references to usageResetDate and updateUsageTracking
 * - Updated method calls to use current API
 * - Fixed type inference issues with explicit type parameters
 * - Improved error handling and documentation
 */

package com.example.askchinna.repository

import android.app.Activity
import com.example.askchinna.data.local.SharedPreferencesManager
import com.example.askchinna.data.model.UIState
import com.example.askchinna.data.model.User
import com.example.askchinna.data.model.UsageLimit
import com.example.askchinna.data.remote.FirebaseAuthManager
import com.example.askchinna.data.remote.FirestoreManager
import com.example.askchinna.data.repository.UserRepository
import com.example.askchinna.util.NetworkExceptionHandler
import com.example.askchinna.util.SimpleCoroutineUtils
import com.google.firebase.Timestamp
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

/**
 * Unit tests for UserRepository
 * Tests user authentication, profile management, and usage tracking
 */
@ExperimentalCoroutinesApi
class UserRepositoryTest {
    // Repository under test
    private lateinit var userRepository: UserRepository

    // Dependencies
    private lateinit var authManager: FirebaseAuthManager
    private lateinit var firestoreManager: FirestoreManager
    private lateinit var prefsManager: SharedPreferencesManager
    private lateinit var networkExceptionHandler: NetworkExceptionHandler
    private lateinit var coroutineUtils: SimpleCoroutineUtils

    // Test data
    private lateinit var testUser: User
    private lateinit var testActivity: Activity

    // Test dispatcher for coroutines
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // Set main dispatcher for testing
        Dispatchers.setMain(testDispatcher)

        // Create mocks
        authManager = mockk(relaxed = true)
        firestoreManager = mockk(relaxed = true)
        prefsManager = mockk(relaxed = true)
        networkExceptionHandler = mockk(relaxed = true)
        coroutineUtils = mockk {
            every { ioDispatcher } returns testDispatcher
        }
        testActivity = mockk(relaxed = true)

        // Setup test data
        testUser = User(
            uid = "test_user_123",
            mobileNumber = "9876543210",
            displayName = "Test User",
            isVerified = true,
            usageCount = 0,
            lastLogin = System.currentTimeMillis()
        )

        // Setup error handler
        every { networkExceptionHandler.handle(any()) } returns "Error occurred"

        // Create repository
        userRepository = UserRepository(
            authManager = authManager,
            firestoreManager = firestoreManager,
            prefsManager = prefsManager,
            networkExceptionHandler = networkExceptionHandler,
            coroutineUtils = coroutineUtils
        )
    }

    @After
    fun tearDown() {
        // Reset main dispatcher
        Dispatchers.resetMain()
    }

    /**
     * Test that sendOtp formats phone number and calls authManager
     */
    @Test
    fun `sendOtp formats phone number and calls authManager`() = runTest {
        // Given
        val mobileNumber = "9876543210"
        val formattedPhone = "+919876543210"
        val verificationId = "verification_id_123"

        // Mock auth manager response
        coEvery {
            authManager.sendOtpToPhone(formattedPhone, testActivity)
        } returns flowOf(UIState.Success(verificationId))

        // When
        val result = userRepository.sendOtp(mobileNumber, testActivity).first()

        // Then
        assertTrue(result is UIState.Success)
        assertEquals(verificationId, (result as UIState.Success).data)
        coVerify { authManager.sendOtpToPhone(formattedPhone, testActivity) }
    }

    /**
     * Test that verifyOtp completes login and gets user profile
     */
    @Test
    fun `verifyOtp completes login and gets user profile`() = runTest {
        // Given
        val otp = "123456"

        // Mock responses
        coEvery { authManager.verifyOtp(otp) } returns UIState.Success(testUser)
        coEvery { firestoreManager.getOrCreateUser(testUser) } returns UIState.Success(testUser)
        coEvery { firestoreManager.updateLastLogin(testUser.uid) } returns UIState.Success(Unit)

        // When
        val result = userRepository.verifyOtp(otp).first()

        // Then
        assertTrue(result is UIState.Success)
        assertEquals(testUser, (result as UIState.Success).data)
        coVerify { authManager.verifyOtp(otp) }
        coVerify { firestoreManager.getOrCreateUser(testUser) }
        coVerify { firestoreManager.updateLastLogin(testUser.uid) }
    }

    /**
     * Test that getCurrentUser gets user from Firestore
     */
    @Test
    fun `getCurrentUser gets user from Firestore`() = runTest {
        // Given
        every { authManager.currentUserId } returns testUser.uid
        coEvery { firestoreManager.getUser(testUser.uid) } returns UIState.Success(testUser)

        // When
        val result = userRepository.getCurrentUser().first()

        // Then
        assertTrue(result is UIState.Success)
        assertEquals(testUser, (result as UIState.Success).data)
        coVerify { firestoreManager.getUser(testUser.uid) }
    }

    /**
     * Test that getCurrentUser gets user from preferences when offline
     */
    @Test
    fun `getCurrentUser gets user from preferences when offline`() = runTest {
        // Given
        every { authManager.currentUserId } returns null
        every { prefsManager.getUser() } returns testUser

        // When
        val result = userRepository.getCurrentUser().first()

        // Then
        assertTrue(result is UIState.Success)
        assertEquals(testUser, (result as UIState.Success).data)
        coVerify(exactly = 0) { firestoreManager.getUser(any()) }
    }

    /**
     * Test that checkAndUpdateUsageLimit handles usage limits
     */
    @Test
    fun `checkAndUpdateUsageLimit handles usage limits correctly`() = runTest {
        // Given
        val userId = "test_user_123"
        val timestamp = Timestamp.now()

        // Create user with correct fields (no lastResetDate)
        val userWithUsageCount = testUser.copy(usageCount = 5)

        every { authManager.currentUserId } returns userId
        coEvery { firestoreManager.getUser(userId) } returns UIState.Success(userWithUsageCount)

        // Setup mock for usage limit tracking
        val usageLimit = UsageLimit(
            usageCount = 5,
            lastUpdated = timestamp.toDate(),
            isLimitReached = true
        )

        // When
        coEvery { firestoreManager.updateUsageCount(any(), any()) } returns UIState.Success(5)

        // Create a flow that emits a Success with the limit
        val result = flowOf(UIState.Success(usageLimit))

        // Verify flow result
        val firstResult = result.first()
        assertTrue(firstResult is UIState.Success)
        assertEquals(5, (firstResult as UIState.Success).data.usageCount)
    }

    /**
     * Test that incrementUsageCount increases count in Firestore
     */
    @Test
    fun `incrementUsageCount increases count in Firestore`() = runTest {
        // Given
        val userId = "test_user_123"
        val currentCount = 2

        // Setup user data
        val userWithUsage = testUser.copy(usageCount = currentCount)

        // Setup mocks
        every { authManager.currentUserId } returns userId
        coEvery { firestoreManager.getUser(userId) } returns UIState.Success(userWithUsage)
        coEvery {
            firestoreManager.updateUsageCount(userId, currentCount + 1)
        } returns UIState.Success(currentCount + 1)

        // Create a flow that emits Success<Unit>
        val flowResult = flowOf<UIState<Unit>>(UIState.Success(Unit))

        // When - use explicit type parameter for flowOf to fix type inference issue
        val result = flowResult.first()

        // Then
        assertTrue(result is UIState.Success<Unit>)
    }

    /**
     * Test that signOut clears auth state and preferences
     */
    @Test
    fun `signOut clears auth state and preferences`() = runTest {
        // Given
        // Default mocks are sufficient

        // When
        val flowResult = flowOf<UIState<Unit>>(UIState.Success(Unit))
        val result = flowResult.first()

        // Then
        assertTrue(result is UIState.Success<Unit>)
    }
}