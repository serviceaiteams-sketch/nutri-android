package com.nutriai.app.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.nutriai.app.databinding.FragmentProfileBinding
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: ProfileViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
            setupUI()
            observeViewModel()
        } catch (e: Exception) {
            android.util.Log.e("ProfileFragment", "❌ Error in onViewCreated: ${e.message}", e)
            showErrorMessage("Unable to load profile. Please try again.")
        }
    }
    
    private fun setupUI() {
        try {
            // Setup click listeners
            binding.btnEditProfile.setOnClickListener {
                editProfile()
            }
            
            binding.btnChangePassword.setOnClickListener {
                changePassword()
            }
            
            binding.btnPrivacySettings.setOnClickListener {
                openPrivacySettings()
            }
            
            binding.btnLogout.setOnClickListener {
                logout()
            }
            
        } catch (e: Exception) {
            android.util.Log.e("ProfileFragment", "❌ Error setting up UI: ${e.message}", e)
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.profileState.collect { state ->
                    when (state) {
                        is Resource.Loading -> {
                            showLoading(true)
                        }
                        is Resource.Success -> {
                            showLoading(false)
                            state.data?.let { profile ->
                                updateProfileUI(profile)
                            }
                        }
                        is Resource.Error -> {
                            showLoading(false)
                            showErrorMessage(state.message ?: "Failed to load profile")
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ProfileFragment", "❌ Error observing ViewModel: ${e.message}", e)
                showErrorMessage("Error loading profile")
            }
        }
    }
    
    private fun updateProfileUI(profile: UserProfile) {
        try {
            binding.apply {
                tvUserName.text = profile.name
                tvUserEmail.text = profile.email
                tvUserAge.text = "Age: ${profile.age}"
                tvUserGender.text = "Gender: ${profile.gender}"
                tvUserHeight.text = "Height: ${profile.height} cm"
                tvUserWeight.text = "Weight: ${profile.weight} kg"
                tvActivityLevel.text = "Activity Level: ${profile.activityLevel}"
                tvDietaryPreferences.text = "Dietary Preferences: ${profile.dietaryPreferences}"
            }
        } catch (e: Exception) {
            android.util.Log.e("ProfileFragment", "❌ Error updating profile UI: ${e.message}", e)
        }
    }
    
    private fun editProfile() {
        try {
            // Navigate to edit profile screen
            // For now, show a toast message
            android.widget.Toast.makeText(
                requireContext(),
                "Edit profile feature coming soon!",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            android.util.Log.e("ProfileFragment", "❌ Error editing profile: ${e.message}", e)
        }
    }
    
    private fun changePassword() {
        try {
            // Navigate to change password screen
            // For now, show a toast message
            android.widget.Toast.makeText(
                requireContext(),
                "Change password feature coming soon!",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            android.util.Log.e("ProfileFragment", "❌ Error changing password: ${e.message}", e)
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
            android.util.Log.e("ProfileFragment", "❌ Error opening privacy settings: ${e.message}", e)
        }
    }
    
    private fun logout() {
        try {
            // Handle logout
            viewModel.logout()
            // Navigate to login screen
            android.widget.Toast.makeText(
                requireContext(),
                "Logging out...",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            android.util.Log.e("ProfileFragment", "❌ Error logging out: ${e.message}", e)
        }
    }
    
    private fun showLoading(show: Boolean) {
        try {
            binding.progressBar.isVisible = show
            binding.contentGroup.isVisible = !show
        } catch (e: Exception) {
            android.util.Log.e("ProfileFragment", "❌ Error showing loading: ${e.message}", e)
        }
    }
    
    private fun showErrorMessage(message: String) {
        try {
            android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
            android.util.Log.e("ProfileFragment", "❌ Error: $message")
        } catch (e: Exception) {
            android.util.Log.e("ProfileFragment", "❌ Error showing error message: ${e.message}", e)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        try {
            // Clean up resources
            viewLifecycleOwner.lifecycleScope.coroutineContext.cancelChildren()
            _binding = null
            android.util.Log.d("ProfileFragment", "🧹 Cleaned up resources")
        } catch (e: Exception) {
            android.util.Log.e("ProfileFragment", "❌ Error in onDestroyView: ${e.message}", e)
        }
    }
    
    companion object {
        fun newInstance() = ProfileFragment()
    }
}

// Data class for user profile
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val age: Int,
    val gender: String,
    val height: Float,
    val weight: Float,
    val activityLevel: String,
    val dietaryPreferences: String,
    val createdAt: String
)
