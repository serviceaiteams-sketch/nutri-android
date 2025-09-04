package com.nutriai.app.domain.usecase

import com.nutriai.app.data.models.FoodRecommendation
import com.nutriai.app.domain.model.*
import java.time.DayOfWeek
import kotlin.math.abs
import kotlin.math.min

class WorkoutRecommendationUseCase {
    
    fun generateWorkoutRecommendations(
        userProfile: UserProfile,
        currentNutrition: List<FoodRecommendation>,
        healthReport: HealthReport,
        availableEquipment: List<Equipment> = listOf(Equipment.NONE),
        preferredWorkoutTypes: List<WorkoutType> = emptyList(),
        availableTime: Int = 60, // minutes
        workoutDays: List<DayOfWeek> = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
    ): List<WorkoutRecommendation> {
        
        val recommendations = mutableListOf<WorkoutRecommendation>()
        
        // Analyze current nutrition and health status
        val nutritionAnalysis = analyzeNutrition(currentNutrition)
        val healthAnalysis = analyzeHealthStatus(userProfile, healthReport)
        
        // Generate recommendations based on analysis
        when {
            // Weight loss focus
            healthAnalysis.isWeightLossNeeded -> {
                recommendations.addAll(generateWeightLossWorkouts(
                    userProfile, availableEquipment, availableTime, workoutDays
                ))
            }
            
            // Muscle gain focus
            healthAnalysis.isMuscleGainNeeded -> {
                recommendations.addAll(generateMuscleGainWorkouts(
                    userProfile, availableEquipment, availableTime, workoutDays
                ))
            }
            
            // Endurance focus
            healthAnalysis.isEnduranceNeeded -> {
                recommendations.addAll(generateEnduranceWorkouts(
                    userProfile, availableEquipment, availableTime, workoutDays
                ))
            }
            
            // Flexibility and balance focus
            healthAnalysis.isFlexibilityNeeded -> {
                recommendations.addAll(generateFlexibilityWorkouts(
                    userProfile, availableEquipment, availableTime, workoutDays
                ))
            }
            
            // General fitness and maintenance
            else -> {
                recommendations.addAll(generateBalancedWorkouts(
                    userProfile, availableEquipment, availableTime, workoutDays
                ))
            }
        }
        
        // Add yoga and mindfulness workouts for stress relief
        if (healthAnalysis.isStressReliefNeeded) {
            recommendations.addAll(generateYogaWorkouts(
                userProfile, availableEquipment, availableTime, workoutDays
            ))
        }
        
        // Add recovery workouts if needed
        if (healthAnalysis.isRecoveryNeeded) {
            recommendations.addAll(generateRecoveryWorkouts(
                userProfile, availableEquipment, availableTime, workoutDays
            ))
        }
        
        // Personalize recommendations based on user preferences
        return personalizeRecommendations(
            recommendations, preferredWorkoutTypes, availableEquipment, availableTime
        )
    }
    
    private fun analyzeNutrition(nutrition: List<FoodRecommendation>): NutritionAnalysis {
        val totalCalories = nutrition.sumOf { (it.calories ?: 0f).toInt() }
        val totalProtein = nutrition.sumOf { (it.protein ?: 0f).toDouble() }
        val totalCarbs = nutrition.sumOf { (it.carbs ?: 0f).toDouble() }
        val totalFat = nutrition.sumOf { (it.fat ?: 0f).toDouble() }
        
        return NutritionAnalysis(
            totalCalories = totalCalories,
            totalProtein = totalProtein,
            totalCarbs = totalCarbs,
            totalFat = totalFat,
            isHighProtein = totalProtein > 100,
            isHighCarb = totalCarbs > 200,
            isHighFat = totalFat > 60,
            isBalanced = abs(totalProtein - 80) < 20 && abs(totalCarbs - 150) < 30 && abs(totalFat - 50) < 15
        )
    }
    
    private fun analyzeHealthStatus(userProfile: UserProfile, healthReport: HealthReport): HealthAnalysis {
        val bmi = userProfile.weight / ((userProfile.height / 100.0) * (userProfile.height / 100.0))
        
        return HealthAnalysis(
            bmi = bmi,
            isWeightLossNeeded = bmi > 25.0,
            isMuscleGainNeeded = bmi < 18.5 || userProfile.goal == "muscle_gain",
            isEnduranceNeeded = healthReport.heartRate > 80 || healthReport.bloodPressure > 120,
            isFlexibilityNeeded = healthReport.flexibilityScore < 7.0,
            isStressReliefNeeded = healthReport.stressLevel > 7.0,
            isRecoveryNeeded = healthReport.fatigueLevel > 7.0,
            fitnessLevel = determineFitnessLevel(userProfile, healthReport)
        )
    }
    
    private fun generateWeightLossWorkouts(
        userProfile: UserProfile,
        availableEquipment: List<Equipment>,
        availableTime: Int,
        workoutDays: List<DayOfWeek>
    ): List<WorkoutRecommendation> {
        val workouts = mutableListOf<WorkoutRecommendation>()
        
        // High-intensity cardio workouts
        workouts.add(
            WorkoutRecommendation(
                id = "wl_hiit_1",
                name = "HIIT Cardio Blast",
                description = "High-intensity interval training for maximum calorie burn",
                workoutType = WorkoutType.HIIT,
                intensity = WorkoutIntensity.HIGH,
                duration = min(availableTime, 45),
                caloriesBurn = calculateCaloriesBurn(userProfile, WorkoutIntensity.HIGH, 45),
                muscleGroups = listOf(MuscleGroup.FULL_BODY),
                equipment = availableEquipment.filter { it in listOf(Equipment.NONE, Equipment.RESISTANCE_BANDS) },
                difficulty = determineDifficulty(userProfile),
                nutritionalFocus = NutritionalFocus.WEIGHT_LOSS,
                recommendedDays = workoutDays,
                contraindications = listOf("High blood pressure", "Heart conditions"),
                instructions = listOf(
                    "Warm up for 5 minutes",
                    "30 seconds high intensity, 30 seconds rest",
                    "Repeat for 8-10 rounds",
                    "Cool down for 5 minutes"
                ),
                reasoning = "HIIT provides maximum calorie burn in minimal time, perfect for weight loss"
            )
        )
        
        // Strength training for muscle building (burns more calories at rest)
        workouts.add(
            WorkoutRecommendation(
                id = "wl_strength_1",
                name = "Full Body Strength Circuit",
                description = "Compound movements to build muscle and boost metabolism",
                workoutType = WorkoutType.STRENGTH_TRAINING,
                intensity = WorkoutIntensity.MODERATE,
                duration = min(availableTime, 60),
                caloriesBurn = calculateCaloriesBurn(userProfile, WorkoutIntensity.MODERATE, 60),
                muscleGroups = listOf(MuscleGroup.FULL_BODY),
                equipment = availableEquipment.filter { it in listOf(Equipment.DUMBBELLS, Equipment.RESISTANCE_BANDS, Equipment.NONE) },
                difficulty = determineDifficulty(userProfile),
                nutritionalFocus = NutritionalFocus.WEIGHT_LOSS,
                recommendedDays = workoutDays,
                contraindications = listOf("Recent injuries", "Joint problems"),
                instructions = listOf(
                    "Warm up with dynamic stretches",
                    "3 sets of 12-15 reps each exercise",
                    "Minimal rest between exercises",
                    "Focus on form and control"
                ),
                reasoning = "Strength training builds muscle which increases resting metabolic rate"
            )
        )
        
        return workouts
    }
    
    private fun generateMuscleGainWorkouts(
        userProfile: UserProfile,
        availableEquipment: List<Equipment>,
        availableTime: Int,
        workoutDays: List<DayOfWeek>
    ): List<WorkoutRecommendation> {
        val workouts = mutableListOf<WorkoutRecommendation>()
        
        // Progressive strength training
        workouts.add(
            WorkoutRecommendation(
                id = "mg_strength_1",
                name = "Progressive Strength Training",
                description = "Build muscle mass with progressive overload",
                workoutType = WorkoutType.STRENGTH_TRAINING,
                intensity = WorkoutIntensity.HIGH,
                duration = min(availableTime, 75),
                caloriesBurn = calculateCaloriesBurn(userProfile, WorkoutIntensity.HIGH, 75),
                muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.BACK, MuscleGroup.SHOULDERS),
                equipment = availableEquipment.filter { it in listOf(Equipment.DUMBBELLS, Equipment.BARBELL, Equipment.WEIGHT_MACHINE) },
                difficulty = determineDifficulty(userProfile),
                nutritionalFocus = NutritionalFocus.MUSCLE_GAIN,
                recommendedDays = workoutDays,
                contraindications = listOf("Untrained beginners", "Joint issues"),
                instructions = listOf(
                    "Warm up with light weights",
                    "4-5 sets of 6-8 reps",
                    "Progressive weight increase",
                    "2-3 minutes rest between sets"
                ),
                reasoning = "Progressive overload with proper rest periods maximizes muscle growth"
            )
        )
        
        // Bodyweight muscle building
        if (Equipment.NONE in availableEquipment) {
            workouts.add(
                WorkoutRecommendation(
                    id = "mg_bodyweight_1",
                    name = "Advanced Bodyweight Strength",
                    description = "Build muscle using only bodyweight exercises",
                    workoutType = WorkoutType.BODYWEIGHT,
                    intensity = WorkoutIntensity.HIGH,
                    duration = min(availableTime, 60),
                    caloriesBurn = calculateCaloriesBurn(userProfile, WorkoutIntensity.HIGH, 60),
                    muscleGroups = listOf(MuscleGroup.FULL_BODY),
                    equipment = listOf(Equipment.NONE),
                    difficulty = determineDifficulty(userProfile),
                    nutritionalFocus = NutritionalFocus.MUSCLE_GAIN,
                    recommendedDays = workoutDays,
                    contraindications = listOf("Beginner level", "Limited mobility"),
                    instructions = listOf(
                        "Warm up with mobility exercises",
                        "3 sets to failure for each exercise",
                        "Increase difficulty progressively",
                        "Focus on slow, controlled movements"
                    ),
                    reasoning = "Bodyweight exercises can build significant muscle when performed with proper intensity"
                )
            )
        }
        
        return workouts
    }
    
    private fun generateEnduranceWorkouts(
        userProfile: UserProfile,
        availableEquipment: List<Equipment>,
        availableTime: Int,
        workoutDays: List<DayOfWeek>
    ): List<WorkoutRecommendation> {
        val workouts = mutableListOf<WorkoutRecommendation>()
        
        // Cardio endurance
        workouts.add(
            WorkoutRecommendation(
                id = "endurance_cardio_1",
                name = "Steady State Cardio",
                description = "Improve cardiovascular endurance and heart health",
                workoutType = WorkoutType.CARDIO,
                intensity = WorkoutIntensity.MODERATE,
                duration = min(availableTime, 45),
                caloriesBurn = calculateCaloriesBurn(userProfile, WorkoutIntensity.MODERATE, 45),
                muscleGroups = listOf(MuscleGroup.FULL_BODY),
                equipment = availableEquipment.filter { it in listOf(Equipment.CARDIO_MACHINE, Equipment.NONE) },
                difficulty = determineDifficulty(userProfile),
                nutritionalFocus = NutritionalFocus.ENDURANCE,
                recommendedDays = workoutDays,
                contraindications = listOf("Severe heart conditions", "Recent surgery"),
                instructions = listOf(
                    "Start with 5-minute warm-up",
                    "Maintain steady pace for 30-35 minutes",
                    "Keep heart rate at 60-70% of max",
                    "5-minute cool-down"
                ),
                reasoning = "Steady state cardio improves heart health and builds endurance"
            )
        )
        
        return workouts
    }
    
    private fun generateFlexibilityWorkouts(
        userProfile: UserProfile,
        availableEquipment: List<Equipment>,
        availableTime: Int,
        workoutDays: List<DayOfWeek>
    ): List<WorkoutRecommendation> {
        val workouts = mutableListOf<WorkoutRecommendation>()
        
        // Yoga for flexibility
        workouts.add(
            WorkoutRecommendation(
                id = "flex_yoga_1",
                name = "Flexibility Flow Yoga",
                description = "Improve flexibility, balance, and mind-body connection",
                workoutType = WorkoutType.YOGA,
                intensity = WorkoutIntensity.LOW,
                duration = min(availableTime, 60),
                caloriesBurn = calculateCaloriesBurn(userProfile, WorkoutIntensity.LOW, 60),
                muscleGroups = listOf(MuscleGroup.FULL_BODY),
                equipment = listOf(Equipment.YOGA_MAT),
                difficulty = determineDifficulty(userProfile),
                nutritionalFocus = NutritionalFocus.FLEXIBILITY,
                recommendedDays = workoutDays,
                contraindications = listOf("Severe back problems", "Pregnancy complications"),
                instructions = listOf(
                    "Begin with breathing exercises",
                    "Gentle stretching sequences",
                    "Hold poses for 30-60 seconds",
                    "End with relaxation pose"
                ),
                reasoning = "Yoga improves flexibility, reduces stress, and enhances recovery"
            )
        )
        
        // Pilates for core strength and flexibility
        workouts.add(
            WorkoutRecommendation(
                id = "flex_pilates_1",
                name = "Core Flexibility Pilates",
                description = "Strengthen core while improving flexibility",
                workoutType = WorkoutType.PILATES,
                intensity = WorkoutIntensity.MODERATE,
                duration = min(availableTime, 45),
                caloriesBurn = calculateCaloriesBurn(userProfile, WorkoutIntensity.MODERATE, 45),
                muscleGroups = listOf(MuscleGroup.CORE, MuscleGroup.GLUTES),
                equipment = listOf(Equipment.YOGA_MAT),
                difficulty = determineDifficulty(userProfile),
                nutritionalFocus = NutritionalFocus.FLEXIBILITY,
                recommendedDays = workoutDays,
                contraindications = listOf("Recent abdominal surgery", "Severe back pain"),
                instructions = listOf(
                    "Focus on breathing and control",
                    "Slow, controlled movements",
                    "Engage core throughout",
                    "Maintain proper alignment"
                ),
                reasoning = "Pilates builds core strength while improving flexibility and posture"
            )
        )
        
        return workouts
    }
    
    private fun generateYogaWorkouts(
        userProfile: UserProfile,
        availableEquipment: List<Equipment>,
        availableTime: Int,
        workoutDays: List<DayOfWeek>
    ): List<WorkoutRecommendation> {
        val workouts = mutableListOf<WorkoutRecommendation>()
        
        // Stress relief yoga
        workouts.add(
            WorkoutRecommendation(
                id = "yoga_stress_1",
                name = "Stress Relief & Mindfulness",
                description = "Gentle yoga sequences to reduce stress and anxiety",
                workoutType = WorkoutType.YOGA,
                intensity = WorkoutIntensity.LOW,
                duration = min(availableTime, 45),
                caloriesBurn = calculateCaloriesBurn(userProfile, WorkoutIntensity.LOW, 45),
                muscleGroups = listOf(MuscleGroup.FULL_BODY),
                equipment = listOf(Equipment.YOGA_MAT),
                difficulty = determineDifficulty(userProfile),
                nutritionalFocus = NutritionalFocus.STRESS_RELIEF,
                recommendedDays = workoutDays,
                contraindications = listOf("Severe mental health conditions"),
                instructions = listOf(
                    "Start with meditation",
                    "Gentle flowing movements",
                    "Focus on breath awareness",
                    "End with deep relaxation"
                ),
                reasoning = "Mindful yoga reduces cortisol levels and promotes mental well-being"
            )
        )
        
        return workouts
    }
    
    private fun generateRecoveryWorkouts(
        userProfile: UserProfile,
        availableEquipment: List<Equipment>,
        availableTime: Int,
        workoutDays: List<DayOfWeek>
    ): List<WorkoutRecommendation> {
        val workouts = mutableListOf<WorkoutRecommendation>()
        
        // Active recovery
        workouts.add(
            WorkoutRecommendation(
                id = "recovery_active_1",
                name = "Active Recovery & Mobility",
                description = "Light movement to promote recovery and reduce soreness",
                workoutType = WorkoutType.FLEXIBILITY,
                intensity = WorkoutIntensity.LOW,
                duration = min(availableTime, 30),
                caloriesBurn = calculateCaloriesBurn(userProfile, WorkoutIntensity.LOW, 30),
                muscleGroups = listOf(MuscleGroup.FULL_BODY),
                equipment = listOf(Equipment.FOAM_ROLLER, Equipment.YOGA_MAT),
                difficulty = determineDifficulty(userProfile),
                nutritionalFocus = NutritionalFocus.RECOVERY,
                recommendedDays = workoutDays,
                contraindications = listOf("Acute injuries", "Severe pain"),
                instructions = listOf(
                    "Gentle stretching",
                    "Foam rolling for tight areas",
                    "Light walking or swimming",
                    "Focus on breathing and relaxation"
                ),
                reasoning = "Active recovery promotes blood flow and reduces muscle soreness"
            )
        )
        
        return workouts
    }
    
    private fun generateBalancedWorkouts(
        userProfile: UserProfile,
        availableEquipment: List<Equipment>,
        availableTime: Int,
        workoutDays: List<DayOfWeek>
    ): List<WorkoutRecommendation> {
        val workouts = mutableListOf<WorkoutRecommendation>()
        
        // Functional training
        workouts.add(
            WorkoutRecommendation(
                id = "balance_functional_1",
                name = "Functional Fitness Circuit",
                description = "Improve overall fitness with functional movements",
                workoutType = WorkoutType.FUNCTIONAL_TRAINING,
                intensity = WorkoutIntensity.MODERATE,
                duration = min(availableTime, 50),
                caloriesBurn = calculateCaloriesBurn(userProfile, WorkoutIntensity.MODERATE, 50),
                muscleGroups = listOf(MuscleGroup.FULL_BODY),
                equipment = availableEquipment.filter { it in listOf(Equipment.DUMBBELLS, Equipment.RESISTANCE_BANDS, Equipment.NONE) },
                difficulty = determineDifficulty(userProfile),
                nutritionalFocus = NutritionalFocus.BALANCE,
                recommendedDays = workoutDays,
                contraindications = listOf("Recent injuries", "Balance issues"),
                instructions = listOf(
                    "Warm up with dynamic movements",
                    "Circuit training format",
                    "3 rounds of 8-10 exercises",
                    "30 seconds rest between rounds"
                ),
                reasoning = "Functional training improves everyday movement patterns and overall fitness"
            )
        )
        
        return workouts
    }
    
    private fun personalizeRecommendations(
        recommendations: List<WorkoutRecommendation>,
        preferredWorkoutTypes: List<WorkoutType>,
        availableEquipment: List<Equipment>,
        availableTime: Int
    ): List<WorkoutRecommendation> {
        var personalized = recommendations.toMutableList()
        
        // Filter by available equipment
        personalized = personalized.filter { workout ->
            workout.equipment.any { it in availableEquipment }
        }.toMutableList()
        
        // Filter by time availability
        personalized = personalized.filter { workout ->
            workout.duration <= availableTime
        }.toMutableList()
        
        // Prioritize preferred workout types
        if (preferredWorkoutTypes.isNotEmpty()) {
            personalized.sortByDescending { workout ->
                if (workout.workoutType in preferredWorkoutTypes) 1 else 0
            }
        }
        
        // Mark top recommendations safely
        if (personalized.isNotEmpty()) {
            val topCount = min(3, personalized.size)
            val topRecommendations = personalized.take(topCount).map { workout ->
                workout.copy(isRecommended = true)
            }
            
            // Replace the first items with the modified ones
            if (topCount > 0) {
                personalized.subList(0, topCount).clear()
                personalized.addAll(0, topRecommendations)
            }
        }
        
        return personalized
    }
    
    private fun calculateCaloriesBurn(userProfile: UserProfile, intensity: WorkoutIntensity, duration: Int): Int {
        val baseCaloriesPerMinute = when (intensity) {
            WorkoutIntensity.LOW -> 3.0
            WorkoutIntensity.MODERATE -> 6.0
            WorkoutIntensity.HIGH -> 10.0
            WorkoutIntensity.VERY_HIGH -> 15.0
        }
        
        val weightMultiplier = userProfile.weight / 70.0 // Normalize to 70kg
        return (baseCaloriesPerMinute * duration * weightMultiplier).toInt()
    }
    
    private fun determineDifficulty(userProfile: UserProfile): WorkoutDifficulty {
        return when {
            userProfile.fitnessLevel < 3 -> WorkoutDifficulty.BEGINNER
            userProfile.fitnessLevel < 6 -> WorkoutDifficulty.INTERMEDIATE
            userProfile.fitnessLevel < 8 -> WorkoutDifficulty.ADVANCED
            else -> WorkoutDifficulty.EXPERT
        }
    }
    
    private fun determineFitnessLevel(userProfile: UserProfile, healthReport: HealthReport): Int {
        var level = 5 // Start with average
        
        // Adjust based on age
        when {
            userProfile.age < 30 -> level += 1
            userProfile.age > 50 -> level -= 1
        }
        
        // Adjust based on activity level
        when (userProfile.activityLevel) {
            "sedentary" -> level -= 2
            "lightly_active" -> level -= 1
            "moderately_active" -> level += 0
            "very_active" -> level += 1
            "extremely_active" -> level += 2
        }
        
        // Adjust based on health metrics
        if (healthReport.heartRate < 70) level += 1
        if (healthReport.bloodPressure < 120) level += 1
        if (healthReport.flexibilityScore > 7) level += 1
        
        return level.coerceIn(1, 10)
    }
}

// Data classes for analysis
data class NutritionAnalysis(
    val totalCalories: Int,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val isHighProtein: Boolean,
    val isHighCarb: Boolean,
    val isHighFat: Boolean,
    val isBalanced: Boolean
)

data class HealthAnalysis(
    val bmi: Double,
    val isWeightLossNeeded: Boolean,
    val isMuscleGainNeeded: Boolean,
    val isEnduranceNeeded: Boolean,
    val isFlexibilityNeeded: Boolean,
    val isStressReliefNeeded: Boolean,
    val isRecoveryNeeded: Boolean,
    val fitnessLevel: Int
)
