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
        // Animate header with slide up
        val headerAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_up)
        binding.root.findViewById<View>(R.id.headerSection)?.startAnimation(headerAnimation)
        
        // Animate stats cards with staggered delays
        val statsCards = listOf(
            binding.root.findViewById<View>(R.id.stepsCard),
            binding.root.findViewById<View>(R.id.caloriesCard)
        )
        
        statsCards.forEachIndexed { index, card ->
            card?.postDelayed({
                val cardAnimation = AnimationUtils.loadAnimation(context, R.anim.scale_in)
                card.startAnimation(cardAnimation)
            }, 200L + (index * 100L))
        }
        
        // Animate activity cards
        val activityCards = listOf(
            binding.root.findViewById<View>(R.id.morningWalkCard),
            binding.root.findViewById<View>(R.id.yogaCard)
        )
        
        activityCards.forEachIndexed { index, card ->
            card?.postDelayed({
                val cardAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_up)
                card.startAnimation(cardAnimation)
            }, 600L + (index * 150L))
        }
        
        // Animate quick actions
        binding.root.findViewById<View>(R.id.quickActionsSection)?.postDelayed({
            val actionsAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
            binding.root.findViewById<View>(R.id.quickActionsSection)?.startAnimation(actionsAnimation)
        }, 1000L)
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
