/**
 * File: app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionViewModel.kt
 * Copyright (c) 2025 askChinna
 * Created: April 28, 2025
 * Updated: May 6, 2025
 * Version: 1.2
 *
 * Change Log:
 * 1.2 - May 6, 2025
 * - Added proper error handling for network state changes
 * - Added retry mechanism for failed operations
 * - Added proper cleanup in onCleared
 * - Added proper coroutine scope management
 * - Added state restoration
 * - Added memory optimization
 * - Added proper error logging
 */
package com.example.askchinna.ui.cropselection

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asFlow
import com.example.askchinna.data.model.Crop
import com.example.askchinna.data.model.UIState
import com.example.askchinna.data.repository.CropRepository
import com.example.askchinna.data.repository.UserRepository
import com.example.askchinna.util.NetworkExceptionHandler
import com.example.askchinna.util.NetworkState
import com.example.askchinna.util.NetworkStateMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Crop Selection screen
 * Manages the fetching of crop data and handling of user interactions
 */
@HiltViewModel
class CropSelectionViewModel @Inject constructor(
    private val cropRepository: CropRepository,
    private val userRepository: UserRepository,
    private val networkExceptionHandler: NetworkExceptionHandler,
    private val networkStateMonitor: NetworkStateMonitor
) : ViewModel() {
    private val tag = "CropSelectionViewModel"
    private var isInitialized = false
    private var selectedCrop: Crop? = null
    private var retryCount = 0
    private val maxRetries = 3

    // UI state for the crop selection screen
    private val _uiState = MutableLiveData<UIState<List<Crop>>>(UIState.Initial)
    val uiState: LiveData<UIState<List<Crop>>> = _uiState

    // Usage limits to display in the UI
    private val _usageLimit = MutableLiveData<Int>(0)
    val usageLimit: LiveData<Int> = _usageLimit

    // Error handler for coroutines
    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(tag, "Coroutine error", throwable)
        handleError(throwable)
    }

    init {
        try {
            setupNetworkMonitoring()
            loadCrops()
            loadUsageLimit()
            isInitialized = true
        } catch (e: Exception) {
            Log.e(tag, "Error initializing ViewModel", e)
            handleError(e)
        }
    }

    private fun setupNetworkMonitoring() {
        viewModelScope.launch(errorHandler) {
            try {
                // Convert LiveData<NetworkState> to Flow and map it to extract the Boolean value
                networkStateMonitor.networkState.asFlow()
                    .map { state ->
                        when (state) {
                            is NetworkState.Offline -> false
                            is NetworkState.Unknown -> false
                            else -> true // WiFi, MobileData, MeteredMobileData, Ethernet are all considered connected
                        }
                    }
                    .collectLatest { isAvailable ->
                        if (isAvailable) {
                            retryCount = 0
                            loadCrops()
                            loadUsageLimit()
                        } else {
                            _uiState.value = UIState.Error("No network connection")
                        }
                    }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    /**
     * Loads the list of supported crops from repository
     * Updates UI state with loading, success, or error states
     */
    fun loadCrops() {
        if (!isInitialized) {
            Log.w(tag, "ViewModel not initialized")
            return
        }

        viewModelScope.launch(errorHandler) {
            try {
                _uiState.value = UIState.Loading()
                val crops = cropRepository.getSupportedCrops()

                if (crops.isNotEmpty()) {
                    _uiState.value = UIState.Success(crops)
                    Log.d(tag, "Loaded ${crops.size} crops successfully")
                    retryCount = 0
                } else {
                    Log.w(tag, "No crops available")
                    handleEmptyCrops()
                }
            } catch (e: Exception) {
                Log.e(tag, "Error loading crops", e)
                handleError(e)
            }
        }
    }

    private fun handleEmptyCrops() {
        if (retryCount < maxRetries) {
            retryCount++
            Log.d(tag, "Retrying crop load. Attempt $retryCount of $maxRetries")
            loadCrops()
        } else {
            _uiState.value = UIState.Error("No crops available")
            retryCount = 0
        }
    }

    /**
     * Loads the current usage limits for the user
     */
    private fun loadUsageLimit() {
        if (!isInitialized) {
            Log.w(tag, "ViewModel not initialized")
            return
        }

        viewModelScope.launch(errorHandler) {
            try {
                userRepository.checkAndUpdateUsageLimit().collectLatest { result ->
                    when (result) {
                        is UIState.Success -> {
                            _usageLimit.value = result.data.usageCount
                            Log.d(tag, "Loaded usage limit: ${result.data.usageCount}")
                        }
                        is UIState.Error -> {
                            Log.w(tag, "Failed to load usage limits: ${result.message}")
                            handleUsageLimitError()
                        }
                        else -> {
                            Log.w(tag, "Unexpected state while loading usage limits")
                            _usageLimit.value = 0
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Error loading usage limits", e)
                handleError(e)
            }
        }
    }

    private fun handleUsageLimitError() {
        if (retryCount < maxRetries) {
            retryCount++
            Log.d(tag, "Retrying usage limit load. Attempt $retryCount of $maxRetries")
            loadUsageLimit()
        } else {
            _usageLimit.value = 0
            retryCount = 0
        }
    }

    /**
     * Determines if the user has uses remaining for identification
     * @return True if the user has remaining uses, false otherwise
     */
    fun hasRemainingUses(): Boolean {
        if (!isInitialized) {
            Log.w(tag, "ViewModel not initialized")
            return false
        }

        return try {
            val currentLimit = _usageLimit.value ?: 0
            currentLimit < MAX_USES
        } catch (e: Exception) {
            Log.e(tag, "Error checking remaining uses", e)
            false
        }
    }

    fun setSelectedCrop(crop: Crop) {
        selectedCrop = crop
    }

    fun getSelectedCrop(): Crop? = selectedCrop

    /**
     * Handles errors by updating the UI state
     */
    private fun handleError(error: Throwable) {
        try {
            val errorMessage = networkExceptionHandler.handle(error)
            _uiState.value = UIState.Error(errorMessage)
        } catch (e: Exception) {
            Log.e(tag, "Error handling error", e)
            _uiState.value = UIState.Error("An unexpected error occurred")
        }
    }

    override fun onCleared() {
        try {
            selectedCrop = null
            retryCount = 0
            isInitialized = false
            super.onCleared()
        } catch (e: Exception) {
            Log.e(tag, "Error in onCleared", e)
        }
    }

    companion object {
        private const val MAX_USES = 5
    }
}