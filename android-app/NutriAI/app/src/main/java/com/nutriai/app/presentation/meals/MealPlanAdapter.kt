package com.nutriai.app.presentation.meals

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nutriai.app.databinding.ItemMealPlanBinding

class MealPlanAdapter : ListAdapter<MealPlan, MealPlanAdapter.MealPlanViewHolder>(MealPlanDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealPlanViewHolder {
        val binding = ItemMealPlanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MealPlanViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: MealPlanViewHolder, position: Int) {
        try {
            val mealPlan = getItem(position)
            holder.bind(mealPlan)
        } catch (e: Exception) {
            android.util.Log.e("MealPlanAdapter", "❌ Error binding meal plan at position $position: ${e.message}", e)
        }
    }
    
    class MealPlanViewHolder(
        private val binding: ItemMealPlanBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(mealPlan: MealPlan) {
            try {
                binding.apply {
                    tvMealPlanName.text = mealPlan.name
                    tvMealPlanDescription.text = mealPlan.description
                    tvTotalCalories.text = "${mealPlan.totalCalories} cal"
                    tvTotalProtein.text = "${mealPlan.totalProtein}g"
                    tvTotalCarbs.text = "${mealPlan.totalCarbs}g"
                    tvTotalFat.text = "${mealPlan.totalFat}g"
                    tvCreatedAt.text = "Created: ${mealPlan.createdAt}"
                    
                    // Set click listener
                    root.setOnClickListener {
                        // Handle meal plan selection
                        android.util.Log.d("MealPlanAdapter", "📋 Selected meal plan: ${mealPlan.name}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MealPlanAdapter", "❌ Error binding meal plan: ${e.message}", e)
            }
        }
    }
    
    private class MealPlanDiffCallback : DiffUtil.ItemCallback<MealPlan>() {
        override fun areItemsTheSame(oldItem: MealPlan, newItem: MealPlan): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: MealPlan, newItem: MealPlan): Boolean {
            return oldItem == newItem
        }
    }
}
