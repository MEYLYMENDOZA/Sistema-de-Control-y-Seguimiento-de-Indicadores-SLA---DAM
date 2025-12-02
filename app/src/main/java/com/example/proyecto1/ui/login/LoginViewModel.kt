package com.example.proyecto1.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.repository.AuthRepository
import com.example.proyecto1.data.remote.dto.UsuarioDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val usuario: UsuarioDto) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val TAG = "LoginViewModel"

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> get() = _loginState

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Por favor completa todos los campos")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            try {
                Log.d(TAG, "🔐 Iniciando login para: $username")
                val result = authRepository.login(username, password)

                result.onSuccess { loginResponse ->
                    if (loginResponse.usuario != null) {
                        Log.d(TAG, "✅ Login exitoso: ${loginResponse.usuario.username}")
                        _loginState.value = LoginState.Success(loginResponse.usuario)
                    } else {
                        Log.w(TAG, "⚠️ Login sin datos de usuario")
                        _loginState.value = LoginState.Error("Error en los datos del usuario")
                    }
                }.onFailure { exception ->
                    Log.e(TAG, "❌ Error en login", exception)
                    _loginState.value = LoginState.Error(
                        exception.message ?: "Error de autenticación"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción en login", e)
                _loginState.value = LoginState.Error(
                    "No se pudo conectar con el servidor. Verifica tu conexión."
                )
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
