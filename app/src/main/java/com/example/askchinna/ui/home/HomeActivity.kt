package com.example.askchinna.ui.home

/**
 * app/src/main/java/com/askchinna/ui/home/HomeActivity.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */


import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.askchinna.R
import com.askchinna.data.model.UIState
import com.askchinna.data.model.UsageLimit
import com.askchinna.databinding.ActivityHomeBinding
import com.askchinna.ui.auth.LoginActivity
import com.askchinna.ui.cropselection.CropSelectionActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Home screen activity
 */
@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViews()
        observeViewModel()

        // Start session timer
        viewModel.startSessionTimer()
    }

    /**
     * Set up toolbar
     */
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    /**
     * Set up views and click listeners
     */
    private fun setupViews() {
        // Start identification button
        binding.btnStartIdentification.setOnClickListener {
            if (viewModel.canPerformIdentification()) {
                startIdentificationProcess()
            } else {
                showUsageLimitReachedDialog()
            }
        }

        // Help button
        binding.btnHelp.setOnClickListener {
            // Show help dialog or navigate to help screen
        }
    }

    /**
     * Observe ViewModel LiveData
     */
    private fun observeViewModel() {
        // User data
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
                    binding.tvUserName.text = user.name
                    binding.tvUserRole.text = user.role.capitalize()
                }
                is UIState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }

        // Usage limit
        viewModel.usageLimit.observe(this) { state ->
            when (state) {
                is UIState.Loading -> {
                    binding.usageLimitView.showLoading(true)
                }
                is UIState.Success -> {
                    binding.usageLimitView.showLoading(false)
                    updateUsageLimitView(state.data)
                }
                is UIState.Error -> {
                    binding.usageLimitView.showLoading(false)
                    binding.usageLimitView.showError(state.message)
                }
                else -> {}
            }
        }

        // Session timer
        viewModel.sessionTimeRemaining.observe(this) { timeRemaining ->
            val formattedTime = viewModel.getFormattedTimeRemaining()
            val percentage = viewModel.getRemainingTimePercentage()

            binding.sessionTimerView.updateTimer(formattedTime, percentage)

            // Show warning if session is about to expire
            if (viewModel.isSessionAboutToExpire()) {
                binding.sessionTimerView.showExpirationWarning(true)
            } else {
                binding.sessionTimerView.showExpirationWarning(false)
            }
        }

        // Session expiration
        viewModel.isSessionExpired.observe(this) { isExpired ->
            if (isExpired) {
                showSessionExpiredDialog()
            }
        }
    }

    /**
     * Update usage limit view with data
     * @param usageLimit Usage limit data
     */
    private fun updateUsageLimitView(usageLimit: UsageLimit) {
        binding.usageLimitView.setUsageLimit(
            currentCount = usageLimit.currentCount,
            remainingCount = usageLimit.remainingCount,
            maxCount = 5,
            isPremium = usageLimit.role != "free"
        )

        // Enable/disable start button based on usage limit
        binding.btnStartIdentification.isEnabled = !usageLimit.isLimitReached
    }

    /**
     * Start the identification process by navigating to crop selection
     */
    private fun startIdentificationProcess() {
        // Increment usage count
        viewModel.incrementUsageCount()

        // Navigate to crop selection screen
        val intent = Intent(this, CropSelectionActivity::class.java)
        startActivity(intent)
    }

    /**
     * Show dialog when session expires
     */
    private fun showSessionExpiredDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.session_expired_title)
            .setMessage(R.string.session_expired_message)
            .setPositiveButton(R.string.start_new_session) { _, _ ->
                viewModel.resetSessionTimer()
                viewModel.startSessionTimer()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Show dialog when usage limit is reached
     */
    private fun showUsageLimitReachedDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.usage_limit_reached_title)
            .setMessage(R.string.usage_limit_reached_message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    /**
     * Create options menu
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    /**
     * Handle options menu item selection
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Logout user and navigate to login screen
     */
    private fun logout() {
        viewModel.logout()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Pause session timer when activity is paused
     */
    override fun onPause() {
        super.onPause()
        viewModel.pauseSessionTimer()
    }

    /**
     * Resume session timer when activity is resumed
     */
    override fun onResume() {
        super.onResume()
        viewModel.startSessionTimer()

        // Refresh data
        viewModel.loadUserData()
        viewModel.checkUsageLimit()
    }
}