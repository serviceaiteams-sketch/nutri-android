package com.nutriai.app.presentation.health

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.nutriai.app.R
import com.nutriai.app.domain.model.WorkoutRecommendation
import com.nutriai.app.domain.model.WorkoutType

class WorkoutRecommendationsAdapter(
    private val onWorkoutClick: (WorkoutRecommendation) -> Unit,
    private val onStartWorkout: (WorkoutRecommendation) -> Unit
) : RecyclerView.Adapter<WorkoutRecommendationsAdapter.WorkoutViewHolder>() {

    private var workouts = listOf<WorkoutRecommendation>()
    private var selectedCategory: WorkoutType? = null

    fun updateWorkouts(newWorkouts: List<WorkoutRecommendation>) {
        workouts = newWorkouts
        notifyDataSetChanged()
    }

    fun filterByCategory(category: WorkoutType?) {
        selectedCategory = category
        notifyDataSetChanged()
    }

    fun getFilteredWorkouts(): List<WorkoutRecommendation> {
        return if (selectedCategory == null) {
            workouts
        } else {
            workouts.filter { it.workoutType == selectedCategory }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_recommendation, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = getFilteredWorkouts()[position]
        holder.bind(workout)
    }

    override fun getItemCount(): Int = getFilteredWorkouts().size

    inner class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val tvWorkoutName: TextView = itemView.findViewById(R.id.tvWorkoutName)
        private val tvWorkoutType: TextView = itemView.findViewById(R.id.tvWorkoutType)
        private val tvWorkoutDescription: TextView = itemView.findViewById(R.id.tvWorkoutDescription)
        private val tvDifficulty: TextView = itemView.findViewById(R.id.tvDifficulty)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val tvCalories: TextView = itemView.findViewById(R.id.tvCalories)
        private val tvIntensity: TextView = itemView.findViewById(R.id.tvIntensity)
        private val tvAiReasoning: TextView = itemView.findViewById(R.id.tvAiReasoning)
        private val chipGroupMuscles: ChipGroup = itemView.findViewById(R.id.chipGroupMuscles)
        private val chipGroupEquipment: ChipGroup = itemView.findViewById(R.id.chipGroupEquipment)
        private val btnStartWorkout: Button = itemView.findViewById(R.id.btnStartWorkout)
        private val btnViewDetails: Button = itemView.findViewById(R.id.btnViewDetails)

        fun bind(workout: WorkoutRecommendation) {
            // Set workout name and description
            tvWorkoutName.text = workout.name
            tvWorkoutDescription.text = workout.description
            
            // Set workout type
            tvWorkoutType.text = getWorkoutTypeDisplayName(workout.workoutType)
            
            // Set difficulty
            tvDifficulty.text = workout.difficulty.name
            setDifficultyBackground(tvDifficulty, workout.difficulty)
            
            // Set workout stats
            tvDuration.text = workout.duration.toString()
            tvCalories.text = workout.caloriesBurn.toString()
            tvIntensity.text = workout.intensity.name
            
            // Set AI reasoning
            tvAiReasoning.text = workout.reasoning
            
            // Set muscle groups
            setupMuscleGroupChips(workout.muscleGroups)
            
            // Set equipment
            setupEquipmentChips(workout.equipment)
            
            // Set click listeners
            itemView.setOnClickListener { onWorkoutClick(workout) }
            btnStartWorkout.setOnClickListener { onStartWorkout(workout) }
            btnViewDetails.setOnClickListener { onWorkoutClick(workout) }
        }

        private fun getWorkoutTypeDisplayName(workoutType: WorkoutType): String {
            return when (workoutType) {
                WorkoutType.STRENGTH_TRAINING -> "Strength Training"
                WorkoutType.CARDIO -> "Cardio"
                WorkoutType.YOGA -> "Yoga"
                WorkoutType.PILATES -> "Pilates"
                WorkoutType.HIIT -> "HIIT"
                WorkoutType.FLEXIBILITY -> "Flexibility"
                WorkoutType.BALANCE -> "Balance"
                WorkoutType.SPORTS -> "Sports"
                WorkoutType.DANCE -> "Dance"
                WorkoutType.SWIMMING -> "Swimming"
                WorkoutType.CYCLING -> "Cycling"
                WorkoutType.RUNNING -> "Running"
                WorkoutType.WALKING -> "Walking"
                WorkoutType.BODYWEIGHT -> "Bodyweight"
                WorkoutType.FUNCTIONAL_TRAINING -> "Functional Training"
            }
        }

        private fun setDifficultyBackground(textView: TextView, difficulty: com.nutriai.app.domain.model.WorkoutDifficulty) {
            val backgroundRes = when (difficulty) {
                com.nutriai.app.domain.model.WorkoutDifficulty.BEGINNER -> R.drawable.bg_difficulty_beginner
                com.nutriai.app.domain.model.WorkoutDifficulty.INTERMEDIATE -> R.drawable.bg_difficulty_intermediate
                com.nutriai.app.domain.model.WorkoutDifficulty.ADVANCED -> R.drawable.bg_difficulty_advanced
                com.nutriai.app.domain.model.WorkoutDifficulty.EXPERT -> R.drawable.bg_difficulty_expert
            }
            textView.setBackgroundResource(backgroundRes)
        }

        private fun setupMuscleGroupChips(muscleGroups: List<com.nutriai.app.domain.model.MuscleGroup>) {
            chipGroupMuscles.removeAllViews()
            
            muscleGroups.forEach { muscleGroup ->
                val chip = Chip(itemView.context)
                chip.text = getMuscleGroupDisplayName(muscleGroup)
                chip.isCheckable = false
                chip.chipBackgroundColor = itemView.context.getColorStateList(R.color.background_tertiary)
                chip.setTextColor(itemView.context.getColor(R.color.black))
                chip.textSize = 12f
                chipGroupMuscles.addView(chip)
            }
        }

        private fun setupEquipmentChips(equipment: List<com.nutriai.app.domain.model.Equipment>) {
            chipGroupEquipment.removeAllViews()
            
            equipment.forEach { equip ->
                val chip = Chip(itemView.context)
                chip.text = getEquipmentDisplayName(equip)
                chip.isCheckable = false
                chip.chipBackgroundColor = itemView.context.getColorStateList(R.color.background_tertiary)
                chip.setTextColor(itemView.context.getColor(R.color.black))
                chip.textSize = 12f
                chipGroupEquipment.addView(chip)
            }
        }

        private fun getMuscleGroupDisplayName(muscleGroup: com.nutriai.app.domain.model.MuscleGroup): String {
            return when (muscleGroup) {
                com.nutriai.app.domain.model.MuscleGroup.CHEST -> "Chest"
                com.nutriai.app.domain.model.MuscleGroup.BACK -> "Back"
                com.nutriai.app.domain.model.MuscleGroup.SHOULDERS -> "Shoulders"
                com.nutriai.app.domain.model.MuscleGroup.BICEPS -> "Biceps"
                com.nutriai.app.domain.model.MuscleGroup.TRICEPS -> "Triceps"
                com.nutriai.app.domain.model.MuscleGroup.FOREARMS -> "Forearms"
                com.nutriai.app.domain.model.MuscleGroup.CORE -> "Core"
                com.nutriai.app.domain.model.MuscleGroup.GLUTES -> "Glutes"
                com.nutriai.app.domain.model.MuscleGroup.QUADRICEPS -> "Quadriceps"
                com.nutriai.app.domain.model.MuscleGroup.HAMSTRINGS -> "Hamstrings"
                com.nutriai.app.domain.model.MuscleGroup.CALVES -> "Calves"
                com.nutriai.app.domain.model.MuscleGroup.FULL_BODY -> "Full Body"
            }
        }

        private fun getEquipmentDisplayName(equipment: com.nutriai.app.domain.model.Equipment): String {
            return when (equipment) {
                com.nutriai.app.domain.model.Equipment.NONE -> "No Equipment"
                com.nutriai.app.domain.model.Equipment.DUMBBELLS -> "Dumbbells"
                com.nutriai.app.domain.model.Equipment.BARBELL -> "Barbell"
                com.nutriai.app.domain.model.Equipment.RESISTANCE_BANDS -> "Resistance Bands"
                com.nutriai.app.domain.model.Equipment.YOGA_MAT -> "Yoga Mat"
                com.nutriai.app.domain.model.Equipment.PULL_UP_BAR -> "Pull-up Bar"
                com.nutriai.app.domain.model.Equipment.BENCH -> "Bench"
                com.nutriai.app.domain.model.Equipment.CARDIO_MACHINE -> "Cardio Machine"
                com.nutriai.app.domain.model.Equipment.WEIGHT_MACHINE -> "Weight Machine"
                com.nutriai.app.domain.model.Equipment.KETTLEBELL -> "Kettlebell"
                com.nutriai.app.domain.model.Equipment.MEDICINE_BALL -> "Medicine Ball"
                com.nutriai.app.domain.model.Equipment.FOAM_ROLLER -> "Foam Roller"
            }
        }
    }
}
