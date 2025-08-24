package com.nutriai.app.presentation.health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nutriai.app.data.models.FoodRecommendationsResponse
import kotlinx.coroutines.flow.combine
import com.nutriai.app.databinding.FragmentHealthRecommendationsBinding
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.launch

class HealthRecommendationsFragment : Fragment() {
    
    private var _binding: FragmentHealthRecommendationsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: HealthReportsViewModel
    private lateinit var foodRecommendationsAdapter: FoodRecommendationsAdapter
    
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
        
        // Get parent fragment's ViewModel
        viewModel = ViewModelProvider(requireParentFragment())[HealthReportsViewModel::class.java]
        
        setupRecyclerView()
        observeViewModel()
    }
    
    private fun setupRecyclerView() {
        android.util.Log.d("HealthRecommendationsFragment", "🔧 Setting up RecyclerView...")
        foodRecommendationsAdapter = FoodRecommendationsAdapter()
        binding.rvRecommendedFoods.apply {
            adapter = foodRecommendationsAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            android.util.Log.d("HealthRecommendationsFragment", "🔧 RecyclerView adapter set: $adapter")
            android.util.Log.d("HealthRecommendationsFragment", "🔧 RecyclerView layout manager set: $layoutManager")
        }
        android.util.Log.d("HealthRecommendationsFragment", "🔧 RecyclerView setup complete. Adapter: $foodRecommendationsAdapter")
        android.util.Log.d("HealthRecommendationsFragment", "🔧 RecyclerView adapter count: ${foodRecommendationsAdapter.itemCount}")
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe both analysis state and recommendations state
            combine(
                viewModel.analysisState,
                viewModel.recommendationsState
            ) { analysisState, recommendationsState ->
                Pair(analysisState, recommendationsState)
            }.collect { (analysisState, recommendationsState) ->
                when {
                    analysisState is Resource.Success && analysisState.data != null -> {
                        // Use food recommendations from main analysis response
                        android.util.Log.d("HealthRecommendationsFragment", "📊 Analysis success, checking food recommendations...")
                        android.util.Log.d("HealthRecommendationsFragment", "🍎 Food recommendations: ${analysisState.data.foodRecommendations}")
                        android.util.Log.d("HealthRecommendationsFragment", "🍎 Food recommendations type: ${analysisState.data.foodRecommendations?.javaClass?.simpleName}")
                        android.util.Log.d("HealthRecommendationsFragment", "🍎 Food recommendations recommendations: ${analysisState.data.foodRecommendations?.recommendations}")
                        android.util.Log.d("HealthRecommendationsFragment", "🍎 Food recommendations recommendations size: ${analysisState.data.foodRecommendations?.recommendations?.size}")
                        
                        // Check if we have food recommendations
                        val hasFoodRecs = analysisState.data.foodRecommendations?.recommendations?.isNotEmpty() == true
                        android.util.Log.d("HealthRecommendationsFragment", "🍎 Has food recommendations: $hasFoodRecs")
                        
                        if (hasFoodRecs) {
                            showEmptyState(false)
                            analysisState.data.foodRecommendations?.let { foodRecs ->
                                android.util.Log.d("HealthRecommendationsFragment", "✅ Using food recommendations from main analysis: $foodRecs")
                                updateRecommendationsUI(foodRecs)
                            }
                        } else {
                            android.util.Log.d("HealthRecommendationsFragment", "⚠️ No food recommendations in main analysis, falling back to separate API")
                            // Fallback to separate recommendations API if main response doesn't have food recs
                            when (recommendationsState) {
                                is Resource.Success -> {
                                    showEmptyState(false)
                                    recommendationsState.data?.let { recommendations ->
                                        updateRecommendationsUI(recommendations)
                                    }
                                }
                                is Resource.Error -> {
                                    showEmptyState(true)
                                }
                                else -> {
                                    showEmptyState(true)
                                }
                            }
                        }
                    }
                    analysisState is Resource.Loading -> {
                        showEmptyState(false)
                    }
                    analysisState is Resource.Error -> {
                        showEmptyState(true)
                    }
                    else -> {
                        showEmptyState(true)
                    }
                }
            }
        }
    }
    
    private fun updateRecommendationsUI(data: FoodRecommendationsResponse) {
        android.util.Log.d("HealthRecommendationsFragment", "🔄 Updating recommendations UI with: $data")
        // Update food recommendations
        data.recommendations?.let { recommendations ->
            android.util.Log.d("HealthRecommendationsFragment", "🍽️ Submitting ${recommendations.size} food recommendations to adapter")
            android.util.Log.d("HealthRecommendationsFragment", "🍽️ Recommendations content: $recommendations")
            foodRecommendationsAdapter.submitList(recommendations)
            
            // Check if RecyclerView is visible
            android.util.Log.d("HealthRecommendationsFragment", "👁️ RecyclerView visible: ${binding.rvRecommendedFoods.isVisible}")
            android.util.Log.d("HealthRecommendationsFragment", "👁️ RecyclerView adapter count: ${foodRecommendationsAdapter.itemCount}")
            android.util.Log.d("HealthRecommendationsFragment", "👁️ RecyclerView height: ${binding.rvRecommendedFoods.height}")
            android.util.Log.d("HealthRecommendationsFragment", "👁️ RecyclerView layout params: ${binding.rvRecommendedFoods.layoutParams}")
            android.util.Log.d("HealthRecommendationsFragment", "👁️ RecyclerView child count: ${binding.rvRecommendedFoods.childCount}")
            android.util.Log.d("HealthRecommendationsFragment", "👁️ RecyclerView scroll state: ${binding.rvRecommendedFoods.scrollState}")
            android.util.Log.d("HealthRecommendationsFragment", "👁️ RecyclerView is attached to window: ${binding.rvRecommendedFoods.isAttachedToWindow}")
        } ?: run {
            android.util.Log.w("HealthRecommendationsFragment", "⚠️ No recommendations list in data")
        }
        
        // Update meal plan if available
        data.mealPlan?.let { mealPlan ->
            binding.mealPlanCard.isVisible = true
            binding.tvBreakfast.text = mealPlan.breakfast?.joinToString("\n") ?: "No suggestions"
            binding.tvLunch.text = mealPlan.lunch?.joinToString("\n") ?: "No suggestions"
            binding.tvDinner.text = mealPlan.dinner?.joinToString("\n") ?: "No suggestions"
            binding.tvSnacks.text = mealPlan.snacks?.joinToString("\n") ?: "No suggestions"
        }
        
        // Update nutrition guidance if from analysis
        viewModel.analysisState.value?.let { analysisState ->
            if (analysisState is Resource.Success) {
                analysisState.data?.nutritionGuidance?.let { guidance ->
                    binding.nutritionGuidanceCard.isVisible = true
                    
                    // Foods to avoid
                    if (!guidance.foodsToAvoid.isNullOrEmpty()) {
                        binding.foodsToAvoidSection.isVisible = true
                        binding.tvFoodsToAvoid.text = guidance.foodsToAvoid.joinToString("\n• ", "• ")
                    }
                    
                    // Supplements
                    if (!guidance.supplementRecommendations.isNullOrEmpty()) {
                        binding.supplementsSection.isVisible = true
                        binding.tvSupplements.text = guidance.supplementRecommendations.joinToString("\n• ", "• ")
                    }
                }
            }
        }
    }
    
    private fun showEmptyState(show: Boolean) {
        android.util.Log.d("HealthRecommendationsFragment", "🎭 Setting empty state: $show")
        binding.emptyState.isVisible = show
        binding.rvRecommendedFoods.isVisible = !show
        android.util.Log.d("HealthRecommendationsFragment", "🎭 Empty state visible: ${binding.emptyState.isVisible}")
        android.util.Log.d("HealthRecommendationsFragment", "🎭 RecyclerView visible: ${binding.rvRecommendedFoods.isVisible}")
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = HealthRecommendationsFragment()
    }
}
