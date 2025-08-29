package com.nutriai.app.presentation.health

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nutriai.app.R
import com.nutriai.app.databinding.ItemKeyMetricBinding

data class KeyMetric(
    val name: String,
    val value: Float,
    val unit: String,
    val status: String,
    val normalRange: String?
)

class KeyMetricsAdapter : ListAdapter<KeyMetric, KeyMetricsAdapter.MetricViewHolder>(MetricDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetricViewHolder {
        val binding = ItemKeyMetricBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MetricViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: MetricViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class MetricViewHolder(
        private val binding: ItemKeyMetricBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(metric: KeyMetric) {
            binding.apply {
                tvMetricName.text = metric.name.replace("_", " ").replaceFirstChar { it.uppercase() }
                tvMetricValue.text = "${metric.value} ${metric.unit}"
                tvMetricStatus.text = metric.status.replaceFirstChar { it.uppercase() }
                
                // Set status color
                val statusColor = when (metric.status.lowercase()) {
                    "normal" -> R.color.success
                    "high" -> R.color.error
                    "low" -> R.color.warning
                    else -> R.color.text_secondary
                }
                tvMetricStatus.setTextColor(root.context.getColor(statusColor))
                statusIndicator.setBackgroundColor(root.context.getColor(statusColor))
                
                // Show normal range if available
                metric.normalRange?.let {
                    tvNormalRange.text = "Normal: $it"
                }
            }
        }
    }
    
    class MetricDiffCallback : DiffUtil.ItemCallback<KeyMetric>() {
        override fun areItemsTheSame(oldItem: KeyMetric, newItem: KeyMetric): Boolean {
            return oldItem.name == newItem.name
        }
        
        override fun areContentsTheSame(oldItem: KeyMetric, newItem: KeyMetric): Boolean {
            return oldItem == newItem
        }
    }
}
