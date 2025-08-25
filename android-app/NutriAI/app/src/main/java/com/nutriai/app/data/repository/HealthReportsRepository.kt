package com.nutriai.app.data.repository

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.nutriai.app.data.local.DataStoreManager
import com.nutriai.app.data.models.*
import com.nutriai.app.data.remote.ApiService
import com.nutriai.app.di.NetworkModule
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException

class HealthReportsRepository {
    
    private lateinit var apiService: ApiService
    private val dataStoreManager = DataStoreManager()
    private val gson = Gson()
    
    /**
     * Initialize the repository with context for dynamic network detection
     */
    fun initialize(context: Context) {
        if (!::apiService.isInitialized) {
            apiService = NetworkModule.getApiService(context)
        }
    }
    
    /**
     * Reset network configuration when network changes
     */
    fun resetNetwork(context: Context) {
        NetworkModule.resetNetwork()
        apiService = NetworkModule.getApiService(context)
    }
    
    fun uploadHealthReports(
        fileUris: List<Uri>,
        healthConditions: List<HealthCondition>,
        context: Context
    ): Flow<Resource<HealthReportUploadResponse>> = flow {
        emit(Resource.Loading())
        
        try {
            val token = dataStoreManager.authToken.first() ?: throw Exception("No auth token")
            
            // Convert URIs to MultipartBody.Part
            val reportParts = mutableListOf<MultipartBody.Part>()
            fileUris.forEachIndexed { index, uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.cacheDir, "report_$index.pdf")
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("reports", file.name, requestFile)
                reportParts.add(part)
            }
            
            // Convert health conditions to JSON
            val healthConditionsJson = gson.toJson(healthConditions)
            val healthConditionsBody = healthConditionsJson.toRequestBody("application/json".toMediaTypeOrNull())
            
            val response = apiService.uploadHealthReports(
                "Bearer $token",
                reportParts,
                healthConditionsBody
            )
            
            if (response.isSuccessful) {
                response.body()?.let { result ->
                    emit(Resource.Success(result))
                } ?: run {
                    emit(Resource.Error("Upload failed"))
                }
            } else {
                emit(Resource.Error("Failed to upload: ${response.message()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Network error: ${e.message}"))
        } catch (e: IOException) {
            emit(Resource.Error("Connection error: ${e.message}"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }
    
    fun analyzeHealthReports(): Flow<Resource<HealthAnalysisResult>> = flow {
        emit(Resource.Loading())
        
        try {
            val token = dataStoreManager.authToken.first() ?: throw Exception("No auth token")
            android.util.Log.d("HealthReportsRepository", "🔍 Calling analyzeHealthReports API...")
            
            val response = apiService.analyzeHealthReports("Bearer $token")
            
            android.util.Log.d("HealthReportsRepository", "📡 API Response: ${response.code()} - ${response.message()}")
            
            if (response.isSuccessful) {
                response.body()?.let { analysis ->
                    android.util.Log.d("HealthReportsRepository", "✅ Analysis received: $analysis")
                    android.util.Log.d("HealthReportsRepository", "📊 Health Score: ${analysis.healthScore}")
                    android.util.Log.d("HealthReportsRepository", "📈 Key Metrics: ${analysis.keyMetrics}")
                    android.util.Log.d("HealthReportsRepository", "💡 Recommendations: ${analysis.recommendations}")
                    emit(Resource.Success(analysis))
                } ?: run {
                    android.util.Log.e("HealthReportsRepository", "❌ Response body is null")
                    emit(Resource.Error("Analysis failed - no response body"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("HealthReportsRepository", "❌ API Error: ${response.code()} - ${response.message()}")
                android.util.Log.e("HealthReportsRepository", "❌ Error Body: $errorBody")
                emit(Resource.Error("Failed to analyze: ${response.message()} - $errorBody"))
            }
        } catch (e: HttpException) {
            android.util.Log.e("HealthReportsRepository", "❌ HTTP Exception: ${e.message}")
            emit(Resource.Error("Network error: ${e.message}"))
        } catch (e: IOException) {
            android.util.Log.e("HealthReportsRepository", "❌ IO Exception: ${e.message}")
            emit(Resource.Error("Connection error: ${e.message}"))
        } catch (e: Exception) {
            android.util.Log.e("HealthReportsRepository", "❌ General Exception: ${e.message}")
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }
    
    fun getHealthConditions(): Flow<Resource<List<HealthConditionDetail>>> = flow {
        emit(Resource.Loading())
        
        try {
            val token = dataStoreManager.authToken.first() ?: throw Exception("No auth token")
            val response = apiService.getHealthConditions("Bearer $token")
            
            if (response.isSuccessful) {
                response.body()?.let { result ->
                    emit(Resource.Success(result.conditions ?: emptyList()))
                } ?: run {
                    emit(Resource.Success(emptyList()))
                }
            } else {
                emit(Resource.Error("Failed to fetch conditions: ${response.message()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Network error: ${e.message}"))
        } catch (e: IOException) {
            emit(Resource.Error("Connection error: ${e.message}"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }
    
    fun addHealthCondition(condition: HealthCondition): Flow<Resource<GenericResponse>> = flow {
        emit(Resource.Loading())
        
        try {
            val token = dataStoreManager.authToken.first() ?: throw Exception("No auth token")
            val response = apiService.addHealthCondition("Bearer $token", condition)
            
            if (response.isSuccessful) {
                response.body()?.let { result ->
                    emit(Resource.Success(result))
                } ?: run {
                    emit(Resource.Error("Failed to add condition"))
                }
            } else {
                emit(Resource.Error("Failed to add: ${response.message()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Network error: ${e.message}"))
        } catch (e: IOException) {
            emit(Resource.Error("Connection error: ${e.message}"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }
    
    fun deleteHealthCondition(conditionId: Int): Flow<Resource<GenericResponse>> = flow {
        emit(Resource.Loading())
        
        try {
            val token = dataStoreManager.authToken.first() ?: throw Exception("No auth token")
            val response = apiService.deleteHealthCondition("Bearer $token", conditionId)
            
            if (response.isSuccessful) {
                response.body()?.let { result ->
                    emit(Resource.Success(result))
                } ?: run {
                    emit(Resource.Error("Failed to delete condition"))
                }
            } else {
                emit(Resource.Error("Failed to delete: ${response.message()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Network error: ${e.message}"))
        } catch (e: IOException) {
            emit(Resource.Error("Connection error: ${e.message}"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }
    
    fun getFoodRecommendations(): Flow<Resource<FoodRecommendationsResponse>> = flow {
        emit(Resource.Loading())
        
        try {
            val token = dataStoreManager.authToken.first() ?: throw Exception("No auth token")
            val response = apiService.getFoodRecommendations("Bearer $token")
            
            if (response.isSuccessful) {
                response.body()?.let { recommendations ->
                    emit(Resource.Success(recommendations))
                } ?: run {
                    emit(Resource.Error("No recommendations available"))
                }
            } else {
                emit(Resource.Error("Failed to fetch: ${response.message()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Network error: ${e.message}"))
        } catch (e: IOException) {
            emit(Resource.Error("Connection error: ${e.message}"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }
}
