package com.nutriai.app.presentation.meals

import android.animation.ValueAnimator
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nutriai.app.R
import com.nutriai.app.data.models.Meal
import com.nutriai.app.data.repository.MealRepository
import com.nutriai.app.databinding.FragmentMealHistoryBinding
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class MealHistoryFragment : Fragment() {
    
    private var _binding: FragmentMealHistoryBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: MealHistoryViewModel
    private lateinit var mealAdapter: MealAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMealHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize dependencies
        val mealRepository = MealRepository()
        val factory = MealHistoryViewModelFactory(mealRepository)
        viewModel = ViewModelProvider(this, factory)[MealHistoryViewModel::class.java]
        
        // Initialize ViewModel with context for dynamic network detection
        viewModel.initialize(requireContext())
        
        setupRecyclerView()
        setupDateSelector()
        observeViewModel()
        animateEntrance()
    }
    
    private fun setupRecyclerView() {
        mealAdapter = MealAdapter(
            onItemClick = { meal ->
                // Meal details are now shown inline with expand/collapse
                // No need for separate navigation
            },
            onDeleteClick = { meal ->
                showDeleteConfirmation(meal)
            }
        )
        
        binding.rvMeals.apply {
            adapter = mealAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }
    
    private fun setupDateSelector() {
        binding.dateCard.setOnClickListener {
            showDatePicker()
        }
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.time = viewModel.selectedDate.value
        
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val newDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.time
                viewModel.setSelectedDate(newDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
            show()
        }
    }
    
    private fun showDeleteConfirmation(meal: Meal) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Meal")
            .setMessage("Are you sure you want to delete this ${meal.mealType}?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteMeal(meal.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
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
        binding.root.findViewById<View>(R.id.dateCard)?.let { dateCard ->
            dateCard.alpha = 0f
            dateCard.translationY = -50f
            dateCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(200L)
                .start()
        }
        
        // Daily summary card slide up animation
        binding.dailySummaryCard.alpha = 0f
        binding.dailySummaryCard.translationY = 50f
        binding.dailySummaryCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(400L)
            .start()
        
        // Animate nutrition values
        animateNutritionValues()
    }
    
    private fun animateNutritionValues() {
        lifecycleScope.launch {
            delay(1000) // Wait for entrance animations
            
            // Check if fragment is still attached
            if (!isAdded || context == null) return@launch
            
            // Animate calories
            binding.tvTotalCalories.text = "0"
            ValueAnimator.ofInt(0, 1050).apply {
                duration = 1500
                addUpdateListener { animator ->
                    binding.tvTotalCalories.text = animator.animatedValue.toString()
                }
                start()
            }
            
            // Animate protein
            binding.tvTotalProtein.text = "0g"
            ValueAnimator.ofInt(0, 65).apply {
                duration = 1500
                startDelay = 200L
                addUpdateListener { animator ->
                    binding.tvTotalProtein.text = "${animator.animatedValue}g"
                }
                start()
            }
            
            // Animate carbs
            binding.tvTotalCarbs.text = "0g"
            ValueAnimator.ofInt(0, 85).apply {
                duration = 1500
                startDelay = 400L
                addUpdateListener { animator ->
                    binding.tvTotalCarbs.text = "${animator.animatedValue}g"
                }
                start()
            }
            
            // Animate fat
            binding.tvTotalFat.text = "0g"
            ValueAnimator.ofInt(0, 52).apply {
                duration = 1500
                startDelay = 600L
                addUpdateListener { animator ->
                    binding.tvTotalFat.text = "${animator.animatedValue}g"
                }
                start()
            }
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedDate.collect { date ->
                binding.tvSelectedDate.text = viewModel.formatDateForDisplay(date)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.mealsState.collect { state ->
                when (state) {
                    is Resource.Loading -> {
                        showLoading(true)
                    }
                    is Resource.Success -> {
                        showLoading(false)
                        state.data?.let { response ->
                            val meals = response.meals ?: emptyList()
                            if (meals.isEmpty()) {
                                showEmptyState(true)
                                mealAdapter.submitList(emptyList())
                            } else {
                                showEmptyState(false)
                                mealAdapter.submitList(meals)
                                animateMealList()
                            }
                            
                            // Update daily summary with animation
                            response.dailySummary?.let { summary ->
                                updateDailySummary(summary)
                            }
                        }
                    }
                    is Resource.Error -> {
                        showLoading(false)
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    }
                    null -> {
                        // Initial state
                    }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteState.collect { state ->
                when (state) {
                    is Resource.Success -> {
                        Toast.makeText(context, "Meal deleted successfully", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Error -> {
                        Toast.makeText(context, "Failed to delete meal", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }
    
    private fun updateDailySummary(summary: com.nutriai.app.data.models.DailySummary) {
        // Animate calories
        val targetCalories = summary.totalCalories?.toInt() ?: 0
        ValueAnimator.ofInt(0, targetCalories).apply {
            duration = 1000
            addUpdateListener { animator ->
                binding.tvTotalCalories.text = animator.animatedValue.toString()
            }
            start()
        }
        
        // Animate protein
        val targetProtein = summary.totalProtein?.toInt() ?: 0
        ValueAnimator.ofInt(0, targetProtein).apply {
            duration = 1000
            startDelay = 200L
            addUpdateListener { animator ->
                binding.tvTotalProtein.text = "${animator.animatedValue}g"
            }
            start()
        }
        
        // Animate carbs
        val targetCarbs = summary.totalCarbs?.toInt() ?: 0
        ValueAnimator.ofInt(0, targetCarbs).apply {
            duration = 1000
            startDelay = 400L
            addUpdateListener { animator ->
                binding.tvTotalCarbs.text = "${animator.animatedValue}g"
            }
            start()
        }
        
        // Animate fat
        val targetFat = summary.totalFat?.toInt() ?: 0
        ValueAnimator.ofInt(0, targetFat).apply {
            duration = 1000
            startDelay = 600L
            addUpdateListener { animator ->
                binding.tvTotalFat.text = "${animator.animatedValue}g"
            }
            start()
        }
    }
    
    private fun animateMealList() {
        // Add staggered animation to meal items
        binding.rvMeals.post {
            for (i in 0 until binding.rvMeals.childCount) {
                val child = binding.rvMeals.getChildAt(i)
                child?.let {
                    it.alpha = 0f
                    it.translationX = 100f
                    it.animate()
                        .alpha(1f)
                        .translationX(0f)
                        .setDuration(500)
                        .setStartDelay((i * 150).toLong())
                        .start()
                }
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.isVisible = show
        binding.rvMeals.isVisible = !show
        binding.dailySummaryCard.isVisible = !show
    }
    
    private fun showEmptyState(show: Boolean) {
        binding.emptyState.isVisible = show
        binding.rvMeals.isVisible = !show
        
        if (show) {
            // Animate empty state
            binding.emptyState.alpha = 0f
            binding.emptyState.animate()
                .alpha(1f)
                .setDuration(600)
                .start()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
