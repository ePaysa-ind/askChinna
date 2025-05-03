/**
 * File: app/src/test/java/com/example/askchinna/viewmodel/CropSelectionViewModelTest.kt
 * Copyright (c) 2025 askChinna
 * Created: April 28, 2025
 * Updated: April 30, 2025
 * Version: 1.1
 *
 * Updated to use StandardTestDispatcher for modern Kotlin testing
 */
package com.example.askchinna.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.askchinna.R
import com.example.askchinna.data.model.Crop
import com.example.askchinna.data.model.UIState
import com.example.askchinna.data.model.UsageLimit
import com.example.askchinna.data.repository.CropRepository
import com.example.askchinna.data.repository.UserRepository
import com.example.askchinna.ui.cropselection.CropSelectionViewModel
import com.example.askchinna.util.NetworkExceptionHandler
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@ExperimentalCoroutinesApi
class CropSelectionViewModelTest {

    // Run tasks synchronously
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    // Test dispatcher for coroutines
    private val testDispatcher = StandardTestDispatcher()

    // Mocks
    private lateinit var cropRepository: CropRepository
    private lateinit var userRepository: UserRepository
    private lateinit var networkExceptionHandler: NetworkExceptionHandler
    private lateinit var viewModel: CropSelectionViewModel

    // Test data
    private val testCrops = listOf(
        Crop("tomato", "Tomato", R.drawable.ic_tomato),
        Crop("rice", "Rice", R.drawable.ic_rice)
    )
    private val testUsageLimit = UsageLimit(remainingUses = 3, maxUses = 5)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Initialize mocks
        cropRepository = mockk()
        userRepository = mockk()
        networkExceptionHandler = mockk()

        // Default mock behavior
        coEvery { cropRepository.getSupportedCrops() } returns testCrops
        coEvery { userRepository.getUserUsageLimit() } returns testUsageLimit
        coEvery { networkExceptionHandler.getErrorMessage(any()) } returns "Error occurred"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when init called, loads crops and usage limits`() = runTest {
        // Initialize viewModel (triggers init)
        viewModel = CropSelectionViewModel(cropRepository, userRepository, networkExceptionHandler)

        // Wait for coroutines to complete
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert crops loaded successfully
        val cropState = viewModel.uiState.value
        assertTrue(cropState is UIState.Success)
        assertEquals(testCrops, (cropState as UIState.Success).data)

        // Assert usage limits loaded
        assertEquals(testUsageLimit, viewModel.usageLimit.value)
    }

    @Test
    fun `when crop loading fails, shows error state`() = runTest {
        // Setup repository to throw exception
        coEvery { cropRepository.getSupportedCrops() } throws IOException("Network error")

        // Initialize viewModel
        viewModel = CropSelectionViewModel(cropRepository, userRepository, networkExceptionHandler)

        // Wait for coroutines to complete
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert error state
        val state = viewModel.uiState.value
        assertTrue(state is UIState.Error)
        assertEquals("Error occurred", (state as UIState.Error).message)
    }

    @Test
    fun `when crops list is empty, shows error state`() = runTest {
        // Setup repository to return empty list
        coEvery { cropRepository.getSupportedCrops() } returns emptyList()

        // Initialize viewModel
        viewModel = CropSelectionViewModel(cropRepository, userRepository, networkExceptionHandler)

        // Wait for coroutines to complete
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert error state
        val state = viewModel.uiState.value
        assertTrue(state is UIState.Error)
        assertEquals("No crops available", (state as UIState.Error).message)
    }

    @Test
    fun `hasRemainingUses returns true when uses remain`() = runTest {
        // Setup with usage remaining
        coEvery { userRepository.getUserUsageLimit() } returns UsageLimit(remainingUses = 3, maxUses = 5)

        // Initialize viewModel
        viewModel = CropSelectionViewModel(cropRepository, userRepository, networkExceptionHandler)

        // Wait for coroutines to complete
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert has remaining uses
        assertTrue(viewModel.hasRemainingUses())
    }

    @Test
    fun `hasRemainingUses returns false when no uses remain`() = runTest {
        // Setup with no usage remaining
        coEvery { userRepository.getUserUsageLimit() } returns UsageLimit(remainingUses = 0, maxUses = 5)

        // Initialize viewModel
        viewModel = CropSelectionViewModel(cropRepository, userRepository, networkExceptionHandler)

        // Wait for coroutines to complete
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert no remaining uses
        assertTrue(!viewModel.hasRemainingUses())
    }

    @Test
    fun `getUsageLimitText formats correctly`() = runTest {
        // Setup
        coEvery { userRepository.getUserUsageLimit() } returns UsageLimit(remainingUses = 2, maxUses = 5)

        // Initialize viewModel
        viewModel = CropSelectionViewModel(cropRepository, userRepository, networkExceptionHandler)

        // Wait for coroutines to complete
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert correct formatting
        assertEquals("2/5", viewModel.getUsageLimitText())
    }
}