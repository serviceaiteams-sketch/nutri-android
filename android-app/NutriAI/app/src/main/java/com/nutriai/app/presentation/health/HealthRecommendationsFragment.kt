package com.nutriai.app.presentation.health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.nutriai.app.data.models.FoodRecommendationsResponse
import kotlinx.coroutines.flow.combine
import com.nutriai.app.databinding.FragmentHealthRecommendationsBinding
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.launch
import com.nutriai.app.presentation.health.HealthReportsFragment

class HealthRecommendationsFragment : Fragment() {
    
    private var _binding: FragmentHealthRecommendationsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: HealthReportsViewModel
    private lateinit var foodRecommendationsViewPagerAdapter: FoodRecommendationsViewPagerAdapter
    
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
                // Try to get ViewModel from activity as fallback
                viewModel = ViewModelProvider(requireActivity())[HealthReportsViewModel::class.java]
                android.util.Log.d("HealthRecommendationsFragment", "✅ ViewModel obtained from activity as fallback")
            }
            
            setupViewPager()
            observeViewModel()
        } catch (e: Exception) {
            android.util.Log.e("HealthRecommendationsFragment", "❌ Error in onViewCreated: ${e.message}", e)
            // Show empty state if setup fails
            showEmptyState(true)
        }
    }
    
    private fun setupViewPager() {
        try {
            android.util.Log.d("HealthRecommendationsFragment", "🔧 Setting up ViewPager...")
            
            // Check if binding is available
            if (_binding == null) {
                android.util.Log.e("HealthRecommendationsFragment", "❌ Binding is null")
                return
            }
            
            foodRecommendationsViewPagerAdapter = FoodRecommendationsViewPagerAdapter()
            
            // Check if ViewPager exists
            if (binding.viewPagerFoodRecommendations == null) {
                android.util.Log.e("HealthRecommendationsFragment", "❌ ViewPager is null")
                return
            }
            
            binding.viewPagerFoodRecommendations.apply {
                adapter = foodRecommendationsViewPagerAdapter
                orientation = ViewPager2.ORIENTATION_HORIZONTAL
                android.util.Log.d("HealthRecommendationsFragment", "🔧 ViewPager adapter set: $adapter")
            }
            
            // Setup page indicator with null check
            try {
                if (binding.tabLayoutFoodRecommendations != null) {
                    TabLayoutMediator(binding.tabLayoutFoodRecommendations, binding.viewPagerFoodRecommendations) { _, _ ->
                        // This lambda is called for each tab, but we don't need to do anything here
                    }.attach()
                }
            } catch (e: Exception) {
                android.util.Log.e("HealthRecommendationsFragment", "❌ Error setting up TabLayout: ${e.message}", e)
            }
            
            android.util.Log.d("HealthRecommendationsFragment", "🔧 ViewPager setup complete. Adapter: $foodRecommendationsViewPagerAdapter")
        } catch (e: Exception) {
            android.util.Log.e("HealthRecommendationsFragment", "❌ Error setting up ViewPager: ${e.message}", e)
            // Show empty state if ViewPager setup fails
            showEmptyState(true)
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Check if ViewModel is initialized
                if (!::viewModel.isInitialized) {
                    android.util.Log.e("HealthRecommendationsFragment", "❌ ViewModel not initialized")
                    showEmptyState(true)
                    return@launch
                }
                
                // Add a small delay to ensure ViewModel is ready
                kotlinx.coroutines.delay(100)
                
                android.util.Log.d("HealthRecommendationsFragment", "🔍 Starting ViewModel observation...")
                
                // Observe both analysis state and recommendations state
                combine(
                    viewModel.analysisState,
                    viewModel.recommendationsState
                ) { analysisState, recommendationsState ->
                    Pair(analysisState, recommendationsState)
                }.collect { (analysisState, recommendationsState) ->
                    try {
                        android.util.Log.d("HealthRecommendationsFragment", "📊 Received state update - Analysis: $analysisState, Recommendations: $recommendationsState")
                        
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
                                            android.util.Log.w("HealthRecommendationsFragment", "⚠️ Recommendations API error: ${recommendationsState.message}")
                                            showEmptyState(true)
                                        }
                                        else -> {
                                            android.util.Log.d("HealthRecommendationsFragment", "ℹ️ Recommendations state: $recommendationsState")
                                            showEmptyState(true)
                                        }
                                    }
                                }
                            }
                            analysisState is Resource.Loading -> {
                                android.util.Log.d("HealthRecommendationsFragment", "⏳ Analysis loading...")
                                showEmptyState(false)
                            }
                            analysisState is Resource.Error -> {
                                android.util.Log.w("HealthRecommendationsFragment", "❌ Analysis error: ${analysisState.message}")
                                showEmptyState(true)
                            }
                            else -> {
                                android.util.Log.d("HealthRecommendationsFragment", "ℹ️ Analysis state: $analysisState")
                                showEmptyState(true)
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("HealthRecommendationsFragment", "❌ Error processing state: ${e.message}", e)
                        showEmptyState(true)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("HealthRecommendationsFragment", "❌ Error in observeViewModel: ${e.message}", e)
                showEmptyState(true)
            }
        }
    }
    
    private fun updateRecommendationsUI(data: FoodRecommendationsResponse) {
        try {
            android.util.Log.d("HealthRecommendationsFragment", "🔄 Updating recommendations UI with: $data")
            
            // Update food recommendations
            data.recommendations?.let { recommendations ->
                try {
                    android.util.Log.d("HealthRecommendationsFragment", "🍽️ Submitting ${recommendations.size} food recommendations to ViewPager adapter")
                    android.util.Log.d("HealthRecommendationsFragment", "🍽️ Recommendations content: $recommendations")
                    
                    // Check if adapter is initialized
                    if (::foodRecommendationsViewPagerAdapter.isInitialized) {
                        // Run on main thread to avoid crashes
                        binding.root.post {
                            try {
                                foodRecommendationsViewPagerAdapter.submitList(recommendations)
                                
                                // Check if ViewPager is visible
                                android.util.Log.d("HealthRecommendationsFragment", "👁️ ViewPager visible: ${binding.viewPagerFoodRecommendations.isVisible}")
                                android.util.Log.d("HealthRecommendationsFragment", "👁️ ViewPager adapter count: ${foodRecommendationsViewPagerAdapter.itemCount}")
                                android.util.Log.d("HealthRecommendationsFragment", "👁️ ViewPager current item: ${binding.viewPagerFoodRecommendations.currentItem}")
                            } catch (e: Exception) {
                                android.util.Log.e("HealthRecommendationsFragment", "❌ Error in ViewPager update: ${e.message}", e)
                                showEmptyState(true)
                            }
                        }
                    } else {
                        android.util.Log.w("HealthRecommendationsFragment", "⚠️ ViewPager adapter not initialized")
                        showEmptyState(true)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("HealthRecommendationsFragment", "❌ Error updating food recommendations: ${e.message}", e)
                    showEmptyState(true)
                }
            } ?: run {
                android.util.Log.w("HealthRecommendationsFragment", "⚠️ No recommendations list in data")
                showEmptyState(true)
            }
            
            // Update meal plan if available
            data.mealPlan?.let { mealPlan ->
                try {
                    binding.mealPlanCard.isVisible = true
                    
                    // Format breakfast items
                    val breakfastText = mealPlan.breakfast?.joinToString("\n") { item ->
                        "${item.day ?: ""}: ${item.meal ?: ""} (${item.calories ?: 0} cal)"
                    } ?: "No suggestions"
                    binding.tvBreakfast.text = breakfastText
                    
                    // Format lunch items
                    val lunchText = mealPlan.lunch?.joinToString("\n") { item ->
                        "${item.day ?: ""}: ${item.meal ?: ""} (${item.calories ?: 0} cal)"
                    } ?: "No suggestions"
                    binding.tvLunch.text = lunchText
                    
                    // Format dinner items
                    val dinnerText = mealPlan.dinner?.joinToString("\n") { item ->
                        "${item.day ?: ""}: ${item.meal ?: ""} (${item.calories ?: 0} cal)"
                    } ?: "No suggestions"
                    binding.tvDinner.text = dinnerText
                    
                    // Format snacks items
                    val snacksText = mealPlan.snacks?.joinToString("\n") { item ->
                        "${item.day ?: ""}: ${item.meal ?: ""} (${item.calories ?: 0} cal)"
                    } ?: "No suggestions"
                    binding.tvSnacks.text = snacksText
                } catch (e: Exception) {
                    android.util.Log.e("HealthRecommendationsFragment", "❌ Error updating meal plan: ${e.message}", e)
                }
            }
            
            // Update nutrition guidance if from analysis
            try {
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
            } catch (e: Exception) {
                android.util.Log.e("HealthRecommendationsFragment", "❌ Error updating nutrition guidance: ${e.message}", e)
            }
        } catch (e: Exception) {
            android.util.Log.e("HealthRecommendationsFragment", "❌ Error in updateRecommendationsUI: ${e.message}", e)
            showEmptyState(true)
        }
    }
    
    private fun showEmptyState(show: Boolean) {
        try {
            android.util.Log.d("HealthRecommendationsFragment", "🎭 Setting empty state: $show")
            binding.emptyState.isVisible = show
            binding.viewPagerFoodRecommendations.isVisible = !show
            android.util.Log.d("HealthRecommendationsFragment", "🎭 Empty state visible: ${binding.emptyState.isVisible}")
            android.util.Log.d("HealthRecommendationsFragment", "🎭 ViewPager visible: ${binding.viewPagerFoodRecommendations.isVisible}")
        } catch (e: Exception) {
            android.util.Log.e("HealthRecommendationsFragment", "❌ Error setting empty state: ${e.message}", e)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = HealthRecommendationsFragment()
    }
}
