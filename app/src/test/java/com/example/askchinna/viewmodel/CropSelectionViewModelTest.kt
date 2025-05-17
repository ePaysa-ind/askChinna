/**
 * File: app/src/test/java/com/example/askchinna/viewmodel/CropSelectionViewModelTest.kt
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Updated: May 14, 2025
 * Version: 1.2
 *
 * Change Log:
 * 1.2 - May 14, 2025
 * - Added missing networkStateMonitor parameter to constructor
 * - Fixed references to getUsageLimitText
 * - Updated test structure and error handling
 * - Improved documentation
 */

package com.example.askchinna.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.askchinna.data.model.Crop
import com.example.askchinna.data.model.UIState
import com.example.askchinna.data.model.UsageLimit
import com.example.askchinna.data.repository.CropRepository
import com.example.askchinna.data.repository.UserRepository
import com.example.askchinna.ui.cropselection.CropSelectionViewModel
import com.example.askchinna.util.NetworkExceptionHandler
import com.example.askchinna.util.NetworkState
import com.example.askchinna.util.NetworkStateMonitor
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

/**
 * Unit tests for CropSelectionViewModel
 * Tests crop loading, network state handling, and usage limit functionality
 */
@ExperimentalCoroutinesApi
class CropSelectionViewModelTest {
    // ExecutorRule to make LiveData work synchronously in tests
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test dispatcher for controlled coroutine execution
    private val testDispatcher = StandardTestDispatcher()

    // Mocks
    private lateinit var cropRepository: CropRepository
    private lateinit var userRepository: UserRepository
    private lateinit var networkExceptionHandler: NetworkExceptionHandler
    private lateinit var networkStateMonitor: NetworkStateMonitor
    private lateinit var cropSelectionViewModel: CropSelectionViewModel
    private lateinit var uiStateObserver: Observer<UIState<List<Crop>>>
    private lateinit var usageLimitObserver: Observer<Int>

    // Test data
    private lateinit var testCrops: List<Crop>

    @Before
    fun setUp() {
        // Set main dispatcher for testing
        Dispatchers.setMain(testDispatcher)

        // Create mocks with relaxed behavior
        cropRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        networkExceptionHandler = mockk(relaxed = true)
        networkStateMonitor = mockk(relaxed = true)
        uiStateObserver = mockk(relaxed = true)
        usageLimitObserver = mockk(relaxed = true)

        // Setup network state monitor
        val networkStateLiveData = MutableLiveData<NetworkState>()
        networkStateLiveData.value = NetworkState.WiFi
        every { networkStateMonitor.networkState } returns networkStateLiveData

        // Setup test data
        testCrops = listOf(
            Crop(id = "rice", name = "Rice", iconResId = 0),
            Crop(id = "wheat", name = "Wheat", iconResId = 0),
            Crop(id = "maize", name = "Maize", iconResId = 0)
        )

        // Setup error handler
        every { networkExceptionHandler.handle(any()) } returns "Error occurred"

        // Set up repository responses
        coEvery { cropRepository.getSupportedCrops() } returns testCrops
        coEvery {
            userRepository.checkAndUpdateUsageLimit()
        } returns flowOf(UIState.Success(UsageLimit(usageCount = 2, lastUpdated = Date())))

        // Create the view model under test
        cropSelectionViewModel = CropSelectionViewModel(
            cropRepository = cropRepository,
            userRepository = userRepository,
            networkExceptionHandler = networkExceptionHandler,
            networkStateMonitor = networkStateMonitor
        )

        // Observe LiveData
        cropSelectionViewModel.uiState.observeForever(uiStateObserver)
        cropSelectionViewModel.usageLimit.observeForever(usageLimitObserver)

        // Advance dispatcher to complete initialization
        runTest {
            testDispatcher.scheduler.advanceUntilIdle()
        }
    }

    @After
    fun tearDown() {
        // Remove observers to prevent memory leaks
        cropSelectionViewModel.uiState.removeObserver(uiStateObserver)
        cropSelectionViewModel.usageLimit.removeObserver(usageLimitObserver)

        // Reset main dispatcher
        Dispatchers.resetMain()
    }

    /**
     * Test that loadCrops loads crops successfully and updates UI state
     */
    @Test
    fun `loadCrops loads crops successfully and updates UI state`() = runTest {
        // Given
        coEvery { cropRepository.getSupportedCrops() } returns testCrops

        // When
        cropSelectionViewModel.loadCrops()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { uiStateObserver.onChanged(match { it is UIState.Loading }) }
        verify { uiStateObserver.onChanged(match {
            it is UIState.Success && it.data == testCrops
        }) }
        coVerify { cropRepository.getSupportedCrops() }
    }

    /**
     * Test that loadCrops handles empty crop list
     */
    @Test
    fun `loadCrops handles empty crop list`() = runTest {
        // Given
        coEvery { cropRepository.getSupportedCrops() } returns emptyList()

        // When
        cropSelectionViewModel.loadCrops()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { uiStateObserver.onChanged(match { it is UIState.Loading }) }
        verify { uiStateObserver.onChanged(match { it is UIState.Error }) }
        coVerify { cropRepository.getSupportedCrops() }
    }

    /**
     * Test that loadCrops handles error
     */
    @Test
    fun `loadCrops handles error`() = runTest {
        // Given
        val exception = RuntimeException("Test exception")
        coEvery { cropRepository.getSupportedCrops() } throws exception
        every { networkExceptionHandler.handle(exception) } returns "Network error"

        // When
        cropSelectionViewModel.loadCrops()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { uiStateObserver.onChanged(match { it is UIState.Loading }) }
        verify { uiStateObserver.onChanged(match {
            it is UIState.Error && it.message == "Network error"
        }) }
        coVerify { cropRepository.getSupportedCrops() }
        verify { networkExceptionHandler.handle(exception) }
    }

    /**
     * Test that network state changes trigger crop loading
     */
    @Test
    fun `network state changes trigger crop loading`() = runTest {
        // Given
        val networkStateLiveData = MutableLiveData<NetworkState>()
        every { networkStateMonitor.networkState } returns networkStateLiveData

        // Initial setup
        cropSelectionViewModel = CropSelectionViewModel(
            cropRepository = cropRepository,
            userRepository = userRepository,
            networkExceptionHandler = networkExceptionHandler,
            networkStateMonitor = networkStateMonitor
        )
        cropSelectionViewModel.uiState.observeForever(uiStateObserver)

        // Advance to complete initialization
        testDispatcher.scheduler.advanceUntilIdle()

        // Reset verification counts
        io.mockk.clearMocks(cropRepository)

        // When - simulate network state change
        networkStateLiveData.value = NetworkState.Offline
        testDispatcher.scheduler.advanceUntilIdle()

        networkStateLiveData.value = NetworkState.WiFi
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(atLeast = 1) { cropRepository.getSupportedCrops() }
    }

    /**
     * Test that setSelectedCrop correctly sets crop
     */
    @Test
    fun `setSelectedCrop correctly sets crop`() = runTest {
        // Given
        val testCrop = Crop(id = "rice", name = "Rice", iconResId = 0)

        // When
        cropSelectionViewModel.setSelectedCrop(testCrop)

        // Then
        val selectedCrop = cropSelectionViewModel.getSelectedCrop()
        assertNotNull(selectedCrop)
        assertEquals(testCrop, selectedCrop)
    }

    /**
     * Test that hasRemainingUses returns true when under limit
     */
    @Test
    fun `hasRemainingUses returns true when under limit`() = runTest {
        // Given - setup in setUp method with usageCount = 2

        // When
        val hasRemaining = cropSelectionViewModel.hasRemainingUses()

        // Then
        assertTrue(hasRemaining)
    }

    /**
     * Test that hasRemainingUses returns false when at limit
     */
    @Test
    fun `hasRemainingUses returns false when at limit`() = runTest {
        // Given
        // Create a new view model with max usage
        val usageLimitMax = UsageLimit(usageCount = 5, lastUpdated = Date())
        coEvery { userRepository.checkAndUpdateUsageLimit() } returns flowOf(UIState.Success(usageLimitMax))

        // Create new view model with this setup
        val viewModel = CropSelectionViewModel(
            cropRepository = cropRepository,
            userRepository = userRepository,
            networkExceptionHandler = networkExceptionHandler,
            networkStateMonitor = networkStateMonitor
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val hasRemaining = viewModel.hasRemainingUses()

        // Then
        assertFalse(hasRemaining)
    }
}