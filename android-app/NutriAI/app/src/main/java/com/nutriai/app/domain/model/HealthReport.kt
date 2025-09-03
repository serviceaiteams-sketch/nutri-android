package com.nutriai.app.domain.model

data class HealthReport(
    val id: String,
    val userId: String,
    val date: String,
    val weight: Double, // in kg
    val bodyFatPercentage: Double,
    val muscleMass: Double, // in kg
    val heartRate: Int, // resting heart rate
    val bloodPressure: Int, // systolic
    val flexibilityScore: Double, // 1-10 scale
    val stressLevel: Double, // 1-10 scale
    val fatigueLevel: Double, // 1-10 scale
    val sleepQuality: Double, // 1-10 scale
    val energyLevel: Double, // 1-10 scale
    val mood: Double, // 1-10 scale
    val notes: String,
    val measurements: Map<String, Double> = emptyMap(),
    val symptoms: List<String> = emptyList()
)
