package com.nutriai.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriai.app.data.models.LoginResponse
import com.nutriai.app.data.repository.AuthRepository
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    
    private val authRepository = AuthRepository()
    
    private val _loginState = MutableStateFlow<Resource<LoginResponse>?>(null)
    val loginState: StateFlow<Resource<LoginResponse>?> = _loginState
    
    /**
     * Initialize the ViewModel with context for network detection
     */
    fun initialize(context: android.content.Context) {
        authRepository.initialize(context)
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            authRepository.login(email, password)
                .onEach { result ->
                    _loginState.value = result
                }
                .launchIn(viewModelScope)
        }
    }
    
    fun resetLoginState() {
        _loginState.value = null
    }
}
