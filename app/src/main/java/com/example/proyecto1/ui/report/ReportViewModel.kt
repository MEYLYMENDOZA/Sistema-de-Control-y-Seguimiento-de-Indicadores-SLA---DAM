package com.example.proyecto1.ui.report

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.SlaRepository // <-- CORREGIDO
import com.example.proyecto1.utils.PdfExporter
import dagger.hilt.android.lifecycle.HiltViewModel // <-- AÑADIDO
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject // <-- AÑADIDO

// --- Modelos de datos para el estado de la UI de Reportes ---

data class ReporteGeneralDto(
    val resumen: ResumenEjecutivoDto,
    val cumplimientoPorTipo: List<CumplimientoPorTipoDto>,
    val cumplimientoPorRol: List<CumplimientoPorRolDto>,
    val ultimosRegistros: List<UltimoRegistroDto> // Usará el DTO actualizado
)

data class ResumenEjecutivoDto(
    val totalCasos: Int,
    val cumplen: Int,
    val noCumplen: Int,
    val porcentajeCumplimiento: Double,
    val promedioDias: Double
)

data class CumplimientoPorTipoDto(
    val tipoSla: String,
    val total: Int,
    val cumplen: Int,
    val porcentajeCumplimiento: Double
)

data class CumplimientoPorRolDto(
    val rol: String,
    val completados: Int,
    val total: Int,
    val porcentaje: Double
)

// DTO para la tabla "Últimos Registros", ahora con más campos
data class UltimoRegistroDto(
    val rol: String,
    val fechaSolicitud: String,
    val fechaIngreso: String,
    val tipo: String,
    val dias: Int?,
    val estado: String
)

// --- Estados de la UI ---

sealed class ReportUiState {
    object Loading : ReportUiState()
    data class Success(val reportData: ReporteGeneralDto) : ReportUiState()
    data class Error(val message: String) : ReportUiState()
}

sealed class ExportState {
    object Idle : ExportState()
    object Exporting : ExportState()
    data class Success(val fileUri: Uri) : ExportState()
    data class Error(val message: String) : ExportState()
}

@HiltViewModel // <-- AÑADIDO
class ReportViewModel @Inject constructor(
    private val repository: SlaRepository,
    private val application: Application // Hilt puede inyectar Application directamente
) : ViewModel() { // <-- CORREGIDO: No necesita ser AndroidViewModel si inyectamos Application

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Loading)
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    private val pdfExporter = PdfExporter(application)

    init {
        fetchReportData()
    }

    fun fetchReportData() {
        viewModelScope.launch {
            _uiState.value = ReportUiState.Loading
            // El repositorio correcto puede tener una API diferente (ej: Flows)
            // Adaptamos la llamada para que sea compatible.
            try {
                 // Asumiendo que esta función existe en el repo correcto
                val result = repository.obtenerReporteGeneral()
                result.onSuccess {
                    _uiState.value = ReportUiState.Success(it.first)
                }.onFailure {
                    _uiState.value = ReportUiState.Error(it.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                _uiState.value = ReportUiState.Error(e.message ?: "Error al cargar el reporte.")
            }
        }
    }

    fun exportarReportePdf() {
        val currentState = _uiState.value
        if (currentState is ReportUiState.Success) {
            viewModelScope.launch {
                _exportState.value = ExportState.Exporting
                try {
                    val file = pdfExporter.exportarReporteDeIndicadores(currentState.reportData)
                    if (file != null) {
                        val context = application.applicationContext
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        _exportState.value = ExportState.Success(uri)
                    } else {
                        _exportState.value = ExportState.Error("No se pudo crear el archivo PDF.")
                    }
                } catch (e: Exception) {
                    _exportState.value = ExportState.Error("Error al exportar: ${e.message}")
                }
            }
        }
    }

    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }
}

// La ViewModelFactory ya no es necesaria con Hilt.
