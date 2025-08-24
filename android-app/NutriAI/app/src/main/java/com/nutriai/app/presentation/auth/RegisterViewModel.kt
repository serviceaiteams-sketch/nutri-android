package com.nutriai.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriai.app.data.models.RegisterResponse
import com.nutriai.app.data.repository.AuthRepository
import com.nutriai.app.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    
    private val authRepository = AuthRepository()
    
    private val _registerState = MutableStateFlow<Resource<RegisterResponse>?>(null)
    val registerState: StateFlow<Resource<RegisterResponse>?> = _registerState
    
    fun register(email: String, password: String, name: String) {
        viewModelScope.launch {
            authRepository.register(email, password, name)
                .onEach { result ->
                    _registerState.value = result
                }
                .launchIn(viewModelScope)
        }
    }
    
    fun resetRegisterState() {
        _registerState.value = null
    }
}

