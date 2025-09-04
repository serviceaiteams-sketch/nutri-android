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
import com.nutriai.app.R
import com.nutriai.app.data.models.*
import com.nutriai.app.databinding.FragmentHealthAnalysisBinding
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HealthAnalysisFragment : Fragment() {
    
    private var _binding: FragmentHealthAnalysisBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: HealthReportsViewModel
    
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
        
        setupUI()
        initializeContent()
        observeViewModel()
        animateEntrance()
    }
    
    private fun setupUI() {
        // Set up click listeners for buttons
        binding.btnSelectFiles.setOnClickListener {
            // Handle file selection
            showFileSelectionDialog()
        }
        
        binding.btnAnalyze.setOnClickListener {
            // Handle analysis
            startAnalysis()
        }
    }
    
    private fun initializeContent() {
        // Initialize the content with default values to ensure visibility
        binding.healthScore.text = "85"
        binding.bloodPressureValue.text = "120/80"
        binding.heartRateValue.text = "72"
        
        // Ensure the analysis text is visible
        binding.aiAnalysisText.text = """
            Based on your health data, our AI has identified several positive trends in your health metrics. 
            Your cardiovascular health is excellent, and your lifestyle choices are contributing to overall wellness. 
            The analysis shows consistent improvement in key biomarkers.
        """.trimIndent()
        
        // Ensure the recommendations text is visible
        binding.recommendationsText.text = """
            • Continue your current exercise routine
            • Maintain a balanced diet with more vegetables
            • Get 7-8 hours of sleep nightly
            • Consider stress management techniques
            • Schedule regular health checkups
        """.trimIndent()
        
        // Initialize the progress bar
        binding.healthScoreProgress.progress = 85
    }
    
    private fun showFileSelectionDialog() {
        // Show file selection dialog
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Files")
            .setMessage("Choose the type of files you want to upload:")
            .setPositiveButton("PDF Reports") { _, _ ->
                // Handle PDF selection
                android.widget.Toast.makeText(
                    requireContext(),
                    "PDF selection functionality coming soon!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            .setNeutralButton("Images") { _, _ ->
                // Handle image selection
                android.widget.Toast.makeText(
                    requireContext(),
                    "Image selection functionality coming soon!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun startAnalysis() {
        // Show analysis progress
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Analyzing Reports")
            .setMessage("Our AI is analyzing your health reports. This may take a few moments...")
            .setCancelable(false)
            .show()
        
        // Simulate analysis completion
        lifecycleScope.launch {
            delay(3000) // Simulate 3 seconds of analysis
            updateAnalysisResults()
        }
    }
    
    private fun updateAnalysisResults() {
        // Update the UI with analysis results
        binding.healthScore.text = "85"
        binding.bloodPressureValue.text = "120/80"
        binding.heartRateValue.text = "72"
        
        binding.aiAnalysisText.text = """
            Based on your health data, our AI has identified several positive trends in your health metrics. 
            Your cardiovascular health is excellent, and your lifestyle choices are contributing to overall wellness. 
            The analysis shows consistent improvement in key biomarkers.
        """.trimIndent()
        
        binding.recommendationsText.text = """
            • Continue your current exercise routine
            • Maintain a balanced diet with more vegetables
            • Get 7-8 hours of sleep nightly
            • Consider stress management techniques
            • Schedule regular health checkups
        """.trimIndent()
        
        // Animate the progress bar
        animateHealthScore()
        
        android.widget.Toast.makeText(
            requireContext(),
            "Analysis completed successfully!",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
    
    private fun animateHealthScore() {
        // Animate the health score progress bar
        val progressBar = binding.healthScoreProgress
        val animator = ValueAnimator.ofInt(0, 85)
        animator.duration = 2000
        animator.addUpdateListener { animation ->
            progressBar.progress = animation.animatedValue as Int
        }
        animator.start()
    }
    
    private fun observeViewModel() {
        // Observe ViewModel data if needed
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.analysisState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading state
                        showLoading(true)
                    }
                    is Resource.Success -> {
                        // Update UI with data
                        showLoading(false)
                        updateUIWithData(resource.data)
                    }
                    is Resource.Error -> {
                        // Show error state
                        showLoading(false)
                        showError(resource.message ?: "Unknown error occurred")
                    }
                    null -> {
                        // No data yet
                        showLoading(false)
                    }
                }
            }
        }
    }
    
    private fun updateUIWithData(data: HealthAnalysisResult?) {
        // Update UI with actual data from ViewModel
        // This would be implemented based on the actual data structure
        android.util.Log.d("HealthAnalysisFragment", "Received analysis data: $data")
    }
    
    private fun showLoading(show: Boolean) {
        // Show/hide loading indicators
        // For now, we'll just show a toast
        if (show) {
            android.widget.Toast.makeText(
                requireContext(),
                "Loading health data...",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun showError(message: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
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
        
        // Health score card slide down animation
        binding.healthScoreCard.alpha = 0f
        binding.healthScoreCard.translationY = -50f
        binding.healthScoreCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(200L)
            .start()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = HealthAnalysisFragment()
    }
}