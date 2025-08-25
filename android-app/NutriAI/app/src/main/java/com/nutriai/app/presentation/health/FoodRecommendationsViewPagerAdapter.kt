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
                tvFoodName.text = recommendation.food
                tvReason.text = recommendation.reason
                
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
                    tvServingSize.text = "1 serving"
                }
                
                // Best time
                tvBestTime.text = recommendation.bestTime ?: "Anytime"
                
                // Preparation tips
                tvPreparationTips.text = recommendation.preparationTips ?: "• Follow standard cooking methods\n• Use fresh ingredients when possible"
                
                // Alternatives
                tvAlternatives.text = recommendation.alternatives ?: "• Try different cooking methods\n• Substitute with similar ingredients"
                
                // Frequency
                recommendation.frequency?.let { freq ->
                    tvFrequency.text = "Frequency: $freq"
                } ?: run {
                    tvFrequency.text = "Frequency: As needed"
                }
                
                // Notes/Warnings
                recommendation.notes?.let { notes ->
                    tvNotes.text = notes
                    tvNotes.visibility = android.view.View.VISIBLE
                } ?: run {
                    tvNotes.visibility = android.view.View.GONE
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FoodRecommendationsViewPagerAdapter", "❌ Error binding recommendation: ${e.message}", e)
        }
    }
    }
}
