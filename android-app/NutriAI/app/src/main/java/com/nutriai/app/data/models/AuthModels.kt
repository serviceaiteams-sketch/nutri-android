package com.nutriai.app.data.models

import com.google.gson.annotations.SerializedName

// Login Request
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String
)

// Login Response
data class LoginResponse(
    @SerializedName("token")
    val token: String,
    
    @SerializedName("user")
    val user: User,
    
    @SerializedName("message")
    val message: String? = null
)

// Register Request
data class RegisterRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("age")
    val age: Int? = null,
    
    @SerializedName("gender")
    val gender: String? = null,
    
    @SerializedName("height")
    val height: Double? = null,
    
    @SerializedName("weight")
    val weight: Double? = null
)

// Register Response
data class RegisterResponse(
    @SerializedName("user")
    val user: User,
    
    @SerializedName("token")
    val token: String,
    
    @SerializedName("message")
    val message: String? = null
)

// User Model
data class User(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("age")
    val age: Int? = null,
    
    @SerializedName("gender")
    val gender: String? = null,
    
    @SerializedName("height")
    val height: Double? = null,
    
    @SerializedName("weight")
    val weight: Double? = null,
    
    @SerializedName("activity_level")
    val activityLevel: String? = null,
    
    @SerializedName("dietary_preferences")
    val dietaryPreferences: String? = null,
    
    @SerializedName("health_conditions")
    val healthConditions: String? = null,
    
    @SerializedName("created_at")
    val createdAt: String? = null
)

// Error Response
data class ErrorResponse(
    @SerializedName("error")
    val error: String? = null,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("details")
    val details: Any? = null
)

// Generic Response
data class GenericResponse(
    @SerializedName("success")
    val success: Boolean? = null,
    
    @SerializedName("message")
    val message: String? = null
)

