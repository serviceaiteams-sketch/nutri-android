package com.nutriai.app.domain.usecase

import android.os.Build
import androidx.annotation.RequiresApi
import com.nutriai.app.domain.model.*
import java.time.DayOfWeek
import java.time.LocalDate

class WorkoutPlanGeneratorUseCase {
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun generateWorkoutPlan(
        userProfile: UserProfile,
        workoutRecommendations: List<WorkoutRecommendation>,
        duration: Int = 4, // weeks
        workoutDaysPerWeek: Int = 4
    ): WorkoutPlan {
        
        val weeklyWorkouts = mutableListOf<WeeklyWorkout>()
        
        for (week in 1..duration) {
            val weeklyWorkout = generateWeeklyWorkout(
                weekNumber = week,
                workoutRecommendations = workoutRecommendations,
                userProfile = userProfile,
                workoutDaysPerWeek = workoutDaysPerWeek
            )
            weeklyWorkouts.add(weeklyWorkout)
        }
        
        return WorkoutPlan(
            id = "plan_${System.currentTimeMillis()}",
            name = generatePlanName(userProfile, workoutRecommendations),
            description = generatePlanDescription(userProfile, workoutRecommendations),
            duration = duration,
            workouts = weeklyWorkouts,
            goals = generatePlanGoals(userProfile, workoutRecommendations),
            nutritionalGuidelines = generateNutritionalGuidelines(userProfile, workoutRecommendations),
            progressTracking = generateProgressTracking(userProfile, workoutRecommendations)
        )
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateWeeklyWorkout(
        weekNumber: Int,
        workoutRecommendations: List<WorkoutRecommendation>,
        userProfile: UserProfile,
        workoutDaysPerWeek: Int
    ): WeeklyWorkout {
        
        val availableDays = listOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        )
        
        val selectedDays = availableDays.take(workoutDaysPerWeek)
        val restDays = availableDays.filter { it !in selectedDays }
        
        val dailyWorkouts = mutableMapOf<DayOfWeek, List<WorkoutRecommendation>>()
        
        selectedDays.forEach { day ->
            val dayWorkouts = selectWorkoutsForDay(
                day = day,
                weekNumber = weekNumber,
                workoutRecommendations = workoutRecommendations,
                userProfile = userProfile
            )
            dailyWorkouts[day] = dayWorkouts
        }
        
        return WeeklyWorkout(
            weekNumber = weekNumber,
            dailyWorkouts = dailyWorkouts,
            restDays = restDays,
            weeklyGoals = generateWeeklyGoals(weekNumber, userProfile)
        )
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun selectWorkoutsForDay(
        day: DayOfWeek,
        weekNumber: Int,
        workoutRecommendations: List<WorkoutRecommendation>,
        userProfile: UserProfile
    ): List<WorkoutRecommendation> {
        
        val dayWorkouts = mutableListOf<WorkoutRecommendation>()
        
        when (day) {
            DayOfWeek.MONDAY -> {
                // Start week with strength training
                val strengthWorkouts = workoutRecommendations.filter { 
                    it.workoutType == WorkoutType.STRENGTH_TRAINING 
                }
                if (strengthWorkouts.isNotEmpty()) {
                    dayWorkouts.add(strengthWorkouts.first())
                }
            }
            
            DayOfWeek.TUESDAY -> {
                // Cardio or HIIT day
                val cardioWorkouts = workoutRecommendations.filter { 
                    it.workoutType in listOf(WorkoutType.CARDIO, WorkoutType.HIIT) 
                }
                if (cardioWorkouts.isNotEmpty()) {
                    dayWorkouts.add(cardioWorkouts.first())
                }
            }
            
            DayOfWeek.WEDNESDAY -> {
                // Active recovery or flexibility
                val recoveryWorkouts = workoutRecommendations.filter { 
                    it.workoutType in listOf(WorkoutType.FLEXIBILITY, WorkoutType.YOGA) 
                }
                if (recoveryWorkouts.isNotEmpty()) {
                    dayWorkouts.add(recoveryWorkouts.first())
                }
            }
            
            DayOfWeek.THURSDAY -> {
                // Strength training (different muscle groups)
                val strengthWorkouts = workoutRecommendations.filter { 
                    it.workoutType == WorkoutType.STRENGTH_TRAINING 
                }
                if (strengthWorkouts.size > 1) {
                    dayWorkouts.add(strengthWorkouts[1])
                } else if (strengthWorkouts.isNotEmpty()) {
                    dayWorkouts.add(strengthWorkouts.first())
                }
            }
            
            DayOfWeek.FRIDAY -> {
                // End week with cardio or functional training
                val endWeekWorkouts = workoutRecommendations.filter { 
                    it.workoutType in listOf(WorkoutType.CARDIO, WorkoutType.FUNCTIONAL_TRAINING) 
                }
                if (endWeekWorkouts.isNotEmpty()) {
                    dayWorkouts.add(endWeekWorkouts.first())
                }
            }
            
            DayOfWeek.SATURDAY -> {
                // Weekend flexibility or yoga
                val weekendWorkouts = workoutRecommendations.filter { 
                    it.workoutType in listOf(WorkoutType.YOGA, WorkoutType.FLEXIBILITY) 
                }
                if (weekendWorkouts.isNotEmpty()) {
                    dayWorkouts.add(weekendWorkouts.first())
                }
            }
            
            DayOfWeek.SUNDAY -> {
                // Rest day or light activity
                val lightWorkouts = workoutRecommendations.filter { 
                    it.intensity == WorkoutIntensity.LOW 
                }
                if (lightWorkouts.isNotEmpty()) {
                    dayWorkouts.add(lightWorkouts.first())
                }
            }
        }
        
        // If no specific workout selected, add a general one
        if (dayWorkouts.isEmpty()) {
            val generalWorkout = workoutRecommendations.firstOrNull { 
                it.workoutType == WorkoutType.FUNCTIONAL_TRAINING 
            } ?: workoutRecommendations.firstOrNull()
            
            generalWorkout?.let { dayWorkouts.add(it) }
        }
        
        return dayWorkouts
    }
    
    private fun generatePlanName(userProfile: UserProfile, workoutRecommendations: List<WorkoutRecommendation>): String {
        val primaryFocus = workoutRecommendations.firstOrNull()?.nutritionalFocus
        val duration = "4-Week"
        
        return when (primaryFocus) {
            NutritionalFocus.WEIGHT_LOSS -> "$duration Weight Loss & Fitness Plan"
            NutritionalFocus.MUSCLE_GAIN -> "$duration Muscle Building Plan"
            NutritionalFocus.ENDURANCE -> "$duration Endurance & Cardio Plan"
            NutritionalFocus.FLEXIBILITY -> "$duration Flexibility & Balance Plan"
            NutritionalFocus.STRENGTH -> "$duration Strength & Power Plan"
            else -> "$duration Comprehensive Fitness Plan"
        }
    }
    
    private fun generatePlanDescription(userProfile: UserProfile, workoutRecommendations: List<WorkoutRecommendation>): String {
        val primaryFocus = workoutRecommendations.firstOrNull()?.nutritionalFocus
        
        return when (primaryFocus) {
            NutritionalFocus.WEIGHT_LOSS -> "A comprehensive 4-week plan designed to help you lose weight, build lean muscle, and improve overall fitness through a combination of strength training, cardio, and flexibility work."
            NutritionalFocus.MUSCLE_GAIN -> "A progressive 4-week plan focused on building muscle mass, increasing strength, and improving body composition through targeted strength training and proper recovery."
            NutritionalFocus.ENDURANCE -> "A structured 4-week plan to enhance cardiovascular fitness, improve endurance, and build stamina through progressive cardio training and functional movements."
            NutritionalFocus.FLEXIBILITY -> "A mindful 4-week plan designed to improve flexibility, balance, and mind-body connection through yoga, stretching, and gentle movement practices."
            NutritionalFocus.STRENGTH -> "A challenging 4-week plan focused on building raw strength, power, and functional movement patterns through progressive overload training."
            else -> "A balanced 4-week fitness plan that combines strength, cardio, flexibility, and recovery to improve overall health and fitness."
        }
    }
    
    private fun generatePlanGoals(userProfile: UserProfile, workoutRecommendations: List<WorkoutRecommendation>): List<String> {
        val goals = mutableListOf<String>()
        val primaryFocus = workoutRecommendations.firstOrNull()?.nutritionalFocus
        
        when (primaryFocus) {
            NutritionalFocus.WEIGHT_LOSS -> {
                goals.add("Lose 2-4 kg of body fat")
                goals.add("Improve cardiovascular fitness")
                goals.add("Build lean muscle mass")
                goals.add("Increase energy levels")
            }
            NutritionalFocus.MUSCLE_GAIN -> {
                goals.add("Gain 2-3 kg of muscle mass")
                goals.add("Increase strength in major lifts")
                goals.add("Improve body composition")
                goals.add("Enhance recovery capacity")
            }
            NutritionalFocus.ENDURANCE -> {
                goals.add("Improve cardiovascular endurance")
                goals.add("Increase stamina and work capacity")
                goals.add("Reduce resting heart rate")
                goals.add("Enhance aerobic fitness")
            }
            NutritionalFocus.FLEXIBILITY -> {
                goals.add("Improve overall flexibility")
                goals.add("Enhance balance and coordination")
                goals.add("Reduce stress and tension")
                goals.add("Improve posture and alignment")
            }
            else -> {
                goals.add("Improve overall fitness")
                goals.add("Build strength and endurance")
                goals.add("Enhance flexibility and mobility")
                goals.add("Establish consistent exercise habits")
            }
        }
        
        return goals
    }
    
    private fun generateNutritionalGuidelines(userProfile: UserProfile, workoutRecommendations: List<WorkoutRecommendation>): List<String> {
        val guidelines = mutableListOf<String>()
        val primaryFocus = workoutRecommendations.firstOrNull()?.nutritionalFocus
        
        when (primaryFocus) {
            NutritionalFocus.WEIGHT_LOSS -> {
                guidelines.add("Create a moderate caloric deficit (300-500 calories per day)")
                guidelines.add("Prioritize protein intake (1.6-2.2g per kg body weight)")
                guidelines.add("Include plenty of vegetables and fiber")
                guidelines.add("Stay hydrated (2-3 liters of water per day)")
                guidelines.add("Time meals around workouts for optimal performance")
            }
            NutritionalFocus.MUSCLE_GAIN -> {
                guidelines.add("Maintain a slight caloric surplus (200-300 calories per day)")
                guidelines.add("High protein intake (2.0-2.4g per kg body weight)")
                guidelines.add("Include complex carbohydrates for energy")
                guidelines.add("Consume protein within 2 hours post-workout")
                guidelines.add("Ensure adequate sleep (7-9 hours per night)")
            }
            NutritionalFocus.ENDURANCE -> {
                guidelines.add("Maintain adequate carbohydrate intake for energy")
                guidelines.add("Moderate protein intake (1.4-1.8g per kg body weight)")
                guidelines.add("Stay well-hydrated during workouts")
                guidelines.add("Include electrolyte replacement for longer sessions")
                guidelines.add("Focus on whole, nutrient-dense foods")
            }
            else -> {
                guidelines.add("Maintain balanced macronutrient ratios")
                guidelines.add("Adequate protein for muscle maintenance (1.2-1.6g per kg)")
                guidelines.add("Include variety of fruits and vegetables")
                guidelines.add("Stay hydrated throughout the day")
                guidelines.add("Listen to hunger and fullness cues")
            }
        }
        
        return guidelines
    }
    
    private fun generateProgressTracking(userProfile: UserProfile, workoutRecommendations: List<WorkoutRecommendation>): ProgressTracking {
        val primaryFocus = workoutRecommendations.firstOrNull()?.nutritionalFocus
        
        val weightGoal = when (primaryFocus) {
            NutritionalFocus.WEIGHT_LOSS -> userProfile.weight * 0.95 // 5% weight loss
            NutritionalFocus.MUSCLE_GAIN -> userProfile.weight * 1.03 // 3% weight gain
            else -> null
        }
        
        val strengthGoals = mapOf(
            "Push-ups" to (if (userProfile.fitnessLevel < 5) 20.0 else 30.0),
            "Squats" to (if (userProfile.fitnessLevel < 5) 25.0 else 40.0),
            "Plank hold" to (if (userProfile.fitnessLevel < 5) 60.0 else 120.0)
        )
        
        val enduranceGoals = mapOf(
            "Running distance" to (if (userProfile.fitnessLevel < 5) 3 else 5),
            "Cycling duration" to (if (userProfile.fitnessLevel < 5) 20 else 30),
            "Swimming laps" to (if (userProfile.fitnessLevel < 5) 10 else 20)
        )
        
        val flexibilityGoals = mapOf(
            "Forward fold" to (if (userProfile.fitnessLevel < 5) 15.0 else 25.0),
            "Shoulder mobility" to (if (userProfile.fitnessLevel < 5) 45.0 else 60.0),
            "Hip flexibility" to (if (userProfile.fitnessLevel < 5) 30.0 else 45.0)
        )
        
        return ProgressTracking(
            weightGoal = weightGoal,
            strengthGoals = strengthGoals,
            enduranceGoals = enduranceGoals,
            flexibilityGoals = flexibilityGoals
        )
    }
    
    private fun generateWeeklyGoals(weekNumber: Int, userProfile: UserProfile): List<String> {
        return when (weekNumber) {
            1 -> listOf(
                "Establish workout routine",
                "Learn proper exercise form",
                "Complete all scheduled workouts"
            )
            2 -> listOf(
                "Increase workout intensity",
                "Improve exercise technique",
                "Maintain consistency"
            )
            3 -> listOf(
                "Push through plateaus",
                "Increase weights/reps",
                "Focus on progression"
            )
            4 -> listOf(
                "Complete final week strong",
                "Assess progress",
                "Plan next phase"
            )
            else -> listOf("Maintain consistency", "Focus on form", "Listen to your body")
        }
    }
}
