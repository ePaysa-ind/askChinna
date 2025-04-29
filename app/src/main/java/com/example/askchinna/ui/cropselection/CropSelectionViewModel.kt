/**
 * File: app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionViewModel.kt
 * Copyright (c) 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */
package com.example.askchinna.ui.cropselection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askchinna.R
import com.example.askchinna.data.model.Crop
import com.example.askchinna.data.model.UIState
import com.example.askchinna.data.model.UsageLimit
import com.example.askchinna.data.repository.CropRepository
import com.example.askchinna.data.repository.UserRepository
import com.example.askchinna.util.NetworkExceptionHandler
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val networkExceptionHandler: NetworkExceptionHandler
) : ViewModel() {

    // UI state for the crop selection screen
    private val _uiState = MutableLiveData<UIState<List<Crop>>>(UIState.Loading)
    val uiState: LiveData<UIState<List<Crop>>> = _uiState

    // Usage limits to display in the UI
    private val _usageLimit = MutableLiveData<UsageLimit>()
    val usageLimit: LiveData<UsageLimit> = _usageLimit

    init {
        loadCrops()
        loadUsageLimit()
    }

    /**
     * Loads the list of supported crops from repository
     * Updates UI state with loading, success, or error states
     */
    fun loadCrops() {
        viewModelScope.launch {
            _uiState.value = UIState.Loading
            try {
                val crops = cropRepository.getSupportedCrops()
                if (crops.isNotEmpty()) {
                    _uiState.value = UIState.Success(crops)
                } else {
                    _uiState.value = UIState.Error(
                        message = "No crops available",
                        retryAction = { loadCrops() }
                    )
                }
            } catch (e: Exception) {
                val errorMessage = networkExceptionHandler.getErrorMessage(e)
                _uiState.value = UIState.Error(
                    message = errorMessage,
                    retryAction = { loadCrops() }
                )
            }
        }
    }

    /**
     * Loads the current usage limits for the user
     */
    private fun loadUsageLimit() {
        viewModelScope.launch {
            try {
                val limit = userRepository.getUserUsageLimit()
                _usageLimit.value = limit
            } catch (e: Exception) {
                // Silently fail for usage limits - we'll use default values
            }
        }
    }

    /**
     * Provides the current usage limit information as a formatted string
     * @return A string showing the number of uses remaining
     */
    fun getUsageLimitText(): String {
        val limit = _usageLimit.value ?: UsageLimit(remainingUses = 0, maxUses = 5)
        return "${limit.remainingUses}/${limit.maxUses}"
    }

    /**
     * Determines if the user has uses remaining for identification
     * @return True if the user has remaining uses, false otherwise
     */
    fun hasRemainingUses(): Boolean {
        val limit = _usageLimit.value ?: UsageLimit(remainingUses = 0, maxUses = 5)
        return limit.remainingUses > 0
    }
}
