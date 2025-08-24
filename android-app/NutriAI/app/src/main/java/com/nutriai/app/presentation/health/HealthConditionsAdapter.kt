package com.nutriai.app.presentation.health

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nutriai.app.R
import com.nutriai.app.data.models.HealthCondition
import com.nutriai.app.databinding.ItemHealthConditionBinding

class HealthConditionsAdapter(
    private val onRemove: (HealthCondition) -> Unit
) : ListAdapter<HealthCondition, HealthConditionsAdapter.ConditionViewHolder>(ConditionDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConditionViewHolder {
        val binding = ItemHealthConditionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConditionViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ConditionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ConditionViewHolder(
        private val binding: ItemHealthConditionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(condition: HealthCondition) {
            binding.apply {
                tvConditionName.text = condition.condition
                tvSeverity.text = condition.severity?.capitalize() ?: "Mild"
                
                // Set severity color
                val severityColor = when (condition.severity) {
                    "severe" -> R.color.error
                    "moderate" -> R.color.warning
                    else -> R.color.success
                }
                severityChip.chipBackgroundColor = root.context.getColorStateList(severityColor)
                
                // Show medications if available
                if (!condition.medications.isNullOrEmpty()) {
                    tvMedications.text = "Medications: ${condition.medications.joinToString(", ")}"
                } else {
                    tvMedications.text = "No medications"
                }
                
                btnRemove.setOnClickListener {
                    onRemove(condition)
                }
            }
        }
    }
    
    class ConditionDiffCallback : DiffUtil.ItemCallback<HealthCondition>() {
        override fun areItemsTheSame(oldItem: HealthCondition, newItem: HealthCondition): Boolean {
            return oldItem.condition == newItem.condition
        }
        
        override fun areContentsTheSame(oldItem: HealthCondition, newItem: HealthCondition): Boolean {
            return oldItem == newItem
        }
    }
}
