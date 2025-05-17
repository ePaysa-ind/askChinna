/**
 * File: app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt
 * Copyright (c) 2025 askChinna
 * Created: April 28, 2025
 * Updated: May 6, 2025
 * Version: 1.4
 * 
 * Change Log:
 * 1.4 - May 6, 2025
 * - Added proper error handling for network state changes
 * - Added retry mechanism for failed operations
 * - Added proper cleanup in onDestroy
 * - Added proper coroutine scope management
 * - Added state restoration
 * - Added memory optimization
 * - Added proper error logging
 */
package com.example.askchinna.ui.cropselection

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.askchinna.R
import com.example.askchinna.data.model.Crop
import com.example.askchinna.data.model.UIState
import com.example.askchinna.databinding.ActivityCropSelectionBinding
import com.example.askchinna.ui.identification.ImageCaptureActivity
import com.example.askchinna.util.NetworkStateMonitor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Activity for crop selection
 * Displays a grid of crops that the user can select for pest/disease identification
 */
@AndroidEntryPoint
class CropSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCropSelectionBinding
    private val viewModel: CropSelectionViewModel by viewModels()
    private lateinit var cropAdapter: CropAdapter
    private var isInitialized = false

    @Inject
    lateinit var networkStateMonitor: NetworkStateMonitor

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleError("Coroutine error", throwable as Exception)
    }

    companion object {
        private const val TAG = "CropSelectionActivity"
        private const val STATE_SELECTED_CROP = "selected_crop"
        
        /**
         * Creates an intent to launch this activity
         */
        fun createIntent(context: Context): Intent {
            return Intent(context, CropSelectionActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityCropSelectionBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Perform initialization on a coroutine to improve UI responsiveness
            lifecycleScope.launch {
                setupUIAsync()
                setupNetworkMonitoring()
                observeViewModel()

                if (savedInstanceState != null) {
                    restoreState(savedInstanceState)
                }

                isInitialized = true
            }
        } catch (e: Exception) {
            handleError("Failed to initialize activity", e)
            finish()
        }
    }

    /**
     * Sets up the UI components asynchronously
     */
    @Suppress("DEPRECATION")
    private suspend fun setupUIAsync() {
        try {
            withContext(Dispatchers.Main) {
                // Set up the toolbar
                setSupportActionBar(binding.toolbarCropSelection)
                supportActionBar?.setDisplayShowTitleEnabled(false)

                // Set up back button
                binding.btnBack.setOnClickListener {
                    onBackPressed()
                }

                // Set up crop grid
                setupRecyclerView()
                setupErrorView()
            }
        } catch (e: Exception) {
            handleError("Failed to setup UI", e)
        }
    }

    /**
     * Sets up the RecyclerView for the crop grid
     */
    private fun setupRecyclerView() {
        try {
            cropAdapter = CropAdapter({ crop ->
                onCropSelected(crop)
            }, com.example.askchinna.util.ImageHelper(this))

            binding.recyclerCrops.apply {
                layoutManager = GridLayoutManager(this@CropSelectionActivity, 2)
                adapter = cropAdapter
                setHasFixedSize(true)
            }
        } catch (e: Exception) {
            handleError("Failed to setup RecyclerView", e)
        }
    }

    private fun setupErrorView() {
        try {
            binding.viewError.setRetryClickListener {
                viewModel.loadCrops()
            }
        } catch (e: Exception) {
            handleError("Failed to setup error view", e)
        }
    }

    /**
     * Observes changes in the ViewModel's LiveData
     */
    private fun observeViewModel() {
        try {
            // Observe UI state for crop list
            viewModel.uiState.observe(this) { state ->
                updateUIState(state)
            }

            // Observe usage limit
            viewModel.usageLimit.observe(this) { limit ->
                updateUsageLimit(limit)
            }
        } catch (e: Exception) {
            handleError("Failed to observe ViewModel", e)
        }
    }

    /**
     * Updates the UI based on the current state
     */
    private fun updateUIState(state: UIState<List<Crop>>) {
        try {
            when (state) {
                is UIState.Initial -> showLoadingState()
                is UIState.Loading -> showLoadingState()
                is UIState.Success -> {
                    showContentState()
                    cropAdapter.submitList(state.data)
                }
                is UIState.Error -> {
                    showErrorState()
                    showError(state.message)
                }
            }
        } catch (e: Exception) {
            handleError("Failed to update UI state", e)
        }
    }

    /**
     * Updates the usage limit display
     */
    private fun updateUsageLimit(limit: Int) {
        try {
            binding.viewUsageLimit.tvUsageLimit.text = getString(R.string.usage_limit_format, limit)
        } catch (e: Exception) {
            handleError("Failed to update usage limit", e)
        }
    }

    private fun showLoadingState() {
        try {
            binding.root.findViewById<View>(R.id.layoutLoading)?.visibility = View.VISIBLE
            binding.root.findViewById<View>(R.id.recyclerCrops)?.visibility = View.GONE
            binding.root.findViewById<View>(R.id.viewError)?.visibility = View.GONE
        } catch (e: Exception) {
            handleError("Failed to show loading state", e)
        }
    }

    private fun showContentState() {
        try {
            binding.root.findViewById<View>(R.id.layoutLoading)?.visibility = View.GONE
            binding.root.findViewById<View>(R.id.recyclerCrops)?.visibility = View.VISIBLE
            binding.root.findViewById<View>(R.id.viewError)?.visibility = View.GONE
        } catch (e: Exception) {
            handleError("Failed to show content state", e)
        }
    }

    private fun showErrorState() {
        try {
            binding.root.findViewById<View>(R.id.layoutLoading)?.visibility = View.GONE
            binding.root.findViewById<View>(R.id.recyclerCrops)?.visibility = View.GONE
            binding.root.findViewById<View>(R.id.viewError)?.visibility = View.VISIBLE
        } catch (e: Exception) {
            handleError("Failed to show error state", e)
        }
    }

    /**
     * Shows an error message to the user
     */
    private fun showError(message: String) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            binding.viewError.apply {
                visibility = View.VISIBLE
                setError(message)
            }
        } catch (e: Exception) {
            handleError("Failed to show error message", e)
        }
    }

    /**
     * Handles crop selection
     * Navigates to the image capture activity if the user has remaining uses
     */
    private fun onCropSelected(crop: Crop) {
        try {
            if (!isInitialized) {
                showError(getString(R.string.error_not_initialized))
                return
            }

            if (viewModel.hasRemainingUses()) {
                navigateToImageCapture(crop)
            } else {
                showError(getString(R.string.error_no_remaining_uses))
            }
        } catch (e: Exception) {
            handleError("Failed to handle crop selection", e)
        }
    }

    /**
     * Navigates to the image capture activity with the selected crop
     */
    private fun navigateToImageCapture(crop: Crop) {
        try {
            val intent = Intent(this, ImageCaptureActivity::class.java).apply {
                putExtra(ImageCaptureActivity.EXTRA_CROP, crop)
            }
            startActivity(intent)
        } catch (e: Exception) {
            handleError("Failed to navigate to image capture", e)
        }
    }

    private fun setupNetworkMonitoring() {
        lifecycleScope.launch(exceptionHandler) {
            networkStateMonitor.observe().collectLatest { isAvailable: Boolean ->
                try {
                    if (!isAvailable) {
                        showNetworkError()
                    } else {
                        viewModel.loadCrops()
                    }
                } catch (e: Exception) {
                    handleError("Network state change error", e)
                }
            }
        }
    }

    private fun showNetworkError() {
        try {
            AlertDialog.Builder(this)
                .setTitle(R.string.network_error_title)
                .setMessage(R.string.network_error_message)
                .setPositiveButton(R.string.retry) { _, _ ->
                    viewModel.loadCrops()
                }
                .setNegativeButton(R.string.ok, null)
                .show()
        } catch (e: Exception) {
            handleError("Failed to show network error dialog", e)
        }
    }

    private fun handleError(message: String, e: Throwable) {
        Log.e(TAG, message, e)
        showError("$message: ${e.message}")
    }

    private fun restoreState(savedInstanceState: Bundle) {
        try {
            val selectedCrop = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                savedInstanceState.getParcelable(STATE_SELECTED_CROP, Crop::class.java)
            } else {
                @Suppress("DEPRECATION")
                savedInstanceState.getParcelable<Crop>(STATE_SELECTED_CROP)
            }
            selectedCrop?.let { crop ->
                viewModel.setSelectedCrop(crop)
            }
        } catch (e: Exception) {
            handleError("Failed to restore state", e)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        try {
            viewModel.getSelectedCrop()?.let { crop ->
                outState.putParcelable(STATE_SELECTED_CROP, crop)
            }
        } catch (e: Exception) {
            handleError("Failed to save state", e)
        }
    }

    override fun onDestroy() {
        try {
            isInitialized = false
            super.onDestroy()
        } catch (e: Exception) {
            handleError("Failed to cleanup resources", e)
        }
    }
}