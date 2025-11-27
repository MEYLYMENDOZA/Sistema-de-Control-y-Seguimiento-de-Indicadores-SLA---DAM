package com.example.proyecto1.presentation.tendencia

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.remote.dto.PuntoHistoricoDto
import com.example.proyecto1.data.remote.dto.PuntoTendenciaDto
import com.example.proyecto1.data.repository.TendenciaRepository
// import com.example.proyecto1.utils.PdfExporterTendencia // TODO: Crear cuando sea necesario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel para la pantalla de Tendencia y Proyección SLA
 * US-12: Visualizar tendencia y proyección de cumplimiento SLA
 */
class TendenciaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TendenciaRepository()
    private val pdfExporter = com.example.proyecto1.utils.PdfExporterTendencia(application)

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

    private val _areasDisponibles = MutableStateFlow<List<com.example.proyecto1.data.remote.dto.AreaFiltroDto>>(emptyList())
    val areasDisponibles: StateFlow<List<com.example.proyecto1.data.remote.dto.AreaFiltroDto>> get() = _areasDisponibles

    private val _tiposSlaDisponibles = MutableStateFlow<List<com.example.proyecto1.data.remote.dto.TipoSlaDto>>(emptyList())
    val tiposSlaDisponibles: StateFlow<List<com.example.proyecto1.data.remote.dto.TipoSlaDto>> get() = _tiposSlaDisponibles

    private val _periodosDisponibles = MutableStateFlow<List<com.example.proyecto1.data.remote.dto.PeriodoDto>>(emptyList())
    val periodosDisponibles: StateFlow<List<com.example.proyecto1.data.remote.dto.PeriodoDto>> get() = _periodosDisponibles

    // Filtros actuales (para PDF)
    private var filtrosActuales = FiltrosReporte()

    init {
        cargarTodosFiltros()
    }

    /**
     * Carga todos los filtros disponibles desde la base de datos
     */
    private fun cargarTodosFiltros() {
        viewModelScope.launch {
            cargarAniosDisponibles()
            cargarAreasDisponibles()
            cargarTiposSlaDisponibles()
            cargarPeriodosDisponibles()
        }
    }

    /**
     * Carga los años disponibles desde la base de datos
     */
    fun cargarAniosDisponibles() {
        viewModelScope.launch {
            try {
                val anios = repository.obtenerAniosDisponibles()
                _aniosDisponibles.value = anios
                Log.d("TendenciaViewModel", "✅ Años disponibles: $anios")
            } catch (e: Exception) {
                Log.e("TendenciaViewModel", "❌ Error al cargar años", e)
            }
        }
    }

    /**
     * Carga los meses disponibles para un año específico
     */
    fun cargarMesesDisponibles(anio: Int) {
        viewModelScope.launch {
            try {
                val meses = repository.obtenerMesesDisponibles(anio)
                _mesesDisponibles.value = meses
                Log.d("TendenciaViewModel", "✅ Meses disponibles para $anio: $meses")
            } catch (e: Exception) {
                Log.e("TendenciaViewModel", "❌ Error al cargar meses", e)
            }
        }
    }

    /**
     * Carga las áreas disponibles desde la base de datos
     */
    private fun cargarAreasDisponibles() {
        viewModelScope.launch {
            try {
                val areas = repository.obtenerAreasDisponibles()
                _areasDisponibles.value = areas
                Log.d("TendenciaViewModel", "✅ Áreas disponibles: ${areas.size}")
            } catch (e: Exception) {
                Log.e("TendenciaViewModel", "❌ Error al cargar áreas", e)
            }
        }
    }

    /**
     * Carga los tipos de SLA disponibles desde la configuración
     */
    private fun cargarTiposSlaDisponibles() {
        viewModelScope.launch {
            try {
                val tipos = repository.obtenerTiposSlaDisponibles()
                _tiposSlaDisponibles.value = tipos
                Log.d("TendenciaViewModel", "✅ Tipos SLA disponibles: ${tipos.map { it.codigo }}")
            } catch (e: Exception) {
                Log.e("TendenciaViewModel", "❌ Error al cargar tipos SLA", e)
            }
        }
    }

    /**
     * Carga los períodos sugeridos basados en datos disponibles
     */
    private fun cargarPeriodosDisponibles() {
        viewModelScope.launch {
            try {
                val periodos = repository.obtenerPeriodosSugeridos()
                _periodosDisponibles.value = periodos
                Log.d("TendenciaViewModel", "✅ Períodos disponibles: ${periodos.size}")
            } catch (e: Exception) {
                Log.e("TendenciaViewModel", "❌ Error al cargar períodos", e)
            }
        }
    }

    /**
     * Carga datos crudos y CALCULA tendencia LOCALMENTE
     */
    fun cargarReporteTendencia(
        mes: Int? = null,
        anio: Int? = null,
        tipoSla: String = "SLA1",
        idArea: Int? = null
    ) {
        viewModelScope.launch {
            try {
                _cargando.value = true
                _error.value = null

                Log.d("TendenciaViewModel", "📊 Cargando datos crudos: año=$anio, tipo=$tipoSla")

                // Guardar filtros
                filtrosActuales = FiltrosReporte(mes, anio, tipoSla, idArea)

                // 1. Obtener datos crudos del backend
                val resultado = repository.obtenerDatosCrudos(
                    anio = anio,
                    tipoSla = tipoSla,
                    idArea = idArea
                )

                resultado.fold(
                    onSuccess = { datosDto ->
                        Log.d("TendenciaViewModel", "📦 Datos recibidos: ${datosDto.totalMeses} meses")

                        // 2. CALCULAR tendencia LOCALMENTE con TendenciaCalculator
                        val calculator = com.example.proyecto1.utils.TendenciaCalculator()
                        val calculado = calculator.calcularTendencia(datosDto.datosMensuales)

                        if (calculado != null) {
                            // 3. Actualizar estados con datos calculados EN LA APP
                            _historico.value = calculado.historico
                            _tendencia.value = calculado.lineaTendencia
                            _proyeccion.value = calculado.proyeccion
                            _pendiente.value = calculado.pendiente
                            _intercepto.value = calculado.intercepto
                            _estadoTendencia.value = calculado.estadoTendencia.name.lowercase()
                            _totalRegistros.value = datosDto.totalSolicitudes

                            // Actualizar timestamp
                            val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            _ultimaActualizacion.value = formato.format(Date())

                            Log.d("TendenciaViewModel", "✅ Tendencia calculada LOCALMENTE")
                            Log.d("TendenciaViewModel", "📈 Proyección: ${calculado.proyeccion}%")
                            Log.d("TendenciaViewModel", "📊 Tendencia: ${calculado.estadoTendencia}")
                            Log.d("TendenciaViewModel", "📐 Regresión: y = ${calculado.pendiente}x + ${calculado.intercepto}")
                        } else {
                            _error.value = "Datos insuficientes (mínimo 3 meses necesarios)"
                            Log.w("TendenciaViewModel", "⚠️ No se puede calcular tendencia con ${datosDto.totalMeses} meses")
                        }
                    },
                    onFailure = { exception ->
                        val mensajeError = when {
                            exception.message?.contains("HTTP 400") == true ->
                                "Parámetros inválidos. Verifica el tipo de SLA."
                            exception.message?.contains("HTTP") == true ->
                                "Error de conexión: ${exception.message}"
                            else ->
                                "Error al cargar datos: ${exception.message ?: "Desconocido"}"
                        }
                        _error.value = mensajeError
                        Log.e("TendenciaViewModel", "❌ $mensajeError", exception)
                    }
                )
            } catch (e: Exception) {
                _error.value = "Error inesperado: ${e.message}"
                Log.e("TendenciaViewModel", "❌ Error inesperado", e)
            } finally {
                _cargando.value = false
            }
        }
    }

    /**
     * Calcula KPIs derivados del histórico
     */
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

    /**
     * Exporta el reporte a PDF
     */
    fun exportarPDF() {
        viewModelScope.launch {
            try {
                Log.d("TendenciaViewModel", "📄 Exportando reporte a PDF...")

                val kpis = calcularKPIs()
                if (kpis == null) {
                    _error.value = "No hay datos para exportar"
                    return@launch
                }

                pdfExporter.exportar(
                    historico = _historico.value,
                    proyeccion = _proyeccion.value ?: 0.0,
                    estadoTendencia = _estadoTendencia.value ?: "estable",
                    kpis = kpis,
                    filtros = filtrosActuales,
                    context = getApplication<Application>()
                )

                Log.d("TendenciaViewModel", "✅ PDF exportado exitosamente")
            } catch (e: Exception) {
                _error.value = "Error al exportar PDF: ${e.message}"
                Log.e("TendenciaViewModel", "❌ Error al exportar PDF", e)
            }
        }
    }

    /**
     * Comparte el reporte por WhatsApp, Email, etc.
     */
    fun compartirReporte() {
        viewModelScope.launch {
            try {
                Log.d("TendenciaViewModel", "🔗 Compartiendo reporte...")

                val kpis = calcularKPIs()
                if (kpis == null) {
                    _error.value = "No hay datos para compartir"
                    return@launch
                }

                // Exportar PDF con opción de compartir
                pdfExporter.exportar(
                    historico = _historico.value,
                    proyeccion = _proyeccion.value ?: 0.0,
                    estadoTendencia = _estadoTendencia.value ?: "estable",
                    kpis = kpis,
                    filtros = filtrosActuales,
                    context = getApplication<Application>(),
                    compartir = true  // ✅ Activar modo compartir
                )

                Log.d("TendenciaViewModel", "✅ Reporte listo para compartir")
            } catch (e: Exception) {
                _error.value = "Error al compartir: ${e.message}"
                Log.e("TendenciaViewModel", "❌ Error al compartir", e)
            }
        }
    }
}

// ===== MODELOS DE DATOS =====

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

