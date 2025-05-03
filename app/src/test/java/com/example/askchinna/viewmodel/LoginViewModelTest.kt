/**
 * app/src/test/java/com/askchinna/viewmodel/LoginViewModelTest.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Updated: May 1, 2025
 * Version: 1.2
 *
 * Updated to match current LoginViewModel implementation
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class LoginViewModelTest {

    // Rule to make LiveData work synchronously in tests
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test dispatcher for controlling coroutines
    private val testDispatcher = StandardTestDispatcher()

    // Mocks
    private lateinit var userRepository: UserRepository
    private lateinit var coroutineUtils: SimpleCoroutineUtils
    private lateinit var viewModel: LoginViewModel
    private lateinit var mockActivity: Activity

    // Observer for LiveData
    private lateinit var otpSendStateObserver: Observer<UIState<String>>
    private lateinit var autoLoginStateObserver: Observer<Boolean>

    @Before
    fun setup() {
        // Set main dispatcher for coroutines testing
        Dispatchers.setMain(testDispatcher)

        // Create mocks
        userRepository = mockk(relaxed = true)
        coroutineUtils = mockk(relaxed = true)
        mockActivity = mockk(relaxed = true)

        // Set up coroutine utilities to use test dispatcher
        every { coroutineUtils.ioDispatcher } returns testDispatcher

        // Create observers
        otpSendStateObserver = mockk(relaxed = true)
        autoLoginStateObserver = mockk(relaxed = true)

        // Initialize ViewModel with mocked dependencies
        viewModel = LoginViewModel(userRepository, coroutineUtils)

        // Observe LiveData
        viewModel.otpSendState.observeForever(otpSendStateObserver)
        viewModel.autoLoginState.observeForever(autoLoginStateObserver)
    }

    @After
    fun tearDown() {
        // Reset main dispatcher
        Dispatchers.resetMain()

        // Remove observers
        viewModel.otpSendState.removeObserver(otpSendStateObserver)
        viewModel.autoLoginState.removeObserver(autoLoginStateObserver)
    }

    @Test
    fun `sendOtp calls repository and updates LiveData on success`() = runTest {
        // Test data
        val mobileNumber = "9876543210"
        val verificationId = "test_verification_id"

        // Mock repository response for success
        coEvery { userRepository.sendOtp(mobileNumber, mockActivity) } returns
                flow {
                    emit(UIState.Loading())
                    emit(UIState.Success(verificationId))
                }

        // Call the method
        viewModel.sendOtp(mobileNumber, mockActivity)

        // Advance coroutines to complete
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify repository was called with correct parameters
        coVerify { userRepository.sendOtp(mobileNumber, mockActivity) }

        // Verify LiveData emissions happened in correct order
        verify(exactly = 1) { otpSendStateObserver.onChanged(match { it is UIState.Loading }) }
        verify(exactly = 1) { otpSendStateObserver.onChanged(match {
            it is UIState.Success && it.data == verificationId
        })}
    }

    @Test
    fun `sendOtp updates LiveData with error state when repository fails`() = runTest {
        // Test data
        val mobileNumber = "9876543210"
        val errorMessage = "Failed to send OTP"

        // Mock repository response for error
        coEvery { userRepository.sendOtp(mobileNumber, mockActivity) } returns
                flow {
                    emit(UIState.Loading())
                    emit(UIState.Error(errorMessage))
                }

        // Call the method
        viewModel.sendOtp(mobileNumber, mockActivity)

        // Advance coroutines to complete
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify repository was called
        coVerify { userRepository.sendOtp(mobileNumber, mockActivity) }

        // Verify LiveData emissions
        verify(exactly = 1) { otpSendStateObserver.onChanged(match { it is UIState.Loading }) }
        verify(exactly = 1) { otpSendStateObserver.onChanged(match {
            it is UIState.Error && it.message == errorMessage
        })}
    }

    @Test
    fun `initialization checks authentication status - authenticated user`() = runTest {
        // Create a fresh ViewModel to test initialization behavior
        every { coroutineUtils.ioDispatcher } returns testDispatcher

        // Mock authenticated user response
        val mockUser = User(uid = "user_id", mobileNumber = "+919876543210")
        coEvery { userRepository.getCurrentUser() } returns
                flow {
                    emit(UIState.Loading())
                    emit(UIState.Success(mockUser))
                }

        // Create new ViewModel instance which triggers init block
        val newViewModel = LoginViewModel(userRepository, coroutineUtils)
        newViewModel.autoLoginState.observeForever(autoLoginStateObserver)

        // Advance coroutines to complete
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify repository was called
        coVerify { userRepository.getCurrentUser() }

        // Verify LiveData was updated correctly - should be true for authenticated user
        verify { autoLoginStateObserver.onChanged(true) }

        // Clean up
        newViewModel.autoLoginState.removeObserver(autoLoginStateObserver)
    }

    @Test
    fun `initialization checks authentication status - unauthenticated user`() = runTest {
        // Create a fresh ViewModel to test initialization behavior
        every { coroutineUtils.ioDispatcher } returns testDispatcher

        // Mock unauthenticated user response
        coEvery { userRepository.getCurrentUser() } returns
                flow {
                    emit(UIState.Loading())
                    emit(UIState.Error("User not authenticated"))
                }

        // Create new ViewModel instance which triggers init block
        val newViewModel = LoginViewModel(userRepository, coroutineUtils)
        newViewModel.autoLoginState.observeForever(autoLoginStateObserver)

        // Advance coroutines to complete
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify repository was called
        coVerify { userRepository.getCurrentUser() }

        // Verify LiveData was updated correctly - should be false for unauthenticated user
        verify { autoLoginStateObserver.onChanged(false) }

        // Clean up
        newViewModel.autoLoginState.removeObserver(autoLoginStateObserver)
    }
}