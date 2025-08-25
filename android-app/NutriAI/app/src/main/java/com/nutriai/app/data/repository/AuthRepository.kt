package com.nutriai.app.data.repository

import com.nutriai.app.data.local.DataStoreManager
import com.nutriai.app.data.models.LoginRequest
import com.nutriai.app.data.models.LoginResponse
import com.nutriai.app.data.models.RegisterRequest
import com.nutriai.app.data.models.RegisterResponse
import com.nutriai.app.data.remote.ApiService
import com.nutriai.app.di.NetworkModule
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class AuthRepository {
    
    private lateinit var apiService: ApiService
    private val dataStoreManager = DataStoreManager()
    
    /**
     * Initialize the repository with context for dynamic network detection
     */
    fun initialize(context: android.content.Context) {
        if (!::apiService.isInitialized) {
            apiService = NetworkModule.getApiService(context)
        }
    }
    
    // Login
    fun login(email: String, password: String): Flow<Resource<LoginResponse>> = flow {
        emit(Resource.Loading())
        
        try {
            val request = LoginRequest(email, password)
            val response = apiService.login(request)
            
            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    // Save user info and token
                    dataStoreManager.saveUserInfo(loginResponse.user, loginResponse.token)
                    emit(Resource.Success(loginResponse))
                } ?: emit(Resource.Error("Empty response"))
            } else {
                emit(Resource.Error(response.message() ?: "Login failed"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }
    
    // Register
    fun register(
        email: String,
        password: String,
        name: String
    ): Flow<Resource<RegisterResponse>> = flow {
        emit(Resource.Loading())
        
        try {
            val request = RegisterRequest(email, password, name)
            val response = apiService.register(request)
            
            if (response.isSuccessful) {
                response.body()?.let { registerResponse ->
                    // Save user info and token
                    dataStoreManager.saveUserInfo(registerResponse.user, registerResponse.token)
                    emit(Resource.Success(registerResponse))
                } ?: emit(Resource.Error("Empty response"))
            } else {
                emit(Resource.Error(response.message() ?: "Registration failed"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }
    
    // Logout
    suspend fun logout() {
        dataStoreManager.clearAllData()
    }
    
    // Check login status
    fun isLoggedIn(): Flow<Boolean> = dataStoreManager.isLoggedIn
    
    // Get auth token
    fun getAuthToken(): Flow<String?> = dataStoreManager.authToken
}

