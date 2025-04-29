/**
 * Copyright (c) 2025 askChinna App
 * Created: April 28, 2025
 * Version: 1.0
 */
package com.example.askchinna.ui.identification

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askchinna.data.model.Crop
import com.example.askchinna.data.model.IdentificationResult
import com.example.askchinna.data.model.UIState
import com.example.askchinna.data.repository.IdentificationRepository
import com.example.askchinna.util.ImageHelper
import com.example.askchinna.util.NetworkStateMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * ViewModel for handling the pest/disease identification workflow.
 * Manages image processing, quality analysis, and submission to the AI service.
 */
@HiltViewModel
class IdentificationViewModel @Inject constructor(
    private val identificationRepository: IdentificationRepository,
    private val imageHelper: ImageHelper,
    private val networkStateMonitor: NetworkStateMonitor
) : ViewModel() {

    // Selected crop for identification
    private val _selectedCrop = MutableLiveData<Crop>()
    val selectedCrop: LiveData<Crop> = _selectedCrop

    // Captured or selected image
    private val _capturedImage = MutableLiveData<Bitmap>()
    val capturedImage: LiveData<Bitmap> = _capturedImage

    // Image quality analysis
    private val _imageQuality = MutableLiveData<ImageQualityResult>()
    val imageQuality: LiveData<ImageQualityResult> = _imageQuality

    // Image URI for processing
    private val _imageUri = MutableLiveData<Uri>()
    val imageUri: LiveData<Uri> = _imageUri

    // Processing state
    private val _uiState = MutableLiveData<UIState<IdentificationResult>>()
    val uiState: LiveData<UIState<IdentificationResult>> = _uiState

    // Indicates if the device is online
    private val _isOnline = MutableLiveData<Boolean>()
    val isOnline: LiveData<Boolean> = _isOnline

    // Temporary image file
    private var tempImageFile: File? = null

    init {
        // Monitor network state changes
        viewModelScope.launch {
            networkStateMonitor.networkState.collect { networkState ->
                _isOnline.postValue(networkState)
            }
        }
    }

    /**
     * Sets the selected crop for identification
     */
    fun setCrop(crop: Crop) {
        _selectedCrop.value = crop
    }

    /**
     * Processes a captured image
     * @param bitmap The bitmap image from camera capture
     */
    fun processCapturedImage(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                _capturedImage.postValue(bitmap)

                // Analyze image quality
                val qualityResult = analyzeImageQuality(bitmap)
                _imageQuality.postValue(qualityResult)

                // Save image to temporary file
                val file = withContext(Dispatchers.IO) {
                    imageHelper.saveBitmapToFile(bitmap)
                }
                tempImageFile = file
                _imageUri.postValue(Uri.fromFile(file))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.postValue(UIState.Error("Failed to process image: ${e.message}"))
            }
        }
    }

    /**
     * Processes a selected image from gallery
     * @param uri The URI of the selected image
     */
    fun processGalleryImage(uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.postValue(UIState.Loading())

                val bitmap = withContext(Dispatchers.IO) {
                    imageHelper.getBitmapFromUri(uri)
                }

                _capturedImage.postValue(bitmap)

                // Analyze image quality
                val qualityResult = analyzeImageQuality(bitmap)
                _imageQuality.postValue(qualityResult)

                // Use original URI for upload
                _imageUri.postValue(uri)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.postValue(UIState.Error("Failed to process image: ${e.message}"))
            }
        }
    }

    /**
     * Analyzes image quality to determine if it's suitable for identification
     * @param bitmap The image to analyze
     * @return ImageQualityResult with analysis results
     */
    private suspend fun analyzeImageQuality(bitmap: Bitmap): ImageQualityResult {
        return withContext(Dispatchers.Default) {
            try {
                // Check image resolution
                val isResolutionOk = bitmap.width >= MIN_IMAGE_WIDTH && bitmap.height >= MIN_IMAGE_HEIGHT

                // Check for focus/blur
                val focusScore = imageHelper.calculateFocusScore(bitmap)
                val isFocused = focusScore >= MIN_FOCUS_SCORE

                // Check for brightness
                val brightness = imageHelper.calculateBrightness(bitmap)
                val isBrightEnough = brightness >= MIN_BRIGHTNESS && brightness <= MAX_BRIGHTNESS

                ImageQualityResult(
                    isAcceptable = isResolutionOk && isFocused && isBrightEnough,
                    isResolutionOk = isResolutionOk,
                    isFocused = isFocused,
                    isBrightEnough = isBrightEnough,
                    focusScore = focusScore,
                    brightness = brightness
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                ImageQualityResult(
                    isAcceptable = false,
                    isResolutionOk = false,
                    isFocused = false,
                    isBrightEnough = false,
                    focusScore = 0.0,
                    brightness = 0.0,
                    errorMessage = "Failed to analyze image quality: ${e.message}"
                )
            }
        }
    }

    /**
     * Submits the image for AI-based identification
     */
    fun submitForIdentification() {
        val crop = _selectedCrop.value ?: return
        val uri = _imageUri.value ?: return

        viewModelScope.launch {
            try {
                _uiState.postValue(UIState.Loading())

                val isOnline = networkStateMonitor.isNetworkAvailable()
                if (!isOnline) {
                    _uiState.postValue(UIState.Error("No internet connection available for identification"))
                    return@launch
                }

                val result = identificationRepository.identifyPestDisease(crop, uri)
                _uiState.postValue(UIState.Success(result))
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.postValue(UIState.Error("Identification failed: ${e.message}"))
            }
        }
    }

    /**
     * Retakes the image, clearing the current one
     */
    fun retakeImage() {
        _capturedImage.value = null
        _imageQuality.value = null
        _imageUri.value = null
        tempImageFile?.delete()
        tempImageFile = null
    }

    /**
     * Retry identification after a failure
     */
    fun retryIdentification() {
        _uiState.value = UIState.Initial()
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up temporary files
        tempImageFile?.delete()
    }

    companion object {
        // Image quality thresholds
        private const val MIN_IMAGE_WIDTH = 640
        private const val MIN_IMAGE_HEIGHT = 480
        private const val MIN_FOCUS_SCORE = 0.5 // Threshold for acceptable focus (0-1)
        private const val MIN_BRIGHTNESS = 0.3 // Minimum acceptable brightness (0-1)
        private const val MAX_BRIGHTNESS = 0.85 // Maximum acceptable brightness (0-1)
    }

    /**
     * Data class representing the result of image quality analysis
     */
    data class ImageQualityResult(
        val isAcceptable: Boolean,
        val isResolutionOk: Boolean,
        val isFocused: Boolean,
        val isBrightEnough: Boolean,
        val focusScore: Double,
        val brightness: Double,
        val errorMessage: String? = null
    )
}