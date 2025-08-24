package com.nutriai.app.data.models

import com.google.gson.annotations.SerializedName

// Dashboard Response
data class DashboardResponse(
    @SerializedName("user")
    val user: UserStats? = null,
    
    @SerializedName("todayStats")
    val todayStats: TodayStats? = null,
    
    @SerializedName("recentMeals")
    val recentMeals: List<RecentMeal>? = null,
    
    @SerializedName("weeklyProgress")
    val weeklyProgress: WeeklyProgress? = null,
    
    @SerializedName("recommendations")
    val recommendations: List<String>? = null
)

// User Statistics
data class UserStats(
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("streak")
    val streak: Int? = null,
    
    @SerializedName("totalMeals")
    val totalMeals: Int? = null,
    
    @SerializedName("joinedDays")
    val joinedDays: Int? = null
)

// Today's Statistics
data class TodayStats(
    @SerializedName("calories")
    val calories: NutrientStat? = null,
    
    @SerializedName("protein")
    val protein: NutrientStat? = null,
    
    @SerializedName("carbs")
    val carbs: NutrientStat? = null,
    
    @SerializedName("fat")
    val fat: NutrientStat? = null,
    
    @SerializedName("water")
    val water: WaterStat? = null
)

// Nutrient Statistics
data class NutrientStat(
    @SerializedName("consumed")
    val consumed: Float? = null,
    
    @SerializedName("target")
    val target: Float? = null,
    
    @SerializedName("unit")
    val unit: String? = null,
    
    @SerializedName("percentage")
    val percentage: Float? = null
)

// Water Statistics
data class WaterStat(
    @SerializedName("consumed")
    val consumed: Int,
    
    @SerializedName("target")
    val target: Int,
    
    @SerializedName("unit")
    val unit: String = "glasses"
)

// Recent Meal
data class RecentMeal(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("calories")
    val calories: Float,
    
    @SerializedName("time")
    val time: String,
    
    @SerializedName("type")
    val type: String // breakfast, lunch, dinner, snack
)

// Weekly Progress
data class WeeklyProgress(
    @SerializedName("days")
    val days: List<DayProgress>
)

// Daily Progress
data class DayProgress(
    @SerializedName("day")
    val day: String,
    
    @SerializedName("calories")
    val calories: Float,
    
    @SerializedName("target")
    val target: Float
)
