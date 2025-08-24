package com.nutriai.app.presentation.health

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nutriai.app.R
import com.nutriai.app.data.models.RiskFactor
import com.nutriai.app.databinding.ItemRiskFactorBinding

class RiskFactorsAdapter : ListAdapter<RiskFactor, RiskFactorsAdapter.RiskFactorViewHolder>(RiskFactorDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiskFactorViewHolder {
        val binding = ItemRiskFactorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RiskFactorViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: RiskFactorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class RiskFactorViewHolder(
        private val binding: ItemRiskFactorBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(riskFactor: RiskFactor) {
            binding.apply {
                tvRiskFactor.text = riskFactor.factor ?: "Unknown Risk Factor"
                tvRiskLevel.text = riskFactor.level?.capitalize() ?: "Unknown"
                tvRiskDescription.text = riskFactor.description ?: ""
                
                // Set risk level color and icon
                val levelLower = riskFactor.level?.lowercase() ?: "low"
                val (levelColor, levelIcon) = when (levelLower) {
                    "high" -> R.color.error to R.drawable.ic_warning
                    "medium" -> R.color.warning to R.drawable.ic_info
                    else -> R.color.success to R.drawable.ic_check_circle
                }
                
                tvRiskLevel.setTextColor(root.context.getColor(levelColor))
                ivRiskIcon.setImageResource(levelIcon)
                ivRiskIcon.setColorFilter(root.context.getColor(levelColor))
            }
        }
    }
    
    class RiskFactorDiffCallback : DiffUtil.ItemCallback<RiskFactor>() {
        override fun areItemsTheSame(oldItem: RiskFactor, newItem: RiskFactor): Boolean {
            return oldItem.factor == newItem.factor
        }
        
        override fun areContentsTheSame(oldItem: RiskFactor, newItem: RiskFactor): Boolean {
            return oldItem == newItem
        }
    }
}
