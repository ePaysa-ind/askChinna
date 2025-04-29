/*
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.askchinna.data.local.AppDatabase
import com.example.askchinna.data.local.SharedPreferencesManager
import com.example.askchinna.data.model.Action
import com.example.askchinna.data.model.Crop
import com.example.askchinna.data.model.IdentificationResult
import com.example.askchinna.data.remote.FirestoreManager
import com.example.askchinna.data.remote.GeminiService
import com.example.askchinna.data.repository.CropRepository
import com.example.askchinna.data.repository.IdentificationRepository
import com.example.askchinna.util.ImageHelper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

/**
 * Unit tests for IdentificationRepository
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class IdentificationRepositoryTest {

    private lateinit var identificationRepository: IdentificationRepository
    private lateinit var appDatabase: AppDatabase
    private lateinit var firestoreManager: FirestoreManager
    private lateinit var geminiService: GeminiService
    private lateinit var cropRepository: CropRepository
    private lateinit var imageHelper: ImageHelper
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        appDatabase = mockk(relaxed = true)
        firestoreManager = mockk(relaxed = true)
        geminiService = mockk(relaxed = true)
        cropRepository = mockk(relaxed = true)
        imageHelper = mockk(relaxed = true)
        sharedPreferencesManager = mockk(relaxed = true)

        identificationRepository = IdentificationRepository(
            appDatabase = appDatabase,
            firestoreManager = firestoreManager,
            geminiService = geminiService,
            cropRepository = cropRepository,
            imageHelper = imageHelper,
            sharedPreferencesManager = sharedPreferencesManager,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `getCachedResult returns result from database when available`() = runTest {
        // Given
        val imagePath = "/storage/emulated/0/Pictures/askChinna/test.jpg"
        val cropId = "crop_123"
        val mockResult = createSampleIdentificationResult(imagePath, cropId)

        coEvery { appDatabase.identificationResultDao().getResultByCropAndImage(cropId, imagePath) } returns mockResult

        // When
        val result = identificationRepository.getCachedResult(imagePath, cropId)

        // Then
        assertNotNull(result)
        assertEquals(mockResult.id, result?.id)
        assertEquals(cropId, result?.cropId)
        assertEquals(imagePath, result?.imagePath)
    }

    @Test
    fun `getCachedResult returns null when no cached result exists`() = runTest {
        // Given
        val imagePath = "/storage/emulated/0/Pictures/askChinna/test.jpg"
        val cropId = "crop_123"

        coEvery { appDatabase.identificationResultDao().getResultByCropAndImage(cropId, imagePath) } returns null

        // When
        val result = identificationRepository.getCachedResult(imagePath, cropId)

        // Then
        assertNull(result)
    }

    @Test
    fun `cacheResult saves result to database`() = runTest {
        // Given
        val result = createSampleIdentificationResult(
            "/storage/emulated/0/Pictures/askChinna/test.jpg",
            "crop_123"
        )

        // When
        identificationRepository.cacheResult(result)

        // Then
        coVerify { appDatabase.identificationResultDao().insert(result) }
    }

    @Test
    fun `identifyIssue calls Gemini service and processes result correctly`() = runTest {
        // Given
        val imagePath = "/storage/emulated/0/Pictures/askChinna/test.jpg"
        val cropId = "crop_123"
        val cropName = "Tomato"
        val mockFile = mockk<File>(relaxed = true)
        val base64Image = "base64encodedimage"
        val mockResponse = """
            {
                "issueType": "Fungal",
                "issueName": "Early Blight",
                "issueDescription": "Early blight is a fungal disease that causes leaf spotting.",
                "severity": 0.75,
                "actions": [
                    {"type": "SPRAY", "title": "Apply Fungicide", "description": "Use copper-based fungicide."},
                    {"type": "REMOVE", "title": "Remove Infected Leaves", "description": "Remove and destroy infected leaves."}
                ]
            }
        """.trimIndent()

        every { imageHelper.getImageFile(imagePath) } returns mockFile
        every { imageHelper.encodeImageToBase64(mockFile) } returns base64Image
        coEvery { geminiService.analyzeImage(any(), any(), any()) } returns mockResponse

        // When
        val result = identificationRepository.identifyIssue(imagePath, cropId, cropName)

        // Then
        assertNotNull(result)
        assertEquals("Early Blight", result.issueName)
        assertEquals("Fungal", result.issueType)
        assertEquals(0.75f, result.severity)
        assertEquals(2, result.actions.size)
        assertEquals(Action.Type.SPRAY, result.actions[0].type)
        assertEquals(Action.Type.REMOVE, result.actions[1].type)
    }

    @Test
    fun `performOfflineIdentification returns fallback result when offline`() = runTest {
        // Given
        val imagePath = "/storage/emulated/0/Pictures/askChinna/test.jpg"
        val cropId = "crop_123"
        val cropName = "Tomato"

        // When
        val result = identificationRepository.performOfflineIdentification(imagePath, cropId, cropName)

        // Then
        assertNotNull(result)
        assertEquals(cropId, result.cropId)
        assertEquals(imagePath, result.imagePath)
        assertEquals(cropName, result.cropName)
        // Verify the result has offline indicator
        assertEquals(true, result.isOfflineResult)
    }

    @Test
    fun `getCropNameById delegates to cropRepository`() = runTest {
        // Given
        val cropId = "crop_123"
        val expectedCropName = "Tomato"
        val mockCrop = Crop(cropId, expectedCropName, "ic_tomato")

        coEvery { cropRepository.getCropById(cropId) } returns mockCrop

        // When
        val cropName = identificationRepository.getCropNameById(cropId)

        // Then
        assertEquals(expectedCropName, cropName)
        coVerify { cropRepository.getCropById(cropId) }
    }

    @Test
    fun `submitFeedback saves feedback to Firestore`() = runTest {
        // Given
        val resultId = "result_123"
        val feedbackValue = "helpful"

        // When
        identificationRepository.submitFeedback(resultId, feedbackValue)

        // Then
        coVerify { firestoreManager.saveResultFeedback(resultId, feedbackValue) }
    }

    private fun createSampleIdentificationResult(
        imagePath: String,
        cropId: String
    ): IdentificationResult {
        return IdentificationResult(
            id = "test_id_123",
            cropId = cropId,
            cropName = "Tomato",
            imagePath = imagePath,
            issueName = "Early Blight",
            issueDescription = "A fungal disease that affects tomato plants.",
            issueType = "Fungal",
            confidence = 0.85f,
            severity = 0.7f,
            timestamp = System.currentTimeMillis(),
            isOfflineResult = false,
            actions = listOf(
                Action(
                    id = "action_1",
                    type = Action.Type.SPRAY,
                    title = "Apply Fungicide",
                    description = "Use copper-based fungicide early in the morning."
                ),
                Action(
                    id = "action_2",
                    type = Action.Type.REMOVE,
                    title = "Remove Infected Leaves",
                    description = "Carefully remove infected leaves and dispose of them."
                )
            )
        )
    }
}

