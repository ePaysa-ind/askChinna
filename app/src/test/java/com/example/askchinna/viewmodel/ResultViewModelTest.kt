/*
 * Copyright (c) 2025 askChinna App Development Team
 * File: app/src/test/java/com/example/askchinna/viewmodel/ResultViewModelTest.kt
 * Created: April 29, 2025
 * Version: 1.0.0
 */

package com.example.askchinna.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.askchinna.data.model.Action
import com.example.askchinna.data.model.Crop
import com.example.askchinna.data.model.IdentificationResult
import com.example.askchinna.data.model.UIState
import com.example.askchinna.data.repository.IdentificationRepository
import com.example.askchinna.ui.results.ResultViewModel
import com.example.askchinna.util.PdfGenerator
import com.example.askchinna.util.SimpleCoroutineUtils
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import timber.log.Timber
import java.io.File
import java.io.IOException

/**
 * Unit tests for ResultViewModel that verify the proper functioning of
 * result display, actions handling, and PDF export features.
 */
@ExperimentalCoroutinesApi
class ResultViewModelTest {
    // Executes each task synchronously using Architecture Components
    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    // Test dispatcher for coroutines
    private val testDispatcher = StandardTestDispatcher()

    // Mocks
    private lateinit var identificationRepository: IdentificationRepository
    private lateinit var pdfGenerator: PdfGenerator
    private lateinit var coroutineUtils: SimpleCoroutineUtils
    private lateinit var resultViewModel: ResultViewModel
    private lateinit var uiStateObserver: Observer<UIState<IdentificationResult>>
    private lateinit var pdfExportStateObserver: Observer<UIState<File>>
    private lateinit var feedbackSubmissionStateObserver: Observer<UIState<Boolean>>

    @Before
    fun setup() {
        // Initialize Timber for logging
        Timber.plant(object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                // No-op for tests
            }
        })

        // Set main dispatcher for coroutines
        Dispatchers.setMain(testDispatcher)

        // Create mocks
        identificationRepository = mockk(relaxed = true)
        pdfGenerator = mockk(relaxed = true)
        coroutineUtils = mockk(relaxed = true)
        uiStateObserver = mockk(relaxed = true)
        pdfExportStateObserver = mockk(relaxed = true)
        feedbackSubmissionStateObserver = mockk(relaxed = true)

        // Create view model
        resultViewModel = ResultViewModel(identificationRepository, pdfGenerator, coroutineUtils)

        // Observe LiveData
        resultViewModel.resultState.observeForever(uiStateObserver)
        resultViewModel.pdfExportState.observeForever(pdfExportStateObserver)
        resultViewModel.feedbackSubmissionState.observeForever(feedbackSubmissionStateObserver)

        // Setup coroutine utils
        every { coroutineUtils.ioDispatcher } returns testDispatcher
        every { coroutineUtils.mainDispatcher } returns testDispatcher
    }

    @After
    fun tearDown() {
        // Clean up observers
        resultViewModel.resultState.removeObserver(uiStateObserver)
        resultViewModel.pdfExportState.removeObserver(pdfExportStateObserver)
        resultViewModel.feedbackSubmissionState.removeObserver(feedbackSubmissionStateObserver)

        // Reset main dispatcher
        Dispatchers.resetMain()

        // Remove Timber tree
        Timber.uprootAll()
    }

    @Test
    fun `loadIdentificationResult fetches result successfully`() = runTest {
        // Given
        val resultId = "test_result_id"
        val mockCrop = Crop(
            id = "rice",
            name = "Rice",
            scientificName = "Oryza sativa",
            description = "Rice crop",
            iconResourceName = "ic_rice",
            growthRegions = listOf("Andhra Pradesh", "Tamil Nadu"),
            growingSeasons = listOf("Kharif", "Rabi"),
            soilTypes = listOf("Alluvial", "Clayey")
        )
        val mockActions = listOf(
            Action(
                type = Action.Type.SPRAY,
                description = "Apply fungicide",
                severity = Action.Severity.HIGH
            )
        )
        val mockResult = IdentificationResult(
            id = resultId,
            cropId = "rice",
            crop = mockCrop,
            diseaseName = "Blast",
            diseaseType = IdentificationResult.DiseaseType.FUNGAL,
            description = "Rice blast is a fungal disease",
            severity = IdentificationResult.Severity.HIGH,
            imageUrl = "https://example.com/image.jpg",
            actions = mockActions,
            timestamp = System.currentTimeMillis(),
            confidence = 0.95f
        )

        // Setup repository response
        coEvery { identificationRepository.getIdentificationResult(resultId) } returns mockResult

        // When
        resultViewModel.loadIdentificationResult(resultId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { uiStateObserver.onChanged(UIState.Loading) }
        verify { uiStateObserver.onChanged(UIState.Success(mockResult)) }
        coVerify { identificationRepository.getIdentificationResult(resultId) }
    }

    @Test
    fun `loadIdentificationResult handles error`() = runTest {
        // Given
        val resultId = "test_result_id"
        val errorMessage = "Network error"
        val exception = IOException(errorMessage)

        // Setup repository response
        coEvery { identificationRepository.getIdentificationResult(resultId) } throws exception

        // When
        resultViewModel.loadIdentificationResult(resultId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { uiStateObserver.onChanged(UIState.Loading) }
        verify { uiStateObserver.onChanged(match { it is UIState.Error && it.message.contains(errorMessage) }) }
    }

    @Test
    fun `exportToPdf generates PDF successfully`() = runTest {
        // Given
        val resultId = "test_result_id"
        val mockCrop = Crop(
            id = "rice",
            name = "Rice",
            scientificName = "Oryza sativa",
            description = "Rice crop",
            iconResourceName = "ic_rice",
            growthRegions = listOf("Andhra Pradesh", "Tamil Nadu"),
            growingSeasons = listOf("Kharif", "Rabi"),
            soilTypes = listOf("Alluvial", "Clayey")
        )
        val mockResult = IdentificationResult(
            id = resultId,
            cropId = "rice",
            crop = mockCrop,
            diseaseName = "Blast",
            diseaseType = IdentificationResult.DiseaseType.FUNGAL,
            description = "Rice blast is a fungal disease",
            severity = IdentificationResult.Severity.HIGH,
            imageUrl = "https://example.com/image.jpg",
            actions = emptyList(),
            timestamp = System.currentTimeMillis(),
            confidence = 0.95f
        )

        val mockPdfFile = mockk<File>()

        // Setup mocks
        coEvery { identificationRepository.getIdentificationResult(resultId) } returns mockResult
        coEvery { pdfGenerator.generateResultPdf(mockResult) } returns mockPdfFile

        // When
        resultViewModel.exportToPdf(resultId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { pdfExportStateObserver.onChanged(UIState.Loading) }
        verify { pdfExportStateObserver.onChanged(UIState.Success(mockPdfFile)) }
        coVerify { pdfGenerator.generateResultPdf(mockResult) }
    }

    @Test
    fun `exportToPdf handles error`() = runTest {
        // Given
        val resultId = "test_result_id"
        val errorMessage = "PDF generation error"
        val exception = IOException(errorMessage)

        // Setup mocks
        coEvery { identificationRepository.getIdentificationResult(resultId) } throws exception

        // When
        resultViewModel.exportToPdf(resultId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { pdfExportStateObserver.onChanged(UIState.Loading) }
        verify { pdfExportStateObserver.onChanged(match { it is UIState.Error && it.message.contains(errorMessage) }) }
    }

    @Test
    fun `submitFeedback sends feedback successfully`() = runTest {
        // Given
        val resultId = "test_result_id"
        val rating = 4
        val comment = "Very helpful diagnosis"
        val isAccurate = true

        // Setup repository response
        coEvery {
            identificationRepository.submitFeedback(resultId, rating, comment, isAccurate)
        } returns true

        // When
        resultViewModel.submitFeedback(resultId, rating, comment, isAccurate)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { feedbackSubmissionStateObserver.onChanged(UIState.Loading) }
        verify { feedbackSubmissionStateObserver.onChanged(UIState.Success(true)) }
        coVerify { identificationRepository.submitFeedback(resultId, rating, comment, isAccurate) }
    }

    @Test
    fun `submitFeedback handles error`() = runTest {
        // Given
        val resultId = "test_result_id"
        val rating = 4
        val comment = "Very helpful diagnosis"
        val isAccurate = true
        val errorMessage = "Network error"
        val exception = IOException(errorMessage)

        // Setup repository response
        coEvery {
            identificationRepository.submitFeedback(resultId, rating, comment, isAccurate)
        } throws exception

        // When
        resultViewModel.submitFeedback(resultId, rating, comment, isAccurate)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { feedbackSubmissionStateObserver.onChanged(UIState.Loading) }
        verify { feedbackSubmissionStateObserver.onChanged(match { it is UIState.Error && it.message.contains(errorMessage) }) }
    }

    @Test
    fun `getRecommendedActions returns filtered actions based on severity`() {
        // Given
        val mockCrop = Crop(
            id = "rice",
            name = "Rice",
            scientificName = "Oryza sativa",
            description = "Rice crop",
            iconResourceName = "ic_rice",
            growthRegions = listOf("Andhra Pradesh", "Tamil Nadu"),
            growingSeasons = listOf("Kharif", "Rabi"),
            soilTypes = listOf("Alluvial", "Clayey")
        )

        val actions = listOf(
            Action(type = Action.Type.SPRAY, description = "Urgent spray", severity = Action.Severity.HIGH),
            Action(type = Action.Type.MONITOR, description = "Regular monitoring", severity = Action.Severity.MEDIUM),
            Action(type = Action.Type.WATER, description = "Optional watering", severity = Action.Severity.LOW)
        )

        val result = IdentificationResult(
            id = "test_id",
            cropId = "rice",
            crop = mockCrop,
            diseaseName = "Blast",
            diseaseType = IdentificationResult.DiseaseType.FUNGAL,
            description = "Rice blast is a fungal disease",
            severity = IdentificationResult.Severity.HIGH,
            imageUrl = "https://example.com/image.jpg",
            actions = actions,
            timestamp = System.currentTimeMillis(),
            confidence = 0.95f
        )

        // Setup
        resultViewModel.resultState.value = UIState.Success(result)

        // When - High Severity
        val highPriorityActions = resultViewModel.getRecommendedActions(Action.Severity.HIGH)

        // Then
        assertEquals(1, highPriorityActions.size)
        assertEquals("Urgent spray", highPriorityActions[0].description)

        // When - Medium Severity
        val mediumPriorityActions = resultViewModel.getRecommendedActions(Action.Severity.MEDIUM)

        // Then
        assertEquals(1, mediumPriorityActions.size)
        assertEquals("Regular monitoring", mediumPriorityActions[0].description)

        // When - Low Severity
        val lowPriorityActions = resultViewModel.getRecommendedActions(Action.Severity.LOW)

        // Then
        assertEquals(1, lowPriorityActions.size)
        assertEquals("Optional watering", lowPriorityActions[0].description)

        // When - All
        val allActions = resultViewModel.getAllRecommendedActions()

        // Then
        assertEquals(3, allActions.size)
    }
}