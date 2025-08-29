package com.nutriai.app.presentation.health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nutriai.app.R
import com.nutriai.app.databinding.FragmentHealthDashboardBinding
import com.nutriai.app.presentation.food.FoodRecognitionFragment
import com.nutriai.app.presentation.meals.MealHistoryFragment
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
    }
    
    private fun setupUI() {
        binding.swipeRefresh.setOnRefreshListener {
            // Refresh health data
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
        // Simple fade in animation for now
        binding.root.alpha = 0f
        binding.root.animate()
            .alpha(1f)
            .setDuration(500)
            .start()
        
        // Add some staggered animations for cards
        binding.root.findViewById<View>(R.id.headerSection)?.let { header ->
            header.alpha = 0f
            header.translationY = -50f
            header.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(200)
                .start()
        }
    }
    
    private fun refreshHealthData() {
        lifecycleScope.launch {
            // Simulate data refresh
            kotlinx.coroutines.delay(1000)
            binding.swipeRefresh.isRefreshing = false
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = HealthDashboardFragment()
    }
}
