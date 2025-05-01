package com.example.askchinna.ui.auth
/**
 * OtpVerificationActivity.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.askchinna.R
import com.askchinna.data.model.UIState
import com.askchinna.databinding.ActivityOtpVerificationBinding
import com.askchinna.ui.home.HomeActivity
import com.askchinna.util.NetworkStateMonitor
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Activity for verifying OTP sent to user's mobile number
 */
@AndroidEntryPoint
class OtpVerificationActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_VERIFICATION_ID = "extra_verification_id"
        const val EXTRA_MOBILE_NUMBER = "extra_mobile_number"
        const val EXTRA_IS_REGISTRATION = "extra_is_registration"
        private const val OTP_RESEND_TIMEOUT_MS = 60000L // 60 seconds
    }

    private lateinit var binding: ActivityOtpVerificationBinding
    private val viewModel: OtpVerificationViewModel by viewModels()
    private var countDownTimer: CountDownTimer? = null

    @Inject
    lateinit var networkMonitor: NetworkStateMonitor

    private var mobileNumber: String = ""
    private var isRegistration: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from intent
        intent.extras?.let { extras ->
            viewModel.setVerificationId(extras.getString(EXTRA_VERIFICATION_ID, ""))
            mobileNumber = extras.getString(EXTRA_MOBILE_NUMBER, "")
            isRegistration = extras.getBoolean(EXTRA_IS_REGISTRATION, false)
        }

        // Set mobile number in UI
        binding.textViewMobileNumber.text = getString(R.string.otp_sent_to, mobileNumber)

        // Setup OTP input validation
        setupOtpInputValidation()

        // Setup click listeners
        setupClickListeners()

        // Start resend timer
        startResendTimer()

        // Observe ViewModel states
        observeViewModel()

        // Setup network monitoring
        setupNetworkMonitoring()
    }

    private fun setupOtpInputValidation() {
        binding.editTextOtp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val otp = s.toString().trim()

                // Enable verify button only if OTP has 6 digits
                val isValid = otp.length == 6 && otp.matches(Regex("[0-9]{6}"))
                binding.buttonVerify.isEnabled = isValid
            }
        })
    }

    private fun setupClickListeners() {
        // Verify button click
        binding.buttonVerify.setOnClickListener {
            val otp = binding.editTextOtp.text.toString().trim()

            if (isRegistration) {
                viewModel.completeRegistration(otp)
            } else {
                viewModel.verifyOtp(otp)
            }
        }

        // Resend OTP click
        binding.textViewResendOtp.setOnClickListener {
            if (binding.textViewResendOtp.isEnabled) {
                viewModel.resendOtp(mobileNumber, this)
                startResendTimer()
            }
        }

        // Back button click
        binding.imageButtonBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun startResendTimer() {
        // Disable resend button
        binding.textViewResendOtp.isEnabled = false

        // Cancel existing timer if any
        countDownTimer?.cancel()

        // Start countdown timer for resend OTP
        countDownTimer = object : CountDownTimer(OTP_RESEND_TIMEOUT_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.textViewResendOtp.text = getString(R.string.resend_otp_countdown, seconds)
            }

            override fun onFinish() {
                binding.textViewResendOtp.text = getString(R.string.resend_otp)
                binding.textViewResendOtp.isEnabled = true
            }
        }.start()
    }

    private fun observeViewModel() {
        // Observe OTP verification state
        viewModel.verificationState.observe(this) { state ->
            when (state) {
                is UIState.Loading -> {
                    showLoading(true)
                }
                is UIState.Success -> {
                    showLoading(false)
                    navigateToHome()
                }
                is UIState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                else -> { /* No-op */ }
            }
        }

        // Observe OTP resend state
        viewModel.resendState.observe(this) { state ->
            when (state) {
                is UIState.Loading -> {
                    // Already handled by resend timer
                }
                is UIState.Success -> {
                    showMessage(getString(R.string.otp_resent_success))
                }
                is UIState.Error -> {
                    showError(state.message)
                }
                else -> { /* No-op */ }
            }
        }
    }

    private fun setupNetworkMonitoring() {
        networkMonitor.isNetworkAvailable.observe(this) { isAvailable ->
            binding.networkStatusView.visibility = if (!isAvailable) View.VISIBLE else View.GONE
            binding.buttonVerify.isEnabled = isAvailable &&
                    binding.editTextOtp.text.toString().length == 6
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.contentGroup.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}