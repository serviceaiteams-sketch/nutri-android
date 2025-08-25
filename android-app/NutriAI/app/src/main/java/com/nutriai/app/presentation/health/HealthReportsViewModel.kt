package com.nutriai.app.presentation.health

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriai.app.data.models.*
import com.nutriai.app.data.repository.HealthReportsRepository
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HealthReportsViewModel(
    private val healthReportsRepository: HealthReportsRepository
) : ViewModel() {
    
    private val _uploadedFiles = MutableStateFlow<List<Uri>>(emptyList())
    val uploadedFiles: StateFlow<List<Uri>> = _uploadedFiles.asStateFlow()
    
    private val _healthConditions = MutableStateFlow<List<HealthCondition>>(emptyList())
    val healthConditions: StateFlow<List<HealthCondition>> = _healthConditions.asStateFlow()
    
    private val _uploadState = MutableStateFlow<Resource<HealthReportUploadResponse>?>(null)
    val uploadState: StateFlow<Resource<HealthReportUploadResponse>?> = _uploadState.asStateFlow()
    
    private val _analysisState = MutableStateFlow<Resource<HealthAnalysisResult>?>(null)
    val analysisState: StateFlow<Resource<HealthAnalysisResult>?> = _analysisState.asStateFlow()
    
    private val _recommendationsState = MutableStateFlow<Resource<FoodRecommendationsResponse>?>(null)
    val recommendationsState: StateFlow<Resource<FoodRecommendationsResponse>?> = _recommendationsState.asStateFlow()
    
    private val _conditionsListState = MutableStateFlow<Resource<List<HealthConditionDetail>>?>(null)
    val conditionsListState: StateFlow<Resource<List<HealthConditionDetail>>?> = _conditionsListState.asStateFlow()
    
    /**
     * Initialize the ViewModel with context for network detection
     */
    fun initialize(context: Context) {
        healthReportsRepository.initialize(context)
        loadHealthConditions()
    }
    
    /**
     * Reset network configuration when network changes
     */
    fun resetNetwork(context: Context) {
        healthReportsRepository.resetNetwork(context)
    }
    
    fun addSelectedFiles(uris: List<Uri>) {
        _uploadedFiles.value = _uploadedFiles.value + uris
    }
    
    fun removeFile(uri: Uri) {
        _uploadedFiles.value = _uploadedFiles.value.filter { it != uri }
    }
    
    fun addHealthCondition(condition: HealthCondition) {
        _healthConditions.value = _healthConditions.value + condition
        // Also save to backend
        viewModelScope.launch {
            healthReportsRepository.addHealthCondition(condition)
                .collect { /* Handle result */ }
        }
    }
    
    fun removeHealthCondition(condition: HealthCondition) {
        _healthConditions.value = _healthConditions.value.filter { it != condition }
    }
    
    fun uploadAndAnalyzeReports(context: Context) {
        if (_uploadedFiles.value.isEmpty()) {
            _uploadState.value = Resource.Error("Please select at least one file")
            return
        }
        
        viewModelScope.launch {
            try {
                // Upload reports
                healthReportsRepository.uploadHealthReports(
                    _uploadedFiles.value,
                    _healthConditions.value,
                    context
                ).collect { uploadResult ->
                    _uploadState.value = uploadResult
                    
                    // If upload successful, analyze reports
                    if (uploadResult is Resource.Success) {
                        analyzeReports()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("HealthReportsViewModel", "Error during upload: $e")
                _uploadState.value = Resource.Error("Upload failed: ${e.message}")
            }
        }
    }
    
    private fun analyzeReports() {
        viewModelScope.launch {
            try {
                android.util.Log.d("HealthReportsViewModel", "🔍 Starting analysis...")
                healthReportsRepository.analyzeHealthReports()
                    .collect { analysisResult ->
                        android.util.Log.d("HealthReportsViewModel", "📊 Analysis result: $analysisResult")
                        _analysisState.value = analysisResult
                        
                        // If analysis successful, get food recommendations
                        if (analysisResult is Resource.Success) {
                            android.util.Log.d("HealthReportsViewModel", "✅ Analysis successful, getting food recommendations...")
                            getFoodRecommendations()
                        } else if (analysisResult is Resource.Error) {
                            android.util.Log.e("HealthReportsViewModel", "❌ Analysis failed: ${analysisResult.message}")
                            // Handle analysis error
                            _analysisState.value = Resource.Error(
                                analysisResult.message ?: "Analysis failed. Please try again."
                            )
                        }
                    }
            } catch (e: Exception) {
                android.util.Log.e("HealthReportsViewModel", "❌ Error during analysis: $e")
                _analysisState.value = Resource.Error("Analysis failed: ${e.message}")
            }
        }
    }
    
    private fun getFoodRecommendations() {
        viewModelScope.launch {
            healthReportsRepository.getFoodRecommendations()
                .collect { result ->
                    _recommendationsState.value = result
                }
        }
    }
    
    private fun loadHealthConditions() {
        viewModelScope.launch {
            healthReportsRepository.getHealthConditions()
                .collect { result ->
                    _conditionsListState.value = result
                }
        }
    }
    
    fun deleteHealthCondition(conditionId: Int) {
        viewModelScope.launch {
            healthReportsRepository.deleteHealthCondition(conditionId)
                .collect { result ->
                    if (result is Resource.Success) {
                        loadHealthConditions()
                    }
                }
        }
    }
    
    fun clearAll() {
        _uploadedFiles.value = emptyList()
        _healthConditions.value = emptyList()
        _uploadState.value = null
        _analysisState.value = null
        _recommendationsState.value = null
    }
}
