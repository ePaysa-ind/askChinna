/**
 * Copyright (c) 2025 askChinna App
 * Created: April 28, 2025
 * Version: 1.0
 */
package com.example.askchinna.ui.identification

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.askchinna.R
import com.example.askchinna.data.model.UIState
import com.example.askchinna.databinding.ActivityImagePreviewBinding
import com.example.askchinna.ui.home.SessionTimerManager
import com.example.askchinna.ui.results.ResultActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Activity for previewing the captured/selected image and initiating identification.
 * This screen allows users to check image quality and submit for analysis.
 */
@AndroidEntryPoint
class ImagePreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImagePreviewBinding
    private val viewModel: IdentificationViewModel by viewModels()

    @Inject
    lateinit var sessionTimerManager: SessionTimerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImagePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up session timer
        binding.sessionTimerView.setTimerManager(sessionTimerManager)

        // Set up click listeners
        setupClickListeners()

        // Set up observers
        setupObservers()

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

        // Retake button
        binding.btnRetake.setOnClickListener {
            viewModel.retakeImage()
            finish()
        }

        // Submit button
        binding.btnSubmit.setOnClickListener {
            // Check if image quality is acceptable
            if (viewModel.imageQuality.value?.isAcceptable == true) {
                submitImage()
            } else {
                showProceedAnywayDialog()
            }
        }
    }

    private fun setupObservers() {
        // Image preview
        viewModel.capturedImage.observe(this) { bitmap ->
            if (bitmap != null) {
                binding.ivPreview.setImageBitmap(bitmap)
            } else {
                // If no image, go back to capture screen
                finish()
            }
        }

        // Crop name
        viewModel.selectedCrop.observe(this) { crop ->
            binding.tvCropName.text = crop.name
            binding.ivCropIcon.setImageResource(crop.iconResId)
        }

        // Image quality analysis
        viewModel.imageQuality.observe(this) { qualityResult ->
            binding.imageQualityView.apply {
                setImageQuality(qualityResult)
                visibility = View.VISIBLE
            }

            // Enable/disable submit button based on image quality
            binding.btnSubmit.isEnabled = true
        }

        // UI state for identification process
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is UIState.Initial -> {
                    binding.loadingView.visibility = View.GONE
                    binding.errorView.visibility = View.GONE
                    binding.contentLayout.visibility = View.VISIBLE
                }
                is UIState.Loading -> {
                    binding.loadingView.visibility = View.VISIBLE
                    binding.errorView.visibility = View.GONE
                    binding.contentLayout.visibility = View.GONE
                }
                is UIState.Success -> {
                    binding.loadingView.visibility = View.GONE
                    binding.errorView.visibility = View.GONE

                    // Navigate to results screen
                    val intent = Intent(this, ResultActivity::class.java).apply {
                        putExtra(ResultActivity.EXTRA_IDENTIFICATION_RESULT, state.data)
                    }
                    startActivity(intent)
                    finish()
                }
                is UIState.Error -> {
                    binding.loadingView.visibility = View.GONE
                    binding.errorView.visibility = View.VISIBLE
                    binding.contentLayout.visibility = View.GONE

                    binding.errorView.setError(
                        message = state.message,
                        buttonText = getString(R.string.retry),
                        onButtonClick = { viewModel.retryIdentification() }
                    )
                }
            }
        }
    }

    private fun setupNetworkStatusObserver() {
        viewModel.isOnline.observe(this) { isOnline ->
            binding.networkStatusView.visibility = if (isOnline) View.GONE else View.VISIBLE

            // Disable submit button if offline
            binding.btnSubmit.isEnabled = isOnline

            if (!isOnline) {
                Toast.makeText(this, R.string.internet_required_for_identification, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitImage() {
        // Check if session time is still valid
        if (!sessionTimerManager.isSessionActive()) {
            Toast.makeText(this, R.string.session_expired, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.submitForIdentification()
    }

    private fun showProceedAnywayDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.low_quality_image_title)
            .setMessage(R.string.low_quality_image_message)
            .setPositiveButton(R.string.proceed_anyway) { _, _ ->
                submitImage()
            }
            .setNegativeButton(R.string.retake_photo) { _, _ ->
                viewModel.retakeImage()
                finish()
            }
            .setCancelable(true)
            .show()
    }
}