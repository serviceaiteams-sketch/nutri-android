package com.nutriai.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriai.app.data.models.DashboardResponse
import com.nutriai.app.data.repository.DashboardRepository
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class DashboardViewModel(private val dashboardRepository: DashboardRepository) : ViewModel() {
    
    private val _dashboardState = MutableStateFlow<Resource<DashboardResponse>?>(null)
    val dashboardState: StateFlow<Resource<DashboardResponse>?> = _dashboardState
    
    /**
     * Initialize the ViewModel with context for network detection
     */
    fun initialize(context: android.content.Context) {
        dashboardRepository.initialize(context)
        fetchDashboardData()
    }
    
    fun fetchDashboardData() {
        viewModelScope.launch {
            dashboardRepository.getDashboardData()
                .onEach { result ->
                    _dashboardState.value = result
                }
                .launchIn(viewModelScope)
        }
    }
    
    fun refresh() {
        fetchDashboardData()
    }
}
