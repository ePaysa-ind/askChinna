package com.example.askchinna.viewmodel

/**
 * app/src/test/java/com/askchinna/viewmodel/LoginViewModelTest.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.askchinna.data.model.UIState
import com.askchinna.data.model.User
import com.askchinna.data.repository.UserRepository
import com.askchinna.ui.auth.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class LoginViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var userRepository: UserRepository

    private lateinit var viewModel: LoginViewModel
    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `validateMobileNumber returns true for valid Indian mobile number`() {
        // Valid Indian mobile number with country code
        val validMobileNumber = "+919876543210"

        // Call method that performs validation
        val result = viewModel.validateMobileNumber(validMobileNumber)

        // Check validation result
        assert(result)
        assert(viewModel.mobileNumberError.value == null)
    }

    @Test
    fun `validateMobileNumber returns false for invalid Indian mobile number`() {
        // Invalid number - too short
        val invalidMobileNumber = "+9198765"

        // Call method that performs validation
        val result = viewModel.validateMobileNumber(invalidMobileNumber)

        // Check validation result
        assert(!result)
        assert(viewModel.mobileNumberError.value != null)
    }

    @Test
    fun `validateMobileNumber returns false for non-Indian country code`() {
        // Non-Indian country code
        val nonIndianNumber = "+14155552671"

        // Call method that performs validation
        val result = viewModel.validateMobileNumber(nonIndianNumber)

        // Check validation result
        assert(!result)
        assert(viewModel.mobileNumberError.value != null)
    }

    @Test
    fun `validateMobileNumber returns false for empty mobile number`() {
        // Empty mobile number
        val emptyMobileNumber = ""

        // Call method that performs validation
        val result = viewModel.validateMobileNumber(emptyMobileNumber)

        // Check validation result
        assert(!result)
        assert(viewModel.mobileNumberError.value != null)
    }

    @Test
    fun `startPhoneAuthentication calls repository and updates state on success`() = runBlockingTest {
        // Valid mobile number
        val mobileNumber = "+919876543210"

        // Mock success response
        val verificationId = "test_verification_id"
        `when`(userRepository.startPhoneNumberVerification(mobileNumber)).thenReturn(
            flow {
                emit(UIState.Loading)
                emit(UIState.Success(verificationId))
            }
        )

        // Call method
        viewModel.startPhoneAuthentication(mobileNumber)

        // Verify repository was called
        verify(userRepository).startPhoneNumberVerification(mobileNumber)

        // Verify state was updated
        assert(viewModel.phoneAuthState.value is UIState.Success)
        assert((viewModel.phoneAuthState.value as UIState.Success<String>).data == verificationId)
    }

    @Test
    fun `startPhoneAuthentication updates state on error`() = runBlockingTest {
        // Valid mobile number
        val mobileNumber = "+919876543210"

        // Mock error response
        val errorMessage = "Failed to send OTP"
        `when`(userRepository.startPhoneNumberVerification(mobileNumber)).thenReturn(
            flow {
                emit(UIState.Loading)
                emit(UIState.Error(errorMessage))
            }
        )

        // Call method
        viewModel.startPhoneAuthentication(mobileNumber)

        // Verify repository was called
        verify(userRepository).startPhoneNumberVerification(mobileNumber)

        // Verify state was updated with error
        assert(viewModel.phoneAuthState.value is UIState.Error)
        assert((viewModel.phoneAuthState.value as UIState.Error).message == errorMessage)
    }

    @Test
    fun `verifyOtp calls repository and updates state on success`() = runBlockingTest {
        // Test verification ID and OTP
        val verificationId = "test_verification_id"
        val otp = "123456"

        // Mock success response
        val mockUser = User(id = "user_id", mobileNumber = "+919876543210", name = "Test User")
        `when`(userRepository.verifyOtp(verificationId, otp)).thenReturn(
            flow {
                emit(UIState.Loading)
                emit(UIState.Success(mockUser))
            }
        )

        // Set verification ID in ViewModel
        viewModel.verificationId = verificationId

        // Call method
        viewModel.verifyOtp(otp)

        // Verify repository was called
        verify(userRepository).verifyOtp(verificationId, otp)

        // Verify state was updated
        assert(viewModel.loginState.value is UIState.Success)
        assert((viewModel.loginState.value as UIState.Success<User>).data.id == mockUser.id)
    }

    @Test
    fun `verifyOtp updates state on error`() = runBlockingTest {
        // Test verification ID and OTP
        val verificationId = "test_verification_id"
        val otp = "123456"

        // Mock error response
        val errorMessage = "Invalid OTP"
        `when`(userRepository.verifyOtp(verificationId, otp)).thenReturn(
            flow {
                emit(UIState.Loading)
                emit(UIState.Error(errorMessage))
            }
        )

        // Set verification ID in ViewModel
        viewModel.verificationId = verificationId

        // Call method
        viewModel.verifyOtp(otp)

        // Verify repository was called
        verify(userRepository).verifyOtp(verificationId, otp)

        // Verify state was updated with error
        assert(viewModel.loginState.value is UIState.Error)
        assert((viewModel.loginState.value as UIState.Error).message == errorMessage)
    }

    @Test
    fun `register calls repository with mobile number and name`() = runBlockingTest {
        // Test data
        val mobileNumber = "+919876543210"
        val name = "Test User"

        // Mock success response
        val mockUser = User(id = "user_id", mobileNumber = mobileNumber, name = name)
        `when`(userRepository.registerUser(mobileNumber, name)).thenReturn(
            flow {
                emit(UIState.Loading)
                emit(UIState.Success(mockUser))
            }
        )

        // Call method
        viewModel.register(mobileNumber, name)

        // Verify repository was called
        verify(userRepository).registerUser(mobileNumber, name)

        // Verify state was updated
        assert(viewModel.registrationState.value is UIState.Success)
    }

    @Test
    fun `validateInput returns false when mobile number is invalid`() {
        // Invalid mobile number
        val mobileNumber = "12345"
        val name = "Test User"

        // Call method
        val result = viewModel.validateRegistrationInput(mobileNumber, name)

        // Verify result
        assert(!result)
        assert(viewModel.mobileNumberError.value != null)
    }

    @Test
    fun `validateInput returns false when name is empty`() {
        // Valid mobile number but empty name
        val mobileNumber = "+919876543210"
        val name = ""

        // Call method
        val result = viewModel.validateRegistrationInput(mobileNumber, name)

        // Verify result
        assert(!result)
        assert(viewModel.nameError.value != null)
    }

    @Test
    fun `validateInput returns true when all inputs are valid`() {
        // Valid inputs
        val mobileNumber = "+919876543210"
        val name = "Test User"

        // Call method
        val result = viewModel.validateRegistrationInput(mobileNumber, name)

        // Verify result
        assert(result)
        assert(viewModel.mobileNumberError.value == null)
        assert(viewModel.nameError.value == null)
    }

    @Test
    fun `resendOtp calls repository and updates state`() = runBlockingTest {
        // Valid mobile number
        val mobileNumber = "+919876543210"

        // Mock success response
        val verificationId = "new_verification_id"
        `when`(userRepository.resendOtp(mobileNumber)).thenReturn(
            flow {
                emit(UIState.Loading)
                emit(UIState.Success(verificationId))
            }
        )

        // Call method
        viewModel.resendOtp(mobileNumber)

        // Verify repository was called
        verify(userRepository).resendOtp(mobileNumber)

        // Verify state was updated
        assert(viewModel.resendOtpState.value is UIState.Success)
        assert((viewModel.resendOtpState.value as UIState.Success<String>).data == verificationId)
    }

    @Test
    fun `formatMobileNumber adds country code if missing`() {
        // Mobile number without country code
        val mobileNumber = "9876543210"

        // Call method
        val formattedNumber = viewModel.formatMobileNumber(mobileNumber)

        // Verify country code was added
        assert(formattedNumber == "+919876543210")
    }

    @Test
    fun `formatMobileNumber preserves existing country code`() {
        // Mobile number with country code
        val mobileNumber = "+919876543210"

        // Call method
        val formattedNumber = viewModel.formatMobileNumber(mobileNumber)

        // Verify number was not changed
        assert(formattedNumber == mobileNumber)
    }
}