package com.nutriai.app.data.remote

import android.content.Context
import com.nutriai.app.di.NetworkModule

/**
 * @deprecated Use NetworkModule.getApiService(context) instead
 * This class is kept for backward compatibility but should not be used in new code
 */
@Deprecated("Use NetworkModule.getApiService(context) instead")
object RetrofitClient {
    
    /**
     * @deprecated Use NetworkModule.getApiService(context) instead
     */
    @Deprecated("Use NetworkModule.getApiService(context) instead")
    val apiService: ApiService
        get() {
            // This will throw an exception because we need context
            throw UnsupportedOperationException(
                "RetrofitClient.apiService is deprecated. " +
                "Use NetworkModule.getApiService(context) instead for dynamic network detection."
            )
        }
}

