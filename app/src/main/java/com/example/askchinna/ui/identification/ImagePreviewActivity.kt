/**
 * file path: app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt
 * Copyright (c) 2025 askChinna App
 * Created: April 28, 2025
 * Updated: May 6, 2025
 * Version: 1.3
 */
package com.example.askchinna.ui.identification

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.askchinna.R
import com.example.askchinna.data.model.UIState
import com.example.askchinna.databinding.ActivityImagePreviewBinding
import com.example.askchinna.ui.results.ResultActivity
import com.example.askchinna.ui.home.SessionTimerManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Activity for previewing the captured/selected image and initiating identification.
 */
@AndroidEntryPoint
class ImagePreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImagePreviewBinding
    private val viewModel: IdentificationViewModel by viewModels()
    private var currentDialog: AlertDialog? = null
    private var retryCount = 0
    private val maxRetries = 3

    @Inject
    lateinit var sessionTimerManager: SessionTimerManager

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleError("Coroutine error", throwable)
    }

    companion object {
        private const val TAG = "ImagePreviewActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            // Initialize binding using the correct method
            binding = ActivityImagePreviewBinding.inflate(layoutInflater)
            setContentView(binding.root)

            if (savedInstanceState != null) {
                retryCount = savedInstanceState.getInt("retryCount", 0)
            }

            binding.sessionTimerView.setTimerManager(sessionTimerManager)
            setupClickListeners()
            setupObservers()
            setupNetworkStatusObserver()
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

    override fun onDestroy() {
        try {
            dismissCurrentDialog()
            
            // Clear image from memory to prevent leaks
            binding.ivPreview.setImageDrawable(null)
            
            super.onDestroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
        }
    }

    private fun setupClickListeners() {
        try {
            binding.btnBack.setOnClickListener { finish() }
            binding.btnRetake.setOnClickListener {
                viewModel.retakeImage()
                finish()
            }
            binding.btnSubmit.setOnClickListener {
                if (viewModel.imageQuality.value?.isAcceptable == true) {
                    submitImage()
                } else {
                    showProceedAnywayDialog()
                }
            }
        } catch (e: Exception) {
            handleError("Error setting up click listeners", e)
        }
    }

    private fun setupObservers() {
        try {
            viewModel.capturedImage.observe(this) { bitmap ->
                try {
                    if (bitmap != null) {
                        binding.ivPreview.setImageBitmap(bitmap)
                    } else {
                        Log.w(TAG, "No captured image available")
                        finish()
                    }
                } catch (e: Exception) {
                    handleError("Error handling captured image", e)
                }
            }

            viewModel.selectedCrop.observe(this) { crop ->
                try {
                    binding.tvCropName.text = crop.name
                    binding.ivCropIcon.setImageResource(crop.iconResId)
                } catch (e: Exception) {
                    handleError("Error handling crop data", e)
                }
            }

            viewModel.imageQuality.observe(this) { qualityResult ->
                try {
                    binding.imageQualityView.apply {
                        setImageQuality(qualityResult)
                        visibility = View.VISIBLE
                    }
                    binding.btnSubmit.isEnabled = viewModel.isOnline.value == true
                } catch (e: Exception) {
                    handleError("Error handling image quality", e)
                }
            }

            viewModel.uiState.observe(this) { state ->
                try {
                    when (state) {
                        is UIState.Initial -> {
                            binding.loadingView.root.visibility = View.GONE
                            binding.errorView.root.visibility = View.GONE
                            binding.contentLayout.visibility = View.VISIBLE
                        }
                        is UIState.Loading -> {
                            binding.loadingView.root.visibility = View.VISIBLE
                            binding.errorView.root.visibility = View.GONE
                            binding.contentLayout.visibility = View.GONE
                        }
                        is UIState.Success<*> -> {
                            binding.loadingView.root.visibility = View.GONE
                            binding.errorView.root.visibility = View.GONE

                            startActivity(
                                Intent(this, ResultActivity::class.java)
                                    .putExtra(ResultActivity.EXTRA_IDENTIFICATION_RESULT, state.data)
                            )
                            finish()
                        }
                        is UIState.Error -> {
                            binding.loadingView.root.visibility = View.GONE
                            binding.errorView.root.visibility = View.VISIBLE
                            binding.contentLayout.visibility = View.GONE

                            // Find the error message TextView and retry button inside errorView
                            val errorMessageTextView = binding.errorView.root.findViewById<TextView>(R.id.text_error_message)
                            val retryButton = binding.errorView.root.findViewById<Button>(R.id.button_retry)

                            // Set error message and retry button click listener
                            errorMessageTextView?.text = state.message
                            retryButton?.apply {
                                text = getString(R.string.retry)
                                setOnClickListener { retryIdentification() }
                                visibility = View.VISIBLE
                            }
                        }
                    }
                } catch (e: Exception) {
                    handleError("Error handling UI state", e)
                }
            }
        } catch (e: Exception) {
            handleError("Error setting up observers", e)
        }
    }

    private fun setupNetworkStatusObserver() {
        try {
            viewModel.isOnline.observe(this) { isOnline ->
                try {
                    binding.networkStatusView.visibility = if (isOnline) View.GONE else View.VISIBLE

                    // If the NetworkStatusView has a setNetworkStatus method, call it
                    binding.networkStatusView
                        .setNetworkStatus(isOnline)

                    binding.btnSubmit.isEnabled = isOnline && (viewModel.imageQuality.value?.isAcceptable == true)
                    if (!isOnline) {
                        showNetworkError()
                    }
                } catch (e: Exception) {
                    handleError("Error handling network status", e)
                }
            }
        } catch (e: Exception) {
            handleError("Error setting up network observer", e)
        }
    }

    private fun submitImage() {
        try {
            val expired = sessionTimerManager.isSessionExpired.value == true
            if (expired) {
                showSessionExpiredDialog()
                return
            }

            lifecycleScope.launch(exceptionHandler) {
                viewModel.submitForIdentification()
            }
        } catch (e: Exception) {
            handleError("Error submitting image", e)
        }
    }

    private fun showProceedAnywayDialog() {
        currentDialog?.dismiss()
        currentDialog = AlertDialog.Builder(this)
            .setTitle(R.string.image_quality_warning_title)
            .setMessage(R.string.image_quality_warning_message)
            .setPositiveButton(R.string.proceed) { _, _ -> submitImage() }
            .setNegativeButton(R.string.retake) { _, _ ->
                viewModel.retakeImage()
                finish()
            }
            .setCancelable(false)
            .create()
        currentDialog?.show()
    }

    private fun showSessionExpiredDialog() {
        try {
            dismissCurrentDialog()
            currentDialog = AlertDialog.Builder(this)
                .setTitle(R.string.session_expired_title)
                .setMessage(R.string.session_expired_message)
                .setPositiveButton(R.string.ok) { _, _ ->
                    finish()
                }
                .setCancelable(false)
                .show()
        } catch (e: Exception) {
            handleError("Error showing session expired dialog", e)
        }
    }

    private fun showNetworkError() {
        Toast.makeText(this, R.string.error_no_network, Toast.LENGTH_SHORT).show()
    }

    private fun dismissCurrentDialog() {
        currentDialog?.dismiss()
        currentDialog = null
    }

    private fun retryIdentification() {
        if (retryCount < maxRetries) {
            retryCount++
            submitImage()
        } else {
            finish()
        }
    }

    private fun handleError(message: String, throwable: Throwable) {
        Log.e(TAG, message, throwable)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}