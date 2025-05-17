/**
 * File: app/src/main/java/com/example/askchinna/MainActivity.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Updated: May 15, 2025
 * Version: 1.4
 * Description: Main activity handling app navigation and auth state - converted to View Binding
 */

package com.example.askchinna

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.askchinna.data.local.SharedPreferencesManager
import com.example.askchinna.databinding.ActivityMainBinding
import com.example.askchinna.ui.auth.LoginActivity
import com.example.askchinna.util.NetworkState
import com.example.askchinna.util.NetworkStateMonitor
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    @Inject
    lateinit var networkStateMonitor: NetworkStateMonitor

    @Inject
    lateinit var
            sharedPreferencesManager: SharedPreferencesManager

    private var isInitialized = false
    private var initializationError: String? = null
    private var isAuthenticated = false
    
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate()
        val splashScreen = installSplashScreen()

        // Keep splash screen visible until app is ready
        splashScreen.setKeepOnScreenCondition {
            !isInitialized
        }

        super.onCreate(savedInstanceState)
        
        // Set up View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize on background thread to improve UI responsiveness
        lifecycleScope.launch {
            initializeComponentsAsync()
        }
        
        // Set up retry button
        binding.retryButton.setOnClickListener {
            lifecycleScope.launch {
                initializeComponentsAsync()
            }
        }
    }

    private suspend fun initializeComponentsAsync() {
        try {
            // Perform initialization on IO thread
            withContext(Dispatchers.IO) {
                // Start network monitoring
                networkStateMonitor.startMonitoring()
                
                // Initialize shared preferences with error handling
                try {
                    sharedPreferencesManager.initialize()
                } catch (e: SecurityException) {
                    Log.e("MainActivity", "Failed to initialize encrypted preferences", e)
                    // Continue without encryption if it fails
                }
            }

            // Mark initialization as complete on main thread
            withContext(Dispatchers.Main) {
                isInitialized = true
                initializationError = null
                updateUI()
                
                // Observe network state
                networkStateMonitor.networkState.observe(this@MainActivity) { state ->
                    updateNetworkStatus(state)
                }
            }
            
            // Check authentication state on IO thread
            withContext(Dispatchers.IO) {
                checkAuthState()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                initializationError = when (e) {
                    is SecurityException -> "Security error: ${e.message}"
                    is IllegalStateException -> "Initialization error: ${e.message}"
                    else -> "Failed to initialize app: ${e.message}"
                }
                // Set initialized to true even on error to prevent infinite splash screen
                isInitialized = true
                updateUI()
            }
        }
    }

    private fun checkAuthState() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        isAuthenticated = currentUser != null

        // If not authenticated, redirect to login
        if (!isAuthenticated && isInitialized) {
            lifecycleScope.launch {
                // Navigate on main thread
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    navigateToLogin()
                }
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Close MainActivity so user can't go back
    }

    private fun updateUI() {
        if (initializationError != null) {
            // Show error view
            binding.progressBar.visibility = View.GONE
            binding.errorView.visibility = View.VISIBLE
            binding.contentContainer.visibility = View.GONE
            binding.errorMessage.text = initializationError
        } else if (!isAuthenticated) {
            // Keep showing loading while redirecting to login
            binding.progressBar.visibility = View.VISIBLE
            binding.errorView.visibility = View.GONE
            binding.contentContainer.visibility = View.GONE
        } else {
            // Show main content (not used currently as we redirect to Home)
            binding.progressBar.visibility = View.GONE
            binding.errorView.visibility = View.GONE
            binding.contentContainer.visibility = View.VISIBLE
        }
    }
    
    private fun updateNetworkStatus(state: NetworkState) {
        Log.d(TAG, "Network state changed: $state")
        val isOffline = state == NetworkState.Offline
        binding.networkStatusBar.visibility = if (isOffline) View.VISIBLE else View.GONE
        binding.networkStatusBar.text = when (state) {
            NetworkState.Offline -> "No internet connection"
            NetworkState.WiFi -> "Connected via WiFi"
            NetworkState.MobileData -> "Connected via Mobile Data"
            NetworkState.MeteredMobileData -> "Connected via Metered Mobile Data"
            NetworkState.Ethernet -> "Connected via Ethernet"
            else -> "Connected"
        }
        binding.networkStatusBar.setBackgroundColor(
            if (isOffline) getColor(android.R.color.holo_red_light)
            else getColor(android.R.color.holo_green_light)
        )
    }

    override fun onStart() {
        super.onStart()
        // Check auth state when activity resumes in case it changed
        if (isInitialized) {
            checkAuthState()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup resources
        networkStateMonitor.stopMonitoring()
        sharedPreferencesManager.cleanup()
    }
}