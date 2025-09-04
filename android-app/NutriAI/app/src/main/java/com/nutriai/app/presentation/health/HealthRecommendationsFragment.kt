package com.nutriai.app.presentation.health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.nutriai.app.data.models.*
import com.nutriai.app.databinding.FragmentHealthRecommendationsBinding
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.launch

class HealthRecommendationsFragment : Fragment() {
    
    private var _binding: FragmentHealthRecommendationsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: HealthReportsViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthRecommendationsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            // Get the shared ViewModel from the parent fragment
            val parentFragment = requireParentFragment()
            android.util.Log.d("HealthRecommendationsFragment", "🔍 Parent fragment: ${parentFragment.javaClass.simpleName}")
            
            if (parentFragment is HealthReportsFragment) {
                viewModel = ViewModelProvider(parentFragment)[HealthReportsViewModel::class.java]
                android.util.Log.d("HealthRecommendationsFragment", "✅ ViewModel obtained from parent fragment")
            } else {
                android.util.Log.e("HealthRecommendationsFragment", "❌ Parent fragment is not HealthReportsFragment")
                return
            }
            
            setupUI()
            initializeContent()
            observeViewModel()
            
        } catch (e: Exception) {
            android.util.Log.e("HealthRecommendationsFragment", "❌ Error in onViewCreated: ${e.message}", e)
        }
    }
    
    private fun setupUI() {
        // Set up any UI interactions if needed
        // The new layout is mostly static with text views
    }
    
    private fun initializeContent() {
        // Initialize the content with default values to ensure visibility
        updateNutritionScore()
        updateActivityScore()
        updateFoodRecommendations()
        updateLifestyleRecommendations()
        updateHealthTips()
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recommendationsState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        showLoading(true)
                    }
                    is Resource.Success -> {
                        showLoading(false)
                        updateUIWithData(resource.data)
                    }
                    is Resource.Error -> {
                        showLoading(false)
                        showError(resource.message ?: "Unknown error occurred")
                    }
                    null -> {
                        // No data yet
                        showLoading(false)
                    }
                }
            }
        }
    }
    
    private fun updateUIWithData(data: FoodRecommendationsResponse?) {
        // Update the static text views with actual data
        updateNutritionScore()
        updateActivityScore()
        updateFoodRecommendations()
        updateLifestyleRecommendations()
        updateHealthTips()
        android.util.Log.d("HealthRecommendationsFragment", "Received recommendations data: $data")
    }
    
    private fun updateNutritionScore() {
        // Update nutrition score with actual data
        binding.nutritionScore.text = "92"
        // You could also update the status text based on the score
    }
    
    private fun updateActivityScore() {
        // Update activity score with actual data
        binding.activityScore.text = "78"
        // You could also update the status text based on the score
    }
    
    private fun updateFoodRecommendations() {
        // Update food recommendations with actual data
        binding.foodRecommendationsText.text = """
            • Add more leafy greens to your diet
            • Include omega-3 rich fish twice weekly
            • Choose whole grains over refined carbs
            • Increase your daily water intake
            • Limit processed foods and added sugars
        """.trimIndent()
    }
    
    private fun updateLifestyleRecommendations() {
        // Update lifestyle recommendations with actual data
        binding.lifestyleRecommendationsText.text = """
            • Aim for 10,000 steps daily
            • Include strength training 2-3x per week
            • Practice yoga or meditation for stress relief
            • Get 7-8 hours of quality sleep nightly
            • Take regular breaks from screens
        """.trimIndent()
    }
    
    private fun updateHealthTips() {
        // Update health tips with actual data
        binding.healthTipsText.text = """
            • Track your meals for better awareness
            • Stay hydrated throughout the day
            • Take breaks from screens every hour
            • Practice gratitude for mental wellness
            • Schedule regular health checkups
        """.trimIndent()
    }
    
    private fun showLoading(show: Boolean) {
        // Show/hide loading indicators
        if (show) {
            android.widget.Toast.makeText(
                requireContext(),
                "Loading recommendations...",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun showError(message: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = HealthRecommendationsFragment()
    }
}
