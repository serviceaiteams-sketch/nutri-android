package com.nutriai.app.presentation.health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.nutriai.app.R
import com.nutriai.app.databinding.FragmentHealthAnalysisBinding
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.launch

class HealthAnalysisFragment : Fragment() {
    
    private var _binding: FragmentHealthAnalysisBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: HealthReportsViewModel
    private lateinit var metricsAdapter: KeyMetricsAdapter
    private lateinit var riskFactorsAdapter: RiskFactorsAdapter
    private lateinit var recommendationsAdapter: HealthRecommendationsAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get parent fragment's ViewModel
        viewModel = ViewModelProvider(requireParentFragment())[HealthReportsViewModel::class.java]
        
        setupRecyclerViews()
        observeViewModel()
    }
    
    private fun setupRecyclerViews() {
        // Key Metrics RecyclerView
        metricsAdapter = KeyMetricsAdapter()
        binding.rvKeyMetrics.apply {
            adapter = metricsAdapter
            layoutManager = GridLayoutManager(context, 2)
        }
        
        // Risk Factors RecyclerView
        riskFactorsAdapter = RiskFactorsAdapter()
        binding.rvRiskFactors.apply {
            adapter = riskFactorsAdapter
            layoutManager = LinearLayoutManager(context)
        }
        
        // Recommendations RecyclerView
        recommendationsAdapter = HealthRecommendationsAdapter()
        binding.rvRecommendations.apply {
            adapter = recommendationsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.analysisState.collect { state ->
                android.util.Log.d("HealthAnalysisFragment", "📱 UI State changed: $state")
                when (state) {
                    is Resource.Loading -> {
                        android.util.Log.d("HealthAnalysisFragment", "⏳ Loading...")
                        showLoading(true)
                    }
                    is Resource.Success -> {
                        android.util.Log.d("HealthAnalysisFragment", "✅ Success state received")
                        showLoading(false)
                        state.data?.let { analysis ->
                            android.util.Log.d("HealthAnalysisFragment", "📊 Analysis data: $analysis")
                            android.util.Log.d("HealthAnalysisFragment", "🎯 Health Score: ${analysis.healthScore}")
                            android.util.Log.d("HealthAnalysisFragment", "📈 Key Metrics: ${analysis.keyMetrics}")
                            updateAnalysisUI(analysis)
                        } ?: run {
                            android.util.Log.e("HealthAnalysisFragment", "❌ Analysis data is null")
                        }
                    }
                    is Resource.Error -> {
                        android.util.Log.e("HealthAnalysisFragment", "❌ Error state: ${state.message}")
                        showLoading(false)
                        showEmptyState(true)
                    }
                    null -> {
                        android.util.Log.d("HealthAnalysisFragment", "🔍 Null state")
                        showEmptyState(true)
                    }
                }
            }
        }
    }
    
    private fun updateAnalysisUI(analysis: com.nutriai.app.data.models.HealthAnalysisResult) {
        try {
            showEmptyState(false)
            
            // Update health score
            binding.tvHealthScore.text = analysis.healthScore?.toString() ?: "--"
            val healthStatus = when (analysis.healthScore ?: 0) {
                in 80..100 -> "Excellent Health"
                in 60..79 -> "Good Health"
                in 40..59 -> "Fair Health"
                else -> "Needs Attention"
            }
            binding.tvHealthStatus.text = healthStatus
            
            // Update health score color
            val scoreColor = when (analysis.healthScore ?: 0) {
                in 80..100 -> R.color.success
                in 60..79 -> R.color.warning
                else -> R.color.error
            }
            binding.tvHealthScore.setTextColor(requireContext().getColor(scoreColor))
            
            // Update report summary
            binding.tvReportSummary.text = analysis.reportSummary ?: "Analysis complete. Please review your health metrics below."
            
            // Update key metrics safely
            try {
                analysis.keyMetrics?.let { metrics ->
                    val metricsList = metrics.entries.mapNotNull { (name, value) ->
                        try {
                            KeyMetric(name, value.value, value.unit, value.status, value.normalRange)
                        } catch (e: Exception) {
                            android.util.Log.e("HealthAnalysis", "Error creating KeyMetric: $e")
                            null
                        }
                    }
                    metricsAdapter.submitList(metricsList)
                }
            } catch (e: Exception) {
                android.util.Log.e("HealthAnalysis", "Error updating key metrics: $e")
                metricsAdapter.submitList(emptyList())
            }
            
            // Update risk factors safely
            try {
                if (!analysis.riskFactors.isNullOrEmpty()) {
                    binding.riskFactorsCard.isVisible = true
                    riskFactorsAdapter.submitList(analysis.riskFactors)
                } else {
                    binding.riskFactorsCard.isVisible = false
                }
            } catch (e: Exception) {
                android.util.Log.e("HealthAnalysis", "Error updating risk factors: $e")
                binding.riskFactorsCard.isVisible = false
            }
            
            // Update recommendations safely
            try {
                analysis.recommendations?.let { recommendations ->
                    recommendationsAdapter.submitList(recommendations)
                } ?: run {
                    recommendationsAdapter.submitList(emptyList())
                }
            } catch (e: Exception) {
                android.util.Log.e("HealthAnalysis", "Error updating recommendations: $e")
                recommendationsAdapter.submitList(emptyList())
            }
            
        } catch (e: Exception) {
            android.util.Log.e("HealthAnalysis", "Error updating analysis UI: $e")
            showEmptyState(true)
        }
    }
    
    private fun showLoading(loading: Boolean) {
        // The loading state would be handled by parent fragment if needed
    }
    
    private fun showEmptyState(show: Boolean) {
        binding.emptyState.isVisible = show
        binding.healthScoreCard.isVisible = !show
        binding.nutritionSummaryCard.isVisible = !show
        binding.keyMetricsCard.isVisible = !show
        binding.recommendationsCard.isVisible = !show
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = HealthAnalysisFragment()
    }
}
