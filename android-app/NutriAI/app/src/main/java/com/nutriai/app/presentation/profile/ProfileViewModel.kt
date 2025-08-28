package com.nutriai.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    
    private val _profileState = MutableStateFlow<Resource<UserProfile>>(Resource.Loading())
    val profileState: StateFlow<Resource<UserProfile>> = _profileState
    
    init {
        loadProfile()
    }
    
    private fun loadProfile() {
        viewModelScope.launch {
            try {
                _profileState.value = Resource.Loading()
                
                // For now, return mock data
                // In the future, this will call the repository
                val mockProfile = UserProfile(
                    id = "1",
                    name = "John Doe",
                    email = "john.doe@example.com",
                    age = 28,
                    gender = "Male",
                    height = 175.0f,
                    weight = 70.0f,
                    activityLevel = "Moderate",
                    dietaryPreferences = "Vegetarian",
                    createdAt = "2024-01-15"
                )
                
                _profileState.value = Resource.Success(mockProfile)
                
            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "❌ Error loading profile: ${e.message}", e)
                _profileState.value = Resource.Error("Failed to load profile: ${e.message}")
            }
        }
    }
    
    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ProfileViewModel", "📝 Updating profile: ${profile.name}")
                
                // For now, just update the state
                // In the future, this will call the repository
                _profileState.value = Resource.Success(profile)
                
            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "❌ Error updating profile: ${e.message}", e)
                _profileState.value = Resource.Error("Failed to update profile: ${e.message}")
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            try {
                android.util.Log.d("ProfileViewModel", "🚪 Logging out user...")
                
                // Clear profile data
                _profileState.value = Resource.Loading()
                
                // In the future, this will call the auth repository to logout
                
            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "❌ Error logging out: ${e.message}", e)
            }
        }
    }
}
