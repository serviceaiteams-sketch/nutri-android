package com.nutriai.app.data.repository

import com.nutriai.app.data.local.DataStoreManager
import com.nutriai.app.data.models.DashboardResponse
import com.nutriai.app.data.remote.ApiService
import com.nutriai.app.data.remote.RetrofitClient
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class DashboardRepository {
    
    private val apiService: ApiService = RetrofitClient.apiService
    private val dataStoreManager = DataStoreManager()
    
    fun getDashboardData(): Flow<Resource<DashboardResponse>> = flow {
        emit(Resource.Loading())
        
        try {
            // Get auth token
            val token = dataStoreManager.authToken.first()
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Authentication required"))
                return@flow
            }
            
            val response = apiService.getDashboard("Bearer $token")
            
            if (response.isSuccessful) {
                response.body()?.let { dashboardData ->
                    emit(Resource.Success(dashboardData))
                } ?: run {
                    // Return empty dashboard data if response is null
                    val emptyDashboard = DashboardResponse()
                    emit(Resource.Success(emptyDashboard))
                }
            } else {
                when (response.code()) {
                    401 -> emit(Resource.Error("Session expired. Please login again."))
                    else -> emit(Resource.Error(response.message() ?: "Failed to load dashboard"))
                }
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }
}
