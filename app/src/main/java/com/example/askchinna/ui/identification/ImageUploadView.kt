package com.example.askchinna.ui.identification

/**
 * Copyright (c) 2025 askChinna App
 * Created: April 28, 2025
 * Version: 1.0
 */
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.askchinna.R
import com.example.askchinna.databinding.ViewImageUploadBinding
import com.example.askchinna.util.SimpleCoroutineUtils
import kotlinx.coroutines.Job

/**
 * Custom view for displaying image upload progress and status.
 * Provides visual feedback during the upload process.
 */
class ImageUploadView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewImageUploadBinding
    private var progressJob: Job? = null
    private var currentProgress = 0

    init {
        // Inflate layout
        binding = ViewImageUploadBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    /**
     * Start uploading process with animation and progress updates
     * @param onCancelClick Callback when cancel button is clicked
     */
    fun startUploading(onCancelClick: () -> Unit) {
        visibility = VISIBLE
        currentProgress = 0

        // Set initial state
        binding.tvStatus.text = context.getString(R.string.uploading_image)
        binding.progressBar.progress = 0
        binding.tvProgressPercent.text = "0%"
        binding.ivStatus.setImageResource(R.drawable.ic_upload)
        binding.ivStatus.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary))

        // Show cancel button
        binding.btnCancel.apply {
            visibility = VISIBLE
            setOnClickListener { onCancelClick() }
        }

        // Start animated progress (simulated for visual feedback)
        startProgressAnimation()
    }

    /**
     * Update progress percentage
     * @param progress Progress value (0-100)
     */
    fun updateProgress(progress: Int) {
        val boundedProgress = progress.coerceIn(0, 100)
        currentProgress = boundedProgress

        binding.progressBar.progress = boundedProgress
        binding.tvProgressPercent.text = "$boundedProgress%"
    }

    /**
     * Show completion status
     * @param success Whether upload completed successfully
     * @param message Optional message to display
     */
    fun completeUpload(success: Boolean, message: String? = null) {
        progressJob?.cancel()

        if (success) {
            // Success state
            binding.tvStatus.text = message ?: context.getString(R.string.upload_complete)
            binding.progressBar.progress = 100
            binding.tvProgressPercent.text = "100%"
            binding.ivStatus.setImageResource(R.drawable.ic_severity_low)
            binding.ivStatus.setColorFilter(ContextCompat.getColor(context, R.color.colorSuccess))
        } else {
            // Error state
            binding.tvStatus.text = message ?: context.getString(R.string.upload_failed)
            binding.ivStatus.setImageResource(R.drawable.ic_severity_high)
            binding.ivStatus.setColorFilter(ContextCompat.getColor(context, R.color.colorError))
        }

        // Hide cancel button when complete
        binding.btnCancel.visibility = GONE

        // Auto hide after a delay if successful
        if (success) {
            SimpleCoroutineUtils.delayOnMain(3000) {
                this.visibility = GONE
            }
        }
    }

    /**
     * Show error state with message
     * @param errorMessage Error message to display
     * @param retryAction Optional retry action callback
     */
    fun showError(errorMessage: String, retryAction: (() -> Unit)? = null) {
        progressJob?.cancel()

        visibility = VISIBLE
        binding.tvStatus.text = errorMessage
        binding.ivStatus.setImageResource(R.drawable.ic_severity_high)
        binding.ivStatus.setColorFilter(ContextCompat.getColor(context, R.color.colorError))

        // Show retry instead of cancel if retry action provided
        binding.btnCancel.apply {
            if (retryAction != null) {
                setText(R.string.retry)
                setOnClickListener { retryAction() }
                visibility = VISIBLE
            } else {
                visibility = GONE
            }
        }
    }

    /**
     * Reset and hide the view
     */
    fun reset() {
        progressJob?.cancel()
        visibility = GONE
        currentProgress = 0
    }

    /**
     * Simulate progress animation for visual feedback
     * Actual progress will be updated by updateProgress()
     */
    private fun startProgressAnimation() {
        progressJob?.cancel()

        // Use a simulated "indeterminate but visually progressing" behavior
        // to give user feedback even when actual progress might be unpredictable
        progressJob = SimpleCoroutineUtils.intervalOnMain(100) {
            if (currentProgress < 90) {
                // Simulate random progress increments that slow down as they approach 90%
                val increment = when {
                    currentProgress < 30 -> (1..3).random()
                    currentProgress < 60 -> (1..2).random()
                    else -> 1
                }

                val newProgress = (currentProgress + increment).coerceAtMost(90)
                if (newProgress > currentProgress) {
                    updateProgress(newProgress)
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        progressJob?.cancel()
    }
}

