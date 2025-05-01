/*
 * ResultViewModel.kt
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.ui.results

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askchinna.data.model.IdentificationResult
import com.example.askchinna.data.model.UIState
import com.example.askchinna.data.repository.IdentificationRepository
import com.example.askchinna.data.repository.UserRepository
import com.example.askchinna.util.NetworkExceptionHandler
import com.example.askchinna.util.SimpleCoroutineUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * ViewModel for the ResultActivity
 * Handles the identification process and manages the UI state
 */
@HiltViewModel
class ResultViewModel @Inject constructor(
    private val identificationRepository: IdentificationRepository,
    private val userRepository: UserRepository,
    private val networkExceptionHandler: NetworkExceptionHandler,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    // UI state LiveData
    private val _uiState = MutableLiveData<UIState>()
    val uiState: LiveData<UIState> = _uiState

    // Store the current result for reuse (e.g., PDF export)
    var currentResult: IdentificationResult? = null
        private set

    /**
     * Starts the identification process with the provided image and crop ID
     */
    fun startIdentification(imagePath: String, cropId: String) {
        _uiState.value = UIState.Loading

        viewModelScope.launch {
            try {
                // Check if we have a cached result first
                val cachedResult = withContext(ioDispatcher) {
                    identificationRepository.getCachedResult(imagePath, cropId)
                }

                if (cachedResult != null) {
                    // Use cached result if available
                    currentResult = cachedResult
                    _uiState.value = UIState.Success(cachedResult)
                    return@launch
                }

                // Ensure the image file exists
                val imageFile = File(imagePath)
                if (!imageFile.exists()) {
                    _uiState.value = UIState.Error("Image file not found")
                    return@launch
                }

                // Get the crop name from the repository
                val cropName = withContext(ioDispatcher) {
                    identificationRepository.getCropNameById(cropId)
                }

                if (cropName.isNullOrEmpty()) {
                    _uiState.value = UIState.Error("Crop information not found")
                    return@launch
                }

                // Record usage
                withContext(ioDispatcher) {
                    userRepository.recordUsage()
                }

                // Perform identification
                SimpleCoroutineUtils.handleNetworkCallWithFallback(
                    networkCall = {
                        identificationRepository.identifyIssue(imagePath, cropId, cropName)
                    },
                    fallback = {
                        // Fallback mechanism for offline mode or API failure
                        identificationRepository.performOfflineIdentification(imagePath, cropId, cropName)
                    },
                    exceptionHandler = networkExceptionHandler,
                    onSuccess = { result ->
                        // Cache the result
                        viewModelScope.launch(ioDispatcher) {
                            identificationRepository.cacheResult(result)
                        }

                        currentResult = result
                        _uiState.value = UIState.Success(result)
                    },
                    onError = { errorMessage ->
                        _uiState.value = UIState.Error(errorMessage)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UIState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    /**
     * Submits user feedback on the identification result
     */
    fun submitFeedback(feedbackType: FeedbackView.FeedbackType) {
        val result = currentResult ?: return

        viewModelScope.launch(ioDispatcher) {
            try {
                val feedbackValue = when (feedbackType) {
                    FeedbackView.FeedbackType.HELPFUL -> "helpful"
                    FeedbackView.FeedbackType.PARTIALLY_HELPFUL -> "partially_helpful"
                    FeedbackView.FeedbackType.NOT_HELPFUL -> "not_helpful"
                }

                // Store feedback in Firestore
                identificationRepository.submitFeedback(
                    resultId = result.id,
                    feedbackValue = feedbackValue
                )
            } catch (e: Exception) {
                // Log error but don't update UI state, as feedback is not critical
                // to the user experience
                e.printStackTrace()
            }
        }
    }

    /**
     * Refreshes the current identification result, for example after a change in connectivity
     */
    fun refreshIdentification() {
        val result = currentResult ?: return
        // If we have the original image path and crop ID, retry identification
        if (result.imagePath.isNotEmpty() && result.cropId.isNotEmpty()) {
            startIdentification(result.imagePath, result.cropId)
        }
    }
}
