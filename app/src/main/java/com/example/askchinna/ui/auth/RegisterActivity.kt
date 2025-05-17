package com.example.askchinna.ui.auth

/**
 * app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Updated: April 29, 2025
 * Version: 1.1
 */

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.askchinna.R
import com.example.askchinna.data.model.UIState
import com.example.askchinna.databinding.ActivityRegisterBinding
import com.example.askchinna.ui.common.NetworkStatusView
import com.example.askchinna.util.NetworkStateMonitor
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Activity for user registration using mobile number with OTP verification
 */
@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    @Inject
    lateinit var networkStateMonitor: NetworkStateMonitor

    private lateinit var nameInput: EditText
    private lateinit var mobileInput: EditText
    private lateinit var registerButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var networkStatusView: NetworkStatusView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeViews()
        setupNetworkMonitoring()
        setupInputValidation()
        setupClickListeners()
        observeViewModel()
    }

    private fun initializeViews() {
        nameInput = binding.editTextName
        mobileInput = binding.editTextMobile
        registerButton = binding.buttonRegister
        progressBar = binding.progressBar
        // Initialize network status view
        networkStatusView = binding.networkStatusView
    }

    private fun setupNetworkMonitoring() {
        networkStateMonitor.startMonitoring()
        lifecycleScope.launch {
            networkStateMonitor.observe().collectLatest { isAvailable ->
                // Set the network status view visibility based on network availability
                binding.networkStatusView.visibility = if (isAvailable) View.GONE else View.VISIBLE
                // Update the network status view
                networkStatusView.setNetworkStatus(isAvailable)
                // Enable/disable the register button based on network availability
                binding.buttonRegister.isEnabled = isAvailable
            }
        }
    }

    private fun setupInputValidation() {
        binding.editTextMobile.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateMobileInput()
            }
        }

        binding.editTextName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateInputs()
            }
        })
    }

    private fun validateMobileInput(): Boolean {
        val mobile = binding.editTextMobile.text.toString()
        return if (mobile.length != 10) {
            binding.editTextMobile.error = getString(R.string.error_invalid_mobile)
            false
        } else {
            binding.editTextMobile.error = null
            true
        }
    }

    private fun validateInputs() {
        val mobile = binding.editTextMobile.text.toString().trim()
        val name = binding.editTextName.text.toString().trim()

        val isMobileValid = mobile.matches(Regex("^[0-9]{10}$"))
        if (mobile.isNotEmpty() && !isMobileValid) {
            binding.editTextMobile.error = getString(R.string.error_invalid_mobile)
        }

        val isNameValid = name.length >= 2
        if (name.isNotEmpty() && !isNameValid) {
            binding.editTextName.error = getString(R.string.error_invalid_name)
        }

        // Only enable button if network is available and inputs are valid
        binding.buttonRegister.isEnabled = isMobileValid && isNameValid && networkStateMonitor.isNetworkAvailable()
    }

    private fun setupClickListeners() {
        binding.buttonRegister.setOnClickListener {
            if (!networkStateMonitor.isNetworkAvailable()) {
                showError(getString(R.string.error_no_network))
                return@setOnClickListener
            }

            if (validateMobileInput()) {
                val name = binding.editTextName.text.toString()
                val mobile = binding.editTextMobile.text.toString()
                viewModel.registerUser(mobile, name, this)
            }
        }

        binding.textViewLogin.setOnClickListener {
            finish()
        }

        binding.imageButtonBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun observeViewModel() {
        viewModel.registrationState.observe(this) { state ->
            when (state) {
                is UIState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.buttonRegister.isEnabled = false
                }
                is UIState.Success<*> -> {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonRegister.isEnabled = true
                    finish()
                }
                is UIState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonRegister.isEnabled = true
                    showError(state.message)
                }
                else -> {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonRegister.isEnabled = true
                }
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction(R.string.retry) {
                if (validateMobileInput()) {
                    val name = binding.editTextName.text.toString()
                    val mobile = binding.editTextMobile.text.toString()
                    viewModel.registerUser(mobile, name, this)
                }
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        networkStateMonitor.stopMonitoring()
    }
}