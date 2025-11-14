package com.example.proyecto1.features.notifications.presentation.dashboard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// 1. Define el Estado del Dashboard
data class AlertsDashboardState(
    val activeAlertsCount: Int = 4,
    val criticalCasesCount: Int = 9,
    val nearLimitCount: Int = 16,
    val compliancePercentage: Int = 79
)

// 2. Crea el ViewModel
class AlertsDashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsDashboardState())
    val uiState = _uiState.asStateFlow()

    // Por ahora, los datos son fijos como en el diseño.
    // Más adelante, la API los alimentará.
}
