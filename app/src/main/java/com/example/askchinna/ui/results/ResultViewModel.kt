/* file path: app/src/main/java/com/example/askchinna/ui/results/ResultViewModel.kt
 * ResultViewModel.kt
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Updated: May 16, 2025
 * Version: 1.5
 */

package com.example.askchinna.ui.results

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askchinna.data.model.IdentificationResult
import com.example.askchinna.data.model.UIState
import com.example.askchinna.data.repository.CropRepository
import com.example.askchinna.data.repository.IdentificationRepository
import com.example.askchinna.data.repository.UserRepository
import com.example.askchinna.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * Enum class for feedback types to avoid redeclaration
 */
enum class FeedbackType {
    HELPFUL,
    PARTIALLY_HELPFUL,
    NOT_HELPFUL
}

/**
 * ViewModel for the ResultActivity
 * Handles the identification process and manages the UI state
 */
@HiltViewModel
class ResultViewModel @Inject constructor(
    private val identificationRepository: IdentificationRepository,
    private val userRepository: UserRepository,
    private val cropRepository: CropRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val TAG = "ResultViewModel"
    private var isInitialized = false

    private val _uiState = MutableLiveData<UIState<IdentificationResult>>()
    val uiState: LiveData<UIState<IdentificationResult>> = _uiState

    var currentResult: IdentificationResult? = null
        private set

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Coroutine error", throwable)
        _uiState.value = UIState.Error(throwable.message ?: "An unexpected error occurred")
    }

    /**
     * Starts the identification process for a given image and crop
     * @param imagePath Path to the image file
     * @param cropId ID of the crop to identify
     */
    fun startIdentification(imagePath: String, cropId: String) {
        try {
            if (!isInitialized) {
                Log.w(TAG, "ViewModel not initialized")
                return
            }

            _uiState.value = UIState.Loading()

            viewModelScope.launch(exceptionHandler) {
                try {
                    // Try to get a cached result first
                    val cachedResult = withContext(ioDispatcher) {
                        try {
                            identificationRepository.getIdentificationById(imagePath)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error getting cached result", e)
                            null
                        }
                    }

                    if (cachedResult != null) {
                        currentResult = cachedResult
                        _uiState.value = UIState.Success(cachedResult)
                        return@launch
                    }

                    val imageFile = File(imagePath)
                    if (!imageFile.exists()) {
                        _uiState.value = UIState.Error("Image file not found")
                        return@launch
                    }

                    // Get the crop by ID
                    val crop = withContext(ioDispatcher) {
                        cropRepository.getCropById(cropId)
                    } ?: run {
                        _uiState.value = UIState.Error("Crop information not found")
                        return@launch
                    }

                    // Track usage
                    try {
                        withContext(ioDispatcher) {
                            userRepository.incrementUsageCount().first()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error tracking usage", e)
                        // Continue with identification even if usage tracking fails
                    }

                    // Attempt identification
                    try {
                        // Convert File to Uri
                        val imageUri = Uri.fromFile(imageFile)

                        // Call identifyPestDisease with the correct parameters
                        val result = withContext(ioDispatcher) {
                            identificationRepository.identifyPestDisease(crop, imageUri)
                        }

                        // Update the result with imagePath for later use
                        val updatedResult = result.copy(imagePath = imagePath)

                        currentResult = updatedResult
                        _uiState.value = UIState.Success(updatedResult)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during identification", e)
                        _uiState.value = UIState.Error(e.message ?: "Identification failed")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Unexpected error", e)
                    _uiState.value = UIState.Error(e.message ?: "An unexpected error occurred")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting identification", e)
            _uiState.value = UIState.Error(e.message ?: "Failed to start identification")
        }
    }

    /**
     * Submits user feedback for the current identification result
     * @param feedbackType Type of feedback (helpful, partially helpful, not helpful)
     */
    fun submitFeedback(feedbackType: FeedbackType) {
        try {
            if (!isInitialized) {
                Log.w(TAG, "ViewModel not initialized")
                return
            }

            val result = currentResult ?: run {
                Log.e(TAG, "No current result available for feedback")
                return
            }

            viewModelScope.launch(ioDispatcher + exceptionHandler) {
                try {
                    val feedbackValue = when (feedbackType) {
                        FeedbackType.HELPFUL -> "helpful"
                        FeedbackType.PARTIALLY_HELPFUL -> "partially_helpful"
                        FeedbackType.NOT_HELPFUL -> "not_helpful"
                    }

                    // Convert feedback value to rating
                    val rating = when (feedbackType) {
                        FeedbackType.HELPFUL -> 5
                        FeedbackType.PARTIALLY_HELPFUL -> 3
                        FeedbackType.NOT_HELPFUL -> 1
                    }

                    val isAccurate = feedbackType != FeedbackType.NOT_HELPFUL

                    // Send feedback to repository
                    identificationRepository.updateResultWithFeedback(
                        resultId = result.id,
                        rating = rating,
                        comment = feedbackValue,
                        isAccurate = isAccurate
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error submitting feedback", e)
                    throw e
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling feedback submission", e)
        }
    }

    /**
     * Refreshes the identification by restarting the process with current result data
     */
    fun refreshIdentification() {
        try {
            if (!isInitialized) {
                Log.w(TAG, "ViewModel not initialized")
                return
            }

            val result = currentResult ?: run {
                Log.e(TAG, "No current result available for refresh")
                return
            }

            if (result.imagePath.isNotEmpty() && result.cropId.isNotEmpty()) {
                startIdentification(result.imagePath, result.cropId)
            } else {
                Log.e(TAG, "Invalid result data for refresh")
                _uiState.value = UIState.Error("Invalid result data")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing identification", e)
            _uiState.value = UIState.Error(e.message ?: "Failed to refresh identification")
        }
    }

    init {
        isInitialized = true
    }

    override fun onCleared() {
        try {
            super.onCleared()
            isInitialized = false
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCleared", e)
        }
    }
}