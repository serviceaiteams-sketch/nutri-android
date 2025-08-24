package com.nutriai.app.data.repository

import com.nutriai.app.data.local.DataStoreManager
import com.nutriai.app.data.models.MealHistoryResponse
import com.nutriai.app.data.models.DailySummary
import com.nutriai.app.data.models.GenericResponse
import com.nutriai.app.data.remote.ApiService
import com.nutriai.app.data.remote.RetrofitClient
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException

class MealRepository {
    
    private val apiService: ApiService = RetrofitClient.apiService
    private val dataStoreManager = DataStoreManager()
    
    fun getMealsByDate(date: String): Flow<Resource<MealHistoryResponse>> = flow {
        emit(Resource.Loading())
        
        try {
            val token = dataStoreManager.authToken.first() ?: throw Exception("No auth token")
            val response = apiService.getMealsByDate("Bearer $token", date)
            
            if (response.isSuccessful) {
                response.body()?.let { mealHistory ->
                    // Calculate daily summary from meals
                    val dailySummary = if (mealHistory.meals?.isNotEmpty() == true) {
                        DailySummary(
                            totalCalories = mealHistory.meals.sumOf { it.totalCalories?.toDouble() ?: 0.0 }.toFloat(),
                            totalProtein = mealHistory.meals.sumOf { it.totalProtein?.toDouble() ?: 0.0 }.toFloat(),
                            totalCarbs = mealHistory.meals.sumOf { it.totalCarbs?.toDouble() ?: 0.0 }.toFloat(),
                            totalFat = mealHistory.meals.sumOf { it.totalFat?.toDouble() ?: 0.0 }.toFloat(),
                            mealCount = mealHistory.meals.size
                        )
                    } else null
                    
                    val responseWithSummary = MealHistoryResponse(
                        meals = mealHistory.meals,
                        dailySummary = dailySummary
                    )
                    emit(Resource.Success(responseWithSummary))
                } ?: run {
                    emit(Resource.Success(MealHistoryResponse()))
                }
            } else {
                emit(Resource.Error("Failed to fetch meals: ${response.message()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Network error: ${e.message}"))
        } catch (e: IOException) {
            emit(Resource.Error("Connection error: ${e.message}"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }
    
    fun deleteMeal(mealId: Int): Flow<Resource<GenericResponse>> = flow {
        emit(Resource.Loading())
        
        try {
            val token = dataStoreManager.authToken.first() ?: throw Exception("No auth token")
            val response = apiService.deleteMeal("Bearer $token", mealId)
            
            if (response.isSuccessful) {
                response.body()?.let { result ->
                    emit(Resource.Success(result))
                } ?: run {
                    emit(Resource.Success(GenericResponse(success = true)))
                }
            } else {
                emit(Resource.Error("Failed to delete meal: ${response.message()}"))
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
