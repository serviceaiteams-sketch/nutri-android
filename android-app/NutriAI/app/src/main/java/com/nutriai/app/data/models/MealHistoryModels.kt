package com.nutriai.app.data.models

import com.google.gson.annotations.SerializedName

// Meal History Response
data class MealHistoryResponse(
    @SerializedName("meals")
    val meals: List<Meal>? = null,
    
    @SerializedName("dailySummary")
    val dailySummary: DailySummary? = null
)

// Individual Meal
data class Meal(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("meal_type")
    val mealType: String,
    
    @SerializedName("food_items")
    val foodItems: List<FoodItem>? = null,
    
    @SerializedName("total_calories")
    val totalCalories: Float? = null,
    
    @SerializedName("total_protein")
    val totalProtein: Float? = null,
    
    @SerializedName("total_carbs")
    val totalCarbs: Float? = null,
    
    @SerializedName("total_fat")
    val totalFat: Float? = null,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("image_url")
    val imageUrl: String? = null
)

// Food Item in Meal
data class FoodItem(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("food_name")
    val foodName: String,
    
    @SerializedName("quantity")
    val quantity: Float? = null,
    
    @SerializedName("unit")
    val unit: String? = null,
    
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

// Daily Summary
data class DailySummary(
    @SerializedName("totalCalories")
    val totalCalories: Float? = null,
    
    @SerializedName("totalProtein")
    val totalProtein: Float? = null,
    
    @SerializedName("totalCarbs")
    val totalCarbs: Float? = null,
    
    @SerializedName("totalFat")
    val totalFat: Float? = null,
    
    @SerializedName("mealCount")
    val mealCount: Int? = null
)
