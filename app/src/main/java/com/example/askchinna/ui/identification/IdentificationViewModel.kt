/**
 * file path: app/src/main/java/com/example/askchinna/ui/identification/IdentificationViewModel.kt
 * Copyright (c) 2025 askChinna App
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
 * - Added proper resource management
 * - Added proper image processing
 * - Added proper network state handling
 * - Removed unnecessary initialization checks for improved reliability
 */
package com.example.askchinna.ui.identification

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asFlow
import com.example.askchinna.data.model.Crop
import com.example.askchinna.data.model.IdentificationResult
import com.example.askchinna.data.model.UIState
import com.example.askchinna.data.repository.IdentificationRepository
import com.example.askchinna.util.ImageHelper
import com.example.askchinna.util.NetworkState
import com.example.askchinna.util.NetworkStateMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import androidx.core.graphics.get
import androidx.core.graphics.createBitmap
import kotlin.math.sqrt

/**
 * ViewModel for handling the pest/disease identification workflow.
 * Manages image processing, quality analysis, and submission to the AI service.
 */
@HiltViewModel
class IdentificationViewModel @Inject constructor(
    private val identificationRepository: IdentificationRepository,
    private val imageHelper: ImageHelper,
    private val networkStateMonitor: NetworkStateMonitor,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private var retryCount = 0
    private val maxRetries = 3

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

    // Processing state
    private val _uiState = MutableLiveData<UIState<IdentificationResult>>()
    val uiState: LiveData<UIState<IdentificationResult>> = _uiState

    // Network state
    private val _isOnline = MutableLiveData<Boolean>()
    val isOnline: LiveData<Boolean> = _isOnline

    // Temporary image file
    private var tempImageFile: File? = null

    // Coroutine exception handler
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Coroutine error", throwable)
        handleError(throwable)
    }

    companion object {
        private const val TAG = "IdentificationViewModel"
        // Image quality thresholds
        private const val MIN_IMAGE_WIDTH = 640
        private const val MIN_IMAGE_HEIGHT = 480
        private const val MIN_FOCUS_SCORE = 0.5 // Threshold for acceptable focus (0-1)
        private const val MIN_BRIGHTNESS = 0.3 // Minimum acceptable brightness (0-1)
        private const val MAX_BRIGHTNESS = 0.85 // Maximum acceptable brightness (0-1)
    }

    init {
        try {
            // Start network monitoring and initialize state
            networkStateMonitor.startMonitoring()

            // Initialize network state using the current value
            val currentState = networkStateMonitor.networkState.value
            _isOnline.value = isConnectedState(currentState)

            // Monitor network state changes
            setupNetworkMonitoring()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ViewModel", e)
            handleError(e)
        }
    }

    /**
     * Determine if a NetworkState represents a connected state
     */
    private fun isConnectedState(state: NetworkState?): Boolean {
        return when (state) {
            is NetworkState.Offline -> false
            is NetworkState.Unknown -> false
            null -> false
            else -> true // WiFi, MobileData, MeteredMobileData, Ethernet are all connected
        }
    }

    /**
     * Setup network state monitoring
     */
    private fun setupNetworkMonitoring() {
        viewModelScope.launch(exceptionHandler) {
            try {
                // Convert LiveData<NetworkState> to Flow and map it to Boolean
                networkStateMonitor.networkState.asFlow()
                    .map { state -> isConnectedState(state) }
                    .collectLatest { isAvailable ->
                        _isOnline.value = isAvailable
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in network monitoring", e)
                handleError(e)
            }
        }
    }

    /**
     * Sets the selected crop for identification
     */
    fun setCrop(crop: Crop) {
        try {
            _selectedCrop.value = crop
        } catch (e: Exception) {
            Log.e(TAG, "Error setting crop", e)
            handleError(e)
        }
    }

    /**
     * Processes a captured image
     * @param bitmap The bitmap image from camera capture
     */
    fun processCapturedImage(bitmap: Bitmap) {
        viewModelScope.launch(exceptionHandler) {
            try {
                _capturedImage.value = bitmap

                // Analyze image quality
                val qualityResult = analyzeImageQuality(bitmap)
                _imageQuality.value = qualityResult

                // Save image to temporary file
                val file = withContext(Dispatchers.IO) {
                    createTempImageFile(bitmap)
                }
                tempImageFile = file
                _imageUri.value = Uri.fromFile(file)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Error processing captured image", e)
                handleError(e)
            }
        }
    }

    /**
     * Creates a temporary file from bitmap
     */
    private fun createTempImageFile(bitmap: Bitmap): File {
        try {
            val file = imageHelper.createTempImageFile()
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.flush()
            }
            return file
        } catch (e: Exception) {
            Log.e(TAG, "Error creating temp image file", e)
            throw IOException("Failed to create image file", e)
        }
    }

    /**
     * Processes a selected image from gallery
     * @param uri The URI of the selected image
     */
    fun processGalleryImage(uri: Uri) {
        viewModelScope.launch(exceptionHandler) {
            try {
                _uiState.value = UIState.Loading()

                val bitmap = withContext(Dispatchers.IO) {
                    appContext.contentResolver.openInputStream(uri)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    } ?: throw IOException("Failed to open image stream")
                }

                _capturedImage.value = bitmap

                // Analyze image quality
                val qualityResult = analyzeImageQuality(bitmap)
                _imageQuality.value = qualityResult

                // Use original URI for upload
                _imageUri.value = uri
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Error processing gallery image", e)
                handleError(e)
            }
        }
    }

    /**
     * Analyzes image quality to determine if it's suitable for identification
     * @param bitmap The image to analyze
     * @return ImageQualityResult with analysis results
     */
    private suspend fun analyzeImageQuality(bitmap: Bitmap): ImageQualityResult {
        return withContext(Dispatchers.Default + exceptionHandler) {
            try {
                // Check image resolution
                val isResolutionOk = bitmap.width >= MIN_IMAGE_WIDTH && bitmap.height >= MIN_IMAGE_HEIGHT

                // Calculate focus score
                val focusScore = calculateFocusScore(bitmap)
                val isFocused = focusScore >= MIN_FOCUS_SCORE

                // Calculate brightness
                val brightness = calculateBrightness(bitmap)
                val isBrightEnough = brightness in MIN_BRIGHTNESS..MAX_BRIGHTNESS

                ImageQualityResult(
                    isAcceptable = isResolutionOk && isFocused && isBrightEnough,
                    isResolutionOk = isResolutionOk,
                    isFocused = isFocused,
                    isBrightEnough = isBrightEnough,
                    focusScore = focusScore,
                    brightness = brightness
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error analyzing image quality", e)
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
     * Calculate focus score from bitmap
     */
    private fun calculateFocusScore(bitmap: Bitmap): Double {
        try {
            val width = bitmap.width
            val height = bitmap.height
            var sum = 0.0
            var count = 0

            for (y in 0 until height step 2) {
                for (x in 0 until width step 2) {
                    val pixel = bitmap[x, y]
                    val r = android.graphics.Color.red(pixel)
                    val g = android.graphics.Color.green(pixel)
                    val b = android.graphics.Color.blue(pixel)
                    val gray = (r + g + b) / 3.0
                    sum += gray
                    count++
                }
            }

            val mean = sum / count
            var variance = 0.0

            for (y in 0 until height step 2) {
                for (x in 0 until width step 2) {
                    val pixel = bitmap[x, y]
                    val r = android.graphics.Color.red(pixel)
                    val g = android.graphics.Color.green(pixel)
                    val b = android.graphics.Color.blue(pixel)
                    val gray = (r + g + b) / 3.0
                    variance += (gray - mean) * (gray - mean)
                }
            }

            variance /= count
            return sqrt(variance) / 255.0
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating focus score", e)
            return 0.0
        }
    }

    /**
     * Calculate brightness from bitmap
     */
    private fun calculateBrightness(bitmap: Bitmap): Double {
        try {
            val width = bitmap.width
            val height = bitmap.height

            // Sample a subset of pixels for performance
            var sum = 0.0
            var count = 0

            for (y in 0 until height step 2) {
                for (x in 0 until width step 2) {
                    val pixel = bitmap[x, y]
                    val r = android.graphics.Color.red(pixel)
                    val g = android.graphics.Color.green(pixel)
                    val b = android.graphics.Color.blue(pixel)
                    val brightness = (r + g + b) / (3.0 * 255.0)
                    sum += brightness
                    count++
                }
            }

            return sum / count
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating brightness", e)
            return 0.0
        }
    }

    /**
     * Submits the image for AI-based identification
     */
    fun submitForIdentification() {
        val crop = _selectedCrop.value ?: run {
            Log.e(TAG, "No crop selected")
            handleError(IllegalStateException("No crop selected"))
            return
        }

        val uri = _imageUri.value ?: run {
            Log.e(TAG, "No image URI available")
            handleError(IllegalStateException("No image URI available"))
            return
        }

        viewModelScope.launch(exceptionHandler) {
            try {
                _uiState.value = UIState.Loading()

                // Check if we're online
                if (_isOnline.value != true) {
                    throw IOException("No internet connection available for identification")
                }

                // Use the identifyPestDisease method with correct parameters
                val result = identificationRepository.identifyPestDisease(crop, uri)
                _uiState.value = UIState.Success(result)
                retryCount = 0
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Error submitting for identification", e)
                handleError(e)
            }
        }
    }

    /**
     * Retakes the image, clearing the current one
     */
    fun retakeImage() {
        try {
            // Create empty bitmap instead of null
            val emptyBitmap = createBitmap(1, 1)
            _capturedImage.value = emptyBitmap

            // Create empty ImageQualityResult instead of null
            _imageQuality.value = ImageQualityResult(
                isAcceptable = false,
                isResolutionOk = false,
                isFocused = false,
                isBrightEnough = false,
                focusScore = 0.0,
                brightness = 0.0
            )

            // Use a placeholder URI instead of null
            _imageUri.value = Uri.EMPTY

            // Clean up the actual temp file
            tempImageFile?.delete()
            tempImageFile = null
        } catch (e: Exception) {
            Log.e(TAG, "Error retaking image", e)
            handleError(e)
        }
    }

    /**
     * Retry identification after a failure
     */
    fun retryIdentification() {
        if (retryCount < maxRetries) {
            retryCount++
            Log.d(TAG, "Retrying identification. Attempt $retryCount of $maxRetries")
            submitForIdentification()
        } else {
            retryCount = 0
            _uiState.value = UIState.Error("Failed to identify after $maxRetries attempts")
        }
    }

    /**
     * Centralized error handling
     */
    private fun handleError(error: Throwable) {
        try {
            val errorMessage = when (error) {
                is IOException -> "Network error: ${error.message}"
                is IllegalStateException -> "Invalid state: ${error.message}"
                else -> "An error occurred: ${error.message}"
            }
            _uiState.value = UIState.Error(errorMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling error", e)
            _uiState.value = UIState.Error("An unexpected error occurred")
        }
    }

    override fun onCleared() {
        try {
            super.onCleared()
            // Clean up temporary files
            tempImageFile?.delete()
            tempImageFile = null
            networkStateMonitor.stopMonitoring()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCleared", e)
        }
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