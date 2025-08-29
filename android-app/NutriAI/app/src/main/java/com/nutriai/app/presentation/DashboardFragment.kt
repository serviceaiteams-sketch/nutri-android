package com.nutriai.app.presentation

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.nutriai.app.R
import com.nutriai.app.data.models.DashboardResponse
import com.nutriai.app.data.models.NutrientStat
import com.nutriai.app.data.repository.DashboardRepository
import com.nutriai.app.databinding.FragmentDashboardBinding
import com.nutriai.app.databinding.ItemNutrientCardBinding
import com.nutriai.app.presentation.dashboard.DashboardViewModel
import com.nutriai.app.presentation.dashboard.DashboardViewModelFactory
import com.nutriai.app.presentation.food.FoodRecognitionFragment
import com.nutriai.app.presentation.meals.MealHistoryFragment
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: DashboardViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize dependencies
        val dashboardRepository = DashboardRepository()
        val factory = DashboardViewModelFactory(dashboardRepository)
        viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]
        
        // Initialize ViewModel with context for dynamic network detection
        viewModel.initialize(requireContext())
        
        setupUI()
        observeDashboardData()
        setupClickListeners()
    }
    
    private fun setupUI() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
        
        // Set colors for swipe refresh
        binding.swipeRefresh.setColorSchemeResources(
            R.color.primary,
            R.color.secondary,
            R.color.accent
        )
        
        // Add entrance animations
        animateEntrance()
    }
    
    private fun animateEntrance() {
        // Animate header card
        val headerAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_up)
        binding.headerCard.startAnimation(headerAnimation)
        
        // Animate streak card with delay
        binding.cardStreak.postDelayed({
            val streakAnimation = AnimationUtils.loadAnimation(context, R.anim.scale_in)
            binding.cardStreak.startAnimation(streakAnimation)
        }, 200)
        
        // Animate nutrition cards with staggered delays
        val nutritionCards = listOf(
            binding.caloriesCard.root,
            binding.proteinCard.root,
            binding.carbsCard.root,
            binding.fatCard.root
        )
        
        nutritionCards.forEachIndexed { index, card ->
            card.postDelayed({
                val cardAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_up)
                card.startAnimation(cardAnimation)
            }, 400 + (index * 100))
        }
        
        // Animate quick actions with delay
        binding.quickActionsContainer.postDelayed({
            val actionsAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
            binding.quickActionsContainer.startAnimation(actionsAnimation)
        }, 800)
    }
    
    private fun observeDashboardData() {
        lifecycleScope.launch {
            viewModel.dashboardState.collect { state ->
                when (state) {
                    is Resource.Loading -> {
                        showLoading(true)
                    }
                    is Resource.Success -> {
                        showLoading(false)
                        updateDashboard(state.data!!)
                    }
                    is Resource.Error -> {
                        showLoading(false)
                        showError(state.message ?: "Failed to load dashboard")
                    }
                    null -> {
                        // Initial state
                    }
                }
            }
        }
    }
    
    private fun updateDashboard(data: DashboardResponse) {
        // Update user info with null safety
        data.user?.let { user ->
            binding.tvUserName.text = user.name ?: "User"
            binding.tvStreak.text = "${user.streak ?: 0} days"
        }
        
        // Update nutrition cards with null safety
        data.todayStats?.let { stats ->
            stats.calories?.let { nutrient ->
                updateNutrientCard(
                    binding.caloriesCard,
                    "Calories",
                    nutrient,
                    R.color.accent
                )
            }
            
            stats.protein?.let { nutrient ->
                updateNutrientCard(
                    binding.proteinCard,
                    "Protein",
                    nutrient,
                    R.color.info
                )
            }
            
            stats.carbs?.let { nutrient ->
                updateNutrientCard(
                    binding.carbsCard,
                    "Carbs",
                    nutrient,
                    R.color.warning
                )
            }
            
            stats.fat?.let { nutrient ->
                updateNutrientCard(
                    binding.fatCard,
                    "Fat",
                    nutrient,
                    R.color.success
                )
            }
        }
    }
    
    private fun updateNutrientCard(
        cardBinding: ItemNutrientCardBinding,
        name: String,
        stat: NutrientStat,
        progressColor: Int
    ) {
        cardBinding.tvNutrientName.text = name
        val consumed = stat.consumed ?: 0f
        val target = stat.target ?: 2000f // Default target
        val unit = stat.unit ?: "kcal"
        val percentage = stat.percentage ?: 0f
        
        cardBinding.tvNutrientValue.text = "${consumed.toInt()} / ${target.toInt()} $unit"
        cardBinding.progressNutrient.progress = percentage.toInt().coerceIn(0, 100)
        cardBinding.tvNutrientPercentage.text = "${percentage.toInt()}%"
        
        // Set progress color
        cardBinding.progressNutrient.progressDrawable?.setTint(
            resources.getColor(progressColor, null)
        )
    }
    
    private fun setupClickListeners() {
        binding.btnLogMeal.setOnClickListener {
            // Add button press animation
            animateButtonPress(binding.btnLogMeal) {
                // Navigate to meal history to show logged meals
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, MealHistoryFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
        
        binding.btnScanFood.setOnClickListener {
            // Add button press animation
            animateButtonPress(binding.btnScanFood) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, FoodRecognitionFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
    
    private fun animateButtonPress(button: View, onComplete: () -> Unit) {
        val scaleDown = ObjectAnimator.ofFloat(button, "scaleX", 0.95f)
        val scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", 0.95f)
        val scaleUp = ObjectAnimator.ofFloat(button, "scaleX", 1.0f)
        val scaleUpY = ObjectAnimator.ofFloat(button, "scaleY", 1.0f)
        
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleDown, scaleDownY)
        animatorSet.duration = 100
        
        val animatorSet2 = AnimatorSet()
        animatorSet2.playTogether(scaleUp, scaleUpY)
        animatorSet2.duration = 100
        
        val finalSet = AnimatorSet()
        finalSet.playSequentially(animatorSet, animatorSet2)
        finalSet.start()
        
        finalSet.addListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationStart(animation: android.animation.Animator) {}
            override fun onAnimationEnd(animation: android.animation.Animator) {
                onComplete()
            }
            override fun onAnimationCancel(animation: android.animation.Animator) {}
            override fun onAnimationRepeat(animation: android.animation.Animator) {}
        })
    }
    
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.swipeRefresh.isRefreshing = false
    }
    
    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

