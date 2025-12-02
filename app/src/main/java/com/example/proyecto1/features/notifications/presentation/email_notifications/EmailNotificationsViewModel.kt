package com.example.proyecto1.features.notifications.presentation.email_notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.remote.api.RetrofitClient
import com.example.proyecto1.features.notifications.domain.model.EmailNotificationHistory
import com.example.proyecto1.features.notifications.domain.model.NotificationStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmailNotificationsState(
    val sentCount: Int = 0,
    val pendingCount: Int = 0,
    val failedCount: Int = 0,
    val historyList: List<EmailNotificationHistory> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class EmailNotificationsViewModel @Inject constructor(
    private val retrofitClient: RetrofitClient
) : ViewModel() {

    private val apiService get() = retrofitClient.apiService

    private val _uiState = MutableStateFlow(EmailNotificationsState())
    val uiState = _uiState.asStateFlow()

    init {
        loadRealReports()
    }

    private fun loadRealReports() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // 1. LLAMADA A LA API REAL
                val reportesDto = apiService.getReportes()

                // 2. CONVERSIÓN
                val historialReal = reportesDto.map { reporte ->
                    reporte.toDomain()
                }

                // 3. ACTUALIZAR UI
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        historyList = historialReal,
                        sentCount = historialReal.size, // Asumimos que todos son enviados
                        pendingCount = 0,
                        failedCount = 0
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("ERROR API REPORTES: ${e.message}. Usando Mock Data de respaldo.")
                loadMockData() // Si falla, carga datos falsos para que no se vea vacío
            }
        }
    }

    private fun loadMockData() {
        viewModelScope.launch {
            val mockHistory = listOf(
                EmailNotificationHistory("1", "Reporte SLA - Mock", "admin@empresa.com", "reporte_mock.pdf", "10 nov", NotificationStatus.SENT),
                EmailNotificationHistory("2", "Reporte Fallido - Mock", "gerencia@empresa.com", "error.pdf", "11 nov", NotificationStatus.FAILED)
            )
            _uiState.update {
                it.copy(
                    isLoading = false,
                    historyList = mockHistory,
                    sentCount = 1,
                    failedCount = 1
                )
            }
        }
    }

    fun onClearHistoryClicked() {
        _uiState.update { it.copy(historyList = emptyList(), sentCount = 0) }
    }
}