/**
 * file path: app/src/main/java/com/example/askchinna/ui/auth/LoginViewModel.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Updated: May 15, 2025
 * Version: 1.3
 */

package com.example.askchinna.ui.auth

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askchinna.data.model.UIState
import com.example.askchinna.data.repository.UserRepository
import com.example.askchinna.util.SimpleCoroutineUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for login functionality
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val coroutineUtils: SimpleCoroutineUtils
) : ViewModel() {
    private val TAG = "LoginViewModel"

    // LiveData for OTP send state
    private val _otpSendState = MutableLiveData<UIState<String>>()
    val otpSendState: LiveData<UIState<String>> = _otpSendState

    // LiveData for auto-login check
    private val _autoLoginState = MutableLiveData<Boolean>()
    val autoLoginState: LiveData<Boolean> = _autoLoginState

    // Changed to nullable String type
    private val _errorState = MutableLiveData<String?>()

    init {
        // Check if user is already authenticated
        checkAuthentication()
    }

    /**
     * Send OTP to provided mobile number
     * @param mobileNumber User's mobile number (10 digits)
     * @param activity Current activity for Firebase callbacks
     */
    fun sendOtp(mobileNumber: String, activity: Activity) {
        viewModelScope.launch(coroutineUtils.ioDispatcher) {
            try {
                Log.d(TAG, "Sending OTP to $mobileNumber")
                _otpSendState.postValue(UIState.Loading())

                // Format the phone number to include India's country code (+91)
                val formattedNumber = "+91$mobileNumber"
                Log.d(TAG, "Formatted number: $formattedNumber")

                // Temporarily add test device verification
                if (formattedNumber == "+917780350915" ||
                    formattedNumber == "+919866679227" ||
                    formattedNumber == "+917993754064") {
                    Log.d(TAG, "Test phone number detected: $formattedNumber")
                }

                userRepository.sendOtp(formattedNumber, activity).collect { state ->
                    Log.d(TAG, "OTP send state received: $state")
                    when (state) {
                        is UIState.Success -> {
                            Log.d(TAG, "OTP sent successfully: ${state.data}")
                            _otpSendState.postValue(state)
                            _errorState.postValue(null)
                        }
                        is UIState.Error -> {
                            Log.e(TAG, "Error sending OTP: ${state.message}", Exception(state.message))
                            handleError(state.message)
                        }
                        else -> {
                            Log.d(TAG, "Other state: $state")
                            _otpSendState.postValue(state)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception sending OTP", e)
                handleError(e.message ?: "Unknown error occurred")
            }
        }
    }

    /**
     * Check if user is already authenticated
     */
    private fun checkAuthentication() {
        viewModelScope.launch(coroutineUtils.ioDispatcher) {
            try {
                Log.d(TAG, "Checking user authentication status")
                userRepository.getCurrentUser().collect { state ->
                    when (state) {
                        is UIState.Success -> {
                            Log.d(TAG, "User is authenticated: ${state.data.uid}")
                            _autoLoginState.postValue(true)
                            _errorState.postValue(null)
                        }
                        is UIState.Error -> {
                            Log.d(TAG, "User is not authenticated: ${state.message}")
                            _autoLoginState.postValue(false)
                            // Don't treat this as an error, it's expected for new users
                        }
                        else -> {
                            Log.d(TAG, "Other authentication state: $state")
                            _autoLoginState.postValue(false)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception checking authentication", e)
                _autoLoginState.postValue(false)
                handleError(e.message ?: "Failed to check authentication")
            }
        }
    }

    /**
     * Handle error states
     */
    private fun handleError(message: String) {
        Log.e(TAG, "Error handled: $message")
        _otpSendState.postValue(UIState.Error(message))
        _errorState.postValue(message)
    }

    /**
     * Clear error state
     */
    private fun clearError() {
        _errorState.postValue(null)
    }

    override fun onCleared() {
        super.onCleared()
        clearError()
    }
}