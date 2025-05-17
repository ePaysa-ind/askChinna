package com.example.askchinna.ui.auth

/**
 * app/src/main/java/com/example/askchinna/ui/auth/LoginActivity.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Updated: April 29, 2025
 * Version: 1.2
 */

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.askchinna.R
import com.example.askchinna.data.model.UIState
import com.example.askchinna.databinding.ActivityLoginBinding
import com.example.askchinna.ui.common.NetworkStatusView
import com.example.askchinna.ui.home.HomeActivity
import com.example.askchinna.util.NetworkState
import com.example.askchinna.util.NetworkStateMonitor
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Activity for user login using mobile number and OTP
 */

@AndroidEntryPoint
class LoginActivity : AppCompatActivity(), NetworkStatusView.OnRetryConnectionListener {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var networkMonitor: NetworkStateMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set initial button state
        binding.buttonLogin.isEnabled = false
        
        setupMobileInputValidation()
        setupClickListeners()
        observeViewModel()
        setupNetworkMonitoring()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop network monitoring
        networkMonitor.stopMonitoring()
    }

    private fun setupMobileInputValidation() {
        // Show +91 prefix visually (India only support)
        binding.textInputLayoutMobile.prefixText = "+91"
        
        binding.editTextMobile.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val mobile = s.toString().trim()
                val isValid = mobile.matches(Regex("^[0-9]{10}$"))

                if (mobile.isNotEmpty() && !isValid) {
                    binding.editTextMobile.error = getString(R.string.error_invalid_mobile)
                } else {
                    binding.editTextMobile.error = null
                }

                val isAvailable = networkMonitor.networkState.value != NetworkState.Offline
                binding.buttonLogin.isEnabled = isValid && isAvailable
                
                // Debug logging
                Log.d("LoginActivity", "Mobile: $mobile, Valid: $isValid, Network: $isAvailable, Button enabled: ${isValid && isAvailable}")
            }
        })
    }

    private fun setupClickListeners() {
        binding.buttonLogin.setOnClickListener {
            val mobile = binding.editTextMobile.text.toString().trim()
            val isAvailable = networkMonitor.networkState.value != NetworkState.Offline
            
            Log.d("LoginActivity", "Login click - Mobile: $mobile, Network available: $isAvailable")
            
            if (isAvailable) {
                viewModel.sendOtp(mobile, this)
            } else {
                showError(getString(R.string.error_no_network))
            }
        }

        binding.textViewRegister.setOnClickListener {
            val isAvailable = networkMonitor.networkState.value != NetworkState.Offline
            if (isAvailable) {
                startActivity(Intent(this, RegisterActivity::class.java))
            } else {
                showError(getString(R.string.error_no_network))
            }
        }
    }

    private fun observeViewModel() {
        viewModel.otpSendState.observe(this) { state ->
            when (state) {
                is UIState.Loading -> {
                    showLoading(true)
                    binding.buttonLogin.isEnabled = false
                }
                is UIState.Success -> {
                    showLoading(false)
                    binding.buttonLogin.isEnabled = true
                    navigateToOtpVerification(state.data)
                }
                is UIState.Error -> {
                    showLoading(false)
                    binding.buttonLogin.isEnabled = true
                    when {
                        state.message.contains("network", ignoreCase = true) -> {
                            showError(getString(R.string.error_network_failure))
                        }
                        state.message.contains("invalid", ignoreCase = true) -> {
                            showError(getString(R.string.error_invalid_mobile))
                        }
                        else -> {
                            showError(getString(R.string.error_otp_send_failed))
                        }
                    }
                }
                else -> { /* No-op */ }
            }
        }

        viewModel.autoLoginState.observe(this) { autoLogin ->
            if (autoLogin) {
                navigateToHome()
            }
        }
    }

    private fun setupNetworkMonitoring() {
        networkMonitor.startMonitoring()
        networkMonitor.networkState.observe(this) { state ->
            val isAvailable = state != NetworkState.Offline
            binding.networkStatusView.visibility = if (!isAvailable) View.VISIBLE else View.GONE
            binding.networkStatusView.updateNetwork(state)
            
            val mobile = binding.editTextMobile.text.toString().trim()
            val isValid = mobile.matches(Regex("^[0-9]{10}$"))
            binding.buttonLogin.isEnabled = isAvailable && isValid
            
            // Debug logging
            Log.d("LoginActivity", "Network state: $state, Available: $isAvailable, Mobile valid: $isValid")
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.contentGroup.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        Log.e("LoginActivity", "Error: $message")
        
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction(R.string.retry) {
                if (networkMonitor.networkState.value != NetworkState.Offline) {
                    val mobile = binding.editTextMobile.text.toString().trim()
                    viewModel.sendOtp(mobile, this)
                }
            }
            .show()
    }

    private fun navigateToOtpVerification(verificationId: String) {
        val intent = Intent(this, OtpVerificationActivity::class.java).apply {
            putExtra(OtpVerificationActivity.EXTRA_VERIFICATION_ID, verificationId)
            putExtra(OtpVerificationActivity.EXTRA_MOBILE_NUMBER,
                binding.editTextMobile.text.toString().trim())
            putExtra(OtpVerificationActivity.EXTRA_IS_REGISTRATION, false)
        }
        startActivity(intent)
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onRetryConnection() {
        Log.d("LoginActivity", "Retry connection clicked")
        // Restart network monitoring
        networkMonitor.startMonitoring()
        
        // Force a network check
        lifecycleScope.launch {
            delay(500) // Small delay to allow monitoring to restart
            val currentState = networkMonitor.networkState.value
            Log.d("LoginActivity", "Current network state after retry: $currentState")
            
            if (currentState != NetworkState.Offline) {
                // Network is available, hide the error
                binding.networkStatusView.visibility = View.GONE
                
                // Update button state
                val mobile = binding.editTextMobile.text.toString().trim()
                val isValid = mobile.matches(Regex("^[0-9]{10}$"))
                binding.buttonLogin.isEnabled = isValid
            } else {
                // Still offline
                Toast.makeText(this@LoginActivity, getString(R.string.still_no_connection), Toast.LENGTH_SHORT).show()
            }
        }
    }
}