package com.nutriai.app.presentation.food

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.nutriai.app.R
import com.nutriai.app.data.models.RecognizedFood
import com.nutriai.app.databinding.ItemRecognizedFoodBinding

class RecognizedFoodAdapter(
    private val onItemClick: (RecognizedFood) -> Unit
) : ListAdapter<RecognizedFood, RecognizedFoodAdapter.FoodViewHolder>(FoodDiffCallback()) {
    
    private var selectedPosition = 0
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ItemRecognizedFoodBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FoodViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = getItem(position)
        holder.bind(food, position == selectedPosition)
        
        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            onItemClick(food)
        }
    }
    
    inner class FoodViewHolder(
        private val binding: ItemRecognizedFoodBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(food: RecognizedFood, isSelected: Boolean) {
            binding.apply {
                tvFoodName.text = food.name
                val confidence = food.confidence ?: 0.95f
                tvConfidence.text = "${(confidence * 100).toInt()}%"
                
                // Build serving size text
                val servingText = when {
                    food.quantity != null && food.unit != null -> 
                        "Serving: ${food.quantity} ${food.unit}"
                    food.servingSize != null -> 
                        "Serving: ${food.servingSize}"
                    else -> 
                        "Serving: 1 portion"
                }
                tvServingSize.text = servingText
                
                // Nutrition info - use nutrition from response or show placeholders
                if (food.nutrition != null) {
                    // Debug logging
                    android.util.Log.d("FoodAdapter", "Food: ${food.name}, Nutrition: calories=${food.nutrition.calories}, protein=${food.nutrition.protein}")
                    
                    tvCalories.text = "${food.nutrition.calories?.toInt() ?: "0"}"
                    tvProtein.text = "${food.nutrition.protein?.toInt() ?: "0"}g"
                    tvCarbs.text = "${food.nutrition.carbs?.toInt() ?: "0"}g"
                    tvFat.text = "${food.nutrition.fat?.toInt() ?: "0"}g"
                    
                    // Additional nutrition fields
                    tvFiber.text = "${food.nutrition.fiber?.toInt() ?: "0"}g"
                    tvSugar.text = "${food.nutrition.sugar?.toInt() ?: "0"}g"
                    tvSodium.text = "${food.nutrition.sodium?.toInt() ?: "0"}mg"
                } else {
                    // No nutrition data
                    android.util.Log.d("FoodAdapter", "Food: ${food.name}, No nutrition data")
                    tvCalories.text = "0"
                    tvProtein.text = "0g"
                    tvCarbs.text = "0g"
                    tvFat.text = "0g"
                    tvFiber.text = "0g"
                    tvSugar.text = "0g"
                    tvSodium.text = "0mg"
                }
                
                // Set confidence color based on value
                val confidenceColor = when {
                    confidence >= 0.8f -> R.color.success
                    confidence >= 0.6f -> R.color.warning
                    else -> R.color.error
                }
                tvConfidence.setTextColor(root.context.getColor(confidenceColor))
                
                // Set selected state
                (root as MaterialCardView).isChecked = isSelected
                if (isSelected) {
                    root.strokeWidth = root.context.resources.getDimensionPixelSize(R.dimen.card_stroke_width)
                    root.strokeColor = root.context.getColor(R.color.primary)
                } else {
                    root.strokeWidth = 0
                }
            }
        }
    }
    
    class FoodDiffCallback : DiffUtil.ItemCallback<RecognizedFood>() {
        override fun areItemsTheSame(oldItem: RecognizedFood, newItem: RecognizedFood): Boolean {
            return oldItem.name == newItem.name
        }
        
        override fun areContentsTheSame(oldItem: RecognizedFood, newItem: RecognizedFood): Boolean {
            return oldItem == newItem
        }
    }
}
