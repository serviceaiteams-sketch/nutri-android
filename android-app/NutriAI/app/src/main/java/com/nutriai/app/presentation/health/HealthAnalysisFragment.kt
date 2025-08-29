package com.nutriai.app.presentation.health

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.nutriai.app.R
import com.nutriai.app.databinding.FragmentHealthAnalysisBinding
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.delay
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
        animateEntrance()
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
    
    private fun animateEntrance() {
        // Check if fragment is still attached
        if (!isAdded || context == null) return
        
        // Main container fade in
        binding.root.alpha = 0f
        binding.root.animate()
            .alpha(1f)
            .setDuration(800)
            .start()
        
        // Header slide down animation
        binding.root.findViewById<View>(R.id.healthScoreCard)?.let { healthScoreCard ->
            healthScoreCard.alpha = 0f
            healthScoreCard.translationY = -50f
            healthScoreCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(200L)
                .start()
        }
        
        // Report summary card slide up animation
        binding.nutritionSummaryCard.alpha = 0f
        binding.nutritionSummaryCard.translationY = 50f
        binding.nutritionSummaryCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(400L)
            .start()
        
        // Animate health score
        animateHealthScore()
    }
    
    private fun animateHealthScore() {
        lifecycleScope.launch {
            delay(1000) // Wait for entrance animations
            
            // Check if fragment is still attached
            if (!isAdded || context == null) return@launch
            
            // Animate health score from 0 to target
            binding.tvHealthScore.text = "0"
            ValueAnimator.ofInt(0, 85).apply {
                duration = 2000
                addUpdateListener { animator ->
                    binding.tvHealthScore.text = animator.animatedValue.toString()
                }
                start()
            }
            
            // Animate status text
            binding.tvHealthStatus.text = "Excellent"
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
                            animateAnalysisData(analysis)
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
    
    private fun animateAnalysisData(analysis: com.nutriai.app.data.models.HealthAnalysisResult) {
        // Animate health score
        val targetScore = analysis.healthScore ?: 0
        ValueAnimator.ofInt(0, targetScore).apply {
            duration = 1500
            addUpdateListener { animator ->
                binding.tvHealthScore.text = animator.animatedValue.toString()
            }
            start()
        }
        
        // Animate analysis details if visible
        if (binding.cardAnalysisDetails.visibility == View.VISIBLE) {
            // Animate reports analyzed - convert List<String> to count
            val reportsAnalyzed = analysis.analysisDetails?.reportsAnalyzed?.size ?: 0
            ValueAnimator.ofInt(0, reportsAnalyzed).apply {
                duration = 1000
                startDelay = 200L
                addUpdateListener { animator ->
                    binding.tvReportsAnalyzed.text = animator.animatedValue.toString()
                }
                start()
            }
            
            // Animate total tests
            val totalTests = analysis.analysisDetails?.totalTests ?: 0
            ValueAnimator.ofInt(0, totalTests).apply {
                duration = 1000
                startDelay = 400L
                addUpdateListener { animator ->
                    binding.tvTotalTests.text = animator.animatedValue.toString()
                }
                start()
            }
            
            // Animate abnormal findings
            val abnormalFindings = analysis.analysisDetails?.abnormalFindings ?: 0
            ValueAnimator.ofInt(0, abnormalFindings).apply {
                duration = 1000
                startDelay = 600L
                addUpdateListener { animator ->
                    binding.tvAbnormalFindings.text = animator.animatedValue.toString()
                }
                start()
            }
            
            // Animate critical alerts
            val criticalAlerts = analysis.analysisDetails?.criticalAlerts ?: 0
            ValueAnimator.ofInt(0, criticalAlerts).apply {
                duration = 1000
                startDelay = 800L
                addUpdateListener { animator ->
                    binding.tvCriticalAlerts.text = animator.animatedValue.toString()
                }
                start()
            }
        }
    }
    
    private fun updateAnalysisUI(analysis: com.nutriai.app.data.models.HealthAnalysisResult) {
        // Update health score
        analysis.healthScore?.let { score ->
            binding.tvHealthScore.text = score.toString()
            
            // Update status based on score
            val status = when {
                score >= 80 -> "Excellent"
                score >= 60 -> "Good"
                score >= 40 -> "Fair"
                else -> "Poor"
            }
            binding.tvHealthStatus.text = status
        }
        
        // Update report summary
        analysis.reportSummary?.let { summary ->
            binding.tvReportSummary.text = summary
        }
        
        // Update analysis details
        analysis.analysisDetails?.let { details ->
            binding.cardAnalysisDetails.visibility = View.VISIBLE
            binding.tvReportsAnalyzed.text = (details.reportsAnalyzed?.size ?: 0).toString()
            binding.tvTotalTests.text = (details.totalTests ?: 0).toString()
            binding.tvAbnormalFindings.text = (details.abnormalFindings ?: 0).toString()
            binding.tvCriticalAlerts.text = (details.criticalAlerts ?: 0).toString()
        }
        
        // Update key metrics - convert Map<String, MetricValue> to List<KeyMetric>
        analysis.keyMetrics?.let { metrics ->
            val keyMetricsList = metrics.map { (name, metricValue) ->
                KeyMetric(
                    name = name,
                    value = metricValue.value,
                    unit = metricValue.unit,
                    status = metricValue.status,
                    normalRange = metricValue.normalRange
                )
            }
            metricsAdapter.submitList(keyMetricsList)
            animateRecyclerView(binding.rvKeyMetrics)
        }
        
        // Update risk factors
        analysis.riskFactors?.let { riskFactors ->
            if (riskFactors.isNotEmpty()) {
                binding.riskFactorsCard.visibility = View.VISIBLE
                riskFactorsAdapter.submitList(riskFactors)
                animateRecyclerView(binding.rvRiskFactors)
            }
        }
        
        // Update recommendations
        analysis.recommendations?.let { recommendations ->
            recommendationsAdapter.submitList(recommendations)
            animateRecyclerView(binding.rvRecommendations)
        }
        
        // Update nutrition guidance
        analysis.nutritionGuidance?.let { guidance ->
            binding.cardNutritionGuidance.visibility = View.VISIBLE
            binding.tvFoodsToAvoid.text = guidance.foodsToAvoid?.joinToString(", ") ?: "None"
            binding.tvFoodsToIncrease.text = guidance.foodsToIncrease?.joinToString(", ") ?: "None"
        }
    }
    
    private fun animateRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        recyclerView.post {
            for (i in 0 until recyclerView.childCount) {
                val child = recyclerView.getChildAt(i)
                child?.let {
                    it.alpha = 0f
                    it.translationX = 100f
                    it.animate()
                        .alpha(1f)
                        .translationX(0f)
                        .setDuration(500)
                        .setStartDelay((i * 150).toLong())
                        .start()
                }
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.root.findViewById<View>(R.id.healthScoreCard)?.isVisible = !show
        binding.nutritionSummaryCard.isVisible = !show
        binding.cardAnalysisDetails.isVisible = !show
        binding.keyMetricsCard.isVisible = !show
        binding.riskFactorsCard.isVisible = !show
        binding.recommendationsCard.isVisible = !show
        binding.cardNutritionGuidance.isVisible = !show
    }
    
    private fun showEmptyState(show: Boolean) {
        binding.emptyState.isVisible = show
        binding.root.findViewById<View>(R.id.healthScoreCard)?.isVisible = !show
        binding.nutritionSummaryCard.isVisible = !show
        binding.cardAnalysisDetails.isVisible = !show
        binding.keyMetricsCard.isVisible = !show
        binding.riskFactorsCard.isVisible = !show
        binding.recommendationsCard.isVisible = !show
        binding.cardNutritionGuidance.isVisible = !show
        
        if (show) {
            // Animate empty state
            binding.emptyState.alpha = 0f
            binding.emptyState.animate()
                .alpha(1f)
                .setDuration(600)
                .start()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = HealthAnalysisFragment()
    }
}
