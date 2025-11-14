package com.example.proyecto1.features.notifications.presentation.alert_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.features.notifications.domain.model.AlertCriticality
import com.example.proyecto1.features.notifications.domain.model.VisualAlert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. Define el "Estado" para la UI de esta pantalla
data class AlertsHistoryState(
    val alerts: List<VisualAlert> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false
)

// 2. Crea el ViewModel
class AlertsHistoryViewModel : ViewModel() {

    // _uiState es privado y mutable (solo el ViewModel lo edita)
    private val _uiState = MutableStateFlow(AlertsHistoryState())
    // uiState es público e inmutable (la UI solo lo puede leer)
    val uiState = _uiState.asStateFlow()

    init {
        // Apenas se cree el ViewModel, carga los datos de ejemplo
        loadMockAlerts()
    }

    private fun loadMockAlerts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // --- DATOS FICTICIOS (MOCK DATA) ---
            // Basado en los modelos que ya creamos (VisualAlert y AlertCriticality)
            val mockAlerts = listOf(
                VisualAlert(
                    id = "1",
                    typeSLA = "SLA1",
                    roleAffected = "Rol 1 (Juan Pérez)",
                    status = "Incumplido",
                    delayDays = "13 días de retraso",
                    criticality = AlertCriticality.CRITICAL
                ),
                VisualAlert(
                    id = "2",
                    typeSLA = "SLA2",
                    roleAffected = "Rol 3 (Ana Gómez)",
                    status = "Por Vencer",
                    delayDays = "1 día restante",
                    criticality = AlertCriticality.WARNING
                ),
                VisualAlert(
                    id = "3",
                    typeSLA = "SLA1",
                    roleAffected = "Rol 5 (Luis Torres)",
                    status = "Cumplido",
                    delayDays = "0 días de retraso",
                    criticality = AlertCriticality.INFO
                )
            )
            // --- FIN DE DATOS FICTICIOS ---

            // Actualiza el estado con los datos cargados
            _uiState.update {
                it.copy(
                    isLoading = false,
                    alerts = mockAlerts,
                    unreadCount = mockAlerts.count { it.criticality != AlertCriticality.INFO } // Contar solo críticas y warnings
                )
            }
        }
    }

    // Evento para "cerrar" o "descartar" una alerta (US-13)
    fun onDismissAlert(alertId: String) {
        _uiState.update { currentState ->
            val updatedAlerts = currentState.alerts.filterNot { it.id == alertId }
            currentState.copy(
                alerts = updatedAlerts,
                unreadCount = updatedAlerts.count { it.criticality != AlertCriticality.INFO }
            )
        }
    }
}
