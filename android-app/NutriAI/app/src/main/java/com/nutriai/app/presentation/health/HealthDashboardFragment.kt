package com.nutriai.app.presentation.health

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nutriai.app.R
import com.nutriai.app.databinding.FragmentHealthDashboardBinding
import com.nutriai.app.presentation.food.FoodRecognitionFragment
import com.nutriai.app.presentation.meals.MealHistoryFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HealthDashboardFragment : Fragment() {
    
    private var _binding: FragmentHealthDashboardBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupClickListeners()
        animateEntrance()
        animateProgressBars()
    }
    
    private fun setupUI() {
        binding.swipeRefresh.setOnRefreshListener {
            refreshHealthData()
        }
        
        // Set colors for swipe refresh
        binding.swipeRefresh.setColorSchemeResources(
            R.color.primary,
            R.color.secondary,
            R.color.accent
        )
    }
    
    private fun setupClickListeners() {
        binding.btnStartWorkout.setOnClickListener {
            // Navigate to workout screen
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, WorkoutFragment())
                .addToBackStack(null)
                .commit()
        }
        
        binding.btnLogMeal.setOnClickListener {
            // Navigate to meal logging
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MealHistoryFragment())
                .addToBackStack(null)
                .commit()
        }
    }
    
    private fun animateEntrance() {
        // Check if fragment is still attached
        if (!isAdded || context == null) return
        
        // Main container fade in
        binding.root.alpha = 0f
        binding.root.animate()
            .alpha(1f)
            .setDuration(800)
            .start()
        
        // Header slide down animation
        binding.root.findViewById<View>(R.id.headerSection)?.let { header ->
            header.alpha = 0f
            header.translationY = -100f
            header.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(200)
                .start()
        }
        
        // Staggered card animations
        val cards = listOf(
            binding.root.findViewById<View>(R.id.morningWalkCard),
            binding.root.findViewById<View>(R.id.yogaCard)
        )
        
        cards.forEachIndexed { index, card ->
            card?.let {
                it.alpha = 0f
                it.translationX = 100f
                it.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(500)
                    .setStartDelay((400 + (index * 150)).toLong())
                    .start()
            }
        }
        
        // Quick actions fade in
        binding.root.findViewById<View>(R.id.quickActionsSection)?.let { actions ->
            actions.alpha = 0f
            actions.animate()
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(800L)
                .start()
        }
    }
    
    private fun animateProgressBars() {
        lifecycleScope.launch {
            delay(1000) // Wait for entrance animations
            
            // Check if fragment is still attached
            if (!isAdded || context == null) return@launch
            
            // Find all progress bars and animate them
            val progressBars = mutableListOf<ProgressBar>()
            val progressTexts = mutableListOf<TextView>()
            
            // Find progress bars in the layout
            binding.root.findViewById<ProgressBar>(R.id.progressCalories)?.let { progressBars.add(it) }
            binding.root.findViewById<ProgressBar>(R.id.progressProtein)?.let { progressBars.add(it) }
            binding.root.findViewById<ProgressBar>(R.id.progressCarbs)?.let { progressBars.add(it) }
            
            // Find percentage texts
            binding.root.findViewById<TextView>(R.id.tvCaloriesPercentage)?.let { progressTexts.add(it) }
            binding.root.findViewById<TextView>(R.id.tvProteinPercentage)?.let { progressTexts.add(it) }
            binding.root.findViewById<TextView>(R.id.tvCarbsPercentage)?.let { progressTexts.add(it) }
            
            // Animate each progress bar
            progressBars.forEachIndexed { index, progressBar ->
                val targetProgress = progressBar.progress
                progressBar.progress = 0
                
                ValueAnimator.ofInt(0, targetProgress).apply {
                    duration = 1500
                    startDelay = (index * 200).toLong()
                    
                    addUpdateListener { animator ->
                        val progress = animator.animatedValue as Int
                        progressBar.progress = progress
                        
                        // Update percentage text
                        if (index < progressTexts.size) {
                            val percentage = (progress * 100 / progressBar.max)
                            progressTexts[index].text = "$percentage%"
                        }
                    }
                    
                    start()
                }
            }
        }
    }
    
    private fun refreshHealthData() {
        lifecycleScope.launch {
            // Simulate data refresh
            delay(1000)
            
            // Check if fragment is still attached
            if (!isAdded || context == null) return@launch
            
            // Update streak
            binding.root.findViewById<TextView>(R.id.tvStreak)?.text = "7 days"
            
            // Update progress data
            updateProgressData()
            
            binding.swipeRefresh.isRefreshing = false
        }
    }
    
    private fun updateProgressData() {
        // Update calories
        binding.root.findViewById<TextView>(R.id.tvCaloriesValue)?.text = "1200 / 2000 kcal"
        binding.root.findViewById<TextView>(R.id.tvCaloriesPercentage)?.text = "60%"
        
        // Update protein
        binding.root.findViewById<TextView>(R.id.tvProteinValue)?.text = "65 / 120 g"
        binding.root.findViewById<TextView>(R.id.tvProteinPercentage)?.text = "54%"
        
        // Update carbs
        binding.root.findViewById<TextView>(R.id.tvCarbsValue)?.text = "150 / 250 g"
        binding.root.findViewById<TextView>(R.id.tvCarbsPercentage)?.text = "60%"
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = HealthDashboardFragment()
    }
}
