/*file path: app/src/main/java/com/example/askchinna/ui/results/ResultActivity.kt
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Updated: May 3, 2025
 * Version: 1.5
 */

package com.example.askchinna.ui.results

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.askchinna.R
import com.example.askchinna.data.model.IdentificationResult
import com.example.askchinna.data.model.UIState
import com.example.askchinna.ui.home.HomeActivity
import com.example.askchinna.databinding.ActivityResultBinding
import com.example.askchinna.databinding.ViewErrorBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

/**
 * Activity that displays the results of the crop disease/pest identification
 * Shows summary, details, and recommended actions based on the analysis result
 */
@AndroidEntryPoint
class ResultActivity : AppCompatActivity() {

    private val viewModel: ResultViewModel by viewModels()
    private var isInitialized = false
    private lateinit var binding: ActivityResultBinding

    private lateinit var toolbar: Toolbar
    private lateinit var summaryView: SummaryView
    private lateinit var actionPlanView: ActionPlanView
    private lateinit var detailsView: DetailExpandableView
    private lateinit var feedbackView: FeedbackView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorView: ViewErrorBinding
    private lateinit var retryButton: Button
    private lateinit var exportPdfButton: Button
    private lateinit var croppedImageView: ImageView

    companion object {
        private const val TAG = "ResultActivity"
        private const val EXTRA_IMAGE_PATH = "extra_image_path"
        private const val EXTRA_CROP_ID = "extra_crop_id"
        const val EXTRA_IDENTIFICATION_RESULT = "extra_identification_result"

        /**
         * Creates an intent to start this activity
         */
        fun createIntent(context: Context, imagePath: String, cropId: String): Intent {
            return Intent(context, ResultActivity::class.java).apply {
                putExtra(EXTRA_IMAGE_PATH, imagePath)
                putExtra(EXTRA_CROP_ID, cropId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityResultBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Set up back navigation using OnBackPressedCallback
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    try {
                        // Navigate back to HomeActivity and clear the back stack
                        val intent = Intent(this@ResultActivity, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error handling back press", e)
                        showError(getString(R.string.error_navigation))
                    }
                }
            })

            // Initialize views
            initViews()

            // Setup toolbar
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = getString(R.string.title_results)

            // Get data from intent
            val imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)
            val cropId = intent.getStringExtra(EXTRA_CROP_ID)

            // Validate required data
            if (imagePath.isNullOrEmpty() || cropId.isNullOrEmpty()) {
                showError(getString(R.string.error_generic))
                finish()
                return
            }

            // Load the cropped image
            loadCroppedImage(imagePath)

            // Observe UI state
            setupObservers()

            // Set up click listeners
            setupClickListeners()

            // Start identification process
            viewModel.startIdentification(imagePath, cropId)
            isInitialized = true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing activity", e)
            showError(getString(R.string.error_initialization))
            finish()
        }
    }

    /**
     * Initialize all views from layout
     */
    private fun initViews() {
        try {
            toolbar = binding.toolbar
            summaryView = binding.viewSummary
            actionPlanView = binding.viewActionPlan
            detailsView = binding.viewDetails
            feedbackView = binding.viewFeedback
            progressBar = binding.progressBar
            errorView = binding.viewError
            retryButton = errorView.buttonRetry
            exportPdfButton = binding.buttonExportPdf
            croppedImageView = binding.imageCrop
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
            throw e
        }
    }

    /**
     * Set up observers for ViewModel data
     */
    private fun setupObservers() {
        try {
            if (!isInitialized) {
                Log.w(TAG, "Activity not initialized")
                return
            }

            viewModel.uiState.observe(this) { state ->
                try {
                    when (state) {
                        is UIState.Initial -> {} // Handle initial state (or do nothing)
                        is UIState.Loading -> showLoading()
                        is UIState.Success -> {
                            val result = state.data
                            showResult(result)
                        }
                        is UIState.Error -> showError(state.message)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling UI state", e)
                    showError(getString(R.string.error_state_handling))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up observers", e)
            showError(getString(R.string.error_observer_setup))
        }
    }

    /**
     * Set up click listeners for buttons
     */
    private fun setupClickListeners() {
        try {
            if (!isInitialized) {
                Log.w(TAG, "Activity not initialized")
                return
            }

            // Retry button click listener
            retryButton.setOnClickListener {
                try {
                    val imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)
                    val cropId = intent.getStringExtra(EXTRA_CROP_ID)

                    if (!imagePath.isNullOrEmpty() && !cropId.isNullOrEmpty()) {
                        viewModel.startIdentification(imagePath, cropId)
                    } else {
                        showError(getString(R.string.error_generic))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling retry click", e)
                    showError(getString(R.string.error_retry))
                }
            }

            // Export PDF button click listener
            exportPdfButton.setOnClickListener {
                try {
                    viewModel.currentResult?.let {
                        Toast.makeText(
                            this,
                            getString(R.string.loading_message),
                            Toast.LENGTH_SHORT
                        ).show()

                        // This would typically call a method to generate and share the PDF
                        // For now, just show a completion message since we don't have all dependencies
                        Toast.makeText(
                            this,
                            "PDF generation is not implemented in this version",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling PDF export", e)
                    showError(getString(R.string.error_pdf_export))
                }
            }

            // Feedback submission listener
            feedbackView.setOnFeedbackSubmittedListener { feedbackViewType ->
                try {
                    // Convert to the type expected by ViewModel
                    val feedbackType = when (feedbackViewType) {
                        FeedbackType.HELPFUL -> FeedbackType.HELPFUL
                        FeedbackType.PARTIALLY_HELPFUL -> FeedbackType.PARTIALLY_HELPFUL
                        FeedbackType.NOT_HELPFUL -> FeedbackType.NOT_HELPFUL
                    }
                    viewModel.submitFeedback(feedbackType)
                    Toast.makeText(
                        this,
                        getString(R.string.submit_feedback),
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling feedback submission", e)
                    showError(getString(R.string.error_feedback_submission))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
            showError(getString(R.string.error_ui_setup))
        }
    }

    /**
     * Loads the cropped image into the ImageView
     */
    private fun loadCroppedImage(imagePath: String) {
        try {
            if (!isInitialized) {
                Log.w(TAG, "Activity not initialized")
                return
            }

            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                Glide.with(this)
                    .load(imageFile)
                    .centerCrop()
                    .into(croppedImageView)
            } else {
                Log.e(TAG, "Image file not found: $imagePath")
                showError(getString(R.string.error_image_not_found))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading cropped image", e)
            showError(getString(R.string.error_image_loading))
        }
    }

    /**
     * Shows the loading state
     */
    private fun showLoading() {
        try {
            if (!isInitialized) {
                Log.w(TAG, "Activity not initialized")
                return
            }

            progressBar.visibility = View.VISIBLE
            summaryView.visibility = View.GONE
            actionPlanView.visibility = View.GONE
            detailsView.visibility = View.GONE
            feedbackView.visibility = View.GONE
            errorView.root.visibility = View.GONE
            exportPdfButton.visibility = View.GONE
        } catch (e: Exception) {
            Log.e(TAG, "Error showing loading state", e)
            showError(getString(R.string.error_ui_state))
        }
    }

    /**
     * Shows the identification result
     */
    private fun showResult(result: IdentificationResult) {
        try {
            if (!isInitialized) {
                Log.w(TAG, "Activity not initialized")
                return
            }

            progressBar.visibility = View.GONE
            summaryView.visibility = View.VISIBLE
            actionPlanView.visibility = View.VISIBLE
            detailsView.visibility = View.VISIBLE
            feedbackView.visibility = View.VISIBLE
            errorView.root.visibility = View.GONE
            exportPdfButton.visibility = View.VISIBLE

            // Populate views with result data
            summaryView.setData(
                cropName = result.cropName,
                issueName = result.problemName,
                confidence = result.confidence
            )

            detailsView.apply {
                setTitle(result.problemName)
                setContent(result.description)
                // Set appropriate icon based on issue type
                val iconResId = when {
                    result.problemType?.contains("bacterial", ignoreCase = true) == true -> R.drawable.ic_bacterial
                    result.problemType?.contains("viral", ignoreCase = true) == true -> R.drawable.ic_viral
                    result.problemType?.contains("fungal", ignoreCase = true) == true -> R.drawable.ic_fungal
                    result.problemType?.contains("deficiency", ignoreCase = true) == true -> R.drawable.ic_deficiency
                    else -> R.drawable.ic_warning
                }
                setIcon(iconResId)

                // Set severity level
                val severityIconResId = when (result.severity) {
                    3 -> R.drawable.ic_severity_high
                    2 -> R.drawable.ic_severity_medium
                    else -> R.drawable.ic_severity_low
                }
                setSeverityIcon(severityIconResId)
            }

            // Set actions list
            actionPlanView.setActions(result.actions)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing result", e)
            showError(getString(R.string.error_result_display))
        }
    }

    /**
     * Shows an error message
     */
    private fun showError(message: String) {
        try {
            if (!isInitialized) {
                Log.w(TAG, "Activity not initialized")
                return
            }

            progressBar.visibility = View.GONE
            summaryView.visibility = View.GONE
            actionPlanView.visibility = View.GONE
            detailsView.visibility = View.GONE
            feedbackView.visibility = View.GONE
            errorView.root.visibility = View.VISIBLE
            exportPdfButton.visibility = View.GONE

            // Set error message using data binding
            errorView.textErrorMessage.text = message

            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing error message", e)
        }
    }

    private fun handleFeedback(feedbackType: FeedbackType) {
        when (feedbackType) {
            FeedbackType.HELPFUL -> {
                // Handle helpful feedback
                showThankYouMessage()
            }
            FeedbackType.PARTIALLY_HELPFUL -> {
                // Handle partially helpful feedback
                showThankYouMessage()
            }
            FeedbackType.NOT_HELPFUL -> {
                // Handle not helpful feedback
                showThankYouMessage()
            }
        }
    }

    private fun showThankYouMessage() {
        // Show thank you message
        binding.textThankYou.visibility = View.VISIBLE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return try {
            if (item.itemId == android.R.id.home) {
                onBackPressedDispatcher.onBackPressed()
                true
            } else {
                super.onOptionsItemSelected(item)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling menu item selection", e)
            false
        }
    }

    override fun onDestroy() {
        try {
            super.onDestroy()
            isInitialized = false
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
        }
    }
}