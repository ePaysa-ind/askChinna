/*
 * file path: app/src/main/java/com/example/askchinna/util/NetworkStateMonitor.kt
 * file name: NetworkStateMonitor.kt
 * created: April 28, 2025
 * version: 1.2
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
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monitors network state changes and quality.
 */
@Singleton
class NetworkStateMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "NetworkStateMonitor"
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isNetworkAvailable = MutableLiveData<Boolean>()
    val isNetworkAvailable: LiveData<Boolean> = _isNetworkAvailable

    private val _isLowQualityNetwork = MutableLiveData<Boolean>()
    val isLowQualityNetwork: LiveData<Boolean> = _isLowQualityNetwork

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isNetworkAvailable.postValue(true)
            checkNetworkQuality()
        }

        override fun onLost(network: Network) {
            _isNetworkAvailable.postValue(false)
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            checkNetworkQuality()
        }
    }

    private var isMonitoring = false

    init {
        // Initialize network state
        val connected = isNetworkConnected()
        _isNetworkAvailable.value = connected
        if (connected) checkNetworkQuality()
    }

    /**
     * Start listening for network availability and changes.
     */
    fun startMonitoring() {
        if (isMonitoring) return
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
            isMonitoring = true
            Log.d(TAG, "Network monitoring started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting network monitoring: ${e.message}", e)
            isMonitoring = false
        }
    }

    /**
     * Stop listening for network changes.
     */
    fun stopMonitoring() {
        if (!isMonitoring) return
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            isMonitoring = false
            Log.d(TAG, "Network monitoring stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping network monitoring: ${e.message}", e)
        }
    }

    /**
     * Check if network is currently connected.
     * @return true if internet is available
     */
    fun isNetworkConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Alias for synchronous network availability checks.
     */
    fun isNetworkAvailable(): Boolean = isNetworkConnected()

    /**
     * Determine network quality for deciding on certain operations.
     */
    private fun checkNetworkQuality() {
        val network = connectivityManager.activeNetwork ?: return
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return
        val lowQuality =
            !(caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                    || connectivityManager.isActiveNetworkMetered
        _isLowQualityNetwork.postValue(lowQuality)
    }

    /**
     * Check if the current network is sufficient for image uploads.
     * @return true if on WiFi or Ethernet
     */
    fun isNetworkSuitableForImageUpload(): Boolean {
        if (!isNetworkConnected()) return false
        val network = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    /**
     * Get a human-friendly network type string.
     */
    fun getNetworkTypeString(): String {
        val network = connectivityManager.activeNetwork ?: return "Offline"
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return "Unknown"
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> "Bluetooth"
            else -> "Unknown"
        }
    }

    /**
     * Unregisters callbacks; deprecated in favor of stopMonitoring().
     */
    @Deprecated("Use stopMonitoring() instead", ReplaceWith("stopMonitoring()"))
    fun unregister() {
        stopMonitoring()
    }
}
