package com.example.askchinna.ui.auth
/**
 * OtpResendDialogFragment.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */
import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.askchinna.R
import com.askchinna.data.model.UIState
import com.askchinna.databinding.DialogOtpResendBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

/**
 * Dialog fragment for requesting OTP resend to a different mobile number
 */
@AndroidEntryPoint
class OtpResendDialogFragment : DialogFragment() {

    private var _binding: DialogOtpResendBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    private var onOtpSentListener: ((String, String) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogOtpResendBinding.inflate(layoutInflater)

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMobileInputValidation()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupMobileInputValidation() {
        binding.editTextMobile.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val mobile = s.toString().trim()

                // Validate mobile number
                val isValid = mobile.matches(Regex("^[0-9]{10}$"))

                if (mobile.isNotEmpty() && !isValid) {
                    binding.editTextMobile.error = getString(R.string.error_invalid_mobile)
                }

                // Enable send button only if mobile is valid
                binding.buttonSendOtp.isEnabled = isValid
            }
        })
    }

    private fun setupClickListeners() {
        // Send OTP button click
        binding.buttonSendOtp.setOnClickListener {
            val mobile = binding.editTextMobile.text.toString().trim()
            activity?.let {
                viewModel.sendOtp(mobile, it)
            }
        }

        // Close dialog
        binding.imageButtonClose.setOnClickListener {
            dismiss()
        }
    }

    private fun observeViewModel() {
        // Observe OTP send state
        viewModel.otpSendState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UIState.Loading -> {
                    showLoading(true)
                }
                is UIState.Success -> {
                    showLoading(false)
                    // Notify listener and dismiss dialog
                    val mobile = binding.editTextMobile.text.toString().trim()
                    onOtpSentListener?.invoke(mobile, state.data)
                    dismiss()
                }
                is UIState.Error -> {
                    showLoading(false)
                    binding.textViewError.text = state.message
                    binding.textViewError.visibility = View.VISIBLE
                }
                else -> { /* No-op */ }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.contentGroup.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    /**
     * Set listener for OTP sent event
     * @param listener Callback with mobile number and verification ID
     */
    fun setOnOtpSentListener(listener: (String, String) -> Unit) {
        onOtpSentListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "OtpResendDialogFragment"

        fun newInstance(): OtpResendDialogFragment {
            return OtpResendDialogFragment()
        }
    }
}