package com.nutriai.app.data.models

import com.google.gson.annotations.SerializedName

// Health Report Upload Response
data class HealthReportUploadResponse(
    @SerializedName("success")
    val success: Boolean? = null,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("reports")
    val reports: List<UploadedReport>? = null
)

// Uploaded Report
data class UploadedReport(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("filename")
    val filename: String,
    
    @SerializedName("fileType")
    val fileType: String,
    
    @SerializedName("fileSize")
    val fileSize: Long,
    
    @SerializedName("uploadedAt")
    val uploadedAt: String
)

// Health Condition
data class HealthCondition(
    @SerializedName("condition")
    val condition: String,
    
    @SerializedName("diagnosedDate")
    val diagnosedDate: String? = null,
    
    @SerializedName("severity")
    val severity: String? = null,
    
    @SerializedName("medications")
    val medications: List<String>? = null
)

// Health Analysis Result
data class HealthAnalysisResult(
    @SerializedName("reportSummary")
    val reportSummary: String? = null,
    
    @SerializedName("detectedConditions")
    val detectedConditions: List<String>? = null,
    
    @SerializedName("riskFactors")
    val riskFactors: List<RiskFactor>? = null,
    
    @SerializedName("healthScore")
    val healthScore: Int? = null,
    
    @SerializedName("keyMetrics")
    val keyMetrics: Map<String, MetricValue>? = null,
    
    @SerializedName("recommendations")
    val recommendations: List<HealthRecommendation>? = null,
    
    @SerializedName("nutritionGuidance")
    val nutritionGuidance: NutritionGuidance? = null,
    
    @SerializedName("foodRecommendations")
    val foodRecommendations: FoodRecommendationsResponse? = null,
    
    @SerializedName("nextSteps")
    val nextSteps: List<String>? = null
)

// Risk Factor
data class RiskFactor(
    @SerializedName("factor")
    val factor: String? = null,
    
    @SerializedName("level")
    val level: String? = null, // "high", "medium", "low"
    
    @SerializedName("description")
    val description: String? = null
)

// Metric Value
data class MetricValue(
    @SerializedName("value")
    val value: Float,
    
    @SerializedName("unit")
    val unit: String,
    
    @SerializedName("status")
    val status: String, // "normal", "high", "low"
    
    @SerializedName("normalRange")
    val normalRange: String? = null
)

// Health Recommendation
data class HealthRecommendation(
    @SerializedName("category")
    val category: String? = null, // "diet", "lifestyle", "medical", "exercise"
    
    @SerializedName("recommendation")
    val recommendation: String? = null,
    
    @SerializedName("priority")
    val priority: String? = null // "high", "medium", "low"
)

// Nutrition Guidance
data class NutritionGuidance(
    @SerializedName("foodsToAvoid")
    val foodsToAvoid: List<String>? = null,
    
    @SerializedName("foodsToIncrease")
    val foodsToIncrease: List<String>? = null,
    
    @SerializedName("mealPlanSuggestions")
    val mealPlanSuggestions: List<String>? = null,
    
    @SerializedName("supplementRecommendations")
    val supplementRecommendations: List<String>? = null
)

// Health Conditions List Response
data class HealthConditionsResponse(
    @SerializedName("conditions")
    val conditions: List<HealthConditionDetail>? = null
)

// Health Condition Detail
data class HealthConditionDetail(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("condition_name")
    val conditionName: String,
    
    @SerializedName("diagnosed_date")
    val diagnosedDate: String? = null,
    
    @SerializedName("severity")
    val severity: String? = null,
    
    @SerializedName("medications")
    val medications: String? = null,
    
    @SerializedName("symptoms")
    val symptoms: String? = null,
    
    @SerializedName("created_at")
    val createdAt: String
)

// Food Recommendations Response
data class FoodRecommendationsResponse(
    @SerializedName("recommendations")
    val recommendations: List<FoodRecommendation>? = null,
    
    @SerializedName("mealPlan")
    val mealPlan: MealPlanRecommendation? = null,
    
    @SerializedName("weeklyNutrition")
    val weeklyNutrition: WeeklyNutrition? = null,
    
    @SerializedName("shoppingList")
    val shoppingList: List<String>? = null
)

// Food Recommendation
data class FoodRecommendation(
    @SerializedName("food")
    val food: String,
    
    @SerializedName("reason")
    val reason: String,
    
    @SerializedName("category")
    val category: String? = null,
    
    @SerializedName("priority")
    val priority: String? = null,
    
    @SerializedName("calories")
    val calories: Float? = null,
    
    @SerializedName("protein")
    val protein: Float? = null,
    
    @SerializedName("carbs")
    val carbs: Float? = null,
    
    @SerializedName("fat")
    val fat: Float? = null,
    
    @SerializedName("nutrients")
    val nutrients: List<String>? = null,
    
    @SerializedName("servingSize")
    val servingSize: String? = null,
    
    @SerializedName("bestTime")
    val bestTime: String? = null,
    
    @SerializedName("preparationTips")
    val preparationTips: String? = null,
    
    @SerializedName("alternatives")
    val alternatives: String? = null,
    
    @SerializedName("frequency")
    val frequency: String? = null,
    
    @SerializedName("notes")
    val notes: String? = null
)

// Meal Plan Recommendation
data class MealPlanRecommendation(
    @SerializedName("breakfast")
    val breakfast: List<MealPlanItem>? = null,
    
    @SerializedName("lunch")
    val lunch: List<MealPlanItem>? = null,
    
    @SerializedName("dinner")
    val dinner: List<MealPlanItem>? = null,
    
    @SerializedName("snacks")
    val snacks: List<MealPlanItem>? = null
)

// Meal Plan Item
data class MealPlanItem(
    @SerializedName("day")
    val day: String? = null,
    
    @SerializedName("meal")
    val meal: String? = null,
    
    @SerializedName("nutrition")
    val nutrition: String? = null,
    
    @SerializedName("calories")
    val calories: Int? = null,
    
    @SerializedName("protein")
    val protein: String? = null,
    
    @SerializedName("carbs")
    val carbs: String? = null,
    
    @SerializedName("fat")
    val fat: String? = null
)

// Weekly Nutrition
data class WeeklyNutrition(
    @SerializedName("totalCalories")
    val totalCalories: Int? = null,
    
    @SerializedName("averageProtein")
    val averageProtein: String? = null,
    
    @SerializedName("averageCarbs")
    val averageCarbs: String? = null,
    
    @SerializedName("averageFat")
    val averageFat: String? = null
)

// Health Metric
data class HealthMetric(
    @SerializedName("bloodPressure")
    val bloodPressure: BloodPressure? = null,
    
    @SerializedName("bloodSugar")
    val bloodSugar: BloodSugar? = null,
    
    @SerializedName("cholesterol")
    val cholesterol: Cholesterol? = null,
    
    @SerializedName("kidneyFunction")
    val kidneyFunction: KidneyFunction? = null,
    
    @SerializedName("liverFunction")
    val liverFunction: LiverFunction? = null,
    
    @SerializedName("thyroid")
    val thyroid: Thyroid? = null,
    
    @SerializedName("hemoglobin")
    val hemoglobin: Hemoglobin? = null,
    
    @SerializedName("vitaminD")
    val vitaminD: VitaminD? = null
)

// Individual metric classes
data class BloodPressure(
    @SerializedName("systolic")
    val systolic: Int,
    
    @SerializedName("diastolic")
    val diastolic: Int,
    
    @SerializedName("status")
    val status: String
)

data class BloodSugar(
    @SerializedName("fasting")
    val fasting: Int,
    
    @SerializedName("postprandial")
    val postprandial: Int,
    
    @SerializedName("status")
    val status: String
)

data class Cholesterol(
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("hdl")
    val hdl: Int,
    
    @SerializedName("ldl")
    val ldl: Int,
    
    @SerializedName("triglycerides")
    val triglycerides: Int,
    
    @SerializedName("status")
    val status: String
)

data class KidneyFunction(
    @SerializedName("creatinine")
    val creatinine: Float,
    
    @SerializedName("egfr")
    val egfr: Int,
    
    @SerializedName("status")
    val status: String
)

data class LiverFunction(
    @SerializedName("alt")
    val alt: Int,
    
    @SerializedName("ast")
    val ast: Int,
    
    @SerializedName("bilirubin")
    val bilirubin: Float,
    
    @SerializedName("status")
    val status: String
)

data class Thyroid(
    @SerializedName("tsh")
    val tsh: Float,
    
    @SerializedName("t3")
    val t3: Int,
    
    @SerializedName("t4")
    val t4: Float,
    
    @SerializedName("status")
    val status: String
)

data class Hemoglobin(
    @SerializedName("value")
    val value: Int,
    
    @SerializedName("status")
    val status: String
)

data class VitaminD(
    @SerializedName("value")
    val value: Int,
    
    @SerializedName("status")
    val status: String
)
