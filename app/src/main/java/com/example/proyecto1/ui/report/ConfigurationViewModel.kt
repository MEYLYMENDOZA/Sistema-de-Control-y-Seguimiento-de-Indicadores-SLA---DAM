package com.example.proyecto1.ui.report

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.SlaRepository
import com.example.proyecto1.data.remote.dto.ConfigSlaResponseDto
import com.example.proyecto1.data.remote.dto.ConfigSlaUpdateDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Define los posibles estados de la UI para la pantalla de configuración.
 */
sealed class ConfigUiState {
    object Loading : ConfigUiState()
    data class Success(val configs: List<ConfigSlaResponseDto>) : ConfigUiState()
    data class Error(val message: String) : ConfigUiState()
}

/**
 * ViewModel para la pantalla de configuración, adaptado para Hilt.
 */
@HiltViewModel
class ConfigurationViewModel @Inject constructor(
    private val repository: SlaRepository
) : ViewModel() {

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
            try {
                val result = repository.getConfigSla()
                result.onSuccess {
                     val codigosRecibidos = it.joinToString { config -> "${config.codigoSla} (ID: ${config.idSla})" }
                     Log.d(TAG, "🔍 Códigos SLA recibidos de la API: [$codigosRecibidos]")
                    _uiState.value = ConfigUiState.Success(it)
                }.onFailure {
                    _uiState.value = ConfigUiState.Error(it.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                _uiState.value = ConfigUiState.Error(e.message ?: "Error al cargar configuración.")
            }
        }
    }

    fun saveConfigSla(updatedValues: Map<Int, String>) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ConfigUiState.Success) {
                // LÓGICA CENTRALIZADA Y A PRUEBA DE ERRORES
                val updates = currentState.configs.map { originalConfig ->
                    val diasUmbralString = updatedValues[originalConfig.idSla] ?: originalConfig.diasUmbral.toString()
                    val diasUmbral = diasUmbralString.toIntOrNull() ?: originalConfig.diasUmbral
                    Log.d(TAG, "💾 Preparando para guardar ${originalConfig.codigoSla}: ID=${originalConfig.idSla}, Días=$diasUmbral")
                    ConfigSlaUpdateDto(originalConfig.idSla, originalConfig.codigoSla, diasUmbral)
                }
                
                try {
                    val result = repository.updateConfigSla(updates)
                    _saveStatus.value = result
                    if (result.isSuccess) {
                        loadConfigSla()
                    }
                } catch (e: Exception) {
                     _saveStatus.value = Result.failure(e)
                }
            } else {
                _saveStatus.value = Result.failure(Exception("No se puede guardar porque el estado actual no es válido."))
            }
        }
    }

    fun resetSaveStatus() {
        _saveStatus.value = null
    }
}
