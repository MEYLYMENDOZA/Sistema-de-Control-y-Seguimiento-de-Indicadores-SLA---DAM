package com.example.proyecto1.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Estado de la UI para el login
data class LoginUiState(
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val error: String? = null
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(user: String, password: String) {
        // Validaciones básicas
        if (user.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState(error = "El usuario y la contraseña no pueden estar vacíos.")
            return
        }

        _uiState.value = LoginUiState(isLoading = true)

        // Simula una pequeña demora para que el feedback de carga sea visible
        viewModelScope.launch {
            delay(500) // Pequeña demora artificial

            if (user == "admin" && password == "admin123") {
                // ¡Login exitoso!
                _uiState.value = LoginUiState(loginSuccess = true)
            } else {
                // Credenciales incorrectas
                _uiState.value = LoginUiState(error = "Usuario o contraseña incorrectos.")
            }
        }
    }
}
