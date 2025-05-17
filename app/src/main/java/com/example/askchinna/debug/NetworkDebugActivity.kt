package com.example.askchinna.debug

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.askchinna.R

/**
 * Simple debug activity to test network connectivity
 */
class NetworkDebugActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create a simple debug view
        val textView = TextView(this).apply {
            setPadding(16, 16, 16, 16)
            textSize = 16f
        }
        setContentView(textView)
        
        // Test network connectivity directly
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        val networkInfo = buildString {
            appendLine("=== Network Debug Info ===")
            appendLine()
            
            // Check active network
            val activeNetwork = connectivityManager.activeNetwork
            appendLine("Active Network: ${activeNetwork != null}")
            
            if (activeNetwork != null) {
                val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                appendLine("\nNetwork Capabilities:")
                appendLine("- INTERNET: ${capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)}")
                appendLine("- VALIDATED: ${capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)}")
                appendLine("- NOT_RESTRICTED: ${capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)}")
                appendLine("- NOT_METERED: ${capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)}")
                
                appendLine("\nTransports:")
                appendLine("- WIFI: ${capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)}")
                appendLine("- CELLULAR: ${capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)}")
                appendLine("- ETHERNET: ${capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)}")
                appendLine("- VPN: ${capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN)}")
            }
            
            // Check all networks
            val allNetworks = connectivityManager.allNetworks
            appendLine("\nAll Networks Count: ${allNetworks.size}")
            
            // Legacy check (for comparison)
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            appendLine("\nLegacy Network Info:")
            appendLine("- Connected: ${networkInfo?.isConnected}")
            appendLine("- Available: ${networkInfo?.isAvailable}")
            appendLine("- Type: ${networkInfo?.typeName}")
            appendLine("- State: ${networkInfo?.state}")
            
            // Permission check
            appendLine("\nPermissions:")
            appendLine("- ACCESS_NETWORK_STATE: ${checkCallingOrSelfPermission(android.Manifest.permission.ACCESS_NETWORK_STATE) == android.content.pm.PackageManager.PERMISSION_GRANTED}")
        }
        
        textView.text = networkInfo
    }
}