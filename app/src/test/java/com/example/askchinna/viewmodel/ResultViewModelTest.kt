/**
 * File: app/src/test/java/com/example/askchinna/viewmodel/ResultViewModelTest.kt
 * Copyright (c) 2025 askChinna
 * Created: April 29, 2025
 * Updated: May 16, 2025
 * Version: 1.9
 *
 * Change Log:
 * 1.9 - May 16, 2025
 * - Fixed type inference errors by adding explicit type parameters to any() calls
 * - Added explicit String type to File(any()) and Uri.fromFile(any()) calls
 * 1.8 - May 16, 2025
 * - Fixed "Failed matching mocking signature" error in refreshIdentification test
 * - Ensured proper initialization of resultViewModel before tearDown
 * - Added nullability check for resultViewModel in tearDown
 * 1.7 - May 15, 2025
 * - Removed references to non-existent getUserId method
 * - Kept Flow<Unit> for incrementUsageCount return type
 * 1.6 - May 15, 2025
 * - Fixed Flow<T> vs Flow<Unit> type mismatches
 * - Fixed correct return type for incrementUsageCount() to Flow<Unit>
 * - Properly mocked getUserId method
 * - Ensured all Action objects have required parameters
 */

package com.example.askchinna.viewmodel

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.askchinna.data.model.Action
import com.example.askchinna.data.model.ActionCategory
import com.example.askchinna.data.model.ActionStatus
import com.example.askchinna.data.model.Crop
import com.example.askchinna.data.model.IdentificationResult
import com.example.askchinna.data.model.UIState
import com.example.askchinna.data.repository.CropRepository
import com.example.askchinna.data.repository.IdentificationRepository
import com.example.askchinna.data.repository.UserRepository
import com.example.askchinna.ui.results.FeedbackType
import com.example.askchinna.ui.results.ResultViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.IOException
import java.util.Date
import java.util.UUID

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

    // Mock file and URI
    private val mockFile = mockk<File>(relaxed = true)
    private val mockUri = mockk<Uri>(relaxed = true)
    private val mockCrop = mockk<Crop>(relaxed = true)

    // Flag to track if resultViewModel was initialized
    private var viewModelInitialized = false

    @Before
    fun setup() {
        // Set main dispatcher for coroutines
        Dispatchers.setMain(testDispatcher)

        // Create mocks
        identificationRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        cropRepository = mockk(relaxed = true)
        uiStateObserver = mockk(relaxed = true)

        // Set up mock file
        every { mockFile.exists() } returns true
        // Fixed type inference error by adding explicit type parameter to any()
        every { File(any<String>()) } returns mockFile
        every { Uri.fromFile(any<File>()) } returns mockUri

        // Set up mock crop
        every { mockCrop.id } returns "rice"
        every { mockCrop.name } returns "Rice"

        // Setup incrementUsageCount to return Flow<Unit>
        coEvery { userRepository.incrementUsageCount() } returns flowOf(Unit)

        // Create view model
        resultViewModel = ResultViewModel(
            identificationRepository = identificationRepository,
            userRepository = userRepository,
            cropRepository = cropRepository,
            ioDispatcher = testDispatcher
        )

        // Observe LiveData
        resultViewModel.uiState.observeForever(uiStateObserver)

        // Mark that the view model is initialized
        viewModelInitialized = true
    }

    @After
    fun tearDown() {
        // Only remove observer and clean up if the view model was initialized
        if (viewModelInitialized) {
            // Clean up observers
            resultViewModel.uiState.removeObserver(uiStateObserver)
        }

        // Reset main dispatcher
        Dispatchers.resetMain()
    }

    @Test
    fun `startIdentification fetches cached result successfully`() = runTest {
        // Given
        val imagePath = "/path/to/image.jpg"
        val cropId = "rice"

        // Create actions with all required parameters
        val mockActions = listOf(
            Action(
                id = UUID.randomUUID().toString(),
                title = "Apply fungicide",
                description = "Apply fungicide to affected areas",
                priority = 3,
                category = ActionCategory.PEST_CONTROL,
                status = ActionStatus.PENDING
            )
        )

        val mockResult = mockk<IdentificationResult>(relaxed = true)
        every { mockResult.id } returns "test_id"
        every { mockResult.cropId } returns cropId
        every { mockResult.cropName } returns "Rice"
        every { mockResult.actions } returns mockActions

        // Setup repository response
        coEvery {
            identificationRepository.getIdentificationById(imagePath)
        } returns mockResult

        // When
        resultViewModel.startIdentification(imagePath, cropId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { uiStateObserver.onChanged(match { it is UIState.Loading }) }
        verify { uiStateObserver.onChanged(match {
            it is UIState.Success && it.data.id == mockResult.id
        }) }
        coVerify { identificationRepository.getIdentificationById(imagePath) }
    }

    @Test
    fun `startIdentification performs identification when no cached result`() = runTest {
        // Given
        val imagePath = "/path/to/image.jpg"
        val cropId = "rice"

        // Create actions with all required parameters
        val mockActions = listOf(
            Action(
                id = UUID.randomUUID().toString(),
                title = "Apply fungicide",
                description = "Apply fungicide to affected areas",
                priority = 3,
                category = ActionCategory.PEST_CONTROL,
                status = ActionStatus.PENDING
            )
        )

        val mockResult = mockk<IdentificationResult>(relaxed = true)
        every { mockResult.id } returns "test_id"
        every { mockResult.cropId } returns cropId
        every { mockResult.cropName } returns "Rice"
        every { mockResult.actions } returns mockActions

        // Setup repository responses
        coEvery { identificationRepository.getIdentificationById(imagePath) } returns null
        coEvery { cropRepository.getCropById(cropId) } returns mockCrop
        coEvery { userRepository.incrementUsageCount() } returns flowOf(Unit)

        coEvery {
            identificationRepository.identifyPestDisease(mockCrop, mockUri)
        } returns mockResult

        // When
        resultViewModel.startIdentification(imagePath, cropId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { uiStateObserver.onChanged(match { it is UIState.Loading }) }
        verify { uiStateObserver.onChanged(match {
            it is UIState.Success && it.data.id == mockResult.id
        }) }
        coVerify { identificationRepository.identifyPestDisease(mockCrop, mockUri) }
    }

    @Test
    fun `startIdentification handles error properly`() = runTest {
        // Given
        val imagePath = "/path/to/image.jpg"
        val cropId = "rice"
        val errorMessage = "Network error"
        val exception = IOException(errorMessage)

        // Setup repository response
        coEvery { identificationRepository.getIdentificationById(imagePath) } returns null
        coEvery { cropRepository.getCropById(cropId) } returns mockCrop
        coEvery { userRepository.incrementUsageCount() } returns flowOf(Unit)

        coEvery {
            identificationRepository.identifyPestDisease(mockCrop, mockUri)
        } throws exception

        // When
        resultViewModel.startIdentification(imagePath, cropId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { uiStateObserver.onChanged(match { it is UIState.Loading }) }
        verify { uiStateObserver.onChanged(match { it is UIState.Error }) }
        coVerify { identificationRepository.identifyPestDisease(mockCrop, mockUri) }
    }

    @Test
    fun `submitFeedback sends feedback successfully`() = runTest {
        // Given
        val resultId = "test_result_id"
        val feedbackType = FeedbackType.HELPFUL

        // Create a complete identification result to set internal state
        val mockResult = IdentificationResult(
            id = resultId,
            cropId = "rice",
            cropName = "Rice",
            imageUrl = "https://example.com/image.jpg",
            imagePath = "/path/to/image.jpg",
            problemName = "Test Problem",
            description = "Test Description",
            severity = 2,
            confidence = 75.0f,
            actions = emptyList(),
            timestamp = Date(),
            userId = "user_123"
        )

        // Setup repository response for getIdentificationById to set internal state
        coEvery { identificationRepository.getIdentificationById(any()) } returns mockResult

        // Setup repository response for feedback
        coEvery {
            identificationRepository.updateResultWithFeedback(
                resultId = resultId,
                rating = 5,
                comment = "helpful",
                isAccurate = true
            )
        } returns Unit

        // Set up the internal state using public method
        resultViewModel.startIdentification(mockResult.imagePath, mockResult.cropId)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        resultViewModel.submitFeedback(feedbackType)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify {
            identificationRepository.updateResultWithFeedback(
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

        // Create a complete identification result to set internal state
        val mockResult = IdentificationResult(
            id = "test_id",
            cropId = cropId,
            cropName = "Rice",
            imageUrl = "https://example.com/image.jpg",
            imagePath = imagePath,
            problemName = "Test Problem",
            description = "Test Description",
            severity = 2,
            confidence = 75.0f,
            actions = emptyList(),
            timestamp = Date(),
            userId = "user_123"
        )

        // Setup repository response for getIdentificationById - use specific parameters
        coEvery {
            identificationRepository.getIdentificationById(eq(imagePath))
        } returns mockResult

        // Set up the internal state using public method
        resultViewModel.startIdentification(imagePath, cropId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Clear mock verification counts
        io.mockk.clearMocks(uiStateObserver)

        // When
        resultViewModel.refreshIdentification()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { uiStateObserver.onChanged(match { it is UIState.Loading }) }
        verify { uiStateObserver.onChanged(match { it is UIState.Success }) }
        coVerify(exactly = 2) { identificationRepository.getIdentificationById(imagePath) }
    }
}