package com.example.proyecto1.presentation.tendencia

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.remote.dto.AreaFiltroDto
import com.example.proyecto1.data.remote.dto.PuntoHistoricoDto
import com.example.proyecto1.data.remote.dto.PuntoTendenciaDto
import com.example.proyecto1.data.remote.dto.TipoSlaDto
import com.example.proyecto1.data.repository.TendenciaRepository
import com.example.proyecto1.utils.PdfExporterTendencia
import com.example.proyecto1.utils.TendenciaCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TendenciaViewModel @Inject constructor(
    private val repository: TendenciaRepository,
    private val application: Application
) : ViewModel() {

    private val pdfExporter = PdfExporterTendencia(application)

    // Estado de los datos
    private val _historico = MutableStateFlow<List<PuntoHistoricoDto>>(emptyList())
    val historico: StateFlow<List<PuntoHistoricoDto>> get() = _historico

    private val _tendencia = MutableStateFlow<List<PuntoTendenciaDto>>(emptyList())
    val tendencia: StateFlow<List<PuntoTendenciaDto>> get() = _tendencia

    private val _proyeccion = MutableStateFlow<Double?>(null)
    val proyeccion: StateFlow<Double?> get() = _proyeccion

    private val _pendiente = MutableStateFlow<Double?>(null)
    val pendiente: StateFlow<Double?> get() = _pendiente

    private val _intercepto = MutableStateFlow<Double?>(null)
    val intercepto: StateFlow<Double?> get() = _intercepto

    private val _estadoTendencia = MutableStateFlow<String?>(null)
    val estadoTendencia: StateFlow<String?> get() = _estadoTendencia

    private val _totalRegistros = MutableStateFlow(0)
    val totalRegistros: StateFlow<Int> get() = _totalRegistros

    // Estados de UI
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> get() = _cargando

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    private val _ultimaActualizacion = MutableStateFlow<String?>(null)
    val ultimaActualizacion: StateFlow<String?> get() = _ultimaActualizacion

    // Filtros dinámicos
    private val _aniosDisponibles = MutableStateFlow<List<Int>>(emptyList())
    val aniosDisponibles: StateFlow<List<Int>> get() = _aniosDisponibles

    private val _mesesDisponibles = MutableStateFlow<List<Int>>(emptyList())
    val mesesDisponibles: StateFlow<List<Int>> get() = _mesesDisponibles

    private val _areasDisponibles = MutableStateFlow<List<AreaFiltroDto>>(emptyList())
    val areasDisponibles: StateFlow<List<AreaFiltroDto>> get() = _areasDisponibles

    private val _tiposSlaDisponibles = MutableStateFlow<List<TipoSlaDto>>(emptyList())
    val tiposSlaDisponibles: StateFlow<List<TipoSlaDto>> get() = _tiposSlaDisponibles

    private val _periodosDisponibles = MutableStateFlow<List<Int>>(emptyList()) // <-- CAMBIO IMPORTANTE: PeriodoDto a Int
    val periodosDisponibles: StateFlow<List<Int>> get() = _periodosDisponibles

    private var filtrosActuales = FiltrosReporte()

    init {
        cargarTodosFiltros()
    }

    private fun cargarTodosFiltros() {
        viewModelScope.launch {
            cargarAniosDisponibles()
            cargarAreasDisponibles()
            cargarTiposSlaDisponibles()
            cargarPeriodosDisponibles()
        }
    }

    fun cargarAniosDisponibles() {
        viewModelScope.launch {
            try {
                _aniosDisponibles.value = repository.obtenerAniosDisponibles()
            } catch (e: Exception) { Log.e("TendenciaViewModel", "❌ Error al cargar años", e) }
        }
    }

    fun cargarMesesDisponibles(anio: Int) {
        viewModelScope.launch {
            try {
                _mesesDisponibles.value = repository.obtenerMesesDisponibles(anio)
            } catch (e: Exception) { Log.e("TendenciaViewModel", "❌ Error al cargar meses", e) }
        }
    }

    private fun cargarAreasDisponibles() {
        viewModelScope.launch {
            try {
                _areasDisponibles.value = repository.obtenerAreasDisponibles()
            } catch (e: Exception) { Log.e("TendenciaViewModel", "❌ Error al cargar áreas", e) }
        }
    }

    private fun cargarTiposSlaDisponibles() {
        viewModelScope.launch {
            try {
                _tiposSlaDisponibles.value = repository.obtenerTiposSlaDisponibles()
            } catch (e: Exception) { Log.e("TendenciaViewModel", "❌ Error al cargar tipos SLA", e) }
        }
    }

    private fun cargarPeriodosDisponibles() {
        viewModelScope.launch {
            try {
                _periodosDisponibles.value = repository.obtenerPeriodosSugeridos()
            } catch (e: Exception) { Log.e("TendenciaViewModel", "❌ Error al cargar períodos", e) }
        }
    }

    fun cargarReporteTendencia(mes: Int? = null, anio: Int? = null, tipoSla: String = "SLA1", idArea: Int? = null) {
        Log.d("TendenciaViewModel", "🔵 cargarReporteTendencia: mes=$mes, anio=$anio, tipoSla=$tipoSla, idArea=$idArea")
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null
            filtrosActuales = FiltrosReporte(mes, anio, tipoSla, idArea)
            
            Log.d("TendenciaViewModel", "📡 Solicitando datos crudos...")
            repository.obtenerDatosCrudos(anio = anio, tipoSla = tipoSla, idArea = idArea).fold(
                onSuccess = {
                    Log.d("TendenciaViewModel", "✅ Datos recibidos: ${it.datosMensuales.size} meses, ${it.totalSolicitudes} solicitudes")
                    val calculator = TendenciaCalculator()
                    val calculado = calculator.calcularTendencia(it.datosMensuales)
                    if (calculado != null) {
                        _historico.value = calculado.historico
                        _tendencia.value = calculado.lineaTendencia
                        _proyeccion.value = calculado.proyeccion
                        _pendiente.value = calculado.pendiente
                        _intercepto.value = calculado.intercepto
                        _estadoTendencia.value = calculado.estadoTendencia.name.lowercase()
                        _totalRegistros.value = it.totalSolicitudes
                        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        _ultimaActualizacion.value = formato.format(Date())
                        Log.d("TendenciaViewModel", "✅ Tendencia calculada: ${calculado.historico.size} puntos históricos, proyección=${calculado.proyeccion}")
                    } else {
                        _error.value = "Datos insuficientes (mínimo 3 meses necesarios)"
                        Log.w("TendenciaViewModel", "⚠️ Datos insuficientes para calcular tendencia")
                    }
                },
                onFailure = {
                    _error.value = "Error al cargar datos: ${it.message}"
                    Log.e("TendenciaViewModel", "❌ Error al cargar datos", it)
                }
            )
            _cargando.value = false
            Log.d("TendenciaViewModel", "⏹️ Carga finalizada. Histórico=${_historico.value.size} puntos, error=${_error.value}")
        }
    }
    
    // ... (El resto de métodos como calcularKPIs, exportarPDF, etc. permanecen igual)
    fun calcularKPIs(): KPIsTendencia? {
        val datos = _historico.value
        if (datos.isEmpty()) return null

        val mejorMes = datos.maxByOrNull { it.valor }
        val peorMes = datos.minByOrNull { it.valor }
        val promedio = datos.map { it.valor }.average()

        return KPIsTendencia(
            mejorMes = mejorMes?.mes ?: "",
            valorMejorMes = mejorMes?.valor ?: 0.0,
            peorMes = peorMes?.mes ?: "",
            valorPeorMes = peorMes?.valor ?: 0.0,
            promedioHistorico = promedio
        )
    }

    fun exportarPDF() {
        viewModelScope.launch {
            val kpis = calcularKPIs()
            if (kpis == null) {
                _error.value = "No hay datos para exportar"
                return@launch
            }
            pdfExporter.exportar(historico.value, proyeccion.value ?: 0.0, estadoTendencia.value ?: "estable", kpis, filtrosActuales, application)
        }
    }

    fun compartirReporte() {
        viewModelScope.launch {
            val kpis = calcularKPIs()
            if (kpis == null) {
                _error.value = "No hay datos para compartir"
                return@launch
            }
            pdfExporter.exportar(historico.value, proyeccion.value ?: 0.0, estadoTendencia.value ?: "estable", kpis, filtrosActuales, application, compartir = true)
        }
    }
}

data class KPIsTendencia(
    val mejorMes: String,
    val valorMejorMes: Double,
    val peorMes: String,
    val valorPeorMes: Double,
    val promedioHistorico: Double
)

data class FiltrosReporte(
    val mes: Int? = null,
    val anio: Int? = null,
    val tipoSla: String = "SLA1",
    val idArea: Int? = null
)
