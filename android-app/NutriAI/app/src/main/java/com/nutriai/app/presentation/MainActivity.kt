package com.nutriai.app.presentation

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import com.nutriai.app.R
import com.nutriai.app.data.local.DataStoreManager
import com.nutriai.app.data.repository.AuthRepository
import com.nutriai.app.databinding.ActivityMainBinding
import com.nutriai.app.presentation.auth.LoginActivity
import com.nutriai.app.presentation.food.FoodRecognitionFragment
import com.nutriai.app.presentation.meals.MealHistoryFragment
import com.nutriai.app.presentation.health.HealthReportsFragment
import com.nutriai.app.utils.NetworkUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private val authRepository = AuthRepository()
    private val dataStoreManager = DataStoreManager()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize repositories with context for dynamic network detection
        authRepository.initialize(this)
        
        checkAuthentication()
        setupToolbar()
        setupNavigationDrawer()
        loadUserInfo()
        
        // Load default fragment
        if (savedInstanceState == null) {
            binding.navigationView.setCheckedItem(R.id.nav_dashboard)
            // TODO: Load dashboard fragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, DashboardFragment())
                .commit()
        }
        
        // Debug: Test network detection
        testNetworkDetection()
    }
    
    private fun testNetworkDetection() {
        lifecycleScope.launch {
            try {
                val deviceIP = NetworkUtils.getCurrentDeviceIP()
                val networkType = NetworkUtils.getNetworkType(this@MainActivity)
                val wifiSSID = NetworkUtils.getWifiSSID(this@MainActivity)
                
                android.util.Log.d("MainActivity", "🔍 Network Debug Info:")
                android.util.Log.d("MainActivity", "📱 Device IP: $deviceIP")
                android.util.Log.d("MainActivity", "🌐 Network Type: $networkType")
                android.util.Log.d("MainActivity", "📶 WiFi SSID: $wifiSSID")
                
                // Test some common server URLs
                val testUrls = listOf(
                    "http://192.168.29.100:5001/api/",  // Your current network
                    "http://192.168.29.1:5001/api/",    // Your current network router
                    "http://192.168.29.50:5001/api/",   // Your current network alternative
                    "http://192.168.1.100:5001/api/",
                    "http://192.168.1.1:5001/api/",
                    "http://10.129.157.148:5001/api/",
                    "http://localhost:5001/api/",
                    "http://127.0.0.1:5001/api/"
                )
                
                for (url in testUrls) {
                    val isReachable = NetworkUtils.testServerConnection(url)
                    android.util.Log.d("MainActivity", "🔍 Testing $url - Reachable: $isReachable")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "❌ Network test error: ${e.message}")
            }
        }
    }
    
    private fun checkAuthentication() {
        lifecycleScope.launch {
            val isLoggedIn = authRepository.isLoggedIn().first()
            if (!isLoggedIn) {
                navigateToLogin()
            }
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }
    
    private fun setupNavigationDrawer() {
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        
        binding.navigationView.setNavigationItemSelectedListener(this)
    }
    
    private fun loadUserInfo() {
        val headerView = binding.navigationView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.tvUserName)
        val tvUserEmail = headerView.findViewById<TextView>(R.id.tvUserEmail)
        
        lifecycleScope.launch {
            dataStoreManager.userName.collect { name ->
                tvUserName.text = name ?: "User"
            }
        }
        
        lifecycleScope.launch {
            dataStoreManager.userEmail.collect { email ->
                tvUserEmail.text = email ?: "user@example.com"
            }
        }
    }
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_dashboard -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, DashboardFragment())
                    .commit()
            }
            R.id.nav_food_recognition -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, FoodRecognitionFragment())
                    .addToBackStack(null)
                    .commit()
            }
            R.id.nav_meal_tracking -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, MealHistoryFragment())
                    .addToBackStack(null)
                    .commit()
            }
            R.id.nav_health_reports -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, HealthReportsFragment())
                    .addToBackStack(null)
                    .commit()
            }
            R.id.nav_meal_planning -> {
                Toast.makeText(this, "Meal Planning coming soon!", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_profile -> {
                Toast.makeText(this, "Profile coming soon!", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_settings -> {
                showNetworkSettingsDialog()
            }
            R.id.nav_logout -> {
                logout()
            }
        }
        
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    
    private fun logout() {
        lifecycleScope.launch {
            authRepository.logout()
            navigateToLogin()
        }
    }
    
    private fun showNetworkSettingsDialog() {
        val deviceIP = NetworkUtils.getCurrentDeviceIP()
        val networkType = NetworkUtils.getNetworkType(this)
        val wifiSSID = NetworkUtils.getWifiSSID(this)
        
        val message = """
            Network Information:
            Device IP: $deviceIP
            Network Type: $networkType
            WiFi SSID: $wifiSSID
            
            If you're having connection issues, try setting a manual server IP.
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Network Settings")
            .setMessage(message)
            .setPositiveButton("Set Manual IP") { _, _ ->
                showManualIPDialog()
            }
            .setNegativeButton("Test Connection") { _, _ ->
                testNetworkDetection()
                Toast.makeText(this, "Check logcat for network test results", Toast.LENGTH_LONG).show()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }
    
    private fun showManualIPDialog() {
        val input = android.widget.EditText(this)
        input.hint = "Enter server IP (e.g., 192.168.1.100)"
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Set Manual Server IP")
            .setView(input)
            .setPositiveButton("Set") { _, _ ->
                val ip = input.text.toString().trim()
                if (ip.isNotEmpty()) {
                    NetworkUtils.setManualServerIP(ip, this@MainActivity)
                    Toast.makeText(this, "Manual IP set to: $ip", Toast.LENGTH_SHORT).show()
                    // Reset network configuration
                    com.nutriai.app.di.NetworkModule.resetNetwork()
                }
            }
            .setNegativeButton("Clear Manual IP") { _, _ ->
                NetworkUtils.clearManualServerIP(this@MainActivity)
                Toast.makeText(this, "Manual IP cleared", Toast.LENGTH_SHORT).show()
                // Reset network configuration
                com.nutriai.app.di.NetworkModule.resetNetwork()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}

