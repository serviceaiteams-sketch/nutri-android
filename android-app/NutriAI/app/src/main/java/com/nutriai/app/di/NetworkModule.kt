package com.nutriai.app.di

import android.content.Context
import com.nutriai.app.data.remote.ApiService
import com.nutriai.app.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    
    private var retrofit: Retrofit? = null
    private var apiService: ApiService? = null
    
    /**
     * Get the API service with dynamic base URL
     */
    fun getApiService(context: Context): ApiService {
        if (apiService == null) {
            apiService = createRetrofit(context).create(ApiService::class.java)
        }
        return apiService!!
    }
    
    /**
     * Reset the network configuration (useful when network changes)
     */
    fun resetNetwork() {
        retrofit = null
        apiService = null
        Constants.resetBaseUrl()
    }
    
    /**
     * Create Retrofit instance with dynamic base URL
     */
    private fun createRetrofit(context: Context): Retrofit {
        if (retrofit == null) {
            val baseUrl = Constants.getBaseUrl(context)
            android.util.Log.d("NetworkModule", "🌐 Creating Retrofit with base URL: $baseUrl")
            
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(createOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
    
    /**
     * Create OkHttpClient with logging and timeouts
     */
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }
}
