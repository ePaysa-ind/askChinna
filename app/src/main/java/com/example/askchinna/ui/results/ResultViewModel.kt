/* file path: app/src/main/java/com/example/askchinna/ui/results/ResultViewModel.kt
 * ResultViewModel.kt
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Updated: May 1, 2025
 * Version: 1.1
 */

package com.example.askchinna.ui.results

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askchinna.data.model.IdentificationResult
import com.example.askchinna.data.model.UIState
import com.example.askchinna.data.repository.CropRepository
import com.example.askchinna.data.repository.IdentificationRepository
import com.example.askchinna.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableLiveData<UIState<IdentificationResult>>()
    val uiState: LiveData<UIState<IdentificationResult>> = _uiState

    var currentResult: IdentificationResult? = null

    fun startIdentification(imagePath: String, cropId: String) {
        _uiState.value = UIState.Loading()

        viewModelScope.launch {
            try {
                val cachedResult = withContext(ioDispatcher) {
                    identificationRepository.getCachedResult(imagePath, cropId)
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

                val cropName = withContext(ioDispatcher) {
                    identificationRepository.getCropNameById(cropId)
                }

                if (cropName.isNullOrEmpty()) {
                    _uiState.value = UIState.Error("Crop information not found")
                    return@launch
                }

                // Use incrementUsageCount from UserRepository
                try {
                    withContext(ioDispatcher) {
                        userRepository.incrementUsageCount().first()
                    }
                } catch (e: Exception) {
                    // Log but continue if usage tracking fails
                    e.printStackTrace()
                }

                val result = withContext(ioDispatcher) {
                    try {
                        identificationRepository.identifyIssue(imagePath, cropId, cropName)
                    } catch (e: Exception) {
                        // Use identifyOffline instead of performOfflineIdentification
                        // Get crop from CropRepository instead of UserRepository
                        val crop = cropRepository.getCropById(cropId)
                            ?: throw IllegalArgumentException("Crop not found: $cropId")
                        identificationRepository.identifyOffline(imageFile, crop)
                    }
                }

                currentResult = result
                _uiState.value = UIState.Success(result)

                // Use cacheIdentificationResult instead of cacheResult
                viewModelScope.launch(ioDispatcher) {
                    identificationRepository.cacheIdentificationResult(result)
                }

            } catch (e: Exception) {
                _uiState.value = UIState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun submitFeedback(feedbackType: FeedbackType) {
        val result = currentResult ?: return

        viewModelScope.launch(ioDispatcher) {
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

                identificationRepository.submitFeedback(
                    resultId = result.id,
                    rating = rating,
                    comment = feedbackValue,
                    isAccurate = isAccurate
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refreshIdentification() {
        val result = currentResult ?: return
        if (result.imagePath.isNotEmpty() && result.cropId.isNotEmpty()) {
            startIdentification(result.imagePath, result.cropId)
        }
    }
}