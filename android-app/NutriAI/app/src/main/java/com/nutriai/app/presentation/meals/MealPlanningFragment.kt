package com.nutriai.app.presentation.meals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nutriai.app.databinding.FragmentMealPlanningBinding
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

class MealPlanningFragment : Fragment() {
    
    private var _binding: FragmentMealPlanningBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: MealPlanningViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMealPlanningBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            viewModel = ViewModelProvider(this)[MealPlanningViewModel::class.java]
            setupUI()
            observeViewModel()
        } catch (e: Exception) {
            android.util.Log.e("MealPlanningFragment", "❌ Error in onViewCreated: ${e.message}", e)
            showErrorMessage("Unable to load meal planning. Please try again.")
        }
    }
    
    private fun setupUI() {
        try {
            // Setup RecyclerView for meal plans
            binding.recyclerViewMealPlans.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = MealPlanAdapter()
            }
            
            // Setup click listeners
            binding.btnCreateMealPlan.setOnClickListener {
                createNewMealPlan()
            }
            
            binding.btnGenerateMealPlan.setOnClickListener {
                generateAIRecommendedMealPlan()
            }
            
        } catch (e: Exception) {
            android.util.Log.e("MealPlanningFragment", "❌ Error setting up UI: ${e.message}", e)
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.mealPlansState.collect { state ->
                    when (state) {
                        is Resource.Loading -> {
                            showLoading(true)
                        }
                        is Resource.Success -> {
                            showLoading(false)
                            state.data?.let { mealPlans ->
                                updateMealPlansList(mealPlans)
                            }
                        }
                        is Resource.Error -> {
                            showLoading(false)
                            showErrorMessage(state.message ?: "Failed to load meal plans")
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MealPlanningFragment", "❌ Error observing ViewModel: ${e.message}", e)
                showErrorMessage("Error loading meal plans")
            }
        }
    }
    
    private fun updateMealPlansList(mealPlans: List<MealPlan>) {
        try {
            (binding.recyclerViewMealPlans.adapter as? MealPlanAdapter)?.submitList(mealPlans)
            
            // Show/hide empty state
            binding.emptyState.isVisible = mealPlans.isEmpty()
            binding.recyclerViewMealPlans.isVisible = mealPlans.isNotEmpty()
            
        } catch (e: Exception) {
            android.util.Log.e("MealPlanningFragment", "❌ Error updating meal plans list: ${e.message}", e)
        }
    }
    
    private fun createNewMealPlan() {
        try {
            // Navigate to meal plan creation screen
            // For now, show a toast message
            android.widget.Toast.makeText(
                requireContext(),
                "Create meal plan feature coming soon!",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            android.util.Log.e("MealPlanningFragment", "❌ Error creating meal plan: ${e.message}", e)
        }
    }
    
    private fun generateAIRecommendedMealPlan() {
        try {
            // Generate AI-based meal plan
            viewModel.generateAIRecommendedMealPlan()
        } catch (e: Exception) {
            android.util.Log.e("MealPlanningFragment", "❌ Error generating meal plan: ${e.message}", e)
            showErrorMessage("Failed to generate meal plan")
        }
    }
    
    private fun showLoading(show: Boolean) {
        try {
            binding.progressBar.isVisible = show
            binding.contentGroup.isVisible = !show
        } catch (e: Exception) {
            android.util.Log.e("MealPlanningFragment", "❌ Error showing loading: ${e.message}", e)
        }
    }
    
    private fun showErrorMessage(message: String) {
        try {
            android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
            android.util.Log.e("MealPlanningFragment", "❌ Error: $message")
        } catch (e: Exception) {
            android.util.Log.e("MealPlanningFragment", "❌ Error showing error message: ${e.message}", e)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        try {
            // Clean up resources
            viewLifecycleOwner.lifecycleScope.coroutineContext.cancelChildren()
            _binding = null
            android.util.Log.d("MealPlanningFragment", "🧹 Cleaned up resources")
        } catch (e: Exception) {
            android.util.Log.e("MealPlanningFragment", "❌ Error in onDestroyView: ${e.message}", e)
        }
    }
    
    companion object {
        fun newInstance() = MealPlanningFragment()
    }
}

// Data classes for meal planning
data class MealPlan(
    val id: String,
    val name: String,
    val description: String,
    val days: List<MealPlanDay>,
    val totalCalories: Int,
    val totalProtein: Int,
    val totalCarbs: Int,
    val totalFat: Int,
    val createdAt: String
)

data class MealPlanDay(
    val day: String,
    val meals: List<MealPlanMeal>
)

data class MealPlanMeal(
    val type: String, // breakfast, lunch, dinner, snack
    val name: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val ingredients: List<String>,
    val instructions: String
)
