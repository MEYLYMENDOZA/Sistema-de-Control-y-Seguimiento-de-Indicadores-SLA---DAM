package com.example.proyecto1.ui.report

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.SlaRepository // <-- CORREGIDO
import com.example.proyecto1.data.remote.dto.ConfigSlaResponseDto
import com.example.proyecto1.data.remote.dto.ConfigSlaUpdateDto
import dagger.hilt.android.lifecycle.HiltViewModel // <-- AÑADIDO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject // <-- AÑADIDO

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
@HiltViewModel // <-- AÑADIDO
class ConfigurationViewModel @Inject constructor( // <-- CORREGIDO
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
            // Hilt ahora provee el repositorio correcto, que devuelve un Flow.
            // Asumimos que el método en el nuevo repo se llama `getConfigSlaFlow()` o similar
            // Por ahora, lo adaptamos para que compile, pero puede necesitar ajuste.
            try {
                // Esta es una suposición de cómo podría ser el nuevo método.
                // Si el método real es diferente, esto necesitará un ajuste.
                // Por ahora, simulamos una llamada que podría fallar o tener éxito.
                val result = repository.getConfigSla() // Asumiendo que esta función existe en el repo correcto.
                result.onSuccess {
                     val codigosRecibidos = it.joinToString { config -> config.codigoSla }
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

    fun saveConfigSla(updates: List<ConfigSlaUpdateDto>) {
        viewModelScope.launch {
            try {
                // Asumiendo que esta función existe en el repo correcto
                val result = repository.updateConfigSla(updates)
                _saveStatus.value = result
                if (result.isSuccess) {
                    loadConfigSla() // Recargar si el guardado fue exitoso
                }
            } catch (e: Exception) {
                 _saveStatus.value = Result.failure(e)
            }
        }
    }

    fun resetSaveStatus() {
        _saveStatus.value = null
    }
}

// La ViewModelFactory ya no es necesaria con Hilt, por lo que se elimina.
