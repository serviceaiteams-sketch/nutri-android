package com.nutriai.app.data.remote

import com.nutriai.app.data.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import com.nutriai.app.utils.Constants
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // Authentication
    @POST(Constants.AUTH_LOGIN)
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST(Constants.AUTH_REGISTER)
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
    
    // User Profile
    @GET(Constants.USER_PROFILE)
    suspend fun getUserProfile(@Header("Authorization") token: String): Response<User>
    
    @PUT(Constants.USER_PROFILE)
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Body user: User
    ): Response<User>
    
    // Dashboard
    @GET("analytics/dashboard")
    suspend fun getDashboard(@Header("Authorization") token: String): Response<DashboardResponse>
    
    // Food Recognition
    @Multipart
    @POST("ai/recognize-food")
    suspend fun recognizeFood(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): Response<FoodRecognitionResponse>
    
    // Meal Logging
    @POST("meals/log")
    suspend fun logMeal(
        @Header("Authorization") token: String,
        @Body request: MealLogRequest
    ): Response<MealLogResponse>
    
    @GET("meals/daily/{date}")
    suspend fun getMealsByDate(
        @Header("Authorization") token: String,
        @Path("date") date: String
    ): Response<MealHistoryResponse>
    
    @DELETE("meals/{mealId}")
    suspend fun deleteMeal(
        @Header("Authorization") token: String,
        @Path("mealId") mealId: Int
    ): Response<GenericResponse>
    
    // Health Reports
    @Multipart
    @POST("health-analysis/upload-reports")
    suspend fun uploadHealthReports(
        @Header("Authorization") token: String,
        @Part reports: List<MultipartBody.Part>,
        @Part("healthConditions") healthConditions: RequestBody
    ): Response<HealthReportUploadResponse>
    
    @POST("health-analysis/analyze-reports")
    suspend fun analyzeHealthReports(
        @Header("Authorization") token: String
    ): Response<HealthAnalysisResult>
    
    @GET("health-analysis/conditions")
    suspend fun getHealthConditions(
        @Header("Authorization") token: String
    ): Response<HealthConditionsResponse>
    
    @POST("health-analysis/conditions")
    suspend fun addHealthCondition(
        @Header("Authorization") token: String,
        @Body condition: HealthCondition
    ): Response<GenericResponse>
    
    @DELETE("health-analysis/conditions/{id}")
    suspend fun deleteHealthCondition(
        @Header("Authorization") token: String,
        @Path("id") conditionId: Int
    ): Response<GenericResponse>
    
    @GET("health-analysis/food-recommendations")
    suspend fun getFoodRecommendations(
        @Header("Authorization") token: String
    ): Response<FoodRecommendationsResponse>
}

