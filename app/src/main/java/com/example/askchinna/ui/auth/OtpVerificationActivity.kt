package com.example.askchinna.ui.auth
/**
 * OtpVerificationActivity.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Updated: April 29, 2025
 * Version: 1.2
 */

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.askchinna.R
import com.example.askchinna.data.model.UIState
import com.example.askchinna.databinding.ActivityOtpVerificationBinding
import com.example.askchinna.ui.home.HomeActivity
import com.example.askchinna.util.NetworkStateMonitor
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
    lateinit var networkStateMonitor: NetworkStateMonitor

    private var mobileNumber: String = ""
    private var isRegistration: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBackButtonHandling()
        initializeData()
        setupOtpInputValidation()
        setupClickListeners()
        startResendTimer()
        observeViewModel()
        setupNetworkMonitoring()
    }

    private fun initializeData() {
        intent.extras?.let { extras ->
            viewModel.setVerificationId(extras.getString(EXTRA_VERIFICATION_ID, ""))
            mobileNumber = extras.getString(EXTRA_MOBILE_NUMBER, "")
            isRegistration = extras.getBoolean(EXTRA_IS_REGISTRATION, false)
            binding.textViewMobileNumber.text = getString(R.string.otp_sent_to, mobileNumber)
        } ?: run {
            showError(getString(R.string.error_invalid_data))
            finish()
        }
    }

    private fun setupBackButtonHandling() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun setupOtpInputValidation() {
        binding.editTextOtp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val otp = s.toString().trim()
                val isValid = otp.length == 6 && otp.matches(Regex("[0-9]{6}"))
                val isAvailable = networkStateMonitor.isNetworkAvailable()
                binding.buttonVerify.isEnabled = isValid && isAvailable
            }
        })
    }

    private fun setupClickListeners() {
        binding.buttonVerify.setOnClickListener {
            val isAvailable = networkStateMonitor.isNetworkAvailable()
            if (!isAvailable) {
                showError(getString(R.string.error_no_network))
                return@setOnClickListener
            }
            val otp = binding.editTextOtp.text.toString().trim()
            if (isRegistration) {
                viewModel.completeRegistration(otp)
            } else {
                viewModel.verifyOtp(otp)
            }
        }

        binding.textViewResendOtp.setOnClickListener {
            val isAvailable = networkStateMonitor.isNetworkAvailable()
            if (!isAvailable) {
                showError(getString(R.string.error_no_network))
                return@setOnClickListener
            }
            if (binding.textViewResendOtp.isEnabled) {
                viewModel.resendOtp(mobileNumber, this@OtpVerificationActivity)
                startResendTimer()
            }
        }

        binding.imageButtonBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun startResendTimer() {
        binding.textViewResendOtp.isEnabled = false
        countDownTimer?.cancel()

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
        viewModel.verificationState.observe(this) { state ->
            when (state) {
                is UIState.Loading -> {
                    showLoading(true)
                }
                is UIState.Success<*> -> {
                    showLoading(false)
                    navigateToHome()
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

        viewModel.resendState.observe(this) { state ->
            when (state) {
                is UIState.Success<*> -> {
                    showMessage(getString(R.string.otp_resent_success))
                }
                is UIState.Error -> {
                    showError(state.message)
                }
                else -> {}
            }
        }
    }

    private fun setupNetworkMonitoring() {
        networkStateMonitor.startMonitoring()
        val isAvailable = networkStateMonitor.isNetworkAvailable()
        binding.networkStatusView.visibility = if (isAvailable) View.GONE else View.VISIBLE
        binding.buttonVerify.isEnabled = isAvailable && binding.editTextOtp.text.toString().length == 6
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.contentGroup.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction(R.string.retry) {
                val isAvailable = networkStateMonitor.isNetworkAvailable()
                if (isAvailable) {
                    val otp = binding.editTextOtp.text.toString().trim()
                    if (isRegistration) {
                        viewModel.completeRegistration(otp)
                    } else {
                        viewModel.verifyOtp(otp)
                    }
                }
            }
            .show()
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
        networkStateMonitor.stopMonitoring()
    }
}