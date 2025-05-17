/**
 * file path: app/src/main/java/com/example/askchinna/ui/auth/ForgotPasswordActivity.kt
 * Copyright © 2025 askChinna
 * Created: April 29, 2025
 * Version: 1.0
 */

package com.example.askchinna.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.askchinna.R
import com.example.askchinna.data.model.UIState
import com.example.askchinna.databinding.ActivityForgotPasswordBinding
import com.example.askchinna.ui.common.NetworkStatusView
import com.example.askchinna.util.NetworkStateMonitor
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: ForgotPasswordViewModel by viewModels()

    @Inject
    lateinit var networkStateMonitor: NetworkStateMonitor

    private lateinit var networkStatusView: NetworkStatusView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize network status view
        networkStatusView = binding.networkStatusView

        setupInputValidation()
        setupClickListeners()
        observeViewModel()
        setupNetworkMonitoring()
    }

    private fun setupInputValidation() {
        binding.editTextMobile.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateMobileInput()
            }
        })
    }

    private fun validateMobileInput(): Boolean {
        val mobile = binding.editTextMobile.text.toString().trim()
        val isValid = mobile.length == 10 && mobile.matches(Regex("[0-9]{10}"))

        // Update button state based on validation
        binding.buttonResetPassword.isEnabled = isValid && networkStateMonitor.isNetworkAvailable()

        if (mobile.isNotEmpty() && !isValid) {
            binding.editTextMobile.error = getString(R.string.error_invalid_mobile)
        } else {
            binding.editTextMobile.error = null
        }

        return isValid
    }

    private fun setupClickListeners() {
        binding.buttonResetPassword.setOnClickListener {
            if (!networkStateMonitor.isNetworkAvailable()) {
                showError(getString(R.string.error_no_network))
                return@setOnClickListener
            }

            if (validateMobileInput()) {
                val mobile = binding.editTextMobile.text.toString().trim()
                viewModel.sendPasswordResetOtp(mobile, this)
            }
        }

        binding.imageButtonBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun observeViewModel() {
        // Using StateFlow for observation
        lifecycleScope.launch {
            viewModel.resetState.collectLatest { state ->
                handleUIState(state)
            }
        }
    }

    private fun handleUIState(state: UIState<String>) {
        when (state) {
            is UIState.Loading -> {
                showLoading(true)
            }
            is UIState.Success -> {
                showLoading(false)
                // Create a success message with the mobile number
                val mobileNumber = binding.editTextMobile.text.toString().trim()
                // Format mobile for display (e.g., add mask like XXX-XXX-XXXX)
                val formattedMobile = if (mobileNumber.length == 10) {
                    val lastFourDigits = mobileNumber.substring(6)
                    "XXXXXX$lastFourDigits"
                } else {
                    mobileNumber
                }

                // Use a simple string concatenation to avoid formatting issues
                val message = "OTP sent to $formattedMobile. Please check your messages."
                showMessage(message)

                // You might want to navigate to OTP verification screen instead of finishing
                finish()
            }
            is UIState.Error -> {
                showLoading(false)
                showError(state.message)
            }
            else -> {
                showLoading(false)
            }
        }
    }

    private fun setupNetworkMonitoring() {
        networkStateMonitor.startMonitoring()
        lifecycleScope.launch {
            networkStateMonitor.observe().collectLatest { isAvailable ->
                binding.networkStatusView.visibility = if (isAvailable) View.GONE else View.VISIBLE
                networkStatusView.setNetworkStatus(isAvailable)
                validateMobileInput() // This will update button enabled state
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.contentGroup.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction(R.string.retry) {
                if (validateMobileInput() && networkStateMonitor.isNetworkAvailable()) {
                    val mobile = binding.editTextMobile.text.toString().trim()
                    viewModel.sendPasswordResetOtp(mobile, this)
                }
            }
            .show()
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        networkStateMonitor.stopMonitoring()
    }
}