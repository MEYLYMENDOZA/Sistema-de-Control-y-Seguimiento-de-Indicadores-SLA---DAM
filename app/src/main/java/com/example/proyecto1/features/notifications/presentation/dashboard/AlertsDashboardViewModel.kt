package com.example.proyecto1.features.notifications.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.remote.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. Estado del Dashboard
data class AlertsDashboardState(
    val activeAlertsCount: Int = 0,
    val criticalCasesCount: Int = 0,
    val nearLimitCount: Int = 0,
    val compliancePercentage: Int = 100, // Comienza en 100%, se actualiza dinámicamente
    val isLoading: Boolean = false
)

// 2. ViewModel
class AlertsDashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsDashboardState())
    val uiState = _uiState.asStateFlow()

    init {
        // Al entrar al Dashboard, pedimos los datos reales para contar
        fetchDashboardStats()
    }

    private fun fetchDashboardStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // 1. Llamamos a la API (Trae todas las alertas)
                val alertasDto = RetrofitClient.apiService.getAlertas()

                // 2. CALCULAMOS LOS NÚMEROS REALES
                // Total de alertas activas
                val total = alertasDto.size

                // Cuántas dicen "Alto" (Críticas)
                val criticas = alertasDto.count { it.nivel.equals("Alto", ignoreCase = true) }

                // Cuántas dicen "Bajo" o "Medio" (Cerca del Límite/Advertencia)
                val advertencias = alertasDto.count {
                    it.nivel.equals("Bajo", ignoreCase = true) || it.nivel.equals("Medio", ignoreCase = true)
                }

                // 3. CALCULAR CUMPLIMIENTO DINÁMICAMENTE
                // Lógica:
                // - Comenzamos con 100%
                // - Cada alerta crítica resta 10%
                // - Cada advertencia resta 5%
                // - Mínimo 0%, máximo 100%
                val complianceBase = 100
                val complianceAdjusted = (complianceBase - (criticas * 10) - (advertencias * 5)).coerceIn(0, 100)

                // 4. Actualizamos la pantalla con los números reales de SQL
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        activeAlertsCount = total,
                        criticalCasesCount = criticas,
                        nearLimitCount = advertencias,
                        compliancePercentage = complianceAdjusted  // ✅ DINÁMICO
                    )
                }
            } catch (e: Exception) {
                // Si falla, se queda con el estado por defecto
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}