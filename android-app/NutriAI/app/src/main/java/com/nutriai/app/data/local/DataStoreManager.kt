package com.nutriai.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.nutriai.app.NutriAIApplication
import com.nutriai.app.data.models.User
import com.nutriai.app.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreManager {
    
    private val dataStore: DataStore<Preferences> = NutriAIApplication.dataStore
    
    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey(Constants.PREF_AUTH_TOKEN)
        private val USER_ID_KEY = intPreferencesKey(Constants.PREF_USER_ID)
        private val USER_EMAIL_KEY = stringPreferencesKey(Constants.PREF_USER_EMAIL)
        private val USER_NAME_KEY = stringPreferencesKey(Constants.PREF_USER_NAME)
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey(Constants.PREF_IS_LOGGED_IN)
    }
    
    // Save auth token
    suspend fun saveAuthToken(token: String) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
        }
    }
    
    // Get auth token
    val authToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN_KEY]
    }
    
    // Save user info
    suspend fun saveUserInfo(user: User, token: String) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = user.id
            preferences[USER_EMAIL_KEY] = user.email
            preferences[USER_NAME_KEY] = user.name
            preferences[AUTH_TOKEN_KEY] = token
            preferences[IS_LOGGED_IN_KEY] = true
        }
    }
    
    // Get login status
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN_KEY] ?: false
    }
    
    // Clear all data (logout)
    suspend fun clearAllData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    // Get user email
    val userEmail: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }
    
    // Get user name
    val userName: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }
}

