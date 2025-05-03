/*file path: app/src/main/java/com/example/askchinna/ui/results/ResultActivity.kt
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Updated: May 2, 2025
 * Version: 1.3
 */

package com.example.askchinna.ui.results

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

/**
 * Activity that displays the results of the crop disease/pest identification
 * Shows summary, details, and recommended actions based on the analysis result
 */
@AndroidEntryPoint
class ResultActivity : AppCompatActivity() {

    private val viewModel: ResultViewModel by viewModels()

    private lateinit var toolbar: Toolbar
    private lateinit var summaryView: SummaryView
    private lateinit var actionPlanView: ActionPlanView
    private lateinit var detailsView: DetailExpandableView
    private lateinit var feedbackView: FeedbackView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorView: View
    private lateinit var retryButton: Button
    private lateinit var exportPdfButton: Button
    private lateinit var croppedImageView: ImageView

    companion object {
        private const val EXTRA_IMAGE_PATH = "extra_image_path"
        private const val EXTRA_CROP_ID = "extra_crop_id"

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
        setContentView(R.layout.activity_result)

        // Set up back navigation using OnBackPressedCallback
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Navigate back to HomeActivity and clear the back stack
                val intent = Intent(this@ResultActivity, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
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
            return
        }

        // Load the cropped image
        loadCroppedImage(imagePath)

        // Observe UI state
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is UIState.Loading -> showLoading()
                is UIState.Success -> {
                    val result = state.data
                    showResult(result)
                }

                is UIState.Error -> showError(state.message)
            }
        }

        // Set up click listeners
        setupClickListeners()

        // Start identification process
        viewModel.startIdentification(imagePath, cropId)
    }

    /**
     * Initialize all views from layout
     */
    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        summaryView = findViewById(R.id.view_summary)
        actionPlanView = findViewById(R.id.view_action_plan)
        detailsView = findViewById(R.id.view_details)
        feedbackView = findViewById(R.id.view_feedback)
        progressBar = findViewById(R.id.progress_bar)
        errorView = findViewById(R.id.view_error)
        retryButton = findViewById(R.id.button_retry)
        exportPdfButton = findViewById(R.id.button_export_pdf)
        croppedImageView = findViewById(R.id.image_crop)
    }

    /**
     * Set up click listeners for buttons
     */
    private fun setupClickListeners() {
        // Retry button click listener
        retryButton.setOnClickListener {
            val imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)
            val cropId = intent.getStringExtra(EXTRA_CROP_ID)

            if (!imagePath.isNullOrEmpty() && !cropId.isNullOrEmpty()) {
                viewModel.startIdentification(imagePath, cropId)
            } else {
                showError(getString(R.string.error_generic))
            }
        }

        // Export PDF button click listener
        exportPdfButton.setOnClickListener {
            viewModel.currentResult?.let {
                // Use generatePdf method instead of direct PDF generation
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
        }

        // Feedback submission listener
        feedbackView.setOnFeedbackSubmittedListener { feedbackViewType ->
            // Convert to the type expected by ViewModel (using the separate enum)
            val feedbackType = when (feedbackViewType) {
                FeedbackView.FeedbackType.HELPFUL -> FeedbackType.HELPFUL
                FeedbackView.FeedbackType.PARTIALLY_HELPFUL -> FeedbackType.PARTIALLY_HELPFUL
                FeedbackView.FeedbackType.NOT_HELPFUL -> FeedbackType.NOT_HELPFUL
            }
            viewModel.submitFeedback(feedbackType)
            Toast.makeText(
                this,
                getString(R.string.submit_feedback),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Loads the cropped image into the ImageView
     */
    private fun loadCroppedImage(imagePath: String) {
        val imageFile = File(imagePath)
        if (imageFile.exists()) {
            Glide.with(this)
                .load(imageFile)
                .centerCrop()
                .into(croppedImageView)
        }
    }

    /**
     * Shows the loading state
     */
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        summaryView.visibility = View.GONE
        actionPlanView.visibility = View.GONE
        detailsView.visibility = View.GONE
        feedbackView.visibility = View.GONE
        errorView.visibility = View.GONE
        exportPdfButton.visibility = View.GONE
    }

    /**
     * Shows the identification result
     */
    private fun showResult(result: IdentificationResult) {
        progressBar.visibility = View.GONE
        summaryView.visibility = View.VISIBLE
        actionPlanView.visibility = View.VISIBLE
        detailsView.visibility = View.VISIBLE
        feedbackView.visibility = View.VISIBLE
        errorView.visibility = View.GONE
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
            val severity = when (result.severity) {
                3 -> DetailExpandableView.Severity.HIGH
                2 -> DetailExpandableView.Severity.MEDIUM
                else -> DetailExpandableView.Severity.LOW
            }
            setSeverity(severity)
        }

        // Set actions list
        actionPlanView.setActions(result.actions)
    }

    /**
     * Shows an error message
     */
    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        summaryView.visibility = View.GONE
        actionPlanView.visibility = View.GONE
        detailsView.visibility = View.GONE
        feedbackView.visibility = View.GONE
        errorView.visibility = View.VISIBLE
        exportPdfButton.visibility = View.GONE

        // Set error message in the error view
        val errorTextView = errorView.findViewById<TextView>(R.id.text_error_message)
        errorTextView.text = message
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // Use onBackPressedDispatcher instead of directly calling onBackPressed()
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}