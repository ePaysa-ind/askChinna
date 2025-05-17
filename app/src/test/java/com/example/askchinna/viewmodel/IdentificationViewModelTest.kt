/**
 * File: app/src/test/java/com/example/askchinna/viewmodel/IdentificationViewModelTest.kt
 * Copyright (c) 2025 askChinna
 * Created: April 30, 2025
 * Updated: May 16, 2025
 * Version: 1.4
 *
 * Change Log:
 * 1.4 - May 16, 2025
 * - Added MockFirebaseRule to prevent Firebase initialization errors
 * - Fixed explicit type parameters for any() calls in mocks
 * - Improved test reliability and isolation
 * 1.3 - May 16, 2025
 * - Improved mock object setup to avoid reference issues
 * - Ensured proper mock behavior for NetworkStateMonitor
 * - Added specific slot capture and verification in cleanup test
 * 1.2 - May 15, 2025
 * - Added Robolectric configuration to disable Firebase initialization
 * - Fixed FileOutputStream NPE by using proper file mocking
 * 1.1 - May 15, 2025
 * - Fixed NetworkStateMonitor mock to use method call instead of property
 * - Fixed UIState.Loading constructor usage
 * - Updated test approach to match actual implementation
 */
package com.example.askchinna.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.askchinna.data.model.Crop
import com.example.askchinna.data.model.IdentificationResult
import com.example.askchinna.data.model.UIState
import com.example.askchinna.data.repository.IdentificationRepository
import com.example.askchinna.ui.identification.IdentificationViewModel
import com.example.askchinna.util.ImageHelper
import com.example.askchinna.util.MockFirebaseRule
import com.example.askchinna.util.NetworkStateMonitor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import org.junit.rules.TestRule

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P], manifest = Config.NONE)
class IdentificationViewModelTest {

    // Apply MockFirebaseRule first to ensure Firebase is mocked before tests run
    @get:Rule
    val mockFirebaseRule = MockFirebaseRule()

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    // Mocks
    private lateinit var identificationRepository: IdentificationRepository
    private lateinit var imageHelper: ImageHelper
    private lateinit var networkStateMonitor: NetworkStateMonitor
    private lateinit var context: Context
    private lateinit var viewModel: IdentificationViewModel

    // Test data
    private val mockBitmap = mockk<Bitmap>(relaxed = true)
    private val mockUri = mockk<Uri>(relaxed = true)
    private val mockImageFile = mockk<File>(relaxed = true)
    private val mockCrop = mockk<Crop>(relaxed = true)
    private val mockResult = mockk<IdentificationResult>(relaxed = true)
    private val mockInputStream = mockk<InputStream>(relaxed = true)
    private val mockFileOutputStream = mockk<FileOutputStream>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Initialize mocks
        identificationRepository = mockk(relaxed = true)
        imageHelper = mockk(relaxed = true)
        networkStateMonitor = mockk(relaxed = true)
        context = mockk(relaxed = true)

        // Set up network monitor
        every { networkStateMonitor.startMonitoring() } just runs
        every { networkStateMonitor.isNetworkAvailable() } returns true

        // Set up content resolver
        every { context.contentResolver.openInputStream(any<Uri>()) } returns mockInputStream

        // Set up image helper
        every { imageHelper.createTempImageFile() } returns mockImageFile

        // Set up mock bitmap
        every { mockBitmap.width } returns 1000
        every { mockBitmap.height } returns 800
        every { mockBitmap.compress(any(), any(), any<ByteArrayOutputStream>()) } answers {
            val outputStream = arg<ByteArrayOutputStream>(2)
            // Just write some dummy data to the stream
            outputStream.write(byteArrayOf(1, 2, 3, 4))
            true
        }

        // Set up mock crop
        every { mockCrop.id } returns "rice"
        every { mockCrop.name } returns "Rice"

        // Set up mock file
        every { mockImageFile.delete() } returns true
        every { mockImageFile.absolutePath } returns "/test/path/image.jpg"
        every { mockImageFile.exists() } returns true

        // Proper mocking for FileOutputStream
        every { mockFileOutputStream.write(any<ByteArray>()) } just runs
        every { mockFileOutputStream.flush() } just runs
        every { mockFileOutputStream.close() } just runs
        every { FileOutputStream(any<File>()) } returns mockFileOutputStream

        // Set up repository
        coEvery { identificationRepository.identifyPestDisease(any<Crop>(), any<Uri>()) } returns mockResult

        // Initialize viewModel after setting up mocks
        viewModel = IdentificationViewModel(
            identificationRepository,
            imageHelper,
            networkStateMonitor,
            context
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setCrop updates selected crop state`() = runTest {
        // When
        viewModel.setCrop(mockCrop)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(mockCrop, viewModel.selectedCrop.value)
    }

    @Test
    fun `processGalleryImage processes image successfully`() = runTest {
        // When
        viewModel.processGalleryImage(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { context.contentResolver.openInputStream(mockUri) }
    }

    @Test
    fun `processCapturedImage processes image successfully`() = runTest {
        // When
        viewModel.processCapturedImage(mockBitmap)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { mockBitmap.compress(any(), any(), any<ByteArrayOutputStream>()) }
        verify { imageHelper.createTempImageFile() }
    }

    @Test
    fun `analyzeImageQuality is called during image processing`() = runTest {
        // When
        viewModel.processCapturedImage(mockBitmap)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { mockBitmap.width }
        verify { mockBitmap.height }
    }

    @Test
    fun `submitForIdentification requires selected crop`() = runTest {
        // Given - no crop selected

        // When
        viewModel.submitForIdentification()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        // Without a crop selected, the method returns early, so no repository calls should happen
        coVerify(exactly = 0) { identificationRepository.identifyPestDisease(any<Crop>(), any<Uri>()) }
    }

    @Test
    fun `submitForIdentification checks network availability`() = runTest {
        // Given - explicitly set network availability to false
        every { networkStateMonitor.isNetworkAvailable() } returns false

        // Set up crop and image
        viewModel.setCrop(mockCrop)
        viewModel.processGalleryImage(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.submitForIdentification()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { networkStateMonitor.isNetworkAvailable() }
        assertTrue(viewModel.uiState.value is UIState.Error)
        val errorState = viewModel.uiState.value as UIState.Error
        assertTrue(errorState.message.contains("No internet"))
    }

    @Test
    fun `submitForIdentification uses identifyPestDisease when network available`() = runTest {
        // Given
        every { networkStateMonitor.isNetworkAvailable() } returns true

        viewModel.setCrop(mockCrop)
        viewModel.processGalleryImage(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.submitForIdentification()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { networkStateMonitor.isNetworkAvailable() }
        coVerify { identificationRepository.identifyPestDisease(mockCrop, any<Uri>()) }
    }

    @Test
    fun `submitForIdentification handles error during identification`() = runTest {
        // Given
        val errorMessage = "API error"
        coEvery { identificationRepository.identifyPestDisease(any<Crop>(), any<Uri>()) } throws IOException(errorMessage)

        viewModel.setCrop(mockCrop)
        viewModel.processGalleryImage(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.submitForIdentification()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is UIState.Error)
        val errorState = viewModel.uiState.value as UIState.Error
        assertTrue(errorState.message.contains(errorMessage))
    }

    @Test
    fun `retryIdentification resets to loading state`() = runTest {
        // When
        viewModel.retryIdentification()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Use UIState.Loading() without type arguments
        assertEquals(UIState.Loading(), viewModel.uiState.value)
    }

    @Test
    fun `retakeImage resets state`() = runTest {
        // Given - image processed
        viewModel.processCapturedImage(mockBitmap)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.retakeImage()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { mockImageFile.delete() }
        // The implementation creates an empty bitmap rather than setting to null
        assertNotNull(viewModel.capturedImage.value)
    }

    @Test
    fun `cleanup is performed when resources are released`() = runTest {
        // Given
        val fileSlot = slot<File>()

        // Create temporary file first to capture it in slot
        every { imageHelper.createTempImageFile() } answers {
            mockImageFile.also { fileSlot.captured = it }
        }

        // Process an image to set up temp file
        viewModel.processCapturedImage(mockBitmap)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify file was created
        verify { imageHelper.createTempImageFile() }

        // When - simulate cleanup
        viewModel.retakeImage()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { mockImageFile.delete() }
    }
}