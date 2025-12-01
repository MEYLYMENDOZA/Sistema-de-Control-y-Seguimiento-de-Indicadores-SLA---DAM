package com.example.proyecto1.ui.report

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.remote.dto.ConfigSlaResponseDto
import com.example.proyecto1.data.remote.dto.ConfigSlaUpdateDto
import com.example.proyecto1.data.repository.SlaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Define los posibles estados de la UI para la pantalla de configuración.
 */
sealed class ConfigUiState {
    object Loading : ConfigUiState()
    data class Success(val configs: List<ConfigSlaResponseDto>) : ConfigUiState()
    data class Error(val message: String) : ConfigUiState()
}

/**
 * ViewModel para la pantalla de configuración.
 */
class ConfigurationViewModel(private val repository: SlaRepository) : ViewModel() {

    private val TAG = "ConfigurationViewModel"

    private val _uiState = MutableStateFlow<ConfigUiState>(ConfigUiState.Loading)
    val uiState: StateFlow<ConfigUiState> = _uiState.asStateFlow()

    private val _saveStatus = MutableStateFlow<Result<Unit>?>(null)
    val saveStatus: StateFlow<Result<Unit>?> = _saveStatus.asStateFlow()

    init {
        loadConfigSla()
    }

    fun loadConfigSla() {
        viewModelScope.launch {
            _uiState.value = ConfigUiState.Loading
            repository.getConfigSla().onSuccess {
                // --- LOG DE DIAGNÓSTICO ---
                val codigosRecibidos = it.joinToString { config -> config.codigoSla }
                Log.d(TAG, "🔍 Códigos SLA recibidos de la API: [$codigosRecibidos]")
                // ---------------------------

                _uiState.value = ConfigUiState.Success(it)
            }.onFailure {
                _uiState.value = ConfigUiState.Error(it.message ?: "Error desconocido")
            }
        }
    }

    fun saveConfigSla(updates: List<ConfigSlaUpdateDto>) {
        viewModelScope.launch {
            val result = repository.updateConfigSla(updates)
            _saveStatus.value = result
            // Recargar los datos después de guardar
            if (result.isSuccess) {
                loadConfigSla()
            }
        }
    }

    fun resetSaveStatus() {
        _saveStatus.value = null
    }
}

/**
 * Factory para crear una instancia de ConfigurationViewModel.
 */
class ConfigurationViewModelFactory(private val repository: SlaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConfigurationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConfigurationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
