package com.nutriai.app.domain.model

import java.time.DayOfWeek

data class WorkoutRecommendation(
    val id: String,
    val name: String,
    val description: String,
    val workoutType: WorkoutType,
    val intensity: WorkoutIntensity,
    val duration: Int, // in minutes
    val caloriesBurn: Int,
    val muscleGroups: List<MuscleGroup>,
    val equipment: List<Equipment>,
    val difficulty: WorkoutDifficulty,
    val nutritionalFocus: NutritionalFocus,
    val recommendedDays: List<DayOfWeek>,
    val contraindications: List<String>,
    val instructions: List<String>,
    val videoUrl: String? = null,
    val imageUrl: String? = null,
    val isRecommended: Boolean = false,
    val reasoning: String = ""
)

enum class WorkoutType {
    STRENGTH_TRAINING,
    CARDIO,
    YOGA,
    PILATES,
    HIIT,
    FLEXIBILITY,
    BALANCE,
    SPORTS,
    DANCE,
    SWIMMING,
    CYCLING,
    RUNNING,
    WALKING,
    BODYWEIGHT,
    FUNCTIONAL_TRAINING
}

enum class WorkoutIntensity {
    LOW,
    MODERATE,
    HIGH,
    VERY_HIGH
}

enum class MuscleGroup {
    CHEST,
    BACK,
    SHOULDERS,
    BICEPS,
    TRICEPS,
    FOREARMS,
    CORE,
    GLUTES,
    QUADRICEPS,
    HAMSTRINGS,
    CALVES,
    FULL_BODY
}

enum class Equipment {
    NONE,
    DUMBBELLS,
    BARBELL,
    RESISTANCE_BANDS,
    YOGA_MAT,
    PULL_UP_BAR,
    BENCH,
    CARDIO_MACHINE,
    WEIGHT_MACHINE,
    KETTLEBELL,
    MEDICINE_BALL,
    FOAM_ROLLER
}

enum class WorkoutDifficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    EXPERT
}

enum class NutritionalFocus {
    WEIGHT_LOSS,
    MUSCLE_GAIN,
    ENDURANCE,
    FLEXIBILITY,
    STRENGTH,
    RECOVERY,
    ENERGY_BOOST,
    STRESS_RELIEF,
    BALANCE
}

data class WorkoutPlan(
    val id: String,
    val name: String,
    val description: String,
    val duration: Int, // in weeks
    val workouts: List<WeeklyWorkout>,
    val goals: List<String>,
    val nutritionalGuidelines: List<String>,
    val progressTracking: ProgressTracking
)

data class WeeklyWorkout(
    val weekNumber: Int,
    val dailyWorkouts: Map<DayOfWeek, List<WorkoutRecommendation>>,
    val restDays: List<DayOfWeek>,
    val weeklyGoals: List<String>
)

data class ProgressTracking(
    val weightGoal: Double?,
    val strengthGoals: Map<String, Double>,
    val enduranceGoals: Map<String, Int>,
    val flexibilityGoals: Map<String, Double>
)
