package com.example.proyecto1.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.remote.NetworkConfig
import com.example.proyecto1.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar la configuración de red
 */
class NetworkSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<NetworkUiState>(NetworkUiState.Idle)
    val uiState: StateFlow<NetworkUiState> = _uiState.asStateFlow()

    private val _currentIp = MutableStateFlow<String?>(null)
    val currentIp: StateFlow<String?> = _currentIp.asStateFlow()

    init {
        loadCurrentIp()
    }

    /**
     * Carga la IP actual
     */
    private fun loadCurrentIp() {
        _currentIp.value = RetrofitClient.getCurrentBaseUrl()
    }

    /**
     * Fuerza la búsqueda del servidor en la red
     */
    fun refreshServerIp() {
        viewModelScope.launch {
            _uiState.value = NetworkUiState.Searching

            try {
                RetrofitClient.refresh(getApplication())
                val newIp = RetrofitClient.getCurrentBaseUrl()
                _currentIp.value = newIp
                _uiState.value = NetworkUiState.Success(newIp ?: "No encontrado")
            } catch (e: Exception) {
                _uiState.value = NetworkUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Prueba la conexión actual
     */
    fun testConnection() {
        viewModelScope.launch {
            _uiState.value = NetworkUiState.Testing

            try {
                // Aquí podrías hacer una llamada real a la API para probar
                val currentUrl = RetrofitClient.getCurrentBaseUrl()
                if (currentUrl != null) {
                    _uiState.value = NetworkUiState.Success("Conexión exitosa: $currentUrl")
                } else {
                    _uiState.value = NetworkUiState.Error("No hay servidor configurado")
                }
            } catch (e: Exception) {
                _uiState.value = NetworkUiState.Error("Error de conexión: ${e.message}")
            }
        }
    }
}

/**
 * Estados de la UI de configuración de red
 */
sealed class NetworkUiState {
    object Idle : NetworkUiState()
    object Searching : NetworkUiState()
    object Testing : NetworkUiState()
    data class Success(val message: String) : NetworkUiState()
    data class Error(val message: String) : NetworkUiState()
}

