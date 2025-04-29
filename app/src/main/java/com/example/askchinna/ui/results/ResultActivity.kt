/*
 * Copyright (c) 2025 askChinna App
 * Created: April 29, 2025
 * Version: 1.0
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
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.askchinna.R
import com.example.askchinna.data.model.Action
import com.example.askchinna.data.model.IdentificationResult
import com.example.askchinna.data.model.UIState
import com.example.askchinna.ui.cropselection.CropSelectionActivity
import com.example.askchinna.ui.home.HomeActivity
import com.example.askchinna.util.PdfGenerator
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

        // Initialize views
        initViews()

        // Setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.results_title)

        // Get data from intent
        val imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)
        val cropId = intent.getStringExtra(EXTRA_CROP_ID)

        // Validate required data
        if (imagePath.isNullOrEmpty() || cropId.isNullOrEmpty()) {
            showError(getString(R.string.error_missing_data))
            return
        }

        // Load the cropped image
        loadCroppedImage(imagePath)

        // Observe UI state
        viewModel.uiState.observe(this, Observer { state ->
            when (state) {
                is UIState.Loading -> showLoading()
                is UIState.Success<*> -> {
                    val result = state.data as IdentificationResult
                    showResult(result)
                }
                is UIState.Error -> showError(state.message)
            }
        })

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
                showError(getString(R.string.error_missing_data))
            }
        }

        // Export PDF button click listener
        exportPdfButton.setOnClickListener {
            viewModel.currentResult?.let { result ->
                generateAndSharePdf(result)
            }
        }

        // Feedback submission listener
        feedbackView.setOnFeedbackSubmittedListener { feedbackType ->
            viewModel.submitFeedback(feedbackType)
            Toast.makeText(
                this,
                getString(R.string.feedback_thanks),
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
            issueName = result.issueName,
            confidence = result.confidence
        )

        detailsView.apply {
            setTitle(result.issueName)
            setContent(result.issueDescription)
            // Set appropriate icon based on issue type
            val iconResId = when {
                result.issueType.contains("bacterial", ignoreCase = true) -> R.drawable.ic_bacterial
                result.issueType.contains("viral", ignoreCase = true) -> R.drawable.ic_viral
                result.issueType.contains("fungal", ignoreCase = true) -> R.drawable.ic_fungal
                result.issueType.contains("deficiency", ignoreCase = true) -> R.drawable.ic_deficiency
                else -> R.drawable.ic_warning
            }
            setIcon(iconResId)

            // Set severity level
            val severity = when {
                result.severity >= 0.7 -> DetailExpandableView.Severity.HIGH
                result.severity >= 0.4 -> DetailExpandableView.Severity.MEDIUM
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

    /**
     * Generates a PDF of the results and shares it
     */
    private fun generateAndSharePdf(result: IdentificationResult) {
        val imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH) ?: return

        try {
            // Show loading toast
            Toast.makeText(
                this,
                getString(R.string.generating_pdf),
                Toast.LENGTH_SHORT
            ).show()

            // Use PdfGenerator utility to create and share PDF
            PdfGenerator(this).generateResultPdf(
                result = result,
                imagePath = imagePath,
                onSuccess = { pdfUri ->
                    // Create share intent
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, pdfUri)
                        type = "application/pdf"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    // Start share activity
                    startActivity(Intent.createChooser(
                        shareIntent,
                        getString(R.string.share_result_pdf)
                    ))
                },
                onError = { error ->
                    Toast.makeText(
                        this,
                        getString(R.string.pdf_generation_error, error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        } catch (e: Exception) {
            Toast.makeText(
                this,
                getString(R.string.pdf_generation_error, e.message),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        // Navigate back to HomeActivity and clear the back stack
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}
