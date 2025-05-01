/*
 * Copyright (c) 2025 askChinna App Development Team
 * File: app/src/main/java/com/example/askchinna/data/repository/IdentificationRepository.kt
 * Created: April 29, 2025
 * Version: 1.0.0
 */

package com.example.askchinna.data.repository

import android.graphics.Bitmap
import android.net.Uri
import com.example.askchinna.data.model.Crop
import com.example.askchinna.data.model.IdentificationResult
import com.example.askchinna.data.remote.FirestoreManager
import com.example.askchinna.data.remote.GeminiService
import com.example.askchinna.data.remote.NetworkExceptionHandler
import com.example.askchinna.util.DateTimeUtils
import com.example.askchinna.util.ImageHelper
import com.example.askchinna.util.SessionManager
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * Repository responsible for handling crop disease identification requests,
 * processing images, interacting with the Gemini API, and storing results.
 *
 * This class coordinates between local storage, Firebase, and the Gemini AI API
 * to identify crop diseases from user-submitted images.
 */
class IdentificationRepository(
    private val cropRepository: CropRepository,
    private val firestoreManager: FirestoreManager,
    private val geminiService: GeminiService,
    private val imageHelper: ImageHelper,
    private val sessionManager: SessionManager,
    private val dateTimeUtils: DateTimeUtils,
    private val networkExceptionHandler: NetworkExceptionHandler,
    private val ioDispatcher: CoroutineDispatcher
) {
    // Firebase Storage instance for image uploading
    private val storage = FirebaseStorage.getInstance()

    // Storage reference for crop images
    private val storageRef = storage.reference.child("crop_images")

    /**
     * Identifies a crop disease from a captured image.
     *
     * This method performs the following steps:
     * 1. Processes and prepares the image for analysis
     * 2. Uploads the image to Firebase Storage
     * 3. Calls the Gemini API to analyze the image
     * 4. Processes and stores the identification result
     * 5. Increments the user's identification usage count
     *
     * @param imageUri The URI of the captured image
     * @param cropId The ID of the crop in the image
     * @param userId The ID of the user submitting the identification request
     * @return An IdentificationResult containing the disease diagnosis and recommendations
     * @throws IOException If there's an error processing the image
     * @throws IllegalStateException If the user has exceeded their usage limit
     */
    suspend fun identifyCropDisease(
        imageUri: Uri,
        cropId: String,
        userId: String
    ): IdentificationResult = withContext(ioDispatcher) {
        Timber.d("Starting crop disease identification for crop: $cropId")

        // Check usage limits
        if (sessionManager.hasExceededUsageLimit()) {
            val message = "User has exceeded identification usage limit"
            Timber.w(message)
            throw IllegalStateException(message)
        }

        try {
            // Get crop info
            val crop = cropRepository.getCropById(cropId) ?: throw IllegalArgumentException("Invalid crop ID")

            // Process image
            Timber.d("Processing image for identification")
            val bitmap = imageHelper.getScaledBitmapFromUri(imageUri, MAX_IMAGE_DIMENSION, MAX_IMAGE_DIMENSION)
                ?: throw IOException("Failed to process image")

            // Optimize image quality and size for API
            val processedBitmap = imageHelper.enhanceImageForAnalysis(bitmap)

            // Upload image to Firebase
            Timber.d("Uploading image to Firebase Storage")
            val imageUrl = uploadImageToFirebase(processedBitmap, cropId, userId)

            // Send to Gemini API for analysis
            Timber.d("Sending image to Gemini API for analysis")
            val analysisResponse = geminiService.analyzeImage(
                imageUrl,
                crop.name,
                crop.scientificName
            )

            // Process API response
            Timber.d("Processing Gemini API response")
            val identificationResult = processAnalysisResponse(analysisResponse, crop, imageUrl)

            // Save result to Firestore
            Timber.d("Saving identification result to Firestore")
            saveIdentificationResult(identificationResult, userId)

            // Increment usage count
            sessionManager.incrementIdentificationUsage()

            return@withContext identificationResult
        } catch (e: Exception) {
            Timber.e(e, "Error identifying crop disease")
            throw networkExceptionHandler.handleException(e)
        }
    }

    /**
     * Uploads an image to Firebase Storage.
     *
     * @param bitmap The image to upload
     * @param cropId The ID of the crop in the image
     * @param userId The ID of the user who captured the image
     * @return The URL of the uploaded image
     * @throws IOException If there's an error uploading the image
     */
    private suspend fun uploadImageToFirebase(
        bitmap: Bitmap,
        cropId: String,
        userId: String
    ): String = withContext(ioDispatcher) {
        try {
            val timestamp = dateTimeUtils.formatTimestampForFilename(System.currentTimeMillis())
            val filename = "${userId}_${cropId}_${timestamp}.jpg"
            val imageRef = storageRef.child(cropId).child(filename)

            // Convert bitmap to bytes
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, baos)
            val data = baos.toByteArray()

            // Upload image
            val uploadTask = imageRef.putBytes(data).await()

            // Get download URL
            return@withContext imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Timber.e(e, "Error uploading image to Firebase")
            throw IOException("Failed to upload image: ${e.message}", e)
        }
    }

    /**
     * Processes the analysis response from the Gemini API.
     *
     * @param response The raw response from the Gemini API
     * @param crop The crop that was analyzed
     * @param imageUrl The URL of the analyzed image
     * @return A structured IdentificationResult
     */
    private fun processAnalysisResponse(
        response: GeminiService.AnalysisResponse,
        crop: Crop,
        imageUrl: String
    ): IdentificationResult {
        // Create a unique ID for the result
        val resultId = UUID.randomUUID().toString()

        // Map disease type from string to enum
        val diseaseType = when (response.diseaseType.lowercase()) {
            "fungal" -> IdentificationResult.DiseaseType.FUNGAL
            "bacterial" -> IdentificationResult.DiseaseType.BACTERIAL
            "viral" -> IdentificationResult.DiseaseType.VIRAL
            "deficiency" -> IdentificationResult.DiseaseType.DEFICIENCY
            else -> IdentificationResult.DiseaseType.OTHER
        }

        // Map severity from string to enum
        val severity = when (response.severity.lowercase()) {
            "high" -> IdentificationResult.Severity.HIGH
            "medium" -> IdentificationResult.Severity.MEDIUM
            "low" -> IdentificationResult.Severity.LOW
            else -> IdentificationResult.Severity.MEDIUM
        }

        // Convert recommended actions to Action objects
        val actions = response.recommendations.map { recommendation ->
            val actionType = when {
                recommendation.contains("spray", ignoreCase = true) ->
                    com.example.askchinna.data.model.Action.Type.SPRAY
                recommendation.contains("water", ignoreCase = true) ->
                    com.example.askchinna.data.model.Action.Type.WATER
                recommendation.contains("remove", ignoreCase = true) ->
                    com.example.askchinna.data.model.Action.Type.REMOVE
                recommendation.contains("monitor", ignoreCase = true) ->
                    com.example.askchinna.data.model.Action.Type.MONITOR
                else ->
                    com.example.askchinna.data.model.Action.Type.FERTILIZE
            }

            // Determine severity based on language used
            val actionSeverity = when {
                recommendation.contains("immediately", ignoreCase = true) ||
                        recommendation.contains("urgent", ignoreCase = true) ||
                        recommendation.contains("critical", ignoreCase = true) ->
                    com.example.askchinna.data.model.Action.Severity.HIGH
                recommendation.contains("consider", ignoreCase = true) ||
                        recommendation.contains("may", ignoreCase = true) ||
                        recommendation.contains("option", ignoreCase = true) ->
                    com.example.askchinna.data.model.Action.Severity.LOW
                else ->
                    com.example.askchinna.data.model.Action.Severity.MEDIUM
            }

            com.example.askchinna.data.model.Action(
                type = actionType,
                description = recommendation,
                severity = actionSeverity
            )
        }

        // Create the identification result
        return IdentificationResult(
            id = resultId,
            cropId = crop.id,
            crop = crop,
            diseaseName = response.diseaseName,
            diseaseType = diseaseType,
            description = response.description,
            severity = severity,
            imageUrl = imageUrl,
            actions = actions,
            timestamp = System.currentTimeMillis(),
            confidence = response.confidence
        )
    }

    /**
     * Saves the identification result to Firestore.
     *
     * @param result The identification result to save
     * @param userId The ID of the user who requested the identification
     * @throws IOException If there's an error saving the result
     */
    private suspend fun saveIdentificationResult(
        result: IdentificationResult,
        userId: String
    ) = withContext(ioDispatcher) {
        try {
            firestoreManager.saveIdentificationResult(result, userId)
        } catch (e: Exception) {
            Timber.e(e, "Error saving identification result")
            throw IOException("Failed to save identification result: ${e.message}", e)
        }
    }

    /**
     * Retrieves an identification result by its ID.
     *
     * @param resultId The ID of the result to retrieve
     * @return The identification result
     * @throws IOException If there's an error retrieving the result
     */
    suspend fun getIdentificationResult(resultId: String): IdentificationResult = withContext(ioDispatcher) {
        try {
            Timber.d("Retrieving identification result: $resultId")
            val result = firestoreManager.getIdentificationResult(resultId)

            // Fetch the full crop data if needed
            if (result.crop == null) {
                val crop = cropRepository.getCropById(result.cropId)
                if (crop != null) {
                    return@withContext result.copy(crop = crop)
                }
            }

            return@withContext result
        } catch (e: Exception) {
            Timber.e(e, "Error retrieving identification result")
            throw IOException("Failed to retrieve identification result: ${e.message}", e)
        }
    }

    /**
     * Retrieves all identification results for a user.
     *
     * @param userId The ID of the user
     * @return A list of identification results
     * @throws IOException If there's an error retrieving the results
     */
    suspend fun getUserIdentificationResults(userId: String): List<IdentificationResult> = withContext(ioDispatcher) {
        try {
            Timber.d("Retrieving identification results for user: $userId")
            val results = firestoreManager.getUserIdentificationResults(userId)

            // Populate crop data for each result if needed
            return@withContext results.map { result ->
                if (result.crop == null) {
                    val crop = cropRepository.getCropById(result.cropId)
                    if (crop != null) {
                        result.copy(crop = crop)
                    } else {
                        result
                    }
                } else {
                    result
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error retrieving user identification results")
            throw IOException("Failed to retrieve user identification results: ${e.message}", e)
        }
    }

    /**
     * Submits user feedback for an identification result.
     *
     * @param resultId The ID of the result
     * @param rating The user's rating (1-5)
     * @param comment The user's comment
     * @param isAccurate Whether the user found the result accurate
     * @return true if feedback was submitted successfully
     * @throws IOException If there's an error submitting the feedback
     */
    suspend fun submitFeedback(
        resultId: String,
        rating: Int,
        comment: String,
        isAccurate: Boolean
    ): Boolean = withContext(ioDispatcher) {
        try {
            Timber.d("Submitting feedback for result: $resultId")

            // Validate rating
            if (rating < 1 || rating > 5) {
                throw IllegalArgumentException("Rating must be between 1 and 5")
            }

            // Create feedback map
            val feedback = hashMapOf(
                "resultId" to resultId,
                "rating" to rating,
                "comment" to comment,
                "isAccurate" to isAccurate,
                "timestamp" to System.currentTimeMillis()
            )

            // Save feedback to Firestore
            firestoreManager.submitFeedback(resultId, feedback)
            return@withContext true
        } catch (e: Exception) {
            Timber.e(e, "Error submitting feedback")
            throw IOException("Failed to submit feedback: ${e.message}", e)
        }
    }

    /**
     * Saves an identification result as a local file.
     *
     * @param resultId The ID of the result to save
     * @param outputDir The directory to save the file to
     * @return The saved file
     * @throws IOException If there's an error saving the result
     */
    suspend fun saveResultAsFile(resultId: String, outputDir: File): File = withContext(ioDispatcher) {
        try {
            Timber.d("Saving identification result as file: $resultId")

            // Get the result
            val result = getIdentificationResult(resultId)

            // Create a JSON representation
            val json = buildResultJson(result)

            // Save to file
            val timestamp = dateTimeUtils.formatTimestampForFilename(result.timestamp)
            val filename = "identification_${result.cropId}_${timestamp}.json"
            val outputFile = File(outputDir, filename)

            outputFile.writeText(json)
            return@withContext outputFile
        } catch (e: Exception) {
            Timber.e(e, "Error saving result as file")
            throw IOException("Failed to save result as file: ${e.message}", e)
        }
    }

    /**
     * Builds a JSON representation of an identification result.
     *
     * @param result The result to convert to JSON
     * @return A JSON string
     */
    private fun buildResultJson(result: IdentificationResult): String {
        // In a real implementation, this would use a proper JSON library like Gson or Moshi
        // This is a simplified implementation for demonstration
        return """
            {
                "id": "${result.id}",
                "cropId": "${result.cropId}",
                "cropName": "${result.crop?.name ?: "Unknown"}",
                "diseaseName": "${result.diseaseName}",
                "diseaseType": "${result.diseaseType}",
                "description": "${result.description}",
                "severity": "${result.severity}",
                "imageUrl": "${result.imageUrl}",
                "timestamp": ${result.timestamp},
                "confidence": ${result.confidence},
                "actions": [
                    ${result.actions.joinToString(",") { action ->
            """
                        {
                            "type": "${action.type}",
                            "description": "${action.description}",
                            "severity": "${action.severity}"
                        }
                        """.trimIndent()
        }}
                ]
            }
        """.trimIndent()
    }

    /**
     * Downloads the image for an identification result.
     *
     * @param imageUrl The URL of the image to download
     * @param outputDir The directory to save the image to
     * @return The downloaded image file
     * @throws IOException If there's an error downloading the image
     */
    suspend fun downloadResultImage(imageUrl: String, outputDir: File): File = withContext(ioDispatcher) {
        try {
            Timber.d("Downloading result image: $imageUrl")

            // Extract filename from URL
            val filename = imageUrl.substringAfterLast("/").takeIf { it.isNotEmpty() }
                ?: "image_${System.currentTimeMillis()}.jpg"

            // Create output file
            val outputFile = File(outputDir, filename)

            // Download image
            val bitmap = imageHelper.downloadImage(imageUrl)
                ?: throw IOException("Failed to download image")

            // Save bitmap to file
            val outputStream = outputFile.outputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, outputStream)
            outputStream.flush()
            outputStream.close()

            return@withContext outputFile
        } catch (e: Exception) {
            Timber.e(e, "Error downloading result image")
            throw IOException("Failed to download result image: ${e.message}", e)
        }
    }

    companion object {
        // Constants for image processing
        private const val MAX_IMAGE_DIMENSION = 1024 // Maximum width/height for uploaded images
        private const val IMAGE_QUALITY = 85 // JPEG compression quality
    }
}

