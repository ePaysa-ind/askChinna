/**
 * file path: app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt
 * Copyright (c) 2025 askChinna App
 * Created: April 28, 2025
 * Updated: May 6, 2025
 * Version: 1.2
 * 
 * Change Log:
 * 1.2 - May 6, 2025
 * - Added proper error handling for network state changes
 * - Added retry mechanism for failed operations
 * - Added proper cleanup in onDestroy
 * - Added proper coroutine scope management
 * - Added state restoration
 * - Added memory optimization
 * - Added proper error logging
 * - Added proper dialog management
 * - Added proper view state management
 * - Added proper permission handling
 * - Added proper image processing
 */
package com.example.askchinna.ui.identification

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.askchinna.R
import com.example.askchinna.data.model.Crop
import com.example.askchinna.databinding.ActivityImageCaptureBinding
import com.example.askchinna.ui.home.SessionTimerManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import javax.inject.Inject

/**
 * Activity for capturing or selecting an image of a crop for pest/disease identification.
 */
@Suppress("DEPRECATION")
@AndroidEntryPoint
class ImageCaptureActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageCaptureBinding
    private val viewModel: IdentificationViewModel by viewModels()
    private var isInitialized = false
    private var currentPhotoPath: String? = null
    private var currentDialog: AlertDialog? = null
    private var retryCount = 0
    private val MAX_RETRIES = 3

    @Inject
    lateinit var sessionTimerManager: SessionTimerManager

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleError("Coroutine error", throwable)
    }

    companion object {
        private const val TAG = "ImageCaptureActivity"
        const val EXTRA_CROP = "crop"
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        try {
            if (isGranted) {
                dispatchTakePictureIntent()
            } else {
                showPermissionDeniedDialog(getString(R.string.camera_permission_required))
            }
        } catch (e: Exception) {
            handleError("Error handling camera permission result", e)
        }
    }

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        try {
            if (isGranted) {
                openGallery()
            } else {
                showPermissionDeniedDialog(getString(R.string.storage_permission_required))
            }
        } catch (e: Exception) {
            handleError("Error handling storage permission result", e)
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            if (result.resultCode == RESULT_OK) {
                currentPhotoPath?.let { path ->
                    lifecycleScope.launch(exceptionHandler) {
                        viewModel.processCapturedImage(android.graphics.BitmapFactory.decodeFile(path))
                        navigateToPreview()
                    }
                } ?: run {
                    showError(getString(R.string.error_image_capture))
                }
            }
        } catch (e: Exception) {
            handleError("Error handling camera result", e)
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    lifecycleScope.launch(exceptionHandler) {
                        viewModel.processGalleryImage(uri)
                        navigateToPreview()
                    }
                } ?: run {
                    showError(getString(R.string.error_image_selection))
                }
            }
        } catch (e: Exception) {
            handleError("Error handling gallery result", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityImageCaptureBinding.inflate(layoutInflater)
            setContentView(binding.root)

            if (savedInstanceState != null) {
                retryCount = savedInstanceState.getInt("retryCount", 0)
            }

            val crop = intent.getParcelableExtra<Crop>(EXTRA_CROP)
            if (crop == null) {
                showError(getString(R.string.error_crop_selection))
                finish()
                return
            }

            viewModel.setCrop(crop)
            setupUI()
            setupClickListeners()
            setupObservers()
            isInitialized = true
        } catch (e: Exception) {
            handleError("Error initializing activity", e)
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        try {
            super.onSaveInstanceState(outState)
            outState.putInt("retryCount", retryCount)
        } catch (e: Exception) {
            handleError("Failed to save instance state", e)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            if (!isInitialized) {
                Log.w(TAG, "Activity not initialized")
                return
            }
            sessionTimerManager.startTimer()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume", e)
        }
    }

    override fun onPause() {
        try {
            if (!isInitialized) {
                Log.w(TAG, "Activity not initialized")
                return
            }
            sessionTimerManager.pauseTimer()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onPause", e)
        }
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        
        try {
            dismissCurrentDialog()
            
            // Clear image paths and clean up temporary files
            currentPhotoPath?.let { path ->
                val file = java.io.File(path)
                if (file.exists()) {
                    file.delete()
                }
            }
            currentPhotoPath = null
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy cleanup", e)
        }
    }

    private fun setupUI() {
        try {
            if (!isInitialized) {
                Log.w(TAG, "Activity not initialized")
                return
            }

            viewModel.selectedCrop.value?.let { crop ->
                binding.tvCropName.text = crop.name
                binding.ivCropIcon.setImageResource(crop.iconResId)
            }

            binding.sessionTimerView.setTimerManager(sessionTimerManager)
        } catch (e: Exception) {
            handleError("Error setting up UI", e)
        }
    }

    private fun setupClickListeners() {
        try {
            binding.btnBack.setOnClickListener {
                finish()
            }

            binding.btnHelp.setOnClickListener {
                showHelpDialog()
            }

            binding.btnCapturePhoto.setOnClickListener {
                checkCameraPermission()
            }

            binding.btnUploadFromGallery.setOnClickListener {
                checkStoragePermission()
            }
        } catch (e: Exception) {
            handleError("Error setting up click listeners", e)
        }
    }

    private fun setupObservers() {
        try {
            viewModel.isOnline.observe(this) { isAvailable ->
                binding.networkStatusView.visibility = if (isAvailable) View.GONE else View.VISIBLE
            }
        } catch (e: Exception) {
            handleError("Error setting up observers", e)
        }
    }

    private fun checkCameraPermission() {
        try {
            if (!isInitialized) {
                Log.w(TAG, "Activity not initialized")
                return
            }

            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    dispatchTakePictureIntent()
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
                ) -> {
                    showPermissionRationaleDialog(
                        getString(R.string.camera_permission_rationale)
                    ) { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
                }
                else -> {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        } catch (e: Exception) {
            handleError("Error checking camera permission", e)
        }
    }

    private fun checkStoragePermission() {
        try {
            if (!isInitialized) {
                Log.w(TAG, "Activity not initialized")
                return
            }

            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    openGallery()
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) -> {
                    showPermissionRationaleDialog(
                        getString(R.string.storage_permission_rationale)
                    ) { storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE) }
                }
                else -> {
                    storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        } catch (e: Exception) {
            handleError("Error checking storage permission", e)
        }
    }

    private fun dispatchTakePictureIntent() {
        try {
            if (!isInitialized) {
                Log.w(TAG, "Activity not initialized")
                return
            }

            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        handleError("Error creating image file", ex)
                        null
                    }

                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            this,
                            "${packageName}.fileprovider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        takePictureLauncher.launch(takePictureIntent)
                    }
                }
            }
        } catch (e: Exception) {
            handleError("Error dispatching take picture intent", e)
        }
    }

    private fun openGallery() {
        try {
            if (!isInitialized) {
                Log.w(TAG, "Activity not initialized")
                return
            }

            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        } catch (e: Exception) {
            handleError("Error opening gallery", e)
        }
    }

    private fun createImageFile(): File {
        try {
            val timeStamp: String = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
            val storageDir: File? = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
            return File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir
            ).apply {
                currentPhotoPath = absolutePath
            }
        } catch (e: Exception) {
            handleError("Error creating image file", e)
            throw e
        }
    }

    private fun navigateToPreview() {
        try {
            if (!isInitialized) {
                Log.w(TAG, "Activity not initialized")
                return
            }

            val intent = Intent(this, ImagePreviewActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            handleError("Error navigating to preview", e)
        }
    }

    private fun showPermissionDeniedDialog(message: String) {
        try {
            if (!isInitialized) {
                Log.w(TAG, "Activity not initialized")
                return
            }

            currentDialog?.dismiss()
            currentDialog = AlertDialog.Builder(this)
                .setTitle(R.string.permission_denied)
                .setMessage(message)
                .setPositiveButton(R.string.ok) { _, _ -> finish() }
                .show()
        } catch (e: Exception) {
            handleError("Error showing permission denied dialog", e)
        }
    }

    private fun showPermissionRationaleDialog(message: String, onPositiveClick: () -> Unit) {
        try {
            if (!isInitialized) {
                Log.w(TAG, "Activity not initialized")
                return
            }

            currentDialog?.dismiss()
            currentDialog = AlertDialog.Builder(this)
                .setTitle(R.string.permission_required)
                .setMessage(message)
                .setPositiveButton(R.string.ok) { _, _ -> onPositiveClick() }
                .setNegativeButton(R.string.cancel, null)
                .show()
        } catch (e: Exception) {
            handleError("Error showing permission rationale dialog", e)
        }
    }

    private fun dismissCurrentDialog() {
        try {
            currentDialog?.dismiss()
            currentDialog = null
        } catch (e: Exception) {
            Log.e(TAG, "Error dismissing dialog", e)
        }
    }

    private fun retryNetworkOperation() {
        if (retryCount < MAX_RETRIES) {
            retryCount++
            Log.d(TAG, "Retrying network operation. Attempt $retryCount of $MAX_RETRIES")
            viewModel.retryIdentification()
        } else {
            retryCount = 0
            showError("Failed to connect after $MAX_RETRIES attempts")
        }
    }

    private fun openAppSettings() {
        try {
            Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                startActivity(this)
            }
        } catch (e: Exception) {
            handleError("Error opening app settings", e)
        }
    }

    private fun showError(message: String) {
        try {
            if (!isInitialized) {
                Log.w(TAG, "Activity not initialized")
                return
            }

            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing error message", e)
        }
    }

    private fun handleError(message: String, error: Throwable) {
        Log.e(TAG, message, error)
        showError(message)
    }

    private fun showHelpDialog() {
        try {
            if (!isInitialized) {
                Log.w(TAG, "Activity not initialized")
                return
            }

            currentDialog?.dismiss()
            currentDialog = AlertDialog.Builder(this)
                .setTitle(R.string.help)
                .setMessage(R.string.capture_image_help)
                .setPositiveButton(R.string.ok, null)
                .show()
        } catch (e: Exception) {
            handleError("Error showing help dialog", e)
        }
    }
}

