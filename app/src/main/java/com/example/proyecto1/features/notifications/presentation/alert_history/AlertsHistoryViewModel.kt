package com.example.proyecto1.features.notifications.presentation.alert_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.remote.api.RetrofitClient
import com.example.proyecto1.features.notifications.data.model.AlertaCreateDto
import com.example.proyecto1.features.notifications.data.model.PersonalDto
import com.example.proyecto1.features.notifications.domain.model.AlertCriticality
import com.example.proyecto1.features.notifications.domain.model.VisualAlert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. Define el "Estado"
data class AlertsHistoryState(
    val alerts: List<VisualAlert> = emptyList(),
    val personalList: List<PersonalDto> = emptyList(), // <--- NUEVO: Lista de personas
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null // Agregamos esto para saber si hubo error
)

// 2. ViewModel Inteligente
class AlertsHistoryViewModel : ViewModel() {
    // Función para el botón "Verificar Cumplimiento"
    fun verificarCumplimientoSla() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1. Ejecutar el motor en el servidor
                val response = RetrofitClient.apiService.procesarSlas()

                if (response.isSuccessful) {
                    println("Motor SLA ejecutado con éxito.")
                    // 2. Si funcionó, RECARGAR la lista para ver las nuevas alertas
                    loadRealAlerts()
                } else {
                    println("Error en motor: ${response.code()}")
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    private val _uiState = MutableStateFlow(AlertsHistoryState())
    val uiState = _uiState.asStateFlow()

    init {
        // Intentamos cargar datos REALES al iniciar
        loadRealAlerts()
        loadPersonal() // <--- NUEVO: Cargar personas al iniciar
    }

    private fun loadRealAlerts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // INTENTO 1: CONEXIÓN REAL
                // Llamamos a la API
                val alertasDto = RetrofitClient.apiService.getAlertas()

                // Convertimos a datos de UI
                val realAlerts = alertasDto.map { it.toDomain() }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        alerts = realAlerts,
                        unreadCount = realAlerts.size,
                        errorMessage = null // Todo salió bien
                    )
                }
                println("¡ÉXITO! Datos cargados de la API.")

            } catch (e: Exception) {
                // SI FALLA: MUESTRA EL ERROR EN CONSOLA Y CARGA LOS DATOS FALSOS
                e.printStackTrace()
                println("ERROR API: ${e.message}. Cargando datos ficticios de respaldo...")

                loadMockAlerts() // <--- AQUÍ ESTÁ TU SEGURIDAD
            }
        }
    }

    // Esta función se ejecuta si la API falla
    private fun loadMockAlerts() {
        val mockAlerts = listOf(
            VisualAlert("1", "SLA1", "Rol 1 (Juan Pérez)", "Incumplido", "13 días de retraso", AlertCriticality.CRITICAL),
            VisualAlert("2", "SLA2", "Rol 3 (Ana Gómez)", "Por Vencer", "1 día restante", AlertCriticality.WARNING),
            VisualAlert("3", "SLA1", "Rol 5 (Luis Torres)", "Cumplido", "0 días de retraso", AlertCriticality.INFO),
            VisualAlert("99", "ERROR", "Sistema", "Offline", "No se pudo conectar a la API", AlertCriticality.WARNING)
        )

        _uiState.update {
            it.copy(
                isLoading = false,
                alerts = mockAlerts,
                unreadCount = mockAlerts.size,
                errorMessage = "Modo Offline (Datos Ficticios)"
            )
        }
    }

    private fun loadPersonal() {
        viewModelScope.launch {
            try {
                // Llamamos a la API
                val personas = RetrofitClient.apiService.getPersonal()
                _uiState.update { it.copy(personalList = personas) }
            } catch (e: Exception) {
                println("Error cargando personal: ${e.message}")
                // Si falla, podrías poner una lista manual de respaldo si quieres
            }
        }
    }

    fun onDismissAlert(alertId: String) {
        viewModelScope.launch {
            try {
                // 1. Convertimos el ID a Entero (porque tu BD usa Int, pero la UI usa String)
                val idInt = alertId.toInt()

                // 2. Llamamos a la API para borrar en SQL Server
                val response = RetrofitClient.apiService.deleteAlerta(idInt)

                if (response.isSuccessful) {
                    println("Alerta eliminada de la BD correctamente.")

                    // 3. Si la API dijo "OK", entonces la quitamos de la lista visual
                    _uiState.update { currentState ->
                        val updatedAlerts = currentState.alerts.filterNot { it.id == alertId }
                        currentState.copy(
                            alerts = updatedAlerts,
                            unreadCount = updatedAlerts.size // Actualizamos el contador
                        )
                    }
                } else {
                    println("Error al borrar: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Error de conexión al intentar borrar.")
            }
        }
    }
    // Modificamos la función para recibir TRES parámetros
    fun crearAlertaPersonalizada(mensajeUsuario: String, responsable: String, nivel: String) {
        viewModelScope.launch {

            // Combinamos el texto para que se guarde todo en la BD
            // Ejemplo final: "Retraso en entrega - Responsable: Logística"
            val mensajeFinal = "$mensajeUsuario - Responsable: $responsable"

            val nuevaAlerta = AlertaCreateDto(
                idSolicitud = 1, // Usamos una solicitud genérica para manuales
                idTipoAlerta = 2, // ALERTA_CRITICA
                idEstadoAlerta = 1, // Nueva
                nivel = nivel,   // <--- USAMOS EL NIVEL QUE EL USUARIO SELECCIONÓ
                mensaje = mensajeFinal, // <--- Enviamos el mensaje combinado
                enviadoEmail = false
            )

            try {
                _uiState.update { it.copy(isLoading = true) }
                val response = RetrofitClient.apiService.createAlerta(nuevaAlerta)

                if (response.isSuccessful) {
                    println("¡Alerta creada con responsable y nivel!")
                    loadRealAlerts()
                } else {
                    println("Error al crear: ${response.code()}")
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}