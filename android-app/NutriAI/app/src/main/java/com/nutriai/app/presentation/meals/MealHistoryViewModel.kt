package com.nutriai.app.presentation.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriai.app.data.models.MealHistoryResponse
import com.nutriai.app.data.models.GenericResponse
import com.nutriai.app.data.repository.MealRepository
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MealHistoryViewModel(
    private val mealRepository: MealRepository
) : ViewModel() {
    
    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()
    
    private val _mealsState = MutableStateFlow<Resource<MealHistoryResponse>?>(null)
    val mealsState: StateFlow<Resource<MealHistoryResponse>?> = _mealsState.asStateFlow()
    
    private val _deleteState = MutableStateFlow<Resource<GenericResponse>?>(null)
    val deleteState: StateFlow<Resource<GenericResponse>?> = _deleteState.asStateFlow()
    
    /**
     * Initialize the ViewModel with context for network detection
     */
    fun initialize(context: android.content.Context) {
        mealRepository.initialize(context)
        loadMealsForDate(_selectedDate.value)
    }
    
    fun setSelectedDate(date: Date) {
        _selectedDate.value = date
        loadMealsForDate(date)
    }
    
    fun refreshMeals() {
        loadMealsForDate(_selectedDate.value)
    }
    
    private fun loadMealsForDate(date: Date) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = dateFormat.format(date)
        
        viewModelScope.launch {
            mealRepository.getMealsByDate(dateString)
                .onEach { result ->
                    _mealsState.value = result
                }
                .launchIn(viewModelScope)
        }
    }
    
    fun deleteMeal(mealId: Int) {
        viewModelScope.launch {
            mealRepository.deleteMeal(mealId)
                .onEach { result ->
                    _deleteState.value = result
                    // Refresh meals list after successful deletion
                    if (result is Resource.Success) {
                        refreshMeals()
                    }
                }
                .launchIn(viewModelScope)
        }
    }
    
    fun formatDateForDisplay(date: Date): String {
        val today = Calendar.getInstance()
        val selectedCal = Calendar.getInstance().apply { time = date }
        
        return when {
            isSameDay(today, selectedCal) -> "Today"
            isYesterday(today, selectedCal) -> "Yesterday"
            else -> {
                val format = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
                format.format(date)
            }
        }
    }
    
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
    
    private fun isYesterday(today: Calendar, date: Calendar): Boolean {
        val yesterday = Calendar.getInstance().apply {
            time = today.time
            add(Calendar.DAY_OF_YEAR, -1)
        }
        return isSameDay(yesterday, date)
    }
}
