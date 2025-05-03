/**
 * File: app/src/test/java/com/example/askchinna/viewmodel/IdentificationViewModelTest.kt
 * Copyright (c) 2025 askChinna
 * Created: April 30, 2025
 * Version: 1.0
 *
 * Unit tests for IdentificationViewModel using MockK and modern Kotlin testing approaches
 */
package com.example.askchinna.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.askchinna.data.model.Crop
import com.example.askchinna.data.model.IdentificationResult
import com.example.askchinna.data.model.UIState
import com.example.askchinna.data.repository.IdentificationRepository
import com.example.askchinna.data.repository.UserRepository
import com.example.askchinna.ui.identification.IdentificationViewModel
import com.example.askchinna.util.ImageHelper
import com.example.askchinna.util.NetworkStateMonitor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.io.File
import java.io.IOException

@ExperimentalCoroutinesApi
class IdentificationViewModelTest {

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    // Mocks
    private lateinit var identificationRepository: IdentificationRepository
    private lateinit var userRepository: UserRepository
    private lateinit var imageHelper: ImageHelper
    private lateinit var networkStateMonitor: NetworkStateMonitor
    private lateinit var viewModel: IdentificationViewModel

    // Test data
    private val mockBitmap = mockk<Bitmap>()
    private val mockOptimizedBitmap = mockk<Bitmap>()
    private val mockUri = mockk<Uri>()
    private val mockImageFile = mockk<File>()
    private val mockCrop = Crop(
        id = "rice",
        name = "Rice",
        scientificName = "Oryza sativa",
        description = "Rice description",
        iconResourceName = "ic_rice",
        growthRegions = listOf("Region1", "Region2"),
        growingSeasons = listOf("Season1", "Season2"),
        soilTypes = listOf("Soil1", "Soil2")
    )
    private val mockResult = IdentificationResult(
        id = "test_id",
        cropId = "rice",
        crop = mockCrop,
        diseaseName = "Test Disease",
        diseaseType = IdentificationResult.DiseaseType.FUNGAL,
        description = "Test description",
        severity = IdentificationResult.Severity.MEDIUM,
        imageUrl = "https://example.com/image.jpg",
        actions = emptyList(),
        timestamp = System.currentTimeMillis(),
        confidence = 0.85f
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Initialize mocks
        identificationRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        imageHelper = mockk(relaxed = true)
        networkStateMonitor = mockk(relaxed = true)

        // Set up default behavior
        every { networkStateMonitor.networkState } returns MutableStateFlow(true)
        every { networkStateMonitor.isNetworkAvailable() } returns true
        coEvery { userRepository.getRemainingUsageCount() } returns 5
        coEvery { imageHelper.getBitmapFromUri(any()) } returns mockBitmap
        coEvery { imageHelper.optimizeImageForUpload(any(), any()) } returns mockOptimizedBitmap
        coEvery { imageHelper.assessImageQuality(any()) } returns 85
        coEvery { imageHelper.saveImageToCache(any(), any()) } returns mockImageFile
        every { mockImageFile.length() } returns 1024L
        coEvery { identificationRepository.identifyWithGeminiAPI(any(), any()) } returns mockResult
        coEvery { identificationRepository.identifyOffline(any(), any()) } returns mockResult
        coEvery { identificationRepository.cacheIdentificationResult(any()) } returns Unit

        // Initialize viewModel after setting up mocks
        viewModel = IdentificationViewModel(
            identificationRepository,
            userRepository,
            imageHelper,
            networkStateMonitor
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setSelectedCrop updates selected crop state`() = runTest {
        // When
        viewModel.setSelectedCrop(mockCrop)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(mockCrop, viewModel.selectedCrop.value)
    }

    @Test
    fun `processImageFromUri processes image successfully`() = runTest {
        // When
        viewModel.processImageFromUri(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(UIState.Success, viewModel.uiState.value)
        assertEquals(mockOptimizedBitmap, viewModel.capturedImage.value)
        assertEquals(85, viewModel.imageQuality.value)
        coVerify { imageHelper.getBitmapFromUri(mockUri) }
        coVerify { imageHelper.optimizeImageForUpload(mockBitmap, any()) }
        coVerify { imageHelper.assessImageQuality(mockOptimizedBitmap) }
        coVerify { imageHelper.saveImageToCache(mockOptimizedBitmap, any()) }
    }

    @Test
    fun `processImageFromUri handles error when loading image fails`() = runTest {
        // Given
        coEvery { imageHelper.getBitmapFromUri(any()) } returns null

        // When
        viewModel.processImageFromUri(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is UIState.Error)
        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun `processImageFromCamera processes image successfully`() = runTest {
        // When
        viewModel.processImageFromCamera(mockBitmap)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(UIState.Success, viewModel.uiState.value)
        assertEquals(mockOptimizedBitmap, viewModel.capturedImage.value)
        assertEquals(85, viewModel.imageQuality.value)
        coVerify { imageHelper.optimizeImageForUpload(mockBitmap, any()) }
        coVerify { imageHelper.assessImageQuality(mockOptimizedBitmap) }
        coVerify { imageHelper.saveImageToCache(mockOptimizedBitmap, any()) }
    }

    @Test
    fun `submitForIdentification requires selected crop`() = runTest {
        // Given
        viewModel = IdentificationViewModel(
            identificationRepository,
            userRepository,
            imageHelper,
            networkStateMonitor
        ) // Reset viewModel to clear selectedCrop

        // When
        viewModel.submitForIdentification()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNotNull(viewModel.errorMessage.value)
        assertEquals("Please select a crop first", viewModel.errorMessage.value)
    }

    @Test
    fun `submitForIdentification checks usage limits`() = runTest {
        // Given
        coEvery { userRepository.getRemainingUsageCount() } returns 0
        viewModel.setSelectedCrop(mockCrop)
        val fileSlot = slot<File>()
        coEvery { imageHelper.saveImageToCache(any(), any()) } returns mockImageFile
        viewModel.processImageFromCamera(mockBitmap)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.submitForIdentification()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(UIState.Error, viewModel.uiState.value)
        assertEquals("You have reached the maximum usage limit for this month.", viewModel.errorMessage.value)
    }

    @Test
    fun `submitForIdentification uses online API when network available`() = runTest {
        // Given
        every { networkStateMonitor.isNetworkAvailable() } returns true
        viewModel.setSelectedCrop(mockCrop)
        viewModel.processImageFromCamera(mockBitmap)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.submitForIdentification()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(UIState.Success, viewModel.uiState.value)
        assertEquals(mockResult, viewModel.identificationResult.value)
        coVerify { identificationRepository.identifyWithGeminiAPI(mockImageFile, mockCrop) }
        coVerify { userRepository.decrementUsageCount() }
        coVerify { identificationRepository.cacheIdentificationResult(mockResult) }
    }

    @Test
    fun `submitForIdentification uses offline model when network unavailable`() = runTest {
        // Given
        every { networkStateMonitor.isNetworkAvailable() } returns false
        viewModel.setSelectedCrop(mockCrop)
        viewModel.processImageFromCamera(mockBitmap)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.submitForIdentification()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(UIState.Success, viewModel.uiState.value)
        assertEquals(mockResult, viewModel.identificationResult.value)
        coVerify { identificationRepository.identifyOffline(mockImageFile, mockCrop) }
        coVerify { userRepository.decrementUsageCount() }
        coVerify { identificationRepository.cacheIdentificationResult(mockResult) }
    }

    @Test
    fun `submitForIdentification handles error during identification`() = runTest {
        // Given
        val errorMessage = "API error"
        coEvery { identificationRepository.identifyWithGeminiAPI(any(), any()) } throws IOException(errorMessage)
        viewModel.setSelectedCrop(mockCrop)
        viewModel.processImageFromCamera(mockBitmap)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.submitForIdentification()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(UIState.Error, viewModel.uiState.value)
        assertTrue(viewModel.errorMessage.value?.contains(errorMessage) == true)
    }

    @Test
    fun `retryIdentification resets error state and attempts again`() = runTest {
        // Given - error state
        coEvery { identificationRepository.identifyWithGeminiAPI(any(), any()) } throws IOException("First error")
        viewModel.setSelectedCrop(mockCrop)
        viewModel.processImageFromCamera(mockBitmap)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.submitForIdentification()
        testDispatcher.scheduler.advanceUntilIdle()

        // Now setup for success on retry
        coEvery { identificationRepository.identifyWithGeminiAPI(any(), any()) } returns mockResult

        // When
        viewModel.retryIdentification()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(viewModel.errorMessage.value)
        assertEquals(UIState.Success, viewModel.uiState.value)
        assertEquals(mockResult, viewModel.identificationResult.value)
    }

    @Test
    fun `clearImage resets state`() = runTest {
        // Given - image processed
        viewModel.processImageFromCamera(mockBitmap)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.clearImage()

        // Then
        assertNull(viewModel.capturedImage.value)
        assertEquals(0, viewModel.imageQuality.value)
        assertNull(viewModel.errorMessage.value)
        assertEquals(UIState.Initial, viewModel.uiState.value)
    }
}