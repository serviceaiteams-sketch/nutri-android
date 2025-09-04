package com.nutriai.app.presentation.meals

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nutriai.app.databinding.ItemMealPlanBinding

class MealPlanAdapter(
    private val onViewPlan: (MealPlan) -> Unit = {},
    private val onStartPlan: (MealPlan) -> Unit = {},
    private val onSharePlan: (MealPlan) -> Unit = {},
    private val onMoreOptions: (MealPlan) -> Unit = {}
) : ListAdapter<MealPlan, MealPlanAdapter.MealPlanViewHolder>(MealPlanDiffCallback()) {
    
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
                    // Basic info
                    tvMealPlanName.text = mealPlan.name
                    tvMealPlanDescription.text = mealPlan.description
                    tvCreatedAt.text = "Created: ${mealPlan.createdAt}"
                    
                    // Nutrition stats
                    tvTotalCalories.text = mealPlan.totalCalories.toString()
                    tvTotalProtein.text = "${mealPlan.totalProtein}g"
                    tvTotalCarbs.text = "${mealPlan.totalCarbs}g"
                    tvTotalFat.text = "${mealPlan.totalFat}g"
                    
                    // Plan details
                    tvDuration.text = "${mealPlan.days.size} days"
                    tvMealCount.text = "${mealPlan.days.sumOf { it.meals.size }} meals"
                    
                    // Plan type badge
                    tvPlanType.text = "AI Generated" // You can make this dynamic based on mealPlan properties
                    
                    // Set click listeners
                    btnViewPlan.setOnClickListener { onViewPlan(mealPlan) }
                    btnStartPlan.setOnClickListener { onStartPlan(mealPlan) }
                    btnShare.setOnClickListener { onSharePlan(mealPlan) }
                    btnMore.setOnClickListener { onMoreOptions(mealPlan) }
                    
                    // Root click for general selection
                    root.setOnClickListener {
                        onViewPlan(mealPlan)
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
