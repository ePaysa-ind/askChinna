package com.example.askchinna.repository

/**
 * app/src/test/java/com/askchinna/repository/UserRepositoryTest.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */


import com.askchinna.data.model.UIState
import com.askchinna.data.model.UsageLimit
import com.askchinna.data.model.User
import com.askchinna.data.remote.FirebaseAuthManager
import com.askchinna.data.remote.FirestoreManager
import com.askchinna.data.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class UserRepositoryTest {

    @Mock
    private lateinit var firebaseAuthManager: FirebaseAuthManager

    @Mock
    private lateinit var firestoreManager: FirestoreManager

    private lateinit var userRepository: UserRepository

    @Before
    fun setup() {
        userRepository = UserRepository(firebaseAuthManager, firestoreManager)
    }

    @Test
    fun `getCurrentUser emits success state when user retrieval succeeds`() = runBlockingTest {
        // Mock response data
        val user = User(
            id = "user123",
            mobileNumber = "+919876543210",
            name = "Test User"
        )

        // Set up mock
        `when`(firestoreManager.getCurrentUser()).thenReturn(Result.success(user))

        // Call repository method
        val flow = userRepository.getCurrentUser()
        val results = flow.toList()

        // Verify the correct states are emitted
        assert(results.size == 2)
        assert(results[0] is UIState.Loading)
        assert(results[1] is UIState.Success)
        assert((results[1] as UIState.Success<User>).data == user)
    }

    @Test
    fun `getCurrentUser emits error state when user retrieval fails`() = runBlockingTest {
        // Set up mock
        val errorMessage = "User document does not exist"
        `when`(firestoreManager.getCurrentUser()).thenReturn(
            Result.failure(Exception(errorMessage))
        )

        // Call repository method
        val flow = userRepository.getCurrentUser()
        val results = flow.toList()

        // Verify the correct states are emitted
        assert(results.size == 2)
        assert(results[0] is UIState.Loading)
        assert(results[1] is UIState.Error)
        assert((results[1] as UIState.Error).message == errorMessage)
    }

    @Test
    fun `isUserLoggedIn delegates to authManager`() {
        // Set up mock
        `when`(firebaseAuthManager.isUserLoggedIn()).thenReturn(true)

        // Call repository method
        val result = userRepository.isUserLoggedIn()

        // Verify delegation and result
        verify(firebaseAuthManager).isUserLoggedIn()
        assert(result)
    }

    @Test
    fun `startPhoneNumberVerification emits success state with verification ID`() = runBlockingTest {
        // Test data
        val phoneNumber = "+919876543210"
        val verificationId = "test_verification_id"

        // Set up mock
        `when`(firebaseAuthManager.sendVerificationCode(phoneNumber)).thenReturn(
            Result.success(verificationId)
        )

        // Call repository method
        val flow = userRepository.startPhoneNumberVerification(phoneNumber)
        val results = flow.toList()

        // Verify the correct states are emitted
        assert(results.size == 2)
        assert(results[0] is UIState.Loading)
        assert(results[1] is UIState.Success)
        assert((results[1] as UIState.Success<String>).data == verificationId)

        // Verify auth manager was called
        verify(firebaseAuthManager).sendVerificationCode(phoneNumber)
    }

    @Test
    fun `startPhoneNumberVerification emits error state when verification fails`() = runBlockingTest {
        // Test data
        val phoneNumber = "+919876543210"
        val errorMessage = "Failed to send verification code"

        // Set up mock
        `when`(firebaseAuthManager.sendVerificationCode(phoneNumber)).thenReturn(
            Result.failure(Exception(errorMessage))
        )

        // Call repository method
        val flow = userRepository.startPhoneNumberVerification(phoneNumber)
        val results = flow.toList()

        // Verify the correct states are emitted
        assert(results.size == 2)
        assert(results[0] is UIState.Loading)
        assert(results[1] is UIState.Error)
        assert((results[1] as UIState.Error).message == errorMessage)
    }

    @Test
    fun `verifyOtp emits success state with user data on successful verification`() = runBlockingTest {
        // Test data
        val verificationId = "test_verification_id"
        val otp = "123456"
        val userId = "user123"

        // Set up mocks
        `when`(firebaseAuthManager.verifyOtp(verificationId, otp)).thenReturn(
            Result.success(userId)
        )

        val user = User(
            id = userId,
            mobileNumber = "+919876543210",
            name = "Test User"
        )

        `when`(firestoreManager.getCurrentUser()).thenReturn(
            Result.success(user)
        )

        // Call repository method
        val flow = userRepository.verifyOtp(verificationId, otp)
        val results = flow.toList()

        // Verify the correct states are emitted
        assert(results.size == 2)
        assert(results[0] is UIState.Loading)
        assert(results[1] is UIState.Success)
        assert((results[1] as UIState.Success<User>).data == user)

        // Verify both managers were called
        verify(firebaseAuthManager).verifyOtp(verificationId, otp)
        verify(firestoreManager).getCurrentUser()
    }

    @Test
    fun `verifyOtp emits error state when verification fails`() = runBlockingTest {
        // Test data
        val verificationId = "test_verification_id"
        val otp = "123456"
        val errorMessage = "Invalid OTP"

        // Set up mock
        `when`(firebaseAuthManager.verifyOtp(verificationId, otp)).thenReturn(
            Result.failure(Exception(errorMessage))
        )

        // Call repository method
        val flow = userRepository.verifyOtp(verificationId, otp)
        val results = flow.toList()

        // Verify the correct states are emitted
        assert(results.size == 2)
        assert(results[0] is UIState.Loading)
        assert(results[1] is UIState.Error)
        assert((results[1] as UIState.Error).message == errorMessage)
    }

    @Test
    fun `registerUser creates user document after successful authentication`() = runBlockingTest {
        // Test data
        val mobileNumber = "+919876543210"
        val name = "Test User"
        val userId = "user123"

        // Mock user
        val user = User(
            id = userId,
            mobileNumber = mobileNumber,
            name = name
        )

        // Set up mocks
        `when`(firebaseAuthManager.getCurrentUserId()).thenReturn(userId)
        `when`(firestoreManager.createUser(user)).thenReturn(Result.success(userId))

        // Call repository method
        val flow = userRepository.registerUser(mobileNumber, name)
        val states = flow.toList()

        // Verify states
        assert(states[0] is UIState.Loading)
        assert(states[1] is UIState.Success)

        // Verify managers were called
        verify(firebaseAuthManager).getCurrentUserId()
        verify(firestoreManager).createUser(
            User(
                id = userId,
                mobileNumber = mobileNumber,
                name = name
            )
        )
    }

    @Test
    fun `registerUser emits error when user creation fails`() = runBlockingTest {
        // Test data
        val mobileNumber = "+919876543210"
        val name = "Test User"
        val userId = "user123"
        val errorMessage = "Failed to create user document"

        // Set up mocks
        `when`(firebaseAuthManager.getCurrentUserId()).thenReturn(userId)
        `when`(firestoreManager.createUser(
            User(
                id = userId,
                mobileNumber = mobileNumber,
                name = name
            )
        )).thenReturn(Result.failure(Exception(errorMessage)))

        // Call repository method
        val flow = userRepository.registerUser(mobileNumber, name)
        val states = flow.toList()

        // Verify states
        assert(states[0] is UIState.Loading)
        assert(states[1] is UIState.Error)
        assert((states[1] as UIState.Error).message == errorMessage)
    }

    @Test
    fun `resendOtp calls auth manager and emits states`() = runBlockingTest {
        // Test data
        val mobileNumber = "+919876543210"
        val verificationId = "new_verification_id"

        // Set up mock
        `when`(firebaseAuthManager.resendVerificationCode(mobileNumber)).thenReturn(
            Result.success(verificationId)
        )

        // Call repository method
        val flow = userRepository.resendOtp(mobileNumber)
        val states = flow.toList()

        // Verify states
        assert(states[0] is UIState.Loading)
        assert(states[1] is UIState.Success)
        assert((states[1] as UIState.Success<String>).data == verificationId)

        // Verify auth manager was called
        verify(firebaseAuthManager).resendVerificationCode(mobileNumber)
    }

    @Test
    fun `logoutUser calls auth manager`() {
        // Call repository method
        userRepository.logoutUser()

        // Verify auth manager was called
        verify(firebaseAuthManager).logoutUser()
    }

    @Test
    fun `checkUsageLimit emits states from firestore manager`() = runBlockingTest {
        // Test data
        val usageLimit = UsageLimit(
            currentCount = 2,
            remainingCount = 3,
            isLimitReached = false,
            role = "free"
        )

        // Set up mock
        `when`(firestoreManager.checkUsageLimit()).thenReturn(
            Result.success(usageLimit)
        )

        // Call repository method
        val flow = userRepository.checkUsageLimit()
        val states = flow.toList()

        // Verify states
        assert(states[0] is UIState.Loading)
        assert(states[1] is UIState.Success)
        assert((states[1] as UIState.Success<UsageLimit>).data == usageLimit)

        // Verify firestore manager was called
        verify(firestoreManager).checkUsageLimit()
    }

    @Test
    fun `incrementUsageCount emits states from firestore manager`() = runBlockingTest {
        // Test data
        val usageLimit = UsageLimit(
            currentCount = 3,
            remainingCount = 2,
            isLimitReached = false,
            role = "free"
        )

        // Set up mock
        `when`(firestoreManager.incrementUsageCount()).thenReturn(
            Result.success(usageLimit)
        )

        // Call repository method
        val flow = userRepository.incrementUsageCount()
        val states = flow.toList()

        // Verify states
        assert(states[0] is UIState.Loading)
        assert(states[1] is UIState.Success)
        assert((states[1] as UIState.Success<UsageLimit>).data == usageLimit)

        // Verify firestore manager was called
        verify(firestoreManager).incrementUsageCount()
    }

    @Test
    fun `formatPhoneNumber adds country code when missing`() {
        // Test various phone number formats
        val testCases = mapOf(
            "9876543210" to "+919876543210",
            "+919876543210" to "+919876543210",
            "919876543210" to "+919876543210",
            "08876543210" to "+918876543210"
        )

        testCases.forEach { (input, expected) ->
            val result = userRepository.formatPhoneNumber(input)
            assert(result == expected) { "Expected $expected but got $result for input $input" }
        }
    }
}