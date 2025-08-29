package com.nutriai.app.presentation.health

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nutriai.app.data.models.FoodRecommendation
import com.nutriai.app.databinding.ItemFoodRecommendationBinding

class FoodRecommendationsViewPagerAdapter : RecyclerView.Adapter<FoodRecommendationsViewPagerAdapter.FoodViewHolder>() {
    
    private var recommendations: List<FoodRecommendation> = emptyList()
    
    fun submitList(newRecommendations: List<FoodRecommendation>?) {
        recommendations = newRecommendations ?: emptyList()
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ItemFoodRecommendationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FoodViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        try {
            if (position < recommendations.size) {
                val item = recommendations[position]
                holder.bind(item)
            } else {
                android.util.Log.e("FoodRecommendationsViewPagerAdapter", "❌ Position $position out of bounds for size ${recommendations.size}")
            }
        } catch (e: Exception) {
            android.util.Log.e("FoodRecommendationsViewPagerAdapter", "❌ Error binding view holder at position $position: ${e.message}", e)
        }
    }
    
    override fun getItemCount(): Int = recommendations.size
    
    class FoodViewHolder(
        private val binding: ItemFoodRecommendationBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(recommendation: FoodRecommendation) {
            try {
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
            } catch (e: Exception) {
                android.util.Log.e("FoodRecommendationsViewPagerAdapter", "❌ Error binding recommendation: ${e.message}", e)
                // Set default values to prevent crash
                try {
                    binding.tvFoodName.text = "Food Item"
                    binding.tvReason.text = "Good for health"
                    binding.tvCategory.text = "Healthy Food"
                    binding.tvPriority.text = "MEDIUM"
                    binding.tvCalories.text = "0"
                    binding.tvProtein.text = "0g"
                    binding.tvCarbs.text = "0g"
                    binding.tvFat.text = "0g"
                    binding.tvServingSize.text = "1 cup"
                    binding.tvBestTime.text = "Breakfast"
                    binding.tvPreparationTips.text = "• Cook with water or milk\n• Add berries for flavor\n• Sweeten with honey\n\n🔄 Alternatives:\n• Try different cooking methods\n• Substitute with similar ingredients"
                    binding.tvFrequency.text = "Frequency: 3-4 times/week"
                } catch (fallbackError: Exception) {
                    android.util.Log.e("FoodRecommendationsViewPagerAdapter", "❌ Error in fallback binding: ${fallbackError.message}", fallbackError)
                }
            }
        }
    }
}
