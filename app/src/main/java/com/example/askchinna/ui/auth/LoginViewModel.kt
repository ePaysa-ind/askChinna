package com.example.askchinna.ui.auth

/**
 * app/src/main/java/com/askchinna/ui/auth/LoginViewModel.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */

import android.app.Activity
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

    // LiveData for OTP send state
    private val _otpSendState = MutableLiveData<UIState<String>>()
    val otpSendState: LiveData<UIState<String>> = _otpSendState

    // LiveData for auto-login check
    private val _autoLoginState = MutableLiveData<Boolean>()
    val autoLoginState: LiveData<Boolean> = _autoLoginState

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
            _otpSendState.postValue(UIState.Loading())

            userRepository.sendOtp(mobileNumber, activity).collect { state ->
                _otpSendState.postValue(state)
            }
        }
    }

    /**
     * Check if user is already authenticated
     */
    private fun checkAuthentication() {
        viewModelScope.launch(coroutineUtils.ioDispatcher) {
            userRepository.getCurrentUser().collect { state ->
                if (state is UIState.Success) {
                    // Auto-login if user is already authenticated
                    _autoLoginState.postValue(true)
                } else {
                    // User needs to log in
                    _autoLoginState.postValue(false)
                }
            }
        }
    }
}