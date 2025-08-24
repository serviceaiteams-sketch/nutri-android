package com.nutriai.app.presentation.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nutriai.app.data.repository.FoodRepository

class FoodRecognitionViewModelFactory(
    private val foodRepository: FoodRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodRecognitionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FoodRecognitionViewModel(foodRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
