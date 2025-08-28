package com.nutriai.app.presentation.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MealPlanningViewModel : ViewModel() {
    
    private val _mealPlansState = MutableStateFlow<Resource<List<MealPlan>>>(Resource.Loading())
    val mealPlansState: StateFlow<Resource<List<MealPlan>>> = _mealPlansState
    
    init {
        loadMealPlans()
    }
    
    private fun loadMealPlans() {
        viewModelScope.launch {
            try {
                _mealPlansState.value = Resource.Loading()
                
                // For now, return mock data
                // In the future, this will call the repository
                val mockMealPlans = listOf(
                    MealPlan(
                        id = "1",
                        name = "Balanced Weekly Plan",
                        description = "A balanced meal plan for the entire week",
                        days = listOf(
                            MealPlanDay(
                                day = "Monday",
                                meals = listOf(
                                    MealPlanMeal(
                                        type = "breakfast",
                                        name = "Oatmeal with Berries",
                                        calories = 320,
                                        protein = 12,
                                        carbs = 45,
                                        fat = 12,
                                        ingredients = listOf("Oats", "Berries", "Honey", "Nuts"),
                                        instructions = "Cook oats with water, top with berries and nuts"
                                    )
                                )
                            )
                        ),
                        totalCalories = 2000,
                        totalProtein = 120,
                        totalCarbs = 250,
                        totalFat = 65,
                        createdAt = "2024-01-15"
                    )
                )
                
                _mealPlansState.value = Resource.Success(mockMealPlans)
                
            } catch (e: Exception) {
                android.util.Log.e("MealPlanningViewModel", "❌ Error loading meal plans: ${e.message}", e)
                _mealPlansState.value = Resource.Error("Failed to load meal plans: ${e.message}")
            }
        }
    }
    
    fun generateAIRecommendedMealPlan() {
        viewModelScope.launch {
            try {
                android.util.Log.d("MealPlanningViewModel", "🤖 Generating AI recommended meal plan...")
                
                // For now, just reload the meal plans
                // In the future, this will call the AI service
                loadMealPlans()
                
            } catch (e: Exception) {
                android.util.Log.e("MealPlanningViewModel", "❌ Error generating meal plan: ${e.message}", e)
                _mealPlansState.value = Resource.Error("Failed to generate meal plan: ${e.message}")
            }
        }
    }
    
    fun createMealPlan(name: String, description: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("MealPlanningViewModel", "📝 Creating new meal plan: $name")
                
                // For now, just reload the meal plans
                // In the future, this will create a new meal plan
                loadMealPlans()
                
            } catch (e: Exception) {
                android.util.Log.e("MealPlanningViewModel", "❌ Error creating meal plan: ${e.message}", e)
                _mealPlansState.value = Resource.Error("Failed to create meal plan: ${e.message}")
            }
        }
    }
}
