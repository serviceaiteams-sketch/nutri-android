package com.nutriai.app.presentation.health

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nutriai.app.R
import com.nutriai.app.data.models.FoodRecommendation
import com.nutriai.app.databinding.ItemFoodRecommendationBinding

class FoodRecommendationsAdapter : ListAdapter<FoodRecommendation, FoodRecommendationsAdapter.FoodViewHolder>(FoodDiffCallback()) {
    
    override fun submitList(list: List<FoodRecommendation>?) {
        android.util.Log.d("FoodRecommendationsAdapter", "📋 Submitting list: ${list?.size ?: 0} items")
        android.util.Log.d("FoodRecommendationsAdapter", "📋 List content: $list")
        super.submitList(list)
        android.util.Log.d("FoodRecommendationsAdapter", "📋 After submit, item count: ${itemCount}")
        android.util.Log.d("FoodRecommendationsAdapter", "📋 Current list: ${currentList}")
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        android.util.Log.d("FoodRecommendationsAdapter", "🏗️ Creating ViewHolder for viewType: $viewType")
        val binding = ItemFoodRecommendationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        android.util.Log.d("FoodRecommendationsAdapter", "🏗️ ViewHolder created with binding: $binding")
        return FoodViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val item = getItem(position)
        android.util.Log.d("FoodRecommendationsAdapter", "🔗 Binding item $position: $item")
        holder.bind(item)
    }
    
    class FoodViewHolder(
        private val binding: ItemFoodRecommendationBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(recommendation: FoodRecommendation) {
            android.util.Log.d("FoodViewHolder", "🔗 Binding recommendation: $recommendation")
            binding.apply {
                // Basic food information
                tvFoodName.text = recommendation.food ?: "Food Item"
                tvReason.text = recommendation.reason ?: "Good for health"
                
                // Category
                tvCategory.text = recommendation.category ?: "Healthy Food"
                
                // Priority
                tvPriority.text = recommendation.priority ?: "MEDIUM"
                
                // Nutritional information
                recommendation.calories?.let { calories ->
                    tvCalories.text = calories.toString()
                } ?: run {
                    tvCalories.text = "0"
                }
                
                recommendation.protein?.let { protein ->
                    tvProtein.text = "${protein}g"
                } ?: run {
                    tvProtein.text = "0g"
                }
                
                recommendation.carbs?.let { carbs ->
                    tvCarbs.text = "${carbs}g"
                } ?: run {
                    tvCarbs.text = "0g"
                }
                
                recommendation.fat?.let { fat ->
                    tvFat.text = "${fat}g"
                } ?: run {
                    tvFat.text = "0g"
                }
                
                // Serving and timing information
                recommendation.servingSize?.let { serving ->
                    tvServingSize.text = serving
                } ?: run {
                    tvServingSize.text = "1 cup"
                }
                
                // Best time
                tvBestTime.text = recommendation.bestTime ?: "Breakfast"
                
                // Preparation tips (including alternatives)
                val preparationTips = recommendation.preparationTips ?: "• Cook with water or milk\n• Add berries for flavor\n• Sweeten with honey"
                val alternatives = recommendation.alternatives ?: "• Try different cooking methods\n• Substitute with similar ingredients"
                tvPreparationTips.text = "$preparationTips\n\n🔄 Alternatives:\n$alternatives"
                
                // Frequency
                recommendation.frequency?.let { freq ->
                    tvFrequency.text = "Frequency: $freq"
                } ?: run {
                    tvFrequency.text = "Frequency: 3-4 times/week"
                }
            }
        }
    }
    
    class FoodDiffCallback : DiffUtil.ItemCallback<FoodRecommendation>() {
        override fun areItemsTheSame(oldItem: FoodRecommendation, newItem: FoodRecommendation): Boolean {
            val result = oldItem.food == newItem.food
            android.util.Log.d("FoodDiffCallback", "🔍 areItemsTheSame: $result (${oldItem.food} vs ${newItem.food})")
            return result
        }
        
        override fun areContentsTheSame(oldItem: FoodRecommendation, newItem: FoodRecommendation): Boolean {
            val result = oldItem == newItem
            android.util.Log.d("FoodDiffCallback", "🔍 areContentsTheSame: $result (${oldItem} vs ${newItem})")
            return result
        }
    }
}
