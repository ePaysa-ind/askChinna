package com.example.askchinna.ui.auth
/**
 * OtpVerificationViewModel.kt
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
import com.example.askchinna.data.model.User
import com.example.askchinna.data.repository.UserRepository
import com.example.askchinna.util.SimpleCoroutineUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for OTP verification functionality
 */
@HiltViewModel
class OtpVerificationViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val coroutineUtils: SimpleCoroutineUtils
) : ViewModel() {

    // Verification ID received from Firebase
    private var verificationId: String = ""

    // LiveData for OTP verification state
    private val _verificationState = MutableLiveData<UIState<User>>()
    val verificationState: LiveData<UIState<User>> = _verificationState

    // LiveData for OTP resend state
    private val _resendState = MutableLiveData<UIState<String>>()
    val resendState: LiveData<UIState<String>> = _resendState

    /**
     * Set verification ID received from previous screen
     */
    fun setVerificationId(id: String) {
        verificationId = id
    }

    /**
     * Verify OTP for login
     * @param otp OTP code received by user
     */
    fun verifyOtp(otp: String) {
        viewModelScope.launch(coroutineUtils.ioDispatcher) {
            _verificationState.postValue(UIState.Loading())

            userRepository.verifyOtp(otp).collect { state ->
                _verificationState.postValue(state)
            }
        }
    }

    /**
     * Complete registration process after OTP verification
     * @param otp OTP code received by user
     */
    fun completeRegistration(otp: String) {
        viewModelScope.launch(coroutineUtils.ioDispatcher) {
            _verificationState.postValue(UIState.Loading())

            userRepository.completeRegistration(otp).collect { state ->
                _verificationState.postValue(state)
            }
        }
    }

    /**
     * Resend OTP to user's mobile number
     * Use the existing sendOtp method from UserRepository
     * @param mobileNumber User's mobile number
     * @param activity Current activity for Firebase callbacks
     */
    fun resendOtp(mobileNumber: String, activity: Activity) {
        viewModelScope.launch(coroutineUtils.ioDispatcher) {
            _resendState.postValue(UIState.Loading())

            userRepository.sendOtp(mobileNumber, activity).collect { state ->
                _resendState.postValue(state)
            }
        }
    }
}