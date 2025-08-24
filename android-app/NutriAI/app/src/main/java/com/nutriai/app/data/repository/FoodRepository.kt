package com.nutriai.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.nutriai.app.data.local.DataStoreManager
import com.nutriai.app.data.models.FoodRecognitionResponse
import com.nutriai.app.data.models.MealLogRequest
import com.nutriai.app.data.models.MealLogResponse
import com.nutriai.app.data.remote.ApiService
import com.nutriai.app.data.remote.RetrofitClient
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FoodRepository {
    
    private val apiService: ApiService = RetrofitClient.apiService
    private val dataStoreManager = DataStoreManager()
    
    fun recognizeFood(imageFile: File): Flow<Resource<FoodRecognitionResponse>> = flow {
        emit(Resource.Loading())
        
        try {
            // Get auth token
            val token = dataStoreManager.authToken.first()
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Authentication required"))
                return@flow
            }
            
            // Create multipart request
            val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestBody)
            
            val response = apiService.recognizeFood("Bearer $token", imagePart)
            
            if (response.isSuccessful) {
                response.body()?.let { foodResponse ->
                    if (foodResponse.success) {
                        emit(Resource.Success(foodResponse))
                    } else {
                        emit(Resource.Error(foodResponse.error ?: "Failed to recognize food"))
                    }
                } ?: emit(Resource.Error("Empty response"))
            } else {
                when (response.code()) {
                    401 -> emit(Resource.Error("Session expired. Please login again."))
                    413 -> emit(Resource.Error("Image too large. Please use a smaller image."))
                    else -> emit(Resource.Error(response.message() ?: "Failed to recognize food"))
                }
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "HTTP error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your connection."))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } finally {
            // Clean up temp file
            imageFile.delete()
        }
    }
    
    fun logMeal(request: MealLogRequest): Flow<Resource<MealLogResponse>> = flow {
        emit(Resource.Loading())
        
        try {
            // Get auth token
            val token = dataStoreManager.authToken.first()
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Authentication required"))
                return@flow
            }
            
            val response = apiService.logMeal("Bearer $token", request)
            
            if (response.isSuccessful) {
                response.body()?.let { logResponse ->
                    if (logResponse.success) {
                        emit(Resource.Success(logResponse))
                    } else {
                        emit(Resource.Error(logResponse.message ?: "Failed to log meal"))
                    }
                } ?: emit(Resource.Error("Empty response"))
            } else {
                when (response.code()) {
                    401 -> emit(Resource.Error("Session expired. Please login again."))
                    else -> emit(Resource.Error(response.message() ?: "Failed to log meal"))
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
    
    companion object {
        fun createTempImageFile(context: Context, bitmap: Bitmap): File {
            val tempFile = File(context.cacheDir, "temp_food_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.flush()
            outputStream.close()
            return tempFile
        }
        
        fun createTempImageFile(context: Context, uri: Uri): File? {
            return try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Cannot open input stream")
                val tempFile = File(context.cacheDir, "temp_food_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(tempFile)
                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                if (tempFile.exists() && tempFile.length() > 0) {
                    tempFile
                } else {
                    tempFile.delete()
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
