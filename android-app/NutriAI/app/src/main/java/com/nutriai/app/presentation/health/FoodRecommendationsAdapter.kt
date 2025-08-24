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
                tvFoodName.text = recommendation.food
                tvReason.text = recommendation.reason
                
                // Show serving size if available
                recommendation.servingSize?.let {
                    tvServingSize.text = "Serving: $it"
                }
                
                // Show frequency if available
                recommendation.frequency?.let {
                    tvFrequency.text = "Frequency: $it"
                }
                
                // Show nutrients if available
                if (!recommendation.nutrients.isNullOrEmpty()) {
                    tvNutrients.text = "Rich in: ${recommendation.nutrients.joinToString(", ")}"
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
