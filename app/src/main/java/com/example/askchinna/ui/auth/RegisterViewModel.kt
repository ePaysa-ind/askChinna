package com.example.askchinna.ui.auth
/**
 * RegisterViewModel.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */
import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.askchinna.data.model.UIState
import com.askchinna.data.repository.UserRepository
import com.askchinna.util.SimpleCoroutineUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for user registration functionality
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val coroutineUtils: SimpleCoroutineUtils
) : ViewModel() {

    // LiveData for registration state
    private val _registrationState = MutableLiveData<UIState<String>>()
    val registrationState: LiveData<UIState<String>> = _registrationState

    /**
     * Register a new user with mobile number and name
     * @param mobileNumber User's mobile number (10 digits)
     * @param displayName User's display name
     * @param activity Current activity for Firebase callbacks
     */
    fun registerUser(mobileNumber: String, displayName: String, activity: Activity) {
        viewModelScope.launch(coroutineUtils.ioDispatcher) {
            _registrationState.postValue(UIState.Loading())

            userRepository.registerUser(mobileNumber, displayName, activity).collect { state ->
                _registrationState.postValue(state)
            }
        }
    }
}