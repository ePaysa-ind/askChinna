package com.example.askchinna.util
/**
 * app/src/main/java/com/askchinna/util/NetworkStateMonitor.kt
 * Copyright © 2025 askChinna
 * Created: April 28, 2025
 * Version: 1.0
 */


import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monitors network state changes
 */
@Singleton
class NetworkStateMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

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

    init {
        // Set initial values
        val isConnected = isNetworkConnected()
        _isNetworkAvailable.value = isConnected

        if (isConnected) {
            checkNetworkQuality()
        }

        // Register network callback
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    /**
     * Check if network is connected
     * @return Boolean True if network is connected
     */
    private fun isNetworkConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Check network quality
     */
    private fun checkNetworkQuality() {
        val network = connectivityManager.activeNetwork ?: return
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return

        // Consider network low quality if:
        // - Not wifi or ethernet
        // - On metered connection
        val isLowQuality = !(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) ||
                connectivityManager.isActiveNetworkMetered

        _isLowQualityNetwork.postValue(isLowQuality)
    }

    /**
     * Check if current network is suitable for image uploads
     * @return Boolean True if network is suitable
     */
    fun isNetworkSuitableForImageUpload(): Boolean {
        if (!isNetworkConnected()) {
            return false
        }

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        // Prefer wifi or ethernet for image uploads
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    /**
     * Get network type as string
     * @return String Network type
     */
    fun getNetworkTypeString(): String {
        val network = connectivityManager.activeNetwork ?: return "Offline"
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "Unknown"

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> "Bluetooth"
            else -> "Unknown"
        }
    }

    /**
     * Clean up resources
     */
    fun unregister() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // Ignore
        }
    }
}