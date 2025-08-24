package com.nutriai.app.utils

object Constants {
    // API Configuration
    const val BASE_URL = "http://192.168.1.28:5000/api/" // Using direct IP
    const val CONNECT_TIMEOUT = 60L
    const val READ_TIMEOUT = 60L
    const val WRITE_TIMEOUT = 60L
    
    // SharedPreferences/DataStore Keys
    const val PREF_AUTH_TOKEN = "auth_token"
    const val PREF_USER_ID = "user_id"
    const val PREF_USER_EMAIL = "user_email"
    const val PREF_USER_NAME = "user_name"
    const val PREF_IS_LOGGED_IN = "is_logged_in"
    
    // API Endpoints
    const val AUTH_LOGIN = "auth/login"
    const val AUTH_REGISTER = "auth/register"
    const val USER_PROFILE = "users/profile"
    
    // Request Codes
    const val REQUEST_CODE_CAMERA_PERMISSION = 100
    const val REQUEST_CODE_STORAGE_PERMISSION = 101
    
    // Error Messages
    const val ERROR_NETWORK = "Network error. Please check your connection."
    const val ERROR_UNKNOWN = "An unexpected error occurred."
    const val ERROR_INVALID_CREDENTIALS = "Invalid email or password."
    const val ERROR_EMAIL_EXISTS = "Email already exists."
    
    // Validation
    const val MIN_PASSWORD_LENGTH = 6
    const val EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
}
