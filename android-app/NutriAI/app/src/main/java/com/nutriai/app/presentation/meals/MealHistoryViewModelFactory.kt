package com.nutriai.app.presentation.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nutriai.app.data.repository.MealRepository

class MealHistoryViewModelFactory(
    private val mealRepository: MealRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MealHistoryViewModel(mealRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
