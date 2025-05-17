/**
 * File: app/src/test/java/com/example/askchinna/repository/IdentificationRepositoryTest.kt
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Updated: May 16, 2025
 * Version: 1.3
 *
 * Change Log:
 * 1.3 - May 16, 2025
 * - Added MockFirebaseRule to prevent Firebase initialization errors
 * - Fixed explicit type parameters for any() calls in mocks
 * - Improved test structure for better isolation
 * 1.2 - May 14, 2025
 * - Updated repository constructor to match new signature
 * - Fixed Action constructor calls with required parameters
 * - Improved test structure and error handling
 * - Added proper documentation
 * - Enhanced mock setup
 */

package com.example.askchinna.repository

import android.net.Uri
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.askchinna.data.model.Crop
import com.example.askchinna.data.remote.GeminiService
import com.example.askchinna.data.repository.CropRepository
import com.example.askchinna.data.repository.IdentificationRepository
import com.example.askchinna.util.ImageHelper
import com.example.askchinna.util.MockFirebaseRule
import com.example.askchinna.util.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

/**
 * Unit tests for IdentificationRepository
 * Tests pest/disease identification, result processing, and error handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class IdentificationRepositoryTest {

    // Apply MockFirebaseRule first to ensure Firebase is mocked before tests run
    @get:Rule
    val mockFirebaseRule = MockFirebaseRule()

    // Repository under test
    private lateinit var identificationRepository: IdentificationRepository

    // Dependencies
    private lateinit var geminiService: GeminiService
    private lateinit var cropRepository: CropRepository
    private lateinit var imageHelper: ImageHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var firestore: FirebaseFirestore

    // Test dispatcher for coroutines
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Create proper mocks with relaxed behavior
        geminiService = mockk(relaxed = true)
        cropRepository = mockk(relaxed = true)
        imageHelper = mockk(relaxed = true)
        sessionManager = mockk(relaxed = true)
        firestore = mockk(relaxed = true)

        // Create the repository with updated constructor signature
        identificationRepository = IdentificationRepository(
            geminiService = geminiService,
            imageHelper = imageHelper,
            sessionManager = sessionManager,
            ioDispatcher = testDispatcher,
            firestore = firestore
        )
    }

    /**
     * Tests that identifyPestDisease calls Gemini service and processes result correctly
     */
    @Test
    fun `identifyPestDisease calls Gemini service and processes result correctly`() = runTest {
        // Given
        val cropId = "crop_123"
        val cropName = "Tomato"
        // Use integer resource ID instead of string
        val mockCrop = Crop(cropId, cropName, 0) // Use 0 or R.drawable.ic_tomato for resource ID
        val mockUri = mockk<Uri>(relaxed = true)
        val mockFile = mockk<File>(relaxed = true)
        val mockResponse = "Early Blight\n\nDescription: Early blight is a fungal disease that causes leaf spotting.\n\nSeverity: 2\n\nActions:\n1. Apply copper-based fungicide.\n2. Remove infected leaves.\n\nScientific name: Alternaria solani\n\nType: Fungal"

        // Set up mocks
        every { imageHelper.compressImage(any()) } returns mockFile
        coEvery { geminiService.generateContent(any(), any(), any()) } returns Result.success(mockResponse)
        every { sessionManager.getCurrentUser()?.uid } returns "test_user_id"

        // When
        val result = identificationRepository.identifyPestDisease(mockCrop, mockUri)

        // Then
        assertNotNull(result)
        assertEquals(mockCrop.id, result.cropId)
        assertEquals(mockCrop.name, result.cropName)
        // Verify some of the parsed content
        assertTrue(result.actions.isNotEmpty())
    }

    /**
     * Tests that identifyPestDisease returns error result when exception occurs
     */
    @Test
    fun `identifyPestDisease returns error result when exception occurs`() = runTest {
        // Given
        val cropId = "crop_123"
        val cropName = "Tomato"
        // Use integer resource ID instead of string
        val mockCrop = Crop(cropId, cropName, 0) // Use 0 or R.drawable.ic_tomato for resource ID
        val mockUri = mockk<Uri>(relaxed = true)

        // Set up mocks to throw an exception
        coEvery { geminiService.generateContent(any(), any(), any()) } throws Exception("Test exception")
        every { sessionManager.getCurrentUser()?.uid } returns "test_user_id"

        // When
        val result = identificationRepository.identifyPestDisease(mockCrop, mockUri)

        // Then
        assertNotNull(result)
        assertEquals(mockCrop.id, result.cropId)
        assertEquals(mockCrop.name, result.cropName)
        assertEquals("Identification Failed", result.problemName)
        assertTrue(result.confidence == 0f)
    }

    /**
     * Tests that the generatePrompt private method creates proper prompt for crop
     * Using reflection to access private method
     */
    @Test
    fun `generatePrompt creates proper prompt for crop`() = runTest {
        // Given
        val cropId = "crop_123"
        val cropName = "Tomato"
        // Use integer resource ID instead of string
        val mockCrop = Crop(cropId, cropName, 0) // Use 0 or R.drawable.ic_tomato for resource ID

        // Use reflection to access private method
        val generatePromptMethod = IdentificationRepository::class.java.getDeclaredMethod("generatePrompt", Crop::class.java)
        generatePromptMethod.isAccessible = true

        // When
        val prompt = generatePromptMethod.invoke(identificationRepository, mockCrop) as String

        // Then
        assertNotNull(prompt)
        assertTrue(prompt.contains(cropName))
        assertTrue(prompt.contains("pest or disease"))
    }

    /**
     * Tests that the determineActionType private method categorizes action text correctly
     * Using reflection to access private method
     */
    @Test
    fun `determineActionType categorizes action text correctly`() = runTest {
        // Given
        val sprayText = "Apply copper-based fungicide to affected areas"
        val removeText = "Remove infected leaves and dispose of them"
        val waterText = "Ensure proper irrigation to prevent stress"
        val unknownText = "Something completely different"

        // Use reflection to access private method
        val determineActionTypeMethod = IdentificationRepository::class.java.getDeclaredMethod("determineActionType", String::class.java)
        determineActionTypeMethod.isAccessible = true

        // When
        val sprayType = determineActionTypeMethod.invoke(identificationRepository, sprayText) as String
        val removeType = determineActionTypeMethod.invoke(identificationRepository, removeText) as String
        val waterType = determineActionTypeMethod.invoke(identificationRepository, waterText) as String
        val unknownType = determineActionTypeMethod.invoke(identificationRepository, unknownText) as String

        // Then
        assertEquals("spray", sprayType)
        assertEquals("remove", removeType)
        assertEquals("water", waterType)
        assertEquals("general", unknownType)
    }

    /**
     * Tests that the calculateConfidence private method generates appropriate confidence score
     * Using reflection to access private method
     */
    @Test
    fun `calculateConfidence generates appropriate confidence score`() = runTest {
        // Given
        val detailedResponse = "Early Blight\n\nDescription: This is a detailed description over 200 characters long. " +
                "It contains lots of information about the disease, symptoms, and other relevant details. " +
                "This text should trigger the detailed description bonus.\n\n" +
                "Scientific name: Alternaria solani\n\nSeverity: 2"

        val basicResponse = "Early Blight. Spray fungicide."

        // Use reflection to access private method
        val calculateConfidenceMethod = IdentificationRepository::class.java.getDeclaredMethod("calculateConfidence", String::class.java)
        calculateConfidenceMethod.isAccessible = true

        // When
        val detailedConfidence = calculateConfidenceMethod.invoke(identificationRepository, detailedResponse) as Float
        val basicConfidence = calculateConfidenceMethod.invoke(identificationRepository, basicResponse) as Float

        // Then
        assertTrue(detailedConfidence > basicConfidence)
        assertTrue(detailedConfidence >= 65.0f)  // Base confidence plus bonuses
        assertTrue(basicConfidence >= 65.0f)     // At least base confidence
    }
}