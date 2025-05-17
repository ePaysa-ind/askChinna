/**
 * file path: app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt
 * Copyright (c) 2025 askChinna
 * Created: April 28, 2025
 * Updated: May 6, 2025
 * Version: 1.3
 */

package com.example.askchinna.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.askchinna.R
import com.example.askchinna.data.model.UIState
import com.example.askchinna.data.model.UsageLimit
import com.example.askchinna.databinding.ActivityHomeBinding
import com.example.askchinna.ui.auth.LoginActivity
import com.example.askchinna.ui.cropselection.CropSelectionActivity
import com.example.askchinna.util.Constants
import com.example.askchinna.util.NetworkStateMonitor
import com.example.askchinna.data.remote.FirestoreInitializer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var networkStateMonitor: NetworkStateMonitor
    
    @Inject
    lateinit var firestoreInitializer: FirestoreInitializer

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleError("Coroutine error", throwable)
    }

    private var currentDialog: AlertDialog? = null
    private var retryCount = 0
    private val maxRetries = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityHomeBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setupToolbar()
            setupViews()
            setupNetworkMonitoring()
            observeViewModel()
            
            // Initialize Firestore after successful authentication
            initializeFirestore()

            if (savedInstanceState == null) {
                viewModel.startSessionTimer()
            } else {
                restoreState(savedInstanceState)
            }
        } catch (e: Exception) {
            handleError("Failed to initialize HomeActivity", e)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        try {
            super.onSaveInstanceState(outState)
            outState.putInt("retryCount", retryCount)
        } catch (e: Exception) {
            handleError("Failed to save instance state", e)
        }
    }

    private fun setupToolbar() {
        try {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.title = getString(R.string.app_name)
        } catch (e: Exception) {
            handleError("Failed to setup toolbar", e)
        }
    }

    private fun setupViews() {
        try {
            binding.sessionTimerView.setSessionTimeoutMinutes(Constants.SESSION_TIMEOUT_MINUTES.toInt())
            binding.sessionTimerView.setOnSessionExpiredListener {
                viewModel.handleSessionExpired()
            }

            binding.btnStartIdentification.setOnClickListener {
                startIdentificationProcess()
            }
            binding.btnHelp.setOnClickListener {
                showHelpDialog()
            }
        } catch (e: Exception) {
            handleError("Failed to setup views", e)
        }
    }

    private fun setupNetworkMonitoring() {
        lifecycleScope.launch(exceptionHandler) {
            networkStateMonitor.observe().collectLatest { isAvailable ->
                try {
                    binding.btnStartIdentification.isEnabled = isAvailable && viewModel.canPerformIdentification()
                    if (!isAvailable) {
                        showNoNetworkDialog()
                    } else {
                        retryCount = 0
                        viewModel.loadUserData()
                        viewModel.checkUsageLimit()
                    }
                } catch (e: Exception) {
                    handleError("Network state change error", e)
                }
            }
        }
    }

    private fun observeViewModel() {
        try {
            // --- User data ---
            viewModel.userData.observe(this) { state ->
                when (state) {
                    is UIState.Loading -> {
                        binding.layoutUserInfo.visibility = View.GONE
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UIState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.layoutUserInfo.visibility = View.VISIBLE

                        val user = state.data
                        binding.tvUserName.text = user.displayName
                        binding.tvUserRole.visibility = View.GONE
                        retryCount = 0
                    }
                    is UIState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        showError(state.message)
                        retryUserDataLoad()
                    }
                    else -> { /* no-op */ }
                }
            }

            // --- Usage limit ---
            viewModel.usageLimit.observe(this) { state ->
                when (state) {
                    is UIState.Loading -> {
                        binding.usageLimitView.showLoading(true)
                    }
                    is UIState.Success -> {
                        binding.usageLimitView.showLoading(false)
                        updateUsageLimitView(state.data)
                        retryCount = 0
                    }
                    is UIState.Error -> {
                        binding.usageLimitView.showLoading(false)
                        binding.usageLimitView.showError(state.message)
                        retryUsageLimitCheck()
                    }
                    else -> { /* no-op */ }
                }
            }

            // --- Session timer ---
            viewModel.sessionTimeRemaining.observe(this) {
                try {
                    val formattedTime = viewModel.getFormattedTimeRemaining()
                    val percentage = viewModel.getRemainingTimePercentage()

                    binding.sessionTimerView.updateTimer(formattedTime, percentage)
                    binding.sessionTimerView.showExpirationWarning(viewModel.isSessionAboutToExpire())
                } catch (e: Exception) {
                    handleError("Failed to update session timer", e)
                }
            }

            viewModel.isSessionExpired.observe(this) { expired ->
                if (expired) showSessionExpiredDialog()
            }
        } catch (e: Exception) {
            handleError("Failed to observe ViewModel", e)
        }
    }

    private fun updateUsageLimitView(limit: UsageLimit) {
        try {
            val maxCount = Constants.MAX_IDENTIFICATIONS_PER_MONTH
            val currentCount = limit.usageCount
            val remainingCount = maxCount - currentCount
            val isPremium = false // or derive from your own business logic

            binding.usageLimitView.setUsageLimit(
                currentCount = currentCount,
                remainingCount = remainingCount,
                maxCount = maxCount,
                isPremium = isPremium
            )
        } catch (e: Exception) {
            handleError("Failed to update usage limit view", e)
        }
    }

    private fun startIdentificationProcess() {
        try {
            viewModel.incrementUsageCount()
            startActivity(Intent(this, CropSelectionActivity::class.java))
        } catch (e: Exception) {
            handleError("Failed to start identification process", e)
        }
    }

    private fun showSessionExpiredDialog() {
        try {
            dismissCurrentDialog()
            currentDialog = AlertDialog.Builder(this)
                .setTitle(R.string.session_expired_title)
                .setMessage(R.string.session_expired_message)
                .setPositiveButton(R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                    finish()
                }
                .setCancelable(false)
                .create()
                .also { it.show() }
        } catch (e: Exception) {
            handleError("Failed to show session expired dialog", e)
        }
    }

    private fun showNoNetworkDialog() {
        try {
            dismissCurrentDialog()
            currentDialog = AlertDialog.Builder(this)
                .setTitle(R.string.no_network_title)
                .setMessage(R.string.no_network_message)
                .setPositiveButton(R.string.ok, null)
                .create()
                .also { it.show() }
        } catch (e: Exception) {
            handleError("Failed to show no network dialog", e)
        }
    }

    private fun dismissCurrentDialog() {
        try {
            currentDialog?.dismiss()
            currentDialog = null
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error dismissing dialog", e)
        }
    }

    private fun retryUserDataLoad() {
        if (retryCount < maxRetries) {
            retryCount++
            Log.d("HomeActivity", "Retrying user data load. Attempt $retryCount of $maxRetries")
            viewModel.loadUserData()
        } else {
            retryCount = 0
            showError("Failed to load user data after $maxRetries attempts")
        }
    }

    private fun retryUsageLimitCheck() {
        if (retryCount < maxRetries) {
            retryCount++
            Log.d("HomeActivity", "Retrying usage limit check. Attempt $retryCount of $maxRetries")
            viewModel.checkUsageLimit()
        } else {
            retryCount = 0
            showError("Failed to check usage limit after $maxRetries attempts")
        }
    }

    private fun showError(message: String) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error showing error message", e)
        }
    }

    private fun handleError(message: String, error: Throwable) {
        Log.e("HomeActivity", message, error)
        showError(message)
    }
    
    private fun initializeFirestore() {
        lifecycleScope.launch(exceptionHandler) {
            try {
                Log.d("HomeActivity", "Initializing Firestore collections")
                val success = firestoreInitializer.initializeCollections()
                if (success) {
                    Log.d("HomeActivity", "Firestore collections initialized successfully")
                } else {
                    Log.w("HomeActivity", "Failed to initialize Firestore collections")
                }
            } catch (e: Exception) {
                Log.e("HomeActivity", "Error initializing Firestore", e)
                // Continue app operation even if Firestore init fails
            }
        }
    }

    override fun onDestroy() {
        try {
            dismissCurrentDialog()
            super.onDestroy()
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error in onDestroy", e)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu) =
        menuInflater.inflate(R.menu.menu_home, menu).let { true }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.menu_logout -> {
                try {
                    viewModel.logout()
                    Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(this)
                    }
                    true
                } catch (e: Exception) {
                    handleError("Failed to logout", e)
                    false
                }
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onPause() {
        super.onPause()
        viewModel.pauseSessionTimer()
    }

    override fun onResume() {
        super.onResume()
        viewModel.startSessionTimer()
        viewModel.loadUserData()
        viewModel.checkUsageLimit()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        try {
            retryCount = savedInstanceState.getInt("retryCount", 0)
        } catch (e: Exception) {
            handleError("Failed to restore state", e)
        }
    }

    private fun showHelpDialog() {
        try {
            AlertDialog.Builder(this)
                .setTitle(R.string.help_title)
                .setMessage(R.string.help_message)
                .setPositiveButton(R.string.ok, null)
                .show()
        } catch (e: Exception) {
            handleError("Failed to show help dialog", e)
        }
    }

    private fun restoreState(savedInstanceState: Bundle) {
        try {
            retryCount = savedInstanceState.getInt("retryCount", 0)
            viewModel.startSessionTimer()
        } catch (e: Exception) {
            handleError("Failed to restore state", e)
        }
    }
}