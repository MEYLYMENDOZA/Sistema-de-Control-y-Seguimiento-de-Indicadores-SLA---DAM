package com.example.proyecto1.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.repository.SlaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- Modelos de datos para el estado de la UI de Reportes ---

data class ReporteGeneralDto(
    val resumen: ResumenEjecutivoDto,
    val cumplimientoPorTipo: List<CumplimientoPorTipoDto>,
    val cumplimientoPorRol: List<CumplimientoPorRolDto>,
    val ultimosRegistros: List<UltimoRegistroDto>
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

data class UltimoRegistroDto(
    val rol: String,
    val fechaSolicitud: String,
    val fechaIngreso: String
)

/**
 * Define los posibles estados de la UI para la pantalla de reportes.
 */
sealed class ReportUiState {
    object Loading : ReportUiState()
    data class Success(val reportData: ReporteGeneralDto) : ReportUiState()
    data class Error(val message: String) : ReportUiState()
}

/**
 * ViewModel para la pantalla de reportes. Se encarga de obtener y gestionar los datos.
 */
class ReportViewModel(private val repository: SlaRepository) : ViewModel() {

    // StateFlow para exponer el estado de la UI de forma reactiva
    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Loading)
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        // Cargar los datos tan pronto como el ViewModel es creado
        fetchReportData()
    }

    /**
     * Llama al repositorio para obtener los datos del reporte y actualiza el estado de la UI.
     */
    fun fetchReportData() {
        viewModelScope.launch {
            _uiState.value = ReportUiState.Loading
            repository.obtenerReporteGeneral().onSuccess {
                _uiState.value = ReportUiState.Success(it)
            }.onFailure {
                _uiState.value = ReportUiState.Error(it.message ?: "Error desconocido")
            }
        }
    }
}

/**
 * Factory para crear una instancia de ReportViewModel con su dependencia (SlaRepository).
 */
class ReportViewModelFactory(private val repository: SlaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReportViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
