package com.example.proyecto1.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.model.SlaLimits
import com.example.proyecto1.data.repository.SlaLimitsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SlaLimitsUiState(
    val isLoading: Boolean = true,
    val limits: SlaLimits? = null,
    val error: String? = null,
    val updateSuccess: Boolean = false
)

class SlaLimitsViewModel : ViewModel() {

    private val repository = SlaLimitsRepository()

    private val _uiState = MutableStateFlow(SlaLimitsUiState())
    val uiState: StateFlow<SlaLimitsUiState> = _uiState.asStateFlow()

    init {
        loadSlaLimits()
    }

    private fun loadSlaLimits() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val limits = repository.fetchSlaLimits()
                withContext(Dispatchers.Main) {
                    _uiState.value = SlaLimitsUiState(isLoading = false, limits = limits)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = SlaLimitsUiState(isLoading = false, error = e.message)
                }
            }
        }
    }

    fun updateSlaLimits(limiteSla1: String, limiteSla2: String) {
        val sla1 = limiteSla1.toIntOrNull()
        val sla2 = limiteSla2.toIntOrNull()

        if (sla1 == null || sla2 == null) {
            _uiState.value = _uiState.value.copy(error = "Los valores deben ser números enteros.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newLimits = SlaLimits(limite_sla1 = sla1, limite_sla2 = sla2)
                repository.updateSlaLimits(newLimits)
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(isLoading = false, limits = newLimits, updateSuccess = true, error = null)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message, updateSuccess = false)
                }
            }
        }
    }

    /**
     * Restaura los límites a los valores por defecto (35 y 20) y los guarda.
     */
    fun restoreDefaultLimits() {
        // Llama a la función de actualización con los valores predeterminados.
        updateSlaLimits("35", "20")
    }

    fun resetUpdateStatus() {
        _uiState.value = _uiState.value.copy(updateSuccess = false, error = null)
    }
}