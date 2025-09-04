package com.nutriai.app.presentation.health

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nutriai.app.data.models.HealthCondition
import com.nutriai.app.databinding.FragmentHealthUploadBinding
import com.nutriai.app.databinding.DialogAddHealthConditionBinding
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HealthUploadFragment : Fragment() {
    
    private var _binding: FragmentHealthUploadBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: HealthReportsViewModel
    private lateinit var filesAdapter: SelectedFilesAdapter
    private lateinit var conditionsAdapter: HealthConditionsAdapter
    
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        try {
            if (uris.isNotEmpty()) {
                // Filter only supported file types
                val supportedUris = uris.filter { uri ->
                    val mimeType = requireContext().contentResolver.getType(uri)
                    mimeType in listOf("application/pdf", "image/jpeg", "image/jpg", "image/png")
                }
                if (supportedUris.isNotEmpty()) {
                    viewModel.addSelectedFiles(supportedUris)
                } else {
                    Toast.makeText(context, "Please select PDF or image files only", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error selecting files: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthUploadBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get parent fragment's ViewModel
        viewModel = ViewModelProvider(requireParentFragment())[HealthReportsViewModel::class.java]
        
        setupRecyclerViews()
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupRecyclerViews() {
        // Files RecyclerView
        filesAdapter = SelectedFilesAdapter { uri ->
            viewModel.removeFile(uri)
        }
        binding.recyclerViewSelectedFiles.apply {
            adapter = filesAdapter
            layoutManager = LinearLayoutManager(context)
        }
        
        // Conditions RecyclerView
        conditionsAdapter = HealthConditionsAdapter { condition ->
            viewModel.removeHealthCondition(condition)
        }
        binding.recyclerViewHealthConditions.apply {
            adapter = conditionsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }
    
    private fun setupClickListeners() {
        binding.btnSelectFiles.setOnClickListener {
            // Launch file picker for PDFs and images
            filePickerLauncher.launch("*/*")
        }
        
        binding.btnAddCondition.setOnClickListener {
            showAddConditionDialog()
        }
        
        binding.btnAnalyze.setOnClickListener {
            viewModel.uploadAndAnalyzeReports(requireContext())
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uploadedFiles.collect { files ->
                filesAdapter.submitList(files)
                binding.recyclerViewSelectedFiles.isVisible = files.isNotEmpty()
                binding.btnAnalyze.isEnabled = files.isNotEmpty()
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.healthConditions.collect { conditions ->
                conditionsAdapter.submitList(conditions)
                // No conditions text view doesn't exist in new layout
                // binding.tvNoConditions.isVisible = conditions.isEmpty()
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uploadState.collect { state ->
                when (state) {
                    is Resource.Loading -> {
                        binding.btnAnalyze.isEnabled = false
                        // Progress bar doesn't exist in new layout
                        // binding.progressBar.isVisible = true
                    }
                    is Resource.Success -> {
                        // Progress bar doesn't exist in new layout
                        // binding.progressBar.isVisible = false
                        Toast.makeText(context, "Reports uploaded successfully!", Toast.LENGTH_SHORT).show()
                        // Switch to analysis tab safely
                        try {
                            val parentFragment = parentFragment as? HealthReportsFragment
                            parentFragment?.binding?.viewPager?.currentItem = 1
                        } catch (e: Exception) {
                            // Handle navigation error
                            e.printStackTrace()
                        }
                    }
                    is Resource.Error -> {
                        binding.btnAnalyze.isEnabled = true
                        // Progress bar doesn't exist in new layout
                        // binding.progressBar.isVisible = false
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    }
                    null -> {}
                }
            }
        }
    }
    
    private fun showAddConditionDialog() {
        val dialogBinding = DialogAddHealthConditionBinding.inflate(layoutInflater)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Health Condition")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { _, _ ->
                val condition = dialogBinding.etConditionName.text.toString()
                val severity = when (dialogBinding.chipGroupSeverity.checkedChipId) {
                    dialogBinding.chipMild.id -> "mild"
                    dialogBinding.chipModerate.id -> "moderate"
                    dialogBinding.chipSevere.id -> "severe"
                    else -> "mild"
                }
                val medications = dialogBinding.etMedications.text.toString()
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                
                if (condition.isNotEmpty()) {
                    viewModel.addHealthCondition(
                        HealthCondition(
                            condition = condition,
                            severity = severity,
                            medications = medications,
                            diagnosedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                        )
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = HealthUploadFragment()
    }
}
