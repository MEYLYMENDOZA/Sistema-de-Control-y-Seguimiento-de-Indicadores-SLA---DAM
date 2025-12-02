package com.example.proyecto1.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.proyecto1.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel para gestionar la configuración de red.
 * La URL de la API ahora se gestiona de forma centralizada y estática a través de BuildConfig.
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
     * Carga la URL base actual desde BuildConfig.
     */
    private fun loadCurrentIp() {
        _currentIp.value = BuildConfig.API_BASE_URL
    }

    /**
     * La URL es estática, esta función ahora solo confirma la URL configurada.
     */
    fun refreshServerIp() {
        _uiState.value = NetworkUiState.Searching
        val staticUrl = BuildConfig.API_BASE_URL
        _currentIp.value = staticUrl
        _uiState.value = NetworkUiState.Success("URL configurada: $staticUrl")
    }

    /**
     * Comprueba que la URL de conexión esté definida.
     */
    fun testConnection() {
        _uiState.value = NetworkUiState.Testing
        val currentUrl = BuildConfig.API_BASE_URL
        if (currentUrl.isNotBlank()) {
            _uiState.value = NetworkUiState.Success("Conexión configurada para: $currentUrl")
        } else {
            _uiState.value = NetworkUiState.Error("API_BASE_URL no está definida en build.gradle")
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
