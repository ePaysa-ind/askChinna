/*
 * Copyright (c) 2025 askChinna
 * File: app/src/test/java/com/example/askchinna/viewmodel/ResultViewModelTest.kt
 * Created: April 29, 2025
 * Updated: May 2, 2025
 * Version: 1.2.1
 *
 * Unit tests for ResultViewModel, using MockK and StandardTestDispatcher
 */

package com.example.askchinna.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.askchinna.data.model.Action
import com.example.askchinna.data.model.IdentificationResult
import com.example.askchinna.data.model.UIState
import com.example.askchinna.data.repository.CropRepository
import com.example.askchinna.data.repository.IdentificationRepository
import com.example.askchinna.data.repository.UserRepository
import com.example.askchinna.ui.results.FeedbackType
import com.example.askchinna.ui.results.ResultViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.util.Date

/**
 * Unit tests for ResultViewModel that verify the proper functioning of
 * result display, actions handling, and PDF export features.
 */
@ExperimentalCoroutinesApi
class ResultViewModelTest {
    // Executes each task synchronously using Architecture Components
    @get:Rule
    val rule = InstantTaskExecutorRule()

    // Test dispatcher for coroutines
    private val testDispatcher = StandardTestDispatcher()

    // Mocks
    private lateinit var identificationRepository: IdentificationRepository
    private lateinit var userRepository: UserRepository
    private lateinit var cropRepository: CropRepository
    private lateinit var resultViewModel: ResultViewModel
    private lateinit var uiStateObserver: Observer<UIState<IdentificationResult>>

    @Before
    fun setup() {
        // Set main dispatcher for coroutines
        Dispatchers.setMain(testDispatcher)

        // Create mocks
        identificationRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        cropRepository = mockk(relaxed = true)
        uiStateObserver = mockk(relaxed = true)

        // Create view model
        resultViewModel = ResultViewModel(
            identificationRepository = identificationRepository,
            userRepository = userRepository,
            cropRepository = cropRepository,
            ioDispatcher = testDispatcher
        )

        // Observe LiveData
        resultViewModel.uiState.observeForever(uiStateObserver)
    }

    @After
    fun tearDown() {
        // Clean up observers
        resultViewModel.uiState.removeObserver(uiStateObserver)

        // Reset main dispatcher
        Dispatchers.resetMain()
    }

    @Test
    fun `startIdentification fetches cached result successfully`() = runTest {
        // Given
        val imagePath = "/path/to/image.jpg"
        val cropId = "rice"
        val cropName = "Rice"

        val mockActions = listOf(
            Action(
                actionType = Action.Companion.ActionType.SPRAY,
                description = "Apply fungicide",
                priority = 3
            )
        )

        val mockResult = IdentificationResult(
            id = "test_id",
            cropId = cropId,
            cropName = cropName,
            imageUrl = "https://example.com/image.jpg",
            problemName = "Blast",
            description = "Rice blast is a fungal disease",
            problemType = "fungal",
            severity = 3,
            actions = mockActions,
            timestamp = Date(),
            confidence = 95f,
            userId = "user_123",
            imagePath = imagePath
        )

        // Setup repository response
        coEvery { identificationRepository.getCachedResult(imagePath, cropId) } returns mockResult

        // When
        resultViewModel.startIdentification(imagePath, cropId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { uiStateObserver.onChanged(match { it is UIState.Loading }) }
        verify { uiStateObserver.onChanged(match {
            it is UIState.Success && it.data.id == mockResult.id
        }) }
        coVerify { identificationRepository.getCachedResult(imagePath, cropId) }
    }

    @Test
    fun `startIdentification performs identification when no cached result`() = runTest {
        // Given
        val imagePath = "/path/to/image.jpg"
        val cropId = "rice"
        val cropName = "Rice"

        val mockActions = listOf(
            Action(
                actionType = Action.Companion.ActionType.SPRAY,
                description = "Apply fungicide",
                priority = 3
            )
        )

        val mockResult = IdentificationResult(
            id = "test_id",
            cropId = cropId,
            cropName = cropName,
            imageUrl = "https://example.com/image.jpg",
            problemName = "Blast",
            description = "Rice blast is a fungal disease",
            problemType = "fungal",
            severity = 3,
            actions = mockActions,
            timestamp = Date(),
            confidence = 95f,
            userId = "user_123",
            imagePath = imagePath
        )

        // Setup repository responses
        coEvery { identificationRepository.getCachedResult(imagePath, cropId) } returns null
        coEvery { identificationRepository.getCropNameById(cropId) } returns cropName
        coEvery { identificationRepository.identifyIssue(imagePath, cropId, cropName) } returns mockResult

        // When
        resultViewModel.startIdentification(imagePath, cropId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { uiStateObserver.onChanged(match { it is UIState.Loading }) }
        verify { uiStateObserver.onChanged(match {
            it is UIState.Success && it.data.id == mockResult.id
        }) }
        coVerify { identificationRepository.identifyIssue(imagePath, cropId, cropName) }
        coVerify { identificationRepository.cacheIdentificationResult(mockResult) }
    }

    @Test
    fun `startIdentification handles error`() = runTest {
        // Given
        val imagePath = "/path/to/image.jpg"
        val cropId = "rice"
        val errorMessage = "Network error"
        val exception = IOException(errorMessage)

        // Setup repository response
        coEvery { identificationRepository.getCachedResult(imagePath, cropId) } returns null
        coEvery { identificationRepository.getCropNameById(cropId) } returns "Rice"
        coEvery { identificationRepository.identifyIssue(any(), any(), any()) } throws exception

        // When
        resultViewModel.startIdentification(imagePath, cropId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { uiStateObserver.onChanged(match { it is UIState.Loading }) }
        verify { uiStateObserver.onChanged(match {
            it is UIState.Error && it.message.contains(errorMessage)
        }) }
    }

    @Test
    fun `submitFeedback sends feedback successfully`() = runTest {
        // Given
        val resultId = "test_result_id"
        val feedbackType = FeedbackType.HELPFUL

        // Mock current result
        val mockResult = IdentificationResult(
            id = resultId,
            cropId = "rice",
            cropName = "Rice",
            problemName = "Blast",
            description = "Description",
            severity = 3,
            confidence = 95f,
            actions = emptyList(),
            timestamp = Date(),
            userId = "user_123",
            imageUrl = "https://example.com/image.jpg" // Added missing imageUrl
        )
        resultViewModel.currentResult = mockResult

        // Setup repository response - return boolean instead of Unit
        coEvery {
            identificationRepository.submitFeedback(
                resultId = resultId,
                rating = 5,
                comment = "helpful",
                isAccurate = true
            )
        } returns true // Changed from Unit to Boolean

        // When
        resultViewModel.submitFeedback(feedbackType)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify {
            identificationRepository.submitFeedback(
                resultId = resultId,
                rating = 5,
                comment = "helpful",
                isAccurate = true
            )
        }
    }

    @Test
    fun `refreshIdentification restarts identification with current result data`() = runTest {
        // Given
        val imagePath = "/path/to/image.jpg"
        val cropId = "rice"

        // Set current result
        val mockResult = IdentificationResult(
            id = "test_id",
            cropId = cropId,
            cropName = "Rice",
            problemName = "Blast",
            description = "Description",
            severity = 3,
            confidence = 95f,
            actions = emptyList(),
            timestamp = Date(),
            userId = "user_123",
            imagePath = imagePath,
            imageUrl = "https://example.com/image.jpg" // Added missing imageUrl
        )
        resultViewModel.currentResult = mockResult

        // Mock repository responses
        coEvery { identificationRepository.getCachedResult(imagePath, cropId) } returns null
        coEvery { identificationRepository.getCropNameById(cropId) } returns "Rice"
        coEvery {
            identificationRepository.identifyIssue(imagePath, cropId, "Rice")
        } returns mockResult

        // When
        resultViewModel.refreshIdentification()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { uiStateObserver.onChanged(match { it is UIState.Loading }) }
        coVerify { identificationRepository.getCachedResult(imagePath, cropId) }
    }
}