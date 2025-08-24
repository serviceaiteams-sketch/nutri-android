package com.nutriai.app.presentation.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nutriai.app.data.repository.HealthReportsRepository

class HealthReportsViewModelFactory(
    private val healthReportsRepository: HealthReportsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HealthReportsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HealthReportsViewModel(healthReportsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
