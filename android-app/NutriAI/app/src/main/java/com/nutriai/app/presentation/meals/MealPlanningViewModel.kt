package com.nutriai.app.presentation.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriai.app.utils.Resource
import com.nutriai.app.data.remote.ApiService
import com.nutriai.app.di.NetworkModule
import kotlinx.coroutines.flow.first
import com.nutriai.app.data.local.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MealPlanningViewModel : ViewModel() {
    
    private val _mealPlansState = MutableStateFlow<Resource<List<MealPlan>>>(Resource.Loading())
    val mealPlansState: StateFlow<Resource<List<MealPlan>>> = _mealPlansState
    private val _generatedPlanPreview = MutableStateFlow<String?>(null)
    val generatedPlanPreview: StateFlow<String?> = _generatedPlanPreview

    // Hold last generated meal plan payload from server to save on confirmation
    @Suppress("UNCHECKED_CAST")
    private var lastGeneratedPlan: Map<String, Any>? = null
    
    init {
        loadMealPlans()
    }
    
    private lateinit var apiService: ApiService
    private val dataStoreManager = DataStoreManager()

    private fun ensureApi(context: android.content.Context) {
        if (!::apiService.isInitialized) {
            apiService = NetworkModule.getApiService(context)
        }
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
    
    fun generateAIRecommendedMealPlan(context: android.content.Context? = null) {
        viewModelScope.launch {
            try {
                android.util.Log.d("MealPlanningViewModel", "🤖 Generating AI recommended meal plan...")
                context?.let { ensureApi(it) }
                val token = dataStoreManager.authToken.first()
                if (::apiService.isInitialized && token != null) {
                    val body = mapOf(
                        "weekStart" to java.time.LocalDate.now().toString(),
                        "preferences" to mapOf(
                            "dietaryRestrictions" to emptyList<String>(),
                            "cookingTime" to "medium",
                            "servings" to 1,
                            "budget" to "medium"
                        )
                    )
                    val res = apiService.generateMealPlan("Bearer $token", body)
                    if (res.isSuccessful) {
                        val map = res.body() ?: emptyMap()
                        val mealPlan = (map["mealPlan"] as? Map<String, Any>)
                        lastGeneratedPlan = mealPlan

                        // Build human-readable preview (first 3 days)
                        val days = (mealPlan?.get("days") as? List<Map<String, Any>>).orEmpty()
                        val preview = StringBuilder().apply {
                            append("AI Weekly Plan Preview\n\n")
                            days.take(3).forEach { d ->
                                val dayName = (d["day"] as? String) ?: "Day"
                                val meals = d["meals"] as? Map<String, Any>
                                val b = (meals?.get("breakfast") as? Map<*, *>)?.get("name") as? String
                                val l = (meals?.get("lunch") as? Map<*, *>)?.get("name") as? String
                                val di = (meals?.get("dinner") as? Map<*, *>)?.get("name") as? String
                                append("$dayName\n• Breakfast: ${b ?: "-"}\n• Lunch: ${l ?: "-"}\n• Dinner: ${di ?: "-"}\n\n")
                            }
                            if (days.size > 3) append("…and more")
                        }.toString()
                        _generatedPlanPreview.value = preview
                        return@launch
                    }
                }
                // Fallback to mock
                _generatedPlanPreview.value = "AI Weekly Plan Preview\n(Preview unavailable)"
                
            } catch (e: Exception) {
                android.util.Log.e("MealPlanningViewModel", "❌ Error generating meal plan: ${e.message}", e)
                _mealPlansState.value = Resource.Error("Failed to generate meal plan: ${e.message}")
            }
        }
    }

    fun saveGeneratedMealPlan(context: android.content.Context, name: String = "AI Weekly Plan") {
        viewModelScope.launch {
            try {
                ensureApi(context)
                val token = dataStoreManager.authToken.first() ?: return@launch
                val body = mapOf(
                    "name" to name,
                    "mealPlan" to (lastGeneratedPlan ?: emptyMap<String, Any>())
                )
                apiService.saveMealPlan("Bearer $token", body)
                _generatedPlanPreview.value = null
                // Refresh list (shows mock entry for now)
                loadMealPlans()
            } catch (e: Exception) {
                android.util.Log.e("MealPlanningViewModel", "❌ Error saving generated meal plan: ${e.message}", e)
            }
        }
    }

    fun dismissGeneratedPreview() { _generatedPlanPreview.value = null }
    
    fun createMealPlan(name: String, description: String, context: android.content.Context? = null) {
        viewModelScope.launch {
            try {
                android.util.Log.d("MealPlanningViewModel", "📝 Creating new meal plan: $name")
                context?.let { ensureApi(it) }
                val token = dataStoreManager.authToken.first()
                if (::apiService.isInitialized && token != null) {
                    val body = mapOf(
                        "name" to name,
                        "mealPlan" to mapOf("days" to emptyList<Any>())
                    )
                    apiService.saveMealPlan("Bearer $token", body)
                }
                loadMealPlans()
                
            } catch (e: Exception) {
                android.util.Log.e("MealPlanningViewModel", "❌ Error creating meal plan: ${e.message}", e)
                _mealPlansState.value = Resource.Error("Failed to create meal plan: ${e.message}")
            }
        }
    }
}
