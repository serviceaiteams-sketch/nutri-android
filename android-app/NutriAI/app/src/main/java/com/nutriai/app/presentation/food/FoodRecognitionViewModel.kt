package com.nutriai.app.presentation.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriai.app.data.models.FoodRecognitionResponse
import com.nutriai.app.data.models.MealLogRequest
import com.nutriai.app.data.models.MealLogResponse
import com.nutriai.app.data.models.MealFoodItem
import com.nutriai.app.data.models.RecognizedFood
import com.nutriai.app.data.models.TotalNutrition
import com.nutriai.app.data.repository.FoodRepository
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File

class FoodRecognitionViewModel(private val foodRepository: FoodRepository) : ViewModel() {
    
    /**
     * Initialize the ViewModel with context for network detection
     */
    fun initialize(context: android.content.Context) {
        foodRepository.initialize(context)
    }
    
    private val _recognitionState = MutableStateFlow<Resource<FoodRecognitionResponse>?>(null)
    val recognitionState: StateFlow<Resource<FoodRecognitionResponse>?> = _recognitionState
    
    private val _logMealState = MutableStateFlow<Resource<MealLogResponse>?>(null)
    val logMealState: StateFlow<Resource<MealLogResponse>?> = _logMealState
    
    private val _selectedFood = MutableStateFlow<RecognizedFood?>(null)
    val selectedFood: StateFlow<RecognizedFood?> = _selectedFood
    
    fun recognizeFood(imageFile: File) {
        viewModelScope.launch {
            foodRepository.recognizeFood(imageFile)
                .onEach { result ->
                    _recognitionState.value = result
                    
                    // Auto-select first food if available
                    if (result is Resource.Success && result.data?.recognizedFoods?.isNotEmpty() == true) {
                        _selectedFood.value = result.data.recognizedFoods.first()
                    }
                }
                .launchIn(viewModelScope)
        }
    }
    
    fun selectFood(food: RecognizedFood) {
        _selectedFood.value = food
    }
    
    fun logSelectedFood(mealType: String) {
        val selectedFoods = _recognitionState.value?.data?.recognizedFoods?.filter { food ->
            // Get selected foods from adapter or use all if single food
            true // For now, log all recognized foods
        } ?: return
        
        val foodItems = selectedFoods.map { food ->
            MealFoodItem(
                name = food.name,
                quantity = food.quantity ?: 1f,
                unit = food.unit ?: "serving",
                calories = food.nutrition?.calories,
                protein = food.nutrition?.protein,
                carbs = food.nutrition?.carbs,
                fat = food.nutrition?.fat,
                sugar = food.nutrition?.sugar,
                sodium = food.nutrition?.sodium,
                fiber = food.nutrition?.fiber,
                confidenceScore = food.confidence,
                isHealthy = true
            )
        }
        
        // Calculate total nutrition
        val totalNutrition = if (foodItems.isNotEmpty()) {
            TotalNutrition(
                calories = foodItems.sumOf { (it.calories ?: 0f).toDouble() }.toFloat(),
                protein = foodItems.sumOf { (it.protein ?: 0f).toDouble() }.toFloat(),
                carbs = foodItems.sumOf { (it.carbs ?: 0f).toDouble() }.toFloat(),
                fat = foodItems.sumOf { (it.fat ?: 0f).toDouble() }.toFloat()
            )
        } else null
        
        val request = MealLogRequest(
            mealType = mealType,
            foodItems = foodItems,
            totalNutrition = totalNutrition,
            imageUrl = _recognitionState.value?.data?.imageUrl
        )
        
        viewModelScope.launch {
            foodRepository.logMeal(request)
                .onEach { result ->
                    _logMealState.value = result
                }
                .launchIn(viewModelScope)
        }
    }
    
    fun resetStates() {
        _recognitionState.value = null
        _logMealState.value = null
        _selectedFood.value = null
    }
}
