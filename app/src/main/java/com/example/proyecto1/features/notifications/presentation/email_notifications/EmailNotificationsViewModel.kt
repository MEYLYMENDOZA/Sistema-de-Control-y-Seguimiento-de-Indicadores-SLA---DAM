package com.example.proyecto1.features.notifications.presentation.email_notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.features.notifications.domain.model.EmailNotificationHistory
import com.example.proyecto1.features.notifications.domain.model.NotificationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. Define un "Estado" para tu UI
// Esta data class representa todo lo que se ve en la pantalla
data class EmailNotificationsState(
    val sentCount: Int = 0,
    val pendingCount: Int = 0,
    val failedCount: Int = 0,
    val historyList: List<EmailNotificationHistory> = emptyList(),
    val isLoading: Boolean = false
)

// 2. Crea el ViewModel
class EmailNotificationsViewModel : ViewModel() {

    // _uiState es privado y mutable (solo el ViewModel lo edita)
    private val _uiState = MutableStateFlow(EmailNotificationsState())
    // uiState es público e inmutable (la UI solo lo puede leer)
    val uiState = _uiState.asStateFlow()

    init {
        // Apenas se cree el ViewModel, carga los datos de ejemplo
        loadMockData()
    }

    private fun loadMockData() {
        viewModelScope.launch {
            // Simula que está "cargando"
            _uiState.update { it.copy(isLoading = true) }

            // --- DATOS FICTICIOS (MOCK DATA) ---
            // Estos datos simulan lo que vendrá de la API
            val mockHistory = listOf(
                EmailNotificationHistory(
                    id = "1",
                    reportTitle = "Reporte de Indicadores SLA – 10/11/2025",
                    recipient = "admin@empresa.com",
                    pdfName = "Reporte_SLA_2025-11-10.pdf",
                    dateSent = "10 nov 2025, 16:16",
                    status = NotificationStatus.SENT
                ),
                EmailNotificationHistory(
                    id = "2",
                    reportTitle = "Reporte de Indicadores SLA – 09/11/2025",
                    recipient = "team@empresa.com",
                    pdfName = "Reporte_SLA_2025-11-09.pdf",
                    dateSent = "09 nov 2025, 10:00",
                    status = NotificationStatus.PENDING
                ),
                EmailNotificationHistory(
                    id = "3",
                    reportTitle = "Reporte de Indicadores SLA – 08/11/2025",
                    recipient = "manager@empresa.com",
                    pdfName = "Reporte_SLA_2025-11-08.pdf",
                    dateSent = "08 nov 2025, 14:30",
                    status = NotificationStatus.FAILED
                )
            )
            // --- FIN DE DATOS FICTICIOS ---

            // Actualiza el estado con los datos cargados
            _uiState.update {
                it.copy(
                    isLoading = false,
                    historyList = mockHistory,
                    sentCount = mockHistory.count { it.status == NotificationStatus.SENT },
                    pendingCount = mockHistory.count { it.status == NotificationStatus.PENDING },
                    failedCount = mockHistory.count { it.status == NotificationStatus.FAILED }
                )
            }
        }
    }

    // Aquí puedes agregar funciones para los eventos de la UI
    fun onClearHistoryClicked() {
        // Lógica para limpiar el historial...
        _uiState.update {
            it.copy(historyList = emptyList(), sentCount = 0, pendingCount = 0, failedCount = 0)
        }
    }
}