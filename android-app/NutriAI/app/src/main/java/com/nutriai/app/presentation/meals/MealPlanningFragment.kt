package com.nutriai.app.presentation.meals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AlertDialog
import com.nutriai.app.databinding.FragmentMealPlanningBinding
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.CancellationException
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
                adapter = MealPlanAdapter(
                    onViewPlan = { mealPlan -> viewMealPlan(mealPlan) },
                    onStartPlan = { mealPlan -> startMealPlan(mealPlan) },
                    onSharePlan = { mealPlan -> shareMealPlan(mealPlan) },
                    onMoreOptions = { mealPlan -> showMoreOptions(mealPlan) }
                )
            }
            
            // Setup click listeners
            binding.btnGenerateAI.setOnClickListener {
                generateAIRecommendedMealPlan()
            }
            
            binding.btnCreateCustom.setOnClickListener {
                createNewMealPlan()
            }
            
            binding.tvViewAll.setOnClickListener {
                showAllMealPlans()
            }
            
            // Setup category card click listeners
            setupCategoryCards()
            
            // Observe AI preview to confirm before saving
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.generatedPlanPreview.collect { preview ->
                        if (preview != null && _binding != null) {
                            AlertDialog.Builder(requireContext())
                                .setTitle("AI Meal Plan Preview")
                                .setMessage(preview)
                                .setPositiveButton("Save") { _, _ ->
                                    viewModel.saveGeneratedMealPlan(requireContext())
                                }
                                .setNegativeButton("Cancel") { _, _ ->
                                    viewModel.dismissGeneratedPreview()
                                }
                                .show()
                        }
                    }
                }
            }

        } catch (e: Exception) {
            android.util.Log.e("MealPlanningFragment", "❌ Error setting up UI: ${e.message}", e)
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    viewModel.mealPlansState.collect { state ->
                        when (state) {
                            is Resource.Loading -> showLoading(true)
                            is Resource.Success -> {
                                showLoading(false)
                                state.data?.let { updateMealPlansList(it) }
                            }
                            is Resource.Error -> {
                                showLoading(false)
                                showErrorMessage(state.message ?: "Failed to load meal plans")
                            }
                        }
                    }
                } catch (e: CancellationException) {
                    // Lifecycle stopped; ignore
                } catch (e: Exception) {
                    android.util.Log.e("MealPlanningFragment", "❌ Error observing ViewModel: ${e.message}", e)
                    showErrorMessage("Error loading meal plans")
                }
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
            // Ask user before saving
            AlertDialog.Builder(requireContext())
                .setTitle("Save Meal Plan")
                .setMessage("Do you want to save this meal plan now?")
                .setPositiveButton("Save") { _, _ ->
                    viewModel.createMealPlan(
                        name = "My Plan",
                        description = "Custom",
                        context = requireContext()
                    )
                    android.widget.Toast.makeText(requireContext(), "Meal plan saved", android.widget.Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            android.util.Log.e("MealPlanningFragment", "❌ Error creating meal plan: ${e.message}", e)
        }
    }
    
    private fun generateAIRecommendedMealPlan() {
        try {
            // Generate AI-based meal plan
            viewModel.generateAIRecommendedMealPlan(requireContext())
        } catch (e: Exception) {
            android.util.Log.e("MealPlanningFragment", "❌ Error generating meal plan: ${e.message}", e)
            showErrorMessage("Failed to generate meal plan")
        }
    }
    
    private fun showLoading(show: Boolean) {
        try {
            binding.progressBar.isVisible = show
            binding.recyclerViewMealPlans.isVisible = !show
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
    
    private fun setupCategoryCards() {
        // Category cards are handled in the layout, but we can add click listeners here if needed
        // For now, they're just visual elements
    }
    
    private fun viewMealPlan(mealPlan: MealPlan) {
        // Show detailed meal plan view
        val message = """
            ${mealPlan.name}
            
            ${mealPlan.description}
            
            📊 Nutrition Summary:
            • Calories: ${mealPlan.totalCalories} cal
            • Protein: ${mealPlan.totalProtein}g
            • Carbs: ${mealPlan.totalCarbs}g
            • Fat: ${mealPlan.totalFat}g
            
            📅 Duration: ${mealPlan.days.size} days
            🍽️ Total Meals: ${mealPlan.days.sumOf { it.meals.size }}
            
            Created: ${mealPlan.createdAt}
        """.trimIndent()
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Meal Plan Details")
            .setMessage(message)
            .setPositiveButton("Start This Plan") { _, _ ->
                startMealPlan(mealPlan)
            }
            .setNeutralButton("Share") { _, _ ->
                shareMealPlan(mealPlan)
            }
            .setNegativeButton("Close", null)
            .show()
    }
    
    private fun startMealPlan(mealPlan: MealPlan) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Start Meal Plan")
            .setMessage("Ready to start '${mealPlan.name}'?\n\nThis will help you track your daily meals and nutrition goals.")
            .setPositiveButton("Start Now") { _, _ ->
                android.widget.Toast.makeText(
                    requireContext(),
                    "Started meal plan: ${mealPlan.name}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                // Here you would typically navigate to meal tracking or set the active plan
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun shareMealPlan(mealPlan: MealPlan) {
        val shareText = """
            🍽️ Check out my meal plan: ${mealPlan.name}
            
            ${mealPlan.description}
            
            📊 Nutrition:
            • ${mealPlan.totalCalories} calories
            • ${mealPlan.totalProtein}g protein
            • ${mealPlan.totalCarbs}g carbs
            • ${mealPlan.totalFat}g fat
            
            Created with NutriAI app!
        """.trimIndent()
        
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
        }
        
        startActivity(android.content.Intent.createChooser(shareIntent, "Share Meal Plan"))
    }
    
    private fun showMoreOptions(mealPlan: MealPlan) {
        val options = arrayOf("Edit Plan", "Duplicate Plan", "Delete Plan", "Export to PDF")
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Meal Plan Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editMealPlan(mealPlan)
                    1 -> duplicateMealPlan(mealPlan)
                    2 -> deleteMealPlan(mealPlan)
                    3 -> exportMealPlan(mealPlan)
                }
            }
            .show()
    }
    
    private fun editMealPlan(mealPlan: MealPlan) {
        android.widget.Toast.makeText(
            requireContext(),
            "Edit functionality coming soon!",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun duplicateMealPlan(mealPlan: MealPlan) {
        android.widget.Toast.makeText(
            requireContext(),
            "Duplicated: ${mealPlan.name}",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun deleteMealPlan(mealPlan: MealPlan) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Meal Plan")
            .setMessage("Are you sure you want to delete '${mealPlan.name}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                android.widget.Toast.makeText(
                    requireContext(),
                    "Deleted: ${mealPlan.name}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun exportMealPlan(mealPlan: MealPlan) {
        android.widget.Toast.makeText(
            requireContext(),
            "Export functionality coming soon!",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun showAllMealPlans() {
        android.widget.Toast.makeText(
            requireContext(),
            "Showing all meal plans...",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        try {
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
