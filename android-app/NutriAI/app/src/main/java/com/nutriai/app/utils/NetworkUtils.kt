package com.nutriai.app.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*

object NetworkUtils {
    
    private const val TAG = "NetworkUtils"
    
    // Production server URL (for Play Store release)
    // TODO: Replace with your actual production URL when deploying
    private const val PRODUCTION_BASE_URL = "https://nutri-ai-5b9893ad4a00.herokuapp.com/api/"
    
    // Manual override for development (set this if automatic detection fails)
    private var MANUAL_SERVER_IP: String? = null
    
    // Development server URLs (for testing)
    private const val DEV_SERVER_PORT = "5000"  // Your server is running on port 5000
    private val COMMON_DEV_IPS = listOf(
        "192.168.29.2",   // YOUR COMPUTER'S IP ADDRESS - SERVER IS HERE!
        "192.168.29.100", // Your current network range
        "192.168.29.1",   // Your current network router
        "192.168.29.50",  // Your current network alternative
        "192.168.1.100",  // Common home router IP
        "192.168.0.100",  // Alternative home router IP
        "10.0.0.100",     // Another common range
        "172.20.10.1",    // iPhone hotspot
        "192.168.43.1",   // Android hotspot
        "10.129.157.148", // Your previous hotspot IP
        "192.168.1.28",   // Your previous home WiFi IP
        "192.168.1.1",    // Router IP
        "192.168.0.1",    // Alternative router IP
        "10.0.0.1",       // Another router IP
        "172.20.10.2",    // iPhone hotspot alternative
        "192.168.43.2",   // Android hotspot alternative
        "localhost",      // Local development
        "127.0.0.1"       // Localhost IP
    )
    
    /**
     * Get the appropriate base URL based on current environment
     */
    fun getBaseUrl(context: Context): String {
        // Force using Heroku URL for now to test the detailed analysis
        Log.d(TAG, "🚀 Using Heroku URL for testing: $PRODUCTION_BASE_URL")
        return PRODUCTION_BASE_URL
        
        // Original logic (commented out for testing)
        /*
        return if (isProductionBuild()) {
            PRODUCTION_BASE_URL
        } else {
            // For development, try different strategies
            
            // 1. Check if manual IP is set
            MANUAL_SERVER_IP?.let { ip ->
                val manualUrl = "http://$ip:$DEV_SERVER_PORT/api/"
                Log.d(TAG, "🔧 Using manual server IP: $manualUrl")
                return manualUrl
            }
            
            // 2. Check shared preferences for cached server URL
            val prefs = context.getSharedPreferences("NetworkConfig", Context.MODE_PRIVATE)
            val cachedUrl = prefs.getString("server_url", null)
            if (cachedUrl != null) {
                Log.d(TAG, "📱 Using cached server URL: $cachedUrl")
                return cachedUrl
            }
            
            // 3. Return default URL - actual detection will happen asynchronously
            val defaultUrl = "http://192.168.29.2:5000/api/"
            Log.d(TAG, "⚠️ Using default URL: $defaultUrl, will detect server asynchronously")
            
            // Start async detection for next time
            detectAndCacheServerUrl(context)
            
            return defaultUrl
        }
        */
    }
    
    /**
     * Asynchronously detect and cache server URL
     */
    private fun detectAndCacheServerUrl(context: Context) {
        Thread {
            try {
                val detectedUrl = detectLocalServerUrl(context)
                if (detectedUrl != "http://192.168.1.100:$DEV_SERVER_PORT/api/") {
                    val prefs = context.getSharedPreferences("NetworkConfig", Context.MODE_PRIVATE)
                    prefs.edit().putString("server_url", detectedUrl).apply()
                    Log.d(TAG, "✅ Cached detected server URL: $detectedUrl")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error detecting server URL: ${e.message}")
            }
        }.start()
    }
    
    /**
     * Set manual server IP for development (call this if automatic detection fails)
     */
    fun setManualServerIP(ip: String, context: Context) {
        MANUAL_SERVER_IP = ip
        // Also cache it for next app launch
        val prefs = context.getSharedPreferences("NetworkConfig", Context.MODE_PRIVATE)
        prefs.edit().putString("server_url", "http://$ip:$DEV_SERVER_PORT/api/").apply()
        Log.d(TAG, "🔧 Manual server IP set to: $ip")
    }
    
    /**
     * Clear manual server IP
     */
    fun clearManualServerIP(context: Context) {
        MANUAL_SERVER_IP = null
        // Clear cached URL too
        val prefs = context.getSharedPreferences("NetworkConfig", Context.MODE_PRIVATE)
        prefs.edit().remove("server_url").apply()
        Log.d(TAG, "🔧 Manual server IP cleared")
    }
    
    /**
     * Check if this is a production build
     */
    private fun isProductionBuild(): Boolean {
        // You can set this based on BuildConfig.DEBUG or a custom flag
        return false // For now, always use development mode
    }
    
    /**
     * Detect the local server URL by trying common IP addresses
     */
    private fun detectLocalServerUrl(context: Context): String {
        Log.d(TAG, "🔍 Detecting local server URL...")
        
        // First, try to get the current device's network IP
        val deviceIp = getDeviceIpAddress()
        Log.d(TAG, "📱 Device IP: $deviceIp")
        
        // Try the device's network range first
        if (deviceIp != null) {
            val networkRange = getNetworkRange(deviceIp)
            Log.d(TAG, "🌐 Network range: $networkRange")
            
            // Try common server IPs in the same network range
            val networkServerUrl = tryNetworkRange(networkRange)
            if (networkServerUrl != null) {
                Log.d(TAG, "✅ Found server in network range: $networkServerUrl")
                return networkServerUrl
            }
        }
        
        // Fallback to common development IPs
        Log.d(TAG, "🔄 Trying common development IPs...")
        for (ip in COMMON_DEV_IPS) {
            val url = "http://$ip:$DEV_SERVER_PORT/api/"
            if (isServerReachable(url)) {
                Log.d(TAG, "✅ Found working server: $url")
                return url
            }
        }
        
        // Try port 5000 as fallback (in case server is running on old port)
        Log.d(TAG, "🔄 Trying port 5000 as fallback...")
        for (ip in COMMON_DEV_IPS) {
            val url = "http://$ip:5000/api/"
            if (isServerReachable(url)) {
                Log.d(TAG, "✅ Found working server on port 5000: $url")
                return url
            }
        }
        
        // If nothing works, return a default (user can configure manually)
        Log.w(TAG, "⚠️ No server found, using default")
        return "http://192.168.1.100:$DEV_SERVER_PORT/api/"
    }
    
    /**
     * Get the current device's IP address
     */
    private fun getDeviceIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address.hostAddress.indexOf(':') < 0) {
                        val ip = address.hostAddress
                        Log.d(TAG, "📱 Found IP: $ip")
                        return ip
                    }
                }
            }
        } catch (e: SocketException) {
            Log.e(TAG, "❌ Error getting device IP: ${e.message}")
        }
        return null
    }
    
    /**
     * Get the network range from an IP address
     */
    private fun getNetworkRange(ip: String): String {
        val parts = ip.split(".")
        return if (parts.size == 4) {
            "${parts[0]}.${parts[1]}.${parts[2]}"
        } else {
            "192.168.1"
        }
    }
    
    /**
     * Try to find server in a specific network range
     */
    private fun tryNetworkRange(networkRange: String): String? {
        // Try common server IPs in the network range
        val commonEndings = listOf("100", "1", "10", "50", "101", "254", "2", "5", "20", "30", "40", "60", "70", "80", "90")
        
        for (ending in commonEndings) {
            val ip = "$networkRange.$ending"
            val url = "http://$ip:$DEV_SERVER_PORT/api/"
            if (isServerReachable(url)) {
                Log.d(TAG, "✅ Server found at: $url")
                return url
            }
        }
        return null
    }
    
    /**
     * Check if a server is reachable
     */
    private fun isServerReachable(url: String): Boolean {
        return try {
            val baseUrl = url.removeSuffix("/api/")
            val healthUrl = "$baseUrl/api/health"
            
            Log.d(TAG, "🔍 Testing connection to: $healthUrl")
            
            // Try to connect to health endpoint
            val connection = java.net.URL(healthUrl).openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 2000 // 2 seconds timeout
            connection.readTimeout = 2000
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "NutriAI-Android/1.0")
            connection.doInput = true
            
            // Allow any host for development
            if (!isProductionBuild()) {
                connection.instanceFollowRedirects = false
            }
            
            connection.connect()
            val responseCode = connection.responseCode
            val responseMessage = connection.responseMessage
            connection.disconnect()
            
            Log.d(TAG, "📡 Server response from $healthUrl: $responseCode - $responseMessage")
            responseCode == 200
        } catch (e: Exception) {
            Log.e(TAG, "❌ Server not reachable at $url - Error: ${e.javaClass.simpleName}: ${e.message}")
            false
        }
    }
    
    /**
     * Get network type (WiFi, Mobile, etc.)
     */
    fun getNetworkType(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        
        return when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi"
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Mobile"
            else -> "Unknown"
        }
    }
    
    /**
     * Check if device is connected to WiFi
     */
    fun isWifiConnected(context: Context): Boolean {
        return getNetworkType(context) == "WiFi"
    }
    
    /**
     * Get WiFi SSID (network name)
     */
    fun getWifiSSID(context: Context): String? {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            wifiInfo?.ssid?.removeSurrounding("\"")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting WiFi SSID: ${e.message}")
            null
        }
    }
    
    /**
     * Test connection to a specific server URL
     */
    fun testServerConnection(url: String): Boolean {
        return isServerReachable(url)
    }
    
    /**
     * Get current device IP address (public method)
     */
    fun getCurrentDeviceIP(): String? {
        return getDeviceIpAddress()
    }
}
