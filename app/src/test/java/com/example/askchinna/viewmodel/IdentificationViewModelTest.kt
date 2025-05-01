/*
 * File: com/example/askchinna/ui/identification/IdentificationViewModel.kt
 * Copyright (c) 2025 askChinna
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.ui.identification

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askchinna.data.model.Crop
import com.example.askchinna.data.model.IdentificationResult
import com.example.askchinna.data.model.UIState
import com.example.askchinna.data.repository.IdentificationRepository
import com.example.askchinna.data.repository.UserRepository
import com.example.askchinna.util.ImageHelper
import com.example.askchinna.util.NetworkStateMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject

/**
 * ViewModel for the image capturing and pest/disease identification flow.
 * Handles image processing, quality assessment, and API communication.
 * Implements offline functionality and fallback strategies.
 */
@HiltViewModel
class IdentificationViewModel @Inject constructor(
    private val identificationRepository: IdentificationRepository,
    private val userRepository: UserRepository,
    private val imageHelper: ImageHelper,
    private val networkStateMonitor: NetworkStateMonitor
) : ViewModel() {

    private val TAG = "IdentificationViewModel"

    // UI state for different screens
    private val _uiState = MutableLiveData<UIState>(UIState.Initial)
    val uiState: LiveData<UIState> = _uiState

    // Captured or selected image
    private val _capturedImage = MutableLiveData<Bitmap?>()
    val capturedImage: LiveData<Bitmap?> = _capturedImage

    // Image quality assessment
    private val _imageQuality = MutableLiveData<Int>()
    val imageQuality: LiveData<Int> = _imageQuality

    // Selected crop
    private val _selectedCrop = MutableStateFlow<Crop?>(null)
    val selectedCrop: StateFlow<Crop?> = _selectedCrop

    // Identification result
    private val _identificationResult = MutableLiveData<IdentificationResult?>()
    val identificationResult: LiveData<IdentificationResult?> = _identificationResult

    // Error handling
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Remaining usage count
    private val _remainingUsageCount = MutableLiveData<Int>()
    val remainingUsageCount: LiveData<Int> = _remainingUsageCount

    // File for image upload
    private var imageFile: File? = null

    init {
        // Check remaining usage count on initialization
        checkRemainingUsage()

        // Monitor network state for connectivity changes
        viewModelScope.launch {
            networkStateMonitor.networkState.collect { isConnected ->
                if (isConnected && _uiState.value == UIState.Error) {
                    // Retry failed operations when network becomes available
                    Log.d(TAG, "Network reconnected, retrying pending operations")
                    _errorMessage.value = null
                    _uiState.value = UIState.Initial
                }
            }
        }
    }

    /**
     * Sets the selected crop for identification.
     *
     * @param crop The selected crop
     */
    fun setSelectedCrop(crop: Crop) {
        viewModelScope.launch {
            _selectedCrop.value = crop
            Log.d(TAG, "Selected crop: ${crop.name}")
        }
    }

    /**
     * Processes an image from a Uri (gallery selection).
     *
     * @param imageUri The Uri of the selected image
     */
    fun processImageFromUri(imageUri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = UIState.Loading

                // Get bitmap from uri
                val bitmap = withContext(Dispatchers.IO) {
                    imageHelper.getBitmapFromUri(imageUri)
                }

                if (bitmap != null) {
                    processImage(bitmap)
                } else {
                    throw IOException("Failed to load image from gallery")
                }
            } catch (e: Exception) {
                handleImageProcessingError(e)
            }
        }
    }

    /**
     * Processes a captured camera image.
     *
     * @param bitmap The bitmap of the captured image
     */
    fun processImageFromCamera(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                _uiState.value = UIState.Loading
                processImage(bitmap)
            } catch (e: Exception) {
                handleImageProcessingError(e)
            }
        }
    }

    /**
     * Common image processing logic for both camera and gallery sources.
     * Optimizes image, assesses quality, and prepares for upload.
     *
     * @param bitmap The bitmap to process
     */
    private suspend fun processImage(bitmap: Bitmap) {
        try {
            // Optimize image for low-end devices
            val optimizedBitmap = withContext(Dispatchers.IO) {
                imageHelper.optimizeImageForUpload(bitmap, MAX_IMAGE_SIZE_BYTES)
            }

            // Store processed image
            _capturedImage.value = optimizedBitmap

            // Assess image quality
            val quality = withContext(Dispatchers.IO) {
                imageHelper.assessImageQuality(optimizedBitmap)
            }
            _imageQuality.value = quality

            // Save image to cache for upload
            imageFile = withContext(Dispatchers.IO) {
                imageHelper.saveImageToCache(
                    optimizedBitmap,
                    "pest_analysis_${System.currentTimeMillis()}.jpg"
                )
            }

            _uiState.value = UIState.Success

            Log.d(TAG, "Image processed successfully. Quality: $quality, Size: ${imageFile?.length() ?: 0} bytes")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            handleImageProcessingError(e)
        }
    }

    /**
     * Submits the processed image for identification.
     * Handles both online and offline scenarios with fallback strategies.
     */
    fun submitForIdentification() {
        // Validate prerequisites
        val crop = _selectedCrop.value ?: run {
            _errorMessage.value = "Please select a crop first"
            return
        }

        val imageFileToSubmit = imageFile ?: run {
            _errorMessage.value = "Image not available. Please try again."
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = UIState.Loading

                // Check usage limits before proceeding
                val usageCount = userRepository.getRemainingUsageCount()
                if (usageCount <= 0) {
                    _errorMessage.value = "You have reached the maximum usage limit for this month."
                    _uiState.value = UIState.Error
                    return@launch
                }

                // Perform identification based on network availability
                val isNetworkAvailable = networkStateMonitor.isNetworkAvailable()
                val result = if (isNetworkAvailable) {
                    // Online identification using API
                    Log.d(TAG, "Using online identification via Gemini API")
                    identificationRepository.identifyWithGeminiAPI(imageFileToSubmit, crop)
                } else {
                    // Offline identification using local model
                    Log.d(TAG, "Using offline identification with local model")
                    identificationRepository.identifyOffline(imageFileToSubmit, crop)
                }

                // Process result
                if (result != null) {
                    _identificationResult.value = result
                    _uiState.value = UIState.Success

                    // Update usage count
                    userRepository.decrementUsageCount()
                    checkRemainingUsage()

                    // Cache result for offline access
                    identificationRepository.cacheIdentificationResult(result)

                    Log.d(TAG, "Identification successful: ${result.diseaseName}")
                } else {
                    throw IOException("Identification returned null result")
                }

            } catch (e: Exception) {
                if (e is CancellationException) throw e

                Log.e(TAG, "Error during identification", e)
                _errorMessage.value = when {
                    !networkStateMonitor.isNetworkAvailable() ->
                        "No internet connection. Please try again when connected."
                    e.message?.contains("quota") == true ->
                        "Service is currently busy. Please try again later."
                    else -> "Failed to identify pest/disease. ${e.message}"
                }
                _uiState.value = UIState.Error
            }
        }
    }

    /**
     * Checks and updates the remaining usage count for the current user.
     */
    private fun checkRemainingUsage() {
        viewModelScope.launch {
            try {
                val usageCount = userRepository.getRemainingUsageCount()
                _remainingUsageCount.value = usageCount
                Log.d(TAG, "Remaining usage count: $usageCount")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get remaining usage count", e)
                // Default to 0 on error to prevent unlimited usage
                _remainingUsageCount.value = 0
            }
        }
    }

    /**
     * Retries a failed identification.
     * Attempts to use a different strategy based on the previous error.
     */
    fun retryIdentification() {
        _errorMessage.value = null
        _uiState.value = UIState.Initial
        submitForIdentification()
    }

    /**
     * Handles errors during image processing with appropriate user feedback.
     *
     * @param e The exception that occurred
     */
    private fun handleImageProcessingError(e: Exception) {
        Log.e(TAG, "Error processing image", e)
        _errorMessage.value = when (e) {
            is OutOfMemoryError -> "Device memory is low. Please try with a smaller image."
            is IOException -> "Failed to process image: ${e.message}"
            else -> "Unexpected error while processing image: ${e.message}"
        }
        _uiState.value = UIState.Error
    }

    /**
     * Clears current image and state to start over.
     */
    fun clearImage() {
        _capturedImage.value = null
        _imageQuality.value = 0
        imageFile = null
        _errorMessage.value = null
        _uiState.value = UIState.Initial
    }

    companion object {
        private const val MAX_IMAGE_SIZE_BYTES = 2 * 1024 * 1024 // 2MB max size
    }
}