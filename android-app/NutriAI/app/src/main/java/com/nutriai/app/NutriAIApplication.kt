package com.nutriai.app

import android.app.Application
import android.content.Context
import com.google.android.material.color.DynamicColors
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
class NutriAIApplication : Application() {
    
    companion object {
        private lateinit var instance: NutriAIApplication
        
        fun getInstance(): NutriAIApplication = instance
        
        // DataStore instance
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nutriai_prefs")
        val dataStore: DataStore<Preferences> get() = instance.dataStore
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        try {
            DynamicColors.applyToActivitiesIfAvailable(this)
        } catch (_: Exception) { }
    }
}

