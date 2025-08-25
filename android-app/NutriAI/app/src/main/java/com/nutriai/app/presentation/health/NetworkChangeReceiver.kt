package com.nutriai.app.presentation.health

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.nutriai.app.utils.NetworkUtils

class NetworkChangeReceiver(
    private val onNetworkChanged: () -> Unit
) : BroadcastReceiver() {
    
    private val TAG = "NetworkChangeReceiver"
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            context?.let { ctx ->
                val connectivityManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val network = connectivityManager.activeNetwork
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                
                val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
                val networkType = NetworkUtils.getNetworkType(ctx)
                val wifiSSID = NetworkUtils.getWifiSSID(ctx)
                
                Log.d(TAG, "🌐 Network changed - Connected: $isConnected, Type: $networkType, SSID: $wifiSSID")
                
                if (isConnected) {
                    Log.d(TAG, "✅ Network is connected, updating configuration...")
                    onNetworkChanged()
                } else {
                    Log.w(TAG, "❌ Network is disconnected")
                }
            }
        }
    }
}
