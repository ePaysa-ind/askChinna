/*
 * file path: app/src/main/java/com/example/askchinna/util/NetworkStateMonitor.kt
 * file name: NetworkStateMonitor.kt
 * created: April 28, 2025
 * updated: May 15, 2025
 * version: 1.6
 * This file is part of AskChinna.
 * Copyright © 2025 askChinna
 */

package com.example.askchinna.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monitors network state changes and quality.
 * Handles network connectivity changes and provides network quality information.
 *
 * @property context Application context for accessing system services
 */
@Singleton
class NetworkStateMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "NetworkStateMonitor"
    private var connectivityManager: ConnectivityManager? = null
    private var isInitialized = false

    private val _networkState = MutableLiveData<NetworkState>()
    val networkState: LiveData<NetworkState> = _networkState

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var monitoringJob: Job? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            try {
                coroutineScope.launch {
                    updateNetworkState(isAvailable = true)
                }
            } catch (e: Exception) {
                handleError("Network available callback error", e)
            }
        }

        override fun onLost(network: Network) {
            try {
                coroutineScope.launch {
                    updateNetworkState(isAvailable = false)
                }
            } catch (e: Exception) {
                handleError("Network lost callback error", e)
            }
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            try {
                coroutineScope.launch {
                    updateNetworkState(isAvailable = true)
                }
            } catch (e: Exception) {
                handleError("Network capabilities changed callback error", e)
            }
        }
    }

    private var isMonitoring = false

    init {
        try {
            connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            isInitialized = true
        } catch (e: Exception) {
            handleError("Failed to initialize NetworkStateMonitor", e)
        }
    }

    /**
     * Start monitoring network state changes.
     * This method is idempotent - calling it multiple times has no effect.
     *
     * @throws NetworkMonitorException if there's an error registering the network callback
     */
    fun startMonitoring() {
        if (!isInitialized) {
            handleError("NetworkStateMonitor not initialized", IllegalStateException())
            return
        }

        if (isMonitoring) return

        try {
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager?.registerNetworkCallback(networkRequest, networkCallback)
            isMonitoring = true

            // Start periodic network quality checks
            monitoringJob = coroutineScope.launch {
                try {
                    while (isActive) {
                        updateNetworkState(isAvailable = isNetworkConnected())
                        delay(NETWORK_CHECK_INTERVAL)
                    }
                } catch (e: CancellationException) {
                    // Job was cancelled, this is expected
                    Log.d(TAG, "Network monitoring job cancelled")
                } catch (e: Exception) {
                    if (e !is CancellationException) {
                        handleError("Network check error", e)
                    }
                }
            }

            // Initial check
            updateNetworkState(isAvailable = isNetworkConnected())
        } catch (e: Exception) {
            handleError("Error registering network callback", e)
            throw NetworkMonitorException(
                "Failed to start network monitoring: ${e.message}",
                NetworkMonitorError.REGISTRATION_FAILED,
                e
            )
        }
    }

    /**
     * Stop monitoring network state changes.
     * This method is idempotent - calling it multiple times has no effect.
     *
     * @throws NetworkMonitorException if there's an error unregistering the network callback
     */
    fun stopMonitoring() {
        if (!isInitialized) {
            handleError("NetworkStateMonitor not initialized", IllegalStateException())
            return
        }

        if (!isMonitoring) return

        try {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
            isMonitoring = false

            // Cancel periodic checks
            monitoringJob?.cancel()
            monitoringJob = null

            coroutineScope.launch {
                _networkState.postValue(NetworkState.Offline)
            }
        } catch (e: Exception) {
            handleError("Error unregistering network callback", e)
            throw NetworkMonitorException(
                "Failed to stop network monitoring: ${e.message}",
                NetworkMonitorError.UNREGISTRATION_FAILED,
                e
            )
        }
    }

    /**
     * Update network state based on current conditions.
     */
    private fun updateNetworkState(isAvailable: Boolean) {
        try {
            if (!isAvailable) {
                _networkState.postValue(NetworkState.Offline)
                return
            }

            val network = connectivityManager?.activeNetwork ?: run {
                _networkState.postValue(NetworkState.Offline)
                return
            }
            val capabilities = connectivityManager?.getNetworkCapabilities(network) ?: run {
                _networkState.postValue(NetworkState.Offline)
                return
            }

            val state = when {
                !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ->
                    NetworkState.Offline
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->
                    NetworkState.WiFi
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->
                    if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED))
                        NetworkState.MobileData
                    else
                        NetworkState.MeteredMobileData
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ->
                    NetworkState.Ethernet
                else -> NetworkState.Unknown
            }

            _networkState.postValue(state)
        } catch (e: Exception) {
            handleError("Error updating network state", e)
            _networkState.postValue(NetworkState.Offline)
        }
    }

    /**
     * Check if network is currently connected.
     * @return true if internet is available
     */
    private fun isNetworkConnected(): Boolean {
        return try {
            if (!isInitialized) {
                handleError("NetworkStateMonitor not initialized", IllegalStateException())
                return false
            }

            val network = connectivityManager?.activeNetwork ?: return false
            val caps = connectivityManager?.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            handleError("Error checking network connection", e)
            false
        }
    }

    /**
     * Handle errors in the network monitor
     * Logs the error and updates state if needed
     */
    private fun handleError(message: String, e: Exception) {
        Log.e(TAG, message, e)
        try {
            _networkState.postValue(NetworkState.Offline)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update error state", e)
        }
    }

    /**
     * Clean up resources when the monitor is no longer needed
     */
    fun cleanup() {
        try {
            stopMonitoring()
            coroutineScope.cancel()
            connectivityManager = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }

    /**
     * Returns a Flow of network connectivity state.
     *
     * @return Flow of boolean indicating network connectivity
     */
    fun observe(): Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            private val networks = mutableSetOf<Network>()

            override fun onAvailable(network: Network) {
                try {
                    networks.add(network)
                    trySend(true)
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling network available", e)
                }
            }

            override fun onLost(network: Network) {
                try {
                    networks.remove(network)
                    if (networks.isEmpty()) {
                        trySend(false)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling network lost", e)
                }
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                try {
                    val internet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    trySend(internet)
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling network capabilities change", e)
                }
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            connectivityManager.registerNetworkCallback(networkRequest, callback)
        } catch (e: Exception) {
            Log.e(TAG, "Error registering network callback", e)
        }

        // Set initial value
        val currentState = connectivityManager.activeNetwork?.let { network ->
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } ?: false
        trySend(currentState)

        awaitClose {
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering network callback", e)
            }
        }
    }.distinctUntilChanged()

    /**
     * Checks if the device is currently connected to the internet.
     * Checks for WiFi, cellular, or Ethernet connectivity.
     *
     * @return true if connected, false otherwise
     */
    fun isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            // Check for any internet connectivity (WiFi, cellular, or Ethernet)
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    )
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network availability", e)
            false
        }
    }

    companion object {
        private const val NETWORK_CHECK_INTERVAL = 30000L // 30 seconds
    }
}

/**
 * Represents the current network state.
 */
sealed class NetworkState {
    data object Offline : NetworkState()
    data object WiFi : NetworkState()
    data object MobileData : NetworkState()
    data object MeteredMobileData : NetworkState()
    data object Ethernet : NetworkState()
    data object Unknown : NetworkState()
}

/**
 * Specific error types for network monitoring.
 */
enum class NetworkMonitorError {
    REGISTRATION_FAILED,
    UNREGISTRATION_FAILED,
}

/**
 * Exception thrown when there's an error in network monitoring.
 */
class NetworkMonitorException(
    message: String,
    val error: NetworkMonitorError,
    cause: Throwable? = null
) : Exception(message, cause)