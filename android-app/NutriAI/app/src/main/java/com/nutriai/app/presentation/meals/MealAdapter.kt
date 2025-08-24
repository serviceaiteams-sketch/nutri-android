package com.nutriai.app.presentation.meals

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nutriai.app.R
import com.nutriai.app.data.models.Meal
import com.nutriai.app.databinding.ItemMealBinding
import java.text.SimpleDateFormat
import java.util.*

class MealAdapter(
    private val onItemClick: (Meal) -> Unit,
    private val onDeleteClick: (Meal) -> Unit
) : ListAdapter<Meal, MealAdapter.MealViewHolder>(MealDiffCallback()) {
    
    // Track expanded state for each meal
    private val expandedPositions = mutableSetOf<Int>()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val binding = ItemMealBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MealViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }
    
    inner class MealViewHolder(
        private val binding: ItemMealBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(meal: Meal, position: Int) {
            val isExpanded = expandedPositions.contains(position)
            
            binding.apply {
                // Meal type
                tvMealType.text = meal.mealType.capitalize()
                
                // Time
                meal.createdAt?.let { dateStr ->
                    try {
                        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                        val date = inputFormat.parse(dateStr)
                        tvMealTime.text = date?.let { outputFormat.format(it) } ?: ""
                    } catch (e: Exception) {
                        tvMealTime.text = ""
                    }
                }
                
                // Food items
                val foodNames = meal.foodItems?.joinToString(", ") { it.foodName } ?: "No items"
                tvFoodItems.text = foodNames
                
                // Nutrition
                tvCalories.text = "${meal.totalCalories?.toInt() ?: 0} cal"
                tvProtein.text = "${meal.totalProtein?.toInt() ?: 0}g"
                tvCarbs.text = "${meal.totalCarbs?.toInt() ?: 0}g"
                tvFat.text = "${meal.totalFat?.toInt() ?: 0}g"
                
                // Expandable section
                expandableSection.visibility = if (isExpanded) ViewGroup.VISIBLE else ViewGroup.GONE
                
                // Expand/collapse icon
                ivExpandCollapse.setImageResource(
                    if (isExpanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more
                )
                
                // Detailed food items
                val detailedFoodText = meal.foodItems?.joinToString("\n") { foodItem ->
                    "• ${foodItem.foodName} (${foodItem.quantity} ${foodItem.unit ?: "serving"})"
                } ?: "No food items"
                tvDetailedFoodItems.text = detailedFoodText
                
                // Additional nutrition details
                val totalFiber = meal.foodItems?.sumOf { it.fiber?.toInt() ?: 0 } ?: 0
                val totalSugar = meal.foodItems?.sumOf { it.sugar?.toInt() ?: 0 } ?: 0
                val totalSodium = meal.foodItems?.sumOf { it.sodium?.toInt() ?: 0 } ?: 0
                
                tvFiber.text = "${totalFiber}g"
                tvSugar.text = "${totalSugar}g"
                tvSodium.text = "${totalSodium}mg"
                
                // Click listeners
                root.setOnClickListener { 
                    toggleExpanded(position)
                    onItemClick(meal)
                }
                
                btnMore.setOnClickListener { view ->
                    PopupMenu(view.context, view).apply {
                        menuInflater.inflate(R.menu.menu_meal_options, menu)
                        setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.action_delete -> {
                                    onDeleteClick(meal)
                                    true
                                }
                                else -> false
                            }
                        }
                        show()
                    }
                }
            }
        }
    }
    
    private fun toggleExpanded(position: Int) {
        if (expandedPositions.contains(position)) {
            expandedPositions.remove(position)
        } else {
            expandedPositions.add(position)
        }
        notifyItemChanged(position)
    }
    
    class MealDiffCallback : DiffUtil.ItemCallback<Meal>() {
        override fun areItemsTheSame(oldItem: Meal, newItem: Meal): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Meal, newItem: Meal): Boolean {
            return oldItem == newItem
        }
    }
}
