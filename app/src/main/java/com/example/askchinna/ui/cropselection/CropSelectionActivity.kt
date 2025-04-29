/**
 * File: app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt
 * Copyright (c) 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */
package com.example.askchinna.ui.cropselection

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.askchinna.R
import com.example.askchinna.data.model.Crop
import com.example.askchinna.data.model.UIState
import com.example.askchinna.databinding.ActivityCropSelectionBinding
import com.example.askchinna.ui.identification.ImageCaptureActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity for crop selection
 * Displays a grid of crops that the user can select for pest/disease identification
 */
@AndroidEntryPoint
class CropSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCropSelectionBinding
    private val viewModel: CropSelectionViewModel by viewModels()
    private lateinit var cropAdapter: CropAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCropSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    /**
     * Sets up the UI components
     */
    private fun setupUI() {
        // Set up the toolbar
        setSupportActionBar(binding.toolbarCropSelection)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Set up back button
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        // Set up crop grid
        setupRecyclerView()

        // Set up error view retry button
        binding.viewError.findViewById<View>(R.id.btnRetry).setOnClickListener {
            viewModel.loadCrops()
        }
    }

    /**
     * Sets up the RecyclerView for the crop grid
     */
    private fun setupRecyclerView() {
        cropAdapter = CropAdapter { crop ->
            onCropSelected(crop)
        }

        binding.recyclerCrops.apply {
            layoutManager = GridLayoutManager(this@CropSelectionActivity, 2)
            adapter = cropAdapter
        }
    }

    /**
     * Observes changes in the ViewModel's LiveData
     */
    private fun observeViewModel() {
        // Observe UI state for crop list
        viewModel.uiState.observe(this) { state ->
            updateUIState(state)
        }

        // Observe usage limit
        viewModel.usageLimit.observe(this) { limit ->
            binding.viewUsageLimit.findViewById<View>(R.id.txtUsageCount).apply {
                if (this is android.widget.TextView) {
                    text = viewModel.getUsageLimitText()
                }
            }
        }
    }

    /**
     * Updates the UI based on the current state
     */
    private fun updateUIState(state: UIState<List<Crop>>) {
        when (state) {
            is UIState.Loading -> {
                binding.apply {
                    layoutLoading.visibility = View.VISIBLE
                    recyclerCrops.visibility = View.GONE
                    viewError.visibility = View.GONE
                }
            }
            is UIState.Success -> {
                binding.apply {
                    layoutLoading.visibility = View.GONE
                    recyclerCrops.visibility = View.VISIBLE
                    viewError.visibility = View.GONE
                }
                cropAdapter.submitList(state.data)
            }
            is UIState.Error -> {
                binding.apply {
                    layoutLoading.visibility = View.GONE
                    recyclerCrops.visibility = View.GONE
                    viewError.visibility = View.VISIBLE
                }
                binding.viewError.findViewById<android.widget.TextView>(R.id.txtErrorMessage).text = state.message
            }
        }
    }

    /**
     * Handles crop selection
     * Navigates to the image capture activity if the user has remaining uses
     */
    private fun onCropSelected(crop: Crop) {
        if (viewModel.hasRemainingUses()) {
            navigateToImageCapture(crop)
        } else {
            Toast.makeText(
                this,
                getString(R.string.error_no_remaining_uses),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Navigates to the image capture activity with the selected crop
     */
    private fun navigateToImageCapture(crop: Crop) {
        val intent = ImageCaptureActivity.createIntent(this, crop)
        startActivity(intent)
    }

    companion object {
        /**
         * Creates an intent to launch this activity
         */
        fun createIntent(context: Context): Intent {
            return Intent(context, CropSelectionActivity::class.java)
        }
    }
}
