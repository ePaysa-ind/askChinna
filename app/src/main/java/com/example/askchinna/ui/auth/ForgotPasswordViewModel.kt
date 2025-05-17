/**
 * file path: app/src/main/java/com/example/askchinna/ui/auth/ForgotPasswordViewModel.kt
 * Copyright © 2025 askChinna
 * Created: May 13, 2025
 * Version: 1.0
 */
package com.example.askchinna.ui.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askchinna.data.model.UIState
import com.example.askchinna.data.repository.UserRepository
import com.example.askchinna.util.SimpleCoroutineUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for password reset functionality
 *
 * For password reset, we reuse the OTP sending mechanism from the login process,
 * since the forgot password flow uses the same OTP verification.
 */
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val coroutineUtils: SimpleCoroutineUtils
) : ViewModel() {

    // StateFlow for password reset OTP state
    private val _resetState = MutableStateFlow<UIState<String>>(UIState.Initial)
    val resetState: StateFlow<UIState<String>> = _resetState

    /**
     * Send password reset OTP to the given mobile number
     * This leverages the existing sendOtp method from UserRepository
     *
     * @param mobileNumber User's mobile number
     * @param activity Current activity for Firebase callbacks
     */
    fun sendPasswordResetOtp(mobileNumber: String, activity: Activity) {
        viewModelScope.launch(coroutineUtils.ioDispatcher) {
            try {
                _resetState.value = UIState.Loading("Sending password reset OTP...")

                // Use the existing sendOtp method from UserRepository
                userRepository.sendOtp(mobileNumber, activity).collect { state ->
                    _resetState.value = state
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Failed to send password reset OTP"
                _resetState.value = UIState.Error(errorMsg, e)
            }
        }
    }

}