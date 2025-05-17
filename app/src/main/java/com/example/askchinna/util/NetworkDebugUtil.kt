package com.example.askchinna.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log

object NetworkDebugUtil {
    private const val TAG = "NetworkDebugUtil"
    
    fun logDetailedNetworkState(context: Context) {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            
            Log.d(TAG, "=== Network Debug Report ===")
            
            // Check active network
            val activeNetwork = connectivityManager.activeNetwork
            Log.d(TAG, "Active Network exists: ${activeNetwork != null}")
            
            if (activeNetwork != null) {
                val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                Log.d(TAG, "Network Capabilities:")
                Log.d(TAG, "- INTERNET: ${capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)}")
                Log.d(TAG, "- VALIDATED: ${capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)}")
                Log.d(TAG, "- NOT_RESTRICTED: ${capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)}")
                
                Log.d(TAG, "Transports:")
                Log.d(TAG, "- WIFI: ${capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)}")
                Log.d(TAG, "- CELLULAR: ${capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)}")
            }
            
            // Legacy check
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            Log.d(TAG, "Legacy Network Info - Connected: ${networkInfo?.isConnected}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network state", e)
        }
    }
    
    fun isConnectedSimple(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(network)
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } catch (e: Exception) {
            Log.e(TAG, "Error in isConnectedSimple", e)
            false
        }
    }
}