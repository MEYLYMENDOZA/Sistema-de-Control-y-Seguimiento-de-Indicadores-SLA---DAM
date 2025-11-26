package com.example.proyecto1.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.model.SlaConfig
import com.example.proyecto1.data.repository.ConfigurationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel para la pantalla de configuración.
 */
class ConfigurationViewModel : ViewModel() {
    private val repository = ConfigurationRepository()

    private val _slaConfig = MutableStateFlow(SlaConfig())
    val slaConfig: StateFlow<SlaConfig> = _slaConfig

    init {
        loadConfiguration()
    }

    /**
     * Carga la configuración de SLA desde el repositorio en un hilo secundario.
     */
    private fun loadConfiguration() {
        viewModelScope.launch(Dispatchers.IO) { // <-- ¡IMPORTANTE! Se ejecuta en un hilo secundario
            try {
                val config = repository.getConfiguration()
                withContext(Dispatchers.Main) {
                    _slaConfig.value = config
                }
            } catch (e: Exception) {
                // Manejar el error si es necesario
            }
        }
    }

    /**
     * Guarda la configuración de SLA en el repositorio en un hilo secundario.
     */
    fun saveConfiguration(sla1Limit: Int, sla2Limit: Int) {
        viewModelScope.launch(Dispatchers.IO) { // <-- ¡IMPORTANTE! Se ejecuta en un hilo secundario
            try {
                val newConfig = SlaConfig(sla1Limit, sla2Limit)
                repository.saveConfiguration(newConfig)
                withContext(Dispatchers.Main) {
                    _slaConfig.value = newConfig
                }
            } catch (e: Exception) {
                // Manejar el error si es necesario
            }
        }
    }
}