package com.nutriai.app.domain.model

data class UserProfile(
    val id: String,
    val name: String,
    val age: Int,
    val gender: String,
    val height: Double, // in cm
    val weight: Double, // in kg
    val goal: String, // weight_loss, muscle_gain, maintenance, etc.
    val activityLevel: String, // sedentary, lightly_active, moderately_active, very_active, extremely_active
    val fitnessLevel: Int, // 1-10 scale
    val medicalConditions: List<String> = emptyList(),
    val injuries: List<String> = emptyList(),
    val preferences: Map<String, Any> = emptyMap()
)
