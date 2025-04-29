package com.example.askchinna.ui.auth

/**
 * app/src/main/java/com/askchinna/ui/auth/LoginActivity.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.askchinna.R
import com.askchinna.data.model.UIState
import com.askchinna.databinding.ActivityLoginBinding
import com.askchinna.ui.home.HomeActivity
import com.askchinna.util.NetworkStateMonitor
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Activity for user login using mobile number and OTP
 */
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var networkMonitor: NetworkStateMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup UI events
        setupMobileInputValidation()
        setupClickListeners()

        // Observe ViewModel states
        observeViewModel()

        // Check network state
        setupNetworkMonitoring()
    }

    private fun setupMobileInputValidation() {
        binding.editTextMobile.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val mobile = s.toString().trim()

                // Validate mobile number (allow only digits and require 10 digits)
                val isValid = mobile.matches(Regex("^[0-9]{10}$"))

                if (mobile.isNotEmpty() && !isValid) {
                    binding.editTextMobile.error = getString(R.string.error_invalid_mobile)
                }

                // Enable login button only if mobile is valid
                binding.buttonLogin.isEnabled = isValid
            }
        })
    }

    private fun setupClickListeners() {
        // Login button click
        binding.buttonLogin.setOnClickListener {
            val mobile = binding.editTextMobile.text.toString().trim()
            viewModel.sendOtp(mobile, this)
        }

        // Register text click
        binding.textViewRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        // Observe OTP send state
        viewModel.otpSendState.observe(this) { state ->
            when (state) {
                is UIState.Loading -> {
                    showLoading(true)
                }
                is UIState.Success -> {
                    showLoading(false)
                    navigateToOtpVerification(state.data)
                }
                is UIState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                else -> { /* No-op */ }
            }
        }

        // Observe auto-login state (for users already logged in)
        viewModel.autoLoginState.observe(this) { autoLogin ->
            if (autoLogin) {
                navigateToHome()
            }
        }
    }

    private fun setupNetworkMonitoring() {
        networkMonitor.isNetworkAvailable.observe(this) { isAvailable ->
            binding.networkStatusView.visibility = if (!isAvailable) View.VISIBLE else View.GONE
            binding.buttonLogin.isEnabled = isAvailable &&
                    binding.editTextMobile.text.toString().matches(Regex("^[0-9]{10}$"))
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.contentGroup.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
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
}