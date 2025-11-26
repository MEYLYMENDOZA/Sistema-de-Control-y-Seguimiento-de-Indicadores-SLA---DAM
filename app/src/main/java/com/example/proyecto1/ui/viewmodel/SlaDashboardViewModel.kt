package com.example.proyecto1.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.repository.KpiResult
import com.example.proyecto1.data.repository.SlaDashboardRepository
import com.example.proyecto1.data.repository.SlaHistoricoResult
import com.example.proyecto1.utils.CsvReportGenerator
import com.example.proyecto1.utils.PdfReportGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SlaDashboardState(
    val isLoading: Boolean = true,
    val kpiResult: KpiResult? = null,
    val historicoResult: SlaHistoricoResult? = null,
    val error: String? = null
)

class SlaDashboardViewModel : ViewModel() {

    private val repository = SlaDashboardRepository()

    private val _uiState = MutableStateFlow(SlaDashboardState())
    val uiState: StateFlow<SlaDashboardState> = _uiState.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        _uiState.value = SlaDashboardState(isLoading = true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val kpis = repository.fetchSlaKpis()
                val historico = repository.fetchSlaHistorico()
                _uiState.value = SlaDashboardState(isLoading = false, kpiResult = kpis, historicoResult = historico)
            } catch (e: Exception) {
                _uiState.value = SlaDashboardState(isLoading = false, error = e.message ?: "Ocurrió un error inesperado")
            }
        }
    }

    fun generatePdfReport(context: Context) {
        val kpis = _uiState.value.kpiResult
        if (kpis == null) {
            viewModelScope.launch { _toastMessage.emit("No hay datos para generar el reporte.") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val resultMessage = PdfReportGenerator.saveToFile(context, kpis)
            _toastMessage.emit(resultMessage)
        }
    }

    fun generateCsvReport(context: Context) {
        val kpis = _uiState.value.kpiResult
        if (kpis == null) {
            viewModelScope.launch { _toastMessage.emit("No hay datos para generar el reporte.") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val resultMessage = CsvReportGenerator.generate(context, kpis)
            _toastMessage.emit(resultMessage)
        }
    }
}
