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
                        name = "🔥 Weight Loss Plan",
                        description = "AI-optimized meal plan for healthy weight loss",
                        days = listOf(
                            MealPlanDay(
                                day = "Monday",
                                meals = listOf(
                                    MealPlanMeal(
                                        type = "breakfast",
                                        name = "Greek Yogurt Parfait",
                                        calories = 280,
                                        protein = 20,
                                        carbs = 25,
                                        fat = 8,
                                        ingredients = listOf("Greek yogurt", "Berries", "Granola", "Honey"),
                                        instructions = "Layer yogurt, berries, and granola in a glass"
                                    ),
                                    MealPlanMeal(
                                        type = "lunch",
                                        name = "Grilled Chicken Salad",
                                        calories = 350,
                                        protein = 35,
                                        carbs = 15,
                                        fat = 12,
                                        ingredients = listOf("Chicken breast", "Mixed greens", "Tomatoes", "Cucumber", "Olive oil"),
                                        instructions = "Grill chicken, chop vegetables, mix with olive oil dressing"
                                    ),
                                    MealPlanMeal(
                                        type = "dinner",
                                        name = "Baked Salmon with Vegetables",
                                        calories = 420,
                                        protein = 40,
                                        carbs = 20,
                                        fat = 18,
                                        ingredients = listOf("Salmon fillet", "Broccoli", "Sweet potato", "Lemon"),
                                        instructions = "Bake salmon with lemon, roast vegetables"
                                    )
                                )
                            ),
                            MealPlanDay(
                                day = "Tuesday",
                                meals = listOf(
                                    MealPlanMeal(
                                        type = "breakfast",
                                        name = "Avocado Toast",
                                        calories = 320,
                                        protein = 15,
                                        carbs = 30,
                                        fat = 18,
                                        ingredients = listOf("Whole grain bread", "Avocado", "Egg", "Salt", "Pepper"),
                                        instructions = "Toast bread, mash avocado, top with poached egg"
                                    ),
                                    MealPlanMeal(
                                        type = "lunch",
                                        name = "Quinoa Buddha Bowl",
                                        calories = 380,
                                        protein = 18,
                                        carbs = 45,
                                        fat = 14,
                                        ingredients = listOf("Quinoa", "Chickpeas", "Kale", "Tahini", "Lemon"),
                                        instructions = "Cook quinoa, roast chickpeas, massage kale, drizzle with tahini"
                                    ),
                                    MealPlanMeal(
                                        type = "dinner",
                                        name = "Turkey Meatballs with Zoodles",
                                        calories = 400,
                                        protein = 35,
                                        carbs = 25,
                                        fat = 16,
                                        ingredients = listOf("Ground turkey", "Zucchini", "Marinara sauce", "Parmesan"),
                                        instructions = "Make turkey meatballs, spiralize zucchini, simmer in sauce"
                                    )
                                )
                            )
                        ),
                        totalCalories = 1400,
                        totalProtein = 120,
                        totalCarbs = 130,
                        totalFat = 55,
                        createdAt = "2024-01-15"
                    ),
                    MealPlan(
                        id = "2",
                        name = "💪 Muscle Gain Plan",
                        description = "High-protein meal plan for muscle building",
                        days = listOf(
                            MealPlanDay(
                                day = "Monday",
                                meals = listOf(
                                    MealPlanMeal(
                                        type = "breakfast",
                                        name = "Protein Pancakes",
                                        calories = 450,
                                        protein = 35,
                                        carbs = 40,
                                        fat = 15,
                                        ingredients = listOf("Protein powder", "Oats", "Banana", "Eggs", "Almond milk"),
                                        instructions = "Blend ingredients, cook as pancakes"
                                    ),
                                    MealPlanMeal(
                                        type = "lunch",
                                        name = "Chicken & Rice Bowl",
                                        calories = 550,
                                        protein = 45,
                                        carbs = 60,
                                        fat = 12,
                                        ingredients = listOf("Chicken breast", "Brown rice", "Broccoli", "Soy sauce"),
                                        instructions = "Grill chicken, cook rice, steam broccoli, season with soy sauce"
                                    ),
                                    MealPlanMeal(
                                        type = "dinner",
                                        name = "Beef Stir Fry",
                                        calories = 520,
                                        protein = 40,
                                        carbs = 35,
                                        fat = 20,
                                        ingredients = listOf("Lean beef", "Bell peppers", "Onions", "Brown rice", "Ginger"),
                                        instructions = "Stir fry beef and vegetables, serve over rice"
                                    )
                                )
                            )
                        ),
                        totalCalories = 2200,
                        totalProtein = 180,
                        totalCarbs = 200,
                        totalFat = 80,
                        createdAt = "2024-01-14"
                    ),
                    MealPlan(
                        id = "3",
                        name = "🥗 Vegetarian Delight",
                        description = "Plant-based meal plan with complete nutrition",
                        days = listOf(
                            MealPlanDay(
                                day = "Monday",
                                meals = listOf(
                                    MealPlanMeal(
                                        type = "breakfast",
                                        name = "Smoothie Bowl",
                                        calories = 380,
                                        protein = 15,
                                        carbs = 50,
                                        fat = 12,
                                        ingredients = listOf("Frozen berries", "Banana", "Spinach", "Almond butter", "Chia seeds"),
                                        instructions = "Blend fruits and spinach, top with almond butter and chia seeds"
                                    ),
                                    MealPlanMeal(
                                        type = "lunch",
                                        name = "Lentil Curry",
                                        calories = 420,
                                        protein = 20,
                                        carbs = 55,
                                        fat = 10,
                                        ingredients = listOf("Red lentils", "Coconut milk", "Curry spices", "Rice"),
                                        instructions = "Cook lentils in coconut milk with spices, serve over rice"
                                    ),
                                    MealPlanMeal(
                                        type = "dinner",
                                        name = "Stuffed Bell Peppers",
                                        calories = 350,
                                        protein = 18,
                                        carbs = 40,
                                        fat = 12,
                                        ingredients = listOf("Bell peppers", "Quinoa", "Black beans", "Cheese", "Herbs"),
                                        instructions = "Hollow peppers, stuff with quinoa and beans, bake"
                                    )
                                )
                            )
                        ),
                        totalCalories = 1800,
                        totalProtein = 100,
                        totalCarbs = 220,
                        totalFat = 60,
                        createdAt = "2024-01-13"
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

                        // Build human-readable preview (all days)
                        val days = (mealPlan?.get("days") as? List<Map<String, Any>>).orEmpty()
                        val preview = StringBuilder().apply {
                            append("AI Weekly Plan Preview\n\n")
                            days.forEach { d ->
                                val dayName = (d["day"] as? String) ?: "Day"
                                val meals = d["meals"] as? Map<String, Any>
                                fun mealLine(key: String): String {
                                    val m = meals?.get(key) as? Map<*, *>
                                    val n = m?.get("name") as? String
                                    val cal = (m?.get("nutrition") as? Map<*, *>)?.get("calories")
                                    val c = when (cal) {
                                        is Number -> cal.toInt()
                                        is String -> cal
                                        else -> null
                                    }
                                    return if (n != null) {
                                        if (c != null) "$n (${c} cal)" else n
                                    } else "-"
                                }
                                append("$dayName\n")
                                append("• Breakfast: ${mealLine("breakfast")}\n")
                                append("• Lunch: ${mealLine("lunch")}\n")
                                append("• Dinner: ${mealLine("dinner")}\n\n")
                            }
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
