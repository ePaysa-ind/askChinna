/**
 * File: app/src/test/java/com/example/askchinna/viewmodel/LoginViewModelTest.kt
 * Copyright (c) 2025 askChinna
 * Created: April 29, 2025
 * Updated: May 14, 2025
 * Version: 1.3
 *
 * Change Log:
 * 1.3 - May 14, 2025
 * - Replaced deprecated TestCoroutineDispatcher with StandardTestDispatcher
 * - Replaced deprecated advanceUntilIdle() with scheduler.advanceUntilIdle()
 * - Improved test structure and error handling
 * 1.2 - May 14, 2025
 * - Added missing displayName parameter to User constructor
 * - Updated imports to remove unused ones
 * - Added proper error handling and logging
 * - Improved test case documentation
 */

package com.example.askchinna.viewmodel

import android.app.Activity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.askchinna.data.model.UIState
import com.example.askchinna.data.model.User
import com.example.askchinna.data.repository.UserRepository
import com.example.askchinna.ui.auth.LoginViewModel
import com.example.askchinna.util.SimpleCoroutineUtils
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for LoginViewModel
 * Tests authentication flow, OTP handling, and error scenarios
 */
@ExperimentalCoroutinesApi
class LoginViewModelTest {
    // ExecutorRule to make LiveData work synchronously in tests
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test dispatcher for controlled coroutine execution
    private val testDispatcher = StandardTestDispatcher()

    // Mocks
    private lateinit var userRepository: UserRepository
    private lateinit var coroutineUtils: SimpleCoroutineUtils
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var otpStateObserver: Observer<UIState<String>>
    private lateinit var autoLoginStateObserver: Observer<Boolean>

    @Before
    fun setUp() {
        // Set main dispatcher for testing
        Dispatchers.setMain(testDispatcher)

        // Create mocks with relaxed behavior
        userRepository = mockk(relaxed = true)
        coroutineUtils = mockk {
            every { ioDispatcher } returns testDispatcher
        }

        // Create observers for LiveData
        otpStateObserver = mockk(relaxed = true)
        autoLoginStateObserver = mockk(relaxed = true)

        // Create the view model under test
        loginViewModel = LoginViewModel(userRepository, coroutineUtils)

        // Observe LiveData
        loginViewModel.otpSendState.observeForever(otpStateObserver)
        loginViewModel.autoLoginState.observeForever(autoLoginStateObserver)
    }

    @After
    fun tearDown() {
        // Remove observers to prevent memory leaks
        loginViewModel.otpSendState.removeObserver(otpStateObserver)
        loginViewModel.autoLoginState.removeObserver(autoLoginStateObserver)

        // Reset main dispatcher
        Dispatchers.resetMain()
    }

    /**
     * Test that sendOtp updates state with success when OTP is sent successfully
     */
    @Test
    fun `sendOtp updates state with success when OTP sent successfully`() = runTest {
        // Given
        val mobileNumber = "9876543210"
        val activity = mockk<Activity>(relaxed = true)
        val verificationId = "verification_id_123"

        coEvery {
            userRepository.sendOtp(mobileNumber, activity)
        } returns flowOf(UIState.Success(verificationId))

        // When
        loginViewModel.sendOtp(mobileNumber, activity)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify {
            otpStateObserver.onChanged(match {
                it is UIState.Loading
            })
        }
        verify {
            otpStateObserver.onChanged(match {
                it is UIState.Success && it.data == verificationId
            })
        }
        coVerify { userRepository.sendOtp(mobileNumber, activity) }
    }

    /**
     * Test that sendOtp updates state with error when OTP send fails
     */
    @Test
    fun `sendOtp updates state with error when OTP send fails`() = runTest {
        // Given
        val mobileNumber = "9876543210"
        val activity = mockk<Activity>(relaxed = true)
        val errorMessage = "Failed to send OTP"

        coEvery {
            userRepository.sendOtp(mobileNumber, activity)
        } returns flowOf(UIState.Error(errorMessage))

        // When
        loginViewModel.sendOtp(mobileNumber, activity)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify {
            otpStateObserver.onChanged(match {
                it is UIState.Loading
            })
        }
        verify {
            otpStateObserver.onChanged(match {
                it is UIState.Error && it.message == errorMessage
            })
        }
        coVerify { userRepository.sendOtp(mobileNumber, activity) }
    }

    /**
     * Test that checkAuthentication sets autoLoginState true when user is authenticated
     */
    @Test
    fun `checkAuthentication sets autoLoginState true when user is authenticated`() = runTest {
        // Given
        val user = User(
            uid = "user_123",
            mobileNumber = "9876543210",
            displayName = "Test User", // Added required displayName parameter
            isVerified = true
        )

        coEvery {
            userRepository.getCurrentUser()
        } returns flowOf(UIState.Success(user))

        // When (checkAuthentication is called in init block of LoginViewModel)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify {
            autoLoginStateObserver.onChanged(true)
        }
        coVerify { userRepository.getCurrentUser() }
    }

    /**
     * Test that checkAuthentication sets autoLoginState false when user is not authenticated
     */
    @Test
    fun `checkAuthentication sets autoLoginState false when user is not authenticated`() = runTest {
        // Given
        val errorMessage = "User not authenticated"

        coEvery {
            userRepository.getCurrentUser()
        } returns flowOf(UIState.Error(errorMessage))

        // When (checkAuthentication is called in init block of LoginViewModel)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify {
            autoLoginStateObserver.onChanged(false)
        }
        coVerify { userRepository.getCurrentUser() }
    }
}