package com.example.proyecto1.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.model.SlaTicket
import com.example.proyecto1.data.repository.SlaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- Data classes para mantener el estado de la pantalla de reportes ---

data class SlaTypeStats(
    val total: Int = 0,
    val cumplen: Int = 0,
    val porcentaje: Float = 0f
)

data class RolStats(
    val rol: String = "",
    val total: Int = 0,
    val cumplen: Int = 0,
    val porcentaje: Float = 0f
)

data class ReportState(
    val tickets: List<SlaTicket> = emptyList(),
    val totalTickets: Int = 0,
    val ticketsCumplen: Int = 0,
    val ticketsNoCumplen: Int = 0,
    val porcentajeCumplimiento: Float = 0f,
    val promedioDias: Float = 0f,
    val sla1Stats: SlaTypeStats = SlaTypeStats(),
    val sla2Stats: SlaTypeStats = SlaTypeStats(),
    val rolStats: List<RolStats> = emptyList()
)

/**
 * ViewModel para la pantalla de reportes.
 */
class ReportViewModel : ViewModel() {
    private val repository = SlaRepository()

    private val _reportState = MutableStateFlow(ReportState())
    val reportState: StateFlow<ReportState> = _reportState.asStateFlow()

    init {
        loadTicketsAndCalculateStats()
    }

    /**
     * Carga los tickets de SLA desde el repositorio y calcula las estadísticas.
     */
    fun loadTicketsAndCalculateStats() {
        viewModelScope.launch {
            val tickets = repository.getAllTickets()
            _reportState.value = calculateStatistics(tickets)
        }
    }

    private fun calculateStatistics(tickets: List<SlaTicket>): ReportState {
        if (tickets.isEmpty()) return ReportState()

        val totalTickets = tickets.size
        val cumplen = tickets.count { it.estado.equals("Cumple", ignoreCase = true) }
        val noCumplen = totalTickets - cumplen
        val porcentajeCumplimiento = if (totalTickets > 0) (cumplen.toFloat() / totalTickets) * 100 else 0f
        val promedioDias = if (totalTickets > 0) tickets.sumOf { it.dias }.toFloat() / totalTickets else 0f

        val sla1Tickets = tickets.filter { it.tipo.equals("SLA1", ignoreCase = true) }
        val sla1Total = sla1Tickets.size
        val sla1Cumplen = sla1Tickets.count { it.estado.equals("Cumple", ignoreCase = true) }
        val sla1Porcentaje = if (sla1Total > 0) (sla1Cumplen.toFloat() / sla1Total) * 100 else 0f

        val sla2Tickets = tickets.filter { it.tipo.equals("SLA2", ignoreCase = true) }
        val sla2Total = sla2Tickets.size
        val sla2Cumplen = sla2Tickets.count { it.estado.equals("Cumple", ignoreCase = true) }
        val sla2Porcentaje = if (sla2Total > 0) (sla2Cumplen.toFloat() / sla2Total) * 100 else 0f

        val rolStats = tickets.groupBy { it.rol }
            .map { (rol, ticketsPorRol) ->
                val total = ticketsPorRol.size
                val cumplen = ticketsPorRol.count { it.estado.equals("Cumple", ignoreCase = true) }
                val porcentaje = if (total > 0) (cumplen.toFloat() / total) * 100 else 0f
                RolStats(rol, total, cumplen, porcentaje)
            }.sortedBy { it.rol }

        return ReportState(
            tickets = tickets.sortedByDescending { it.fechaSolicitud }.take(10),
            totalTickets = totalTickets,
            ticketsCumplen = cumplen,
            ticketsNoCumplen = noCumplen,
            porcentajeCumplimiento = porcentajeCumplimiento,
            promedioDias = promedioDias,
            sla1Stats = SlaTypeStats(sla1Total, sla1Cumplen, sla1Porcentaje),
            sla2Stats = SlaTypeStats(sla2Total, sla2Cumplen, sla2Porcentaje),
            rolStats = rolStats
        )
    }
}