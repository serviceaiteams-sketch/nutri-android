package com.nutriai.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    
    private val _settingsState = MutableStateFlow<Resource<AppSettings>>(Resource.Loading())
    val settingsState: StateFlow<Resource<AppSettings>> = _settingsState
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                _settingsState.value = Resource.Loading()
                
                // For now, return mock data
                // In the future, this will load from SharedPreferences or DataStore
                val mockSettings = AppSettings(
                    notificationsEnabled = true,
                    darkModeEnabled = false,
                    autoSyncEnabled = true,
                    dataUsageEnabled = true,
                    language = "English",
                    units = "Metric"
                )
                
                _settingsState.value = Resource.Success(mockSettings)
                
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "❌ Error loading settings: ${e.message}", e)
                _settingsState.value = Resource.Error("Failed to load settings: ${e.message}")
            }
        }
    }
    
    fun updateSettings(settings: AppSettings) {
        viewModelScope.launch {
            try {
                android.util.Log.d("SettingsViewModel", "⚙️ Updating settings...")
                
                // For now, just update the state
                // In the future, this will save to SharedPreferences or DataStore
                _settingsState.value = Resource.Success(settings)
                
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "❌ Error updating settings: ${e.message}", e)
                _settingsState.value = Resource.Error("Failed to update settings: ${e.message}")
            }
        }
    }
    
    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = _settingsState.value.data ?: AppSettings()
                val updatedSettings = currentSettings.copy(notificationsEnabled = enabled)
                updateSettings(updatedSettings)
                
                android.util.Log.d("SettingsViewModel", "🔔 Notifications ${if (enabled) "enabled" else "disabled"}")
                
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "❌ Error toggling notifications: ${e.message}", e)
            }
        }
    }
    
    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = _settingsState.value.data ?: AppSettings()
                val updatedSettings = currentSettings.copy(darkModeEnabled = enabled)
                updateSettings(updatedSettings)
                
                android.util.Log.d("SettingsViewModel", "🌙 Dark mode ${if (enabled) "enabled" else "disabled"}")
                
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "❌ Error toggling dark mode: ${e.message}", e)
            }
        }
    }
    
    fun toggleAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = _settingsState.value.data ?: AppSettings()
                val updatedSettings = currentSettings.copy(autoSyncEnabled = enabled)
                updateSettings(updatedSettings)
                
                android.util.Log.d("SettingsViewModel", "🔄 Auto sync ${if (enabled) "enabled" else "disabled"}")
                
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "❌ Error toggling auto sync: ${e.message}", e)
            }
        }
    }
    
    fun toggleDataUsage(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = _settingsState.value.data ?: AppSettings()
                val updatedSettings = currentSettings.copy(dataUsageEnabled = enabled)
                updateSettings(updatedSettings)
                
                android.util.Log.d("SettingsViewModel", "📊 Data usage ${if (enabled) "enabled" else "disabled"}")
                
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "❌ Error toggling data usage: ${e.message}", e)
            }
        }
    }
}
