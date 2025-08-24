package com.nutriai.app.presentation.health

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nutriai.app.R
import com.nutriai.app.data.models.HealthRecommendation
import com.nutriai.app.databinding.ItemHealthRecommendationBinding

class HealthRecommendationsAdapter : ListAdapter<HealthRecommendation, HealthRecommendationsAdapter.RecommendationViewHolder>(RecommendationDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val binding = ItemHealthRecommendationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecommendationViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class RecommendationViewHolder(
        private val binding: ItemHealthRecommendationBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(recommendation: HealthRecommendation) {
            binding.apply {
                tvRecommendation.text = recommendation.recommendation ?: "No recommendation available"
                tvCategory.text = recommendation.category?.capitalize() ?: "General"
                
                // Set category icon and color
                val categoryLower = recommendation.category?.lowercase() ?: "general"
                val (categoryIcon, categoryColor) = when (categoryLower) {
                    "diet" -> R.drawable.ic_food to R.color.success
                    "exercise" -> R.drawable.ic_fitness to R.color.primary
                    "lifestyle" -> R.drawable.ic_lifestyle to R.color.accent
                    "medical" -> R.drawable.ic_medical to R.color.error
                    else -> R.drawable.ic_info to R.color.text_secondary
                }
                
                ivCategoryIcon.setImageResource(categoryIcon)
                ivCategoryIcon.setColorFilter(root.context.getColor(categoryColor))
                categoryChip.chipBackgroundColor = root.context.getColorStateList(categoryColor)
                
                // Set priority indicator
                val priorityLower = recommendation.priority?.lowercase() ?: "low"
                val priorityColor = when (priorityLower) {
                    "high" -> R.color.error
                    "medium" -> R.color.warning
                    else -> R.color.success
                }
                priorityIndicator.setBackgroundColor(root.context.getColor(priorityColor))
            }
        }
    }
    
    class RecommendationDiffCallback : DiffUtil.ItemCallback<HealthRecommendation>() {
        override fun areItemsTheSame(oldItem: HealthRecommendation, newItem: HealthRecommendation): Boolean {
            return oldItem.recommendation == newItem.recommendation
        }
        
        override fun areContentsTheSame(oldItem: HealthRecommendation, newItem: HealthRecommendation): Boolean {
            return oldItem == newItem
        }
    }
}
