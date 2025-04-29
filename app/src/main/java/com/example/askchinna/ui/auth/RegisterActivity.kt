package com.example.askchinna.ui.auth

/**
 * app/src/main/java/com/askchinna/ui/auth/RegisterActivity.kt
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
import com.askchinna.databinding.ActivityRegisterBinding
import com.askchinna.util.NetworkStateMonitor
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Activity for user registration using mobile number with OTP verification
 */
@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    @Inject
    lateinit var networkMonitor: NetworkStateMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup UI events
        setupInputValidation()
        setupClickListeners()

        // Observe ViewModel states
        observeViewModel()

        // Setup network monitoring
        setupNetworkMonitoring()
    }

    private fun setupInputValidation() {
        // Validate mobile number
        binding.editTextMobile.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validateInputs()
            }
        })

        // Validate display name
        binding.editTextName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validateInputs()
            }
        })
    }

    private fun validateInputs() {
        val mobile = binding.editTextMobile.text.toString().trim()
        val name = binding.editTextName.text.toString().trim()

        // Validate mobile number (require 10 digits)
        val isMobileValid = mobile.matches(Regex("^[0-9]{10}$"))
        if (mobile.isNotEmpty() && !isMobileValid) {
            binding.editTextMobile.error = getString(R.string.error_invalid_mobile)
        }

        // Validate name (at least 2 characters)
        val isNameValid = name.length >= 2
        if (name.isNotEmpty() && !isNameValid) {
            binding.editTextName.error = getString(R.string.error_invalid_name)
        }

        // Enable register button only if all inputs are valid
        binding.buttonRegister.isEnabled = isMobileValid && isNameValid
    }

    private fun setupClickListeners() {
        // Register button click
        binding.buttonRegister.setOnClickListener {
            val mobile = binding.editTextMobile.text.toString().trim()
            val name = binding.editTextName.text.toString().trim()

            viewModel.registerUser(mobile, name, this)
        }

        // Login text click
        binding.textViewLogin.setOnClickListener {
            finish()
        }

        // Back button click
        binding.imageButtonBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun observeViewModel() {
        // Observe registration state
        viewModel.registrationState.observe(this) { state ->
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
    }

    private fun setupNetworkMonitoring() {
        networkMonitor.isNetworkAvailable.observe(this) { isAvailable ->
            binding.networkStatusView.visibility = if (!isAvailable) View.VISIBLE else View.GONE
            binding.buttonRegister.isEnabled = isAvailable &&
                    binding.editTextMobile.text.toString().matches(Regex("^[0-9]{10}$")) &&
                    binding.editTextName.text.toString().length >= 2
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
            putExtra(OtpVerificationActivity.EXTRA_IS_REGISTRATION, true)
        }
        startActivity(intent)
    }