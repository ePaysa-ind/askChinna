/**
 * Copyright (c) 2025 askChinna App
 * Created: April 28, 2025
 * Version: 1.0
 */
package com.example.askchinna.ui.identification

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.askchinna.R
import com.example.askchinna.data.model.Crop
import com.example.askchinna.databinding.ActivityImageCaptureBinding
import com.example.askchinna.ui.home.SessionTimerManager
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import javax.inject.Inject

/**
 * Activity for capturing or selecting an image of crop for pest/disease identification.
 * This screen allows users to take a photo using the camera or select from gallery.
 */
@AndroidEntryPoint
class ImageCaptureActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageCaptureBinding
    private val viewModel: IdentificationViewModel by viewModels()

    @Inject
    lateinit var sessionTimerManager: SessionTimerManager

    // Current photo path
    private var currentPhotoPath: String? = null
    private lateinit var selectedCrop: Crop

    // Activity result launcher for camera
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoPath?.let { path ->
                val bitmap = BitmapFactory.decodeFile(path)
                viewModel.processCapturedImage(bitmap)
                navigateToPreview()
            }
        }
    }

    // Activity result launcher for gallery
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.processGalleryImage(it)
            navigateToPreview()
        }
    }

    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            when (pendingAction) {
                PendingAction.TAKE_PHOTO -> dispatchTakePictureIntent()
                PendingAction.PICK_FROM_GALLERY -> pickImageFromGallery()
                else -> {}
            }
        } else {
            Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show()
        }
        pendingAction = PendingAction.NONE
    }

    // Pending action waiting for permission
    private var pendingAction = PendingAction.NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageCaptureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get selected crop from intent
        selectedCrop = intent.getParcelableExtra(EXTRA_CROP)
            ?: throw IllegalArgumentException("No crop provided to ImageCaptureActivity")

        // Set crop information
        viewModel.setCrop(selectedCrop)
        binding.tvCropName.text = selectedCrop.name
        binding.ivCropIcon.setImageResource(selectedCrop.iconResId)

        // Set up session timer
        binding.sessionTimerView.setTimerManager(sessionTimerManager)

        // Set up click listeners
        setupClickListeners()

        // Set up network status observer
        setupNetworkStatusObserver()
    }

    override fun onResume() {
        super.onResume()
        sessionTimerManager.resumeTimer()
    }

    override fun onPause() {
        super.onPause()
        sessionTimerManager.pauseTimer()
    }

    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Capture photo button
        binding.btnCapturePhoto.setOnClickListener {
            checkCameraPermissionAndDispatchIntent()
        }

        // Upload from gallery button
        binding.btnUploadFromGallery.setOnClickListener {
            checkStoragePermissionAndPickImage()
        }

        // Help button
        binding.btnHelp.setOnClickListener {
            showHelpDialog()
        }
    }

    private fun setupNetworkStatusObserver() {
        viewModel.isOnline.observe(this) { isOnline ->
            binding.networkStatusView.visibility = if (isOnline) View.GONE else View.VISIBLE
        }
    }

    private fun checkCameraPermissionAndDispatchIntent() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            dispatchTakePictureIntent()
        } else {
            pendingAction = PendingAction.TAKE_PHOTO
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun checkStoragePermissionAndPickImage() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            pickImageFromGallery()
        } else {
            pendingAction = PendingAction.PICK_FROM_GALLERY
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    Toast.makeText(this, R.string.error_creating_image_file, Toast.LENGTH_SHORT).show()
                    null
                }

                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.askchinna.fileprovider",
                        it
                    )
                    takePictureLauncher.launch(photoURI)
                }
            } ?: run {
                Toast.makeText(this, R.string.no_camera_app_available, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickImageFromGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun createImageFile(): File {
        val storageDir: File = getExternalFilesDir(null) ?: filesDir
        return File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_",
            ".jpg",
            storageDir
        ).apply {
            // Save a file path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun navigateToPreview() {
        val intent = Intent(this, ImagePreviewActivity::class.java)
        startActivity(intent)
    }

    private fun showHelpDialog() {
        // Show a help dialog for image capture guidance
        // Implement this using DialogFragment or AlertDialog
        Toast.makeText(this, R.string.image_capture_help_message, Toast.LENGTH_LONG).show()
    }

    // Enum for pending actions that need permissions
    private enum class PendingAction {
        NONE,
        TAKE_PHOTO,
        PICK_FROM_GALLERY
    }

    companion object {
        const val EXTRA_CROP = "extra_crop"
    }
}