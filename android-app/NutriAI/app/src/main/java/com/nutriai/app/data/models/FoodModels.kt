package com.nutriai.app.data.models

import com.google.gson.annotations.SerializedName
import java.io.File

// Food Recognition Request
data class FoodRecognitionRequest(
    @SerializedName("image")
    val image: String // Base64 encoded image
)

// Food Recognition Response
data class FoodRecognitionResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("recognizedFoods")
    val recognizedFoods: List<RecognizedFood>? = null,
    
    @SerializedName("nutritionData")
    val nutritionData: List<NutritionData>? = null,
    
    @SerializedName("totalNutrition")
    val totalNutrition: TotalNutrition? = null,
    
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("error")
    val error: String? = null
)

// Recognized Food Item
data class RecognizedFood(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("confidence")
    val confidence: Float? = null,
    
    @SerializedName("quantity")
    val quantity: Float? = null,
    
    @SerializedName("unit")
    val unit: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("nutrition")
    val nutrition: FoodNutrition? = null,
    
    @SerializedName("serving_size")
    val servingSize: String? = null,
    
    @SerializedName("health_score")
    val healthScore: Int? = null
)

// Food Nutrition Information
data class FoodNutrition(
    @SerializedName("calories")
    val calories: Float? = null,
    
    @SerializedName("protein")
    val protein: Float? = null,
    
    @SerializedName("carbs")
    val carbs: Float? = null,
    
    @SerializedName("fat")
    val fat: Float? = null,
    
    @SerializedName("fiber")
    val fiber: Float? = null,
    
    @SerializedName("sugar")
    val sugar: Float? = null,
    
    @SerializedName("sodium")
    val sodium: Float? = null
)

// Meal Log Request - Backend format
data class MealLogRequest(
    @SerializedName("meal_type")
    val mealType: String, // breakfast, lunch, dinner, snack
    
    @SerializedName("food_items")
    val foodItems: List<MealFoodItem>,
    
    @SerializedName("total_nutrition")
    val totalNutrition: TotalNutrition? = null,
    
    @SerializedName("image_url")
    val imageUrl: String? = null,
    
    @SerializedName("date")
    val date: String? = null
)

// Food Item for Meal Logging
data class MealFoodItem(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("quantity")
    val quantity: Float = 1f,
    
    @SerializedName("unit")
    val unit: String? = "serving",
    
    @SerializedName("calories")
    val calories: Float? = null,
    
    @SerializedName("protein")
    val protein: Float? = null,
    
    @SerializedName("carbs")
    val carbs: Float? = null,
    
    @SerializedName("fat")
    val fat: Float? = null,
    
    @SerializedName("sugar")
    val sugar: Float? = null,
    
    @SerializedName("sodium")
    val sodium: Float? = null,
    
    @SerializedName("fiber")
    val fiber: Float? = null,
    
    @SerializedName("confidence_score")
    val confidenceScore: Float? = null,
    
    @SerializedName("is_healthy")
    val isHealthy: Boolean? = true
)

// Meal Log Response
data class MealLogResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("meal")
    val meal: LoggedMeal? = null,
    
    @SerializedName("message")
    val message: String? = null
)

// Logged Meal
data class LoggedMeal(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("food_name")
    val foodName: String,
    
    @SerializedName("meal_type")
    val mealType: String,
    
    @SerializedName("logged_at")
    val loggedAt: String
)

// Nutrition Data from API
data class NutritionData(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("calories")
    val calories: Float? = null,
    
    @SerializedName("protein")
    val protein: Float? = null,
    
    @SerializedName("carbs")
    val carbs: Float? = null,
    
    @SerializedName("fat")
    val fat: Float? = null,
    
    @SerializedName("fiber")
    val fiber: Float? = null,
    
    @SerializedName("sugar")
    val sugar: Float? = null,
    
    @SerializedName("sodium")
    val sodium: Float? = null
)

// Total Nutrition
data class TotalNutrition(
    @SerializedName("calories")
    val calories: Float? = null,
    
    @SerializedName("protein")
    val protein: Float? = null,
    
    @SerializedName("carbs")
    val carbs: Float? = null,
    
    @SerializedName("fat")
    val fat: Float? = null
)
