package com.nutriai.app.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.nutriai.app.databinding.FragmentSettingsBinding
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: SettingsViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
            setupUI()
            observeViewModel()
        } catch (e: Exception) {
            android.util.Log.e("SettingsFragment", "❌ Error in onViewCreated: ${e.message}", e)
            showErrorMessage("Unable to load settings. Please try again.")
        }
    }
    
    private fun setupUI() {
        try {
            // Setup click listeners
            binding.btnNetworkSettings.setOnClickListener {
                openNetworkSettings()
            }
            
            binding.btnNotificationSettings.setOnClickListener {
                openNotificationSettings()
            }
            
            binding.btnPrivacySettings.setOnClickListener {
                openPrivacySettings()
            }
            
            binding.btnAbout.setOnClickListener {
                openAbout()
            }
            
            binding.btnHelp.setOnClickListener {
                openHelp()
            }
            
            binding.btnFeedback.setOnClickListener {
                openFeedback()
            }
            
        } catch (e: Exception) {
            android.util.Log.e("SettingsFragment", "❌ Error setting up UI: ${e.message}", e)
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.settingsState.collect { state ->
                    when (state) {
                        is Resource.Loading -> {
                            showLoading(true)
                        }
                        is Resource.Success -> {
                            showLoading(false)
                            state.data?.let { settings ->
                                updateSettingsUI(settings)
                            }
                        }
                        is Resource.Error -> {
                            showLoading(false)
                            showErrorMessage(state.message ?: "Failed to load settings")
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("SettingsFragment", "❌ Error observing ViewModel: ${e.message}", e)
                showErrorMessage("Error loading settings")
            }
        }
    }
    
    private fun updateSettingsUI(settings: AppSettings) {
        try {
            binding.apply {
                switchNotifications.isChecked = settings.notificationsEnabled
                switchDarkMode.isChecked = settings.darkModeEnabled
                switchAutoSync.isChecked = settings.autoSyncEnabled
                switchDataUsage.isChecked = settings.dataUsageEnabled
            }
        } catch (e: Exception) {
            android.util.Log.e("SettingsFragment", "❌ Error updating settings UI: ${e.message}", e)
        }
    }
    
    private fun openNetworkSettings() {
        try {
            // Navigate to network settings screen
            // For now, show a toast message
            android.widget.Toast.makeText(
                requireContext(),
                "Network settings feature coming soon!",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            android.util.Log.e("SettingsFragment", "❌ Error opening network settings: ${e.message}", e)
        }
    }
    
    private fun openNotificationSettings() {
        try {
            // Navigate to notification settings screen
            // For now, show a toast message
            android.widget.Toast.makeText(
                requireContext(),
                "Notification settings feature coming soon!",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            android.util.Log.e("SettingsFragment", "❌ Error opening notification settings: ${e.message}", e)
        }
    }
    
    private fun openPrivacySettings() {
        try {
            // Navigate to privacy settings screen
            // For now, show a toast message
            android.widget.Toast.makeText(
                requireContext(),
                "Privacy settings feature coming soon!",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            android.util.Log.e("SettingsFragment", "❌ Error opening privacy settings: ${e.message}", e)
        }
    }
    
    private fun openAbout() {
        try {
            // Navigate to about screen
            // For now, show a toast message
            android.widget.Toast.makeText(
                requireContext(),
                "About feature coming soon!",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            android.util.Log.e("SettingsFragment", "❌ Error opening about: ${e.message}", e)
        }
    }
    
    private fun openHelp() {
        try {
            // Navigate to help screen
            // For now, show a toast message
            android.widget.Toast.makeText(
                requireContext(),
                "Help feature coming soon!",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            android.util.Log.e("SettingsFragment", "❌ Error opening help: ${e.message}", e)
        }
    }
    
    private fun openFeedback() {
        try {
            // Navigate to feedback screen
            // For now, show a toast message
            android.widget.Toast.makeText(
                requireContext(),
                "Feedback feature coming soon!",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            android.util.Log.e("SettingsFragment", "❌ Error opening feedback: ${e.message}", e)
        }
    }
    
    private fun showLoading(show: Boolean) {
        try {
            binding.progressBar.isVisible = show
            binding.contentGroup.isVisible = !show
        } catch (e: Exception) {
            android.util.Log.e("SettingsFragment", "❌ Error showing loading: ${e.message}", e)
        }
    }
    
    private fun showErrorMessage(message: String) {
        try {
            android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
            android.util.Log.e("SettingsFragment", "❌ Error: $message")
        } catch (e: Exception) {
            android.util.Log.e("SettingsFragment", "❌ Error showing error message: ${e.message}", e)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        try {
            // Clean up resources
            viewLifecycleOwner.lifecycleScope.coroutineContext.cancelChildren()
            _binding = null
            android.util.Log.d("SettingsFragment", "🧹 Cleaned up resources")
        } catch (e: Exception) {
            android.util.Log.e("SettingsFragment", "❌ Error in onDestroyView: ${e.message}", e)
        }
    }
    
    companion object {
        fun newInstance() = SettingsFragment()
    }
}

// Data class for app settings
data class AppSettings(
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val autoSyncEnabled: Boolean = true,
    val dataUsageEnabled: Boolean = true,
    val language: String = "English",
    val units: String = "Metric"
)
