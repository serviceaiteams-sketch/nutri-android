package com.nutriai.app.presentation.meals

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nutriai.app.data.models.Meal
import com.nutriai.app.data.repository.MealRepository
import com.nutriai.app.databinding.FragmentMealHistoryBinding
import com.nutriai.app.utils.Resource
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
        
        setupRecyclerView()
        setupDateSelector()
        observeViewModel()
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
                            }
                            
                            // Update daily summary
                            response.dailySummary?.let { summary ->
                                binding.tvTotalCalories.text = "${summary.totalCalories?.toInt() ?: 0}"
                                binding.tvTotalProtein.text = "${summary.totalProtein?.toInt() ?: 0}g"
                                binding.tvTotalCarbs.text = "${summary.totalCarbs?.toInt() ?: 0}g"
                                binding.tvTotalFat.text = "${summary.totalFat?.toInt() ?: 0}g"
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
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.isVisible = show
        binding.rvMeals.isVisible = !show
        binding.dailySummaryCard.isVisible = !show
    }
    
    private fun showEmptyState(show: Boolean) {
        binding.emptyState.isVisible = show
        binding.rvMeals.isVisible = !show
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
