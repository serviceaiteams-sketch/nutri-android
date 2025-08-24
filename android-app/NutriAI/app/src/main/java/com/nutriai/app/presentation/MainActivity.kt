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
                Toast.makeText(this, "Settings coming soon!", Toast.LENGTH_SHORT).show()
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

