package com.nutriai.app.presentation.health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.nutriai.app.R
import com.nutriai.app.data.models.FoodRecommendation
import com.nutriai.app.domain.model.*
import com.nutriai.app.domain.usecase.WorkoutRecommendationUseCase
import java.time.DayOfWeek

class WorkoutRecommendationsFragment : Fragment() {

    private lateinit var workoutRecommendationUseCase: WorkoutRecommendationUseCase
    private lateinit var workoutAdapter: WorkoutRecommendationsAdapter
    
    // UI Components
    private lateinit var tvAiAnalysis: TextView
    private lateinit var tvAiReasoning: TextView
    private lateinit var tvTopWorkoutName: TextView
    private lateinit var tvTopWorkoutDescription: TextView
    private lateinit var tvTopDuration: TextView
    private lateinit var tvTopCalories: TextView
    private lateinit var tvTopIntensity: TextView
    private lateinit var tvTopReasoning: TextView
    private lateinit var rvWorkouts: RecyclerView
    
    // Category chips
    private lateinit var chipAll: Chip
    private lateinit var chipStrength: Chip
    private lateinit var chipCardio: Chip
    private lateinit var chipYoga: Chip
    private lateinit var chipHiit: Chip
    private lateinit var chipFlexibility: Chip

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_workout_recommendations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupRecyclerView()
        setupCategoryChips()
        initializeWorkoutRecommendationUseCase()
        loadWorkoutRecommendations()
    }

    private fun initializeViews(view: View) {
        tvAiAnalysis = view.findViewById(R.id.tvAiAnalysis)
        tvAiReasoning = view.findViewById(R.id.tvAiReasoning)
        tvTopWorkoutName = view.findViewById(R.id.tvTopWorkoutName)
        tvTopWorkoutDescription = view.findViewById(R.id.tvTopWorkoutDescription)
        tvTopDuration = view.findViewById(R.id.tvTopDuration)
        tvTopCalories = view.findViewById(R.id.tvTopCalories)
        tvTopIntensity = view.findViewById(R.id.tvTopIntensity)
        tvTopReasoning = view.findViewById(R.id.tvTopReasoning)
        rvWorkouts = view.findViewById(R.id.rvWorkouts)
        
        chipAll = view.findViewById(R.id.chipAll)
        chipStrength = view.findViewById(R.id.chipStrength)
        chipCardio = view.findViewById(R.id.chipCardio)
        chipYoga = view.findViewById(R.id.chipYoga)
        chipHiit = view.findViewById(R.id.chipHiit)
        chipFlexibility = view.findViewById(R.id.chipFlexibility)
    }

    private fun setupRecyclerView() {
        workoutAdapter = WorkoutRecommendationsAdapter(
            onWorkoutClick = { workout -> onWorkoutClick(workout) },
            onStartWorkout = { workout -> onStartWorkout(workout) }
        )
        
        rvWorkouts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = workoutAdapter
        }
    }

    private fun setupCategoryChips() {
        // Set up chip click listeners
        chipAll.setOnClickListener { filterWorkoutsByCategory(null) }
        chipStrength.setOnClickListener { filterWorkoutsByCategory(WorkoutType.STRENGTH_TRAINING) }
        chipCardio.setOnClickListener { filterWorkoutsByCategory(WorkoutType.CARDIO) }
        chipYoga.setOnClickListener { filterWorkoutsByCategory(WorkoutType.YOGA) }
        chipHiit.setOnClickListener { filterWorkoutsByCategory(WorkoutType.HIIT) }
        chipFlexibility.setOnClickListener { filterWorkoutsByCategory(WorkoutType.FLEXIBILITY) }
    }

    private fun initializeWorkoutRecommendationUseCase() {
        workoutRecommendationUseCase = WorkoutRecommendationUseCase()
    }

    private fun loadWorkoutRecommendations() {
        // Get user profile and health data (this would come from your data layer)
        val userProfile = getMockUserProfile()
        val currentNutrition = getMockCurrentNutrition()
        val healthReport = getMockHealthReport()
        
        // Generate workout recommendations
        val recommendations = workoutRecommendationUseCase.generateWorkoutRecommendations(
            userProfile = userProfile,
            currentNutrition = currentNutrition,
            healthReport = healthReport,
            availableEquipment = listOf(Equipment.NONE, Equipment.DUMBBELLS, Equipment.RESISTANCE_BANDS),
            preferredWorkoutTypes = listOf(WorkoutType.STRENGTH_TRAINING, WorkoutType.HIIT),
            availableTime = 60,
            workoutDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        )
        
        // Update UI with recommendations
        updateUIWithRecommendations(recommendations)
    }

    private fun updateUIWithRecommendations(recommendations: List<WorkoutRecommendation>) {
        if (recommendations.isNotEmpty()) {
            // Update top recommendation
            val topRecommendation = recommendations.first { it.isRecommended }
            updateTopRecommendation(topRecommendation)
            
            // Update AI analysis
            updateAiAnalysis(recommendations)
            
            // Update workouts list
            workoutAdapter.updateWorkouts(recommendations)
        }
    }

    private fun updateTopRecommendation(workout: WorkoutRecommendation) {
        tvTopWorkoutName.text = workout.name
        tvTopWorkoutDescription.text = workout.description
        tvTopDuration.text = workout.duration.toString()
        tvTopCalories.text = workout.caloriesBurn.toString()
        tvTopIntensity.text = workout.intensity.name
        tvTopReasoning.text = workout.reasoning
    }

    private fun updateAiAnalysis(recommendations: List<WorkoutRecommendation>) {
        val topWorkout = recommendations.first { it.isRecommended }
        val analysis = when (topWorkout.nutritionalFocus) {
            NutritionalFocus.WEIGHT_LOSS -> "AI Analysis: Weight loss focus recommended"
            NutritionalFocus.MUSCLE_GAIN -> "AI Analysis: Muscle gain focus recommended"
            NutritionalFocus.ENDURANCE -> "AI Analysis: Endurance focus recommended"
            NutritionalFocus.FLEXIBILITY -> "AI Analysis: Flexibility focus recommended"
            NutritionalFocus.STRENGTH -> "AI Analysis: Strength focus recommended"
            NutritionalFocus.RECOVERY -> "AI Analysis: Recovery focus recommended"
            NutritionalFocus.ENERGY_BOOST -> "AI Analysis: Energy boost focus recommended"
            NutritionalFocus.STRESS_RELIEF -> "AI Analysis: Stress relief focus recommended"
            NutritionalFocus.BALANCE -> "AI Analysis: Balanced fitness focus recommended"
        }
        
        tvAiAnalysis.text = analysis
        tvAiReasoning.text = "Based on your current nutrition, health metrics, and fitness goals"
    }

    private fun filterWorkoutsByCategory(category: WorkoutType?) {
        workoutAdapter.filterByCategory(category)
        
        // Update chip selection
        updateChipSelection(category)
    }

    private fun updateChipSelection(selectedCategory: WorkoutType?) {
        // Reset all chips
        chipAll.setChipBackgroundColorResource(R.color.background_secondary)
        chipStrength.setChipBackgroundColorResource(R.color.background_secondary)
        chipCardio.setChipBackgroundColorResource(R.color.background_secondary)
        chipYoga.setChipBackgroundColorResource(R.color.background_secondary)
        chipHiit.setChipBackgroundColorResource(R.color.background_secondary)
        chipFlexibility.setChipBackgroundColorResource(R.color.background_secondary)
        
        // Set selected chip
        when (selectedCategory) {
            null -> chipAll.setChipBackgroundColorResource(R.color.primary)
            WorkoutType.STRENGTH_TRAINING -> chipStrength.setChipBackgroundColorResource(R.color.primary)
            WorkoutType.CARDIO -> chipCardio.setChipBackgroundColorResource(R.color.primary)
            WorkoutType.YOGA -> chipYoga.setChipBackgroundColorResource(R.color.primary)
            WorkoutType.HIIT -> chipHiit.setChipBackgroundColorResource(R.color.primary)
            WorkoutType.FLEXIBILITY -> chipFlexibility.setChipBackgroundColorResource(R.color.primary)
        }
    }

    private fun onWorkoutClick(workout: WorkoutRecommendation) {
        // Navigate to workout details
        // This would typically open a new fragment or activity
        showWorkoutDetails(workout)
    }

    private fun onStartWorkout(workout: WorkoutRecommendation) {
        // Start the workout
        // This would typically open a workout session screen
        startWorkoutSession(workout)
    }

    private fun showWorkoutDetails(workout: WorkoutRecommendation) {
        // Show workout details dialog or navigate to details screen
        // For now, just show a simple message
        // You can implement this based on your app's navigation pattern
    }

    private fun startWorkoutSession(workout: WorkoutRecommendation) {
        // Start workout session
        // This would typically open a workout timer/exercise screen
        // You can implement this based on your app's workout flow
    }

    // Mock data methods - replace with actual data from your repository
    private fun getMockUserProfile(): UserProfile {
        return UserProfile(
            id = "user_1",
            name = "John Doe",
            age = 28,
            gender = "male",
            height = 175.0,
            weight = 80.0,
            goal = "weight_loss",
            activityLevel = "moderately_active",
            fitnessLevel = 5
        )
    }

    private fun getMockCurrentNutrition(): List<FoodRecommendation> {
        return listOf(
            FoodRecommendation(
                food = "Grilled Chicken Breast",
                reason = "Lean protein source for muscle building",
                category = "protein",
                priority = "high",
                calories = 250f,
                protein = 45.0f,
                carbs = 0.0f,
                fat = 5.0f,
                nutrients = listOf("Protein", "B6", "B12"),
                servingSize = "100g",
                bestTime = "Post-workout",
                preparationTips = "Grill with minimal oil",
                alternatives = "Turkey breast, fish",
                frequency = "Daily",
                notes = "Excellent for weight loss and muscle gain"
            ),
            FoodRecommendation(
                food = "Brown Rice",
                reason = "Complex carbohydrates for sustained energy",
                category = "carbs",
                priority = "medium",
                calories = 150f,
                protein = 3.0f,
                carbs = 30.0f,
                fat = 1.0f,
                nutrients = listOf("Fiber", "B vitamins", "Minerals"),
                servingSize = "1 cup cooked",
                bestTime = "Pre-workout",
                preparationTips = "Cook with broth for flavor",
                alternatives = "Quinoa, sweet potato",
                frequency = "3-4 times per week",
                notes = "Good source of complex carbs"
            )
        )
    }

    private fun getMockHealthReport(): HealthReport {
        return HealthReport(
            id = "health_1",
            userId = "user_1",
            date = "2024-01-15",
            weight = 80.0,
            bodyFatPercentage = 18.0,
            muscleMass = 65.0,
            heartRate = 75,
            bloodPressure = 125,
            flexibilityScore = 6.5,
            stressLevel = 6.0,
            fatigueLevel = 5.0,
            sleepQuality = 7.0,
            energyLevel = 6.5,
            mood = 7.0,
            notes = "Good progress, continue current routine"
        )
    }
}
