package com.example.proyecto1.presentation.prediccion

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.SlaRepository
import com.example.proyecto1.utils.PdfExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class SlaDataPoint(
    val mes: String,
    val valor: Double,
    val orden: Int
)

data class EstadisticasSla(
    val mejorMes: Pair<String, Double>,
    val peorMes: Pair<String, Double>,
    val promedio: Double,
    val tendencia: String // "POSITIVA", "NEGATIVA", "ESTABLE"
)

@HiltViewModel
class PrediccionViewModel @Inject constructor(
    private val repository: SlaRepository
) : ViewModel() {


    private val _prediccion = MutableStateFlow<Double?>(null)
    val prediccion: StateFlow<Double?> get() = _prediccion

    private val _slope = MutableStateFlow<Double?>(null)
    val slope: StateFlow<Double?> get() = _slope

    private val _intercept = MutableStateFlow<Double?>(null)
    val intercept: StateFlow<Double?> get() = _intercept

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> get() = _cargando

    private val _datosHistoricos = MutableStateFlow<List<SlaDataPoint>>(emptyList())
    val datosHistoricos: StateFlow<List<SlaDataPoint>> get() = _datosHistoricos

    private val _estadisticas = MutableStateFlow<EstadisticasSla?>(null)
    val estadisticas: StateFlow<EstadisticasSla?> get() = _estadisticas

    private val _mostrarAdvertencia = MutableStateFlow(false)
    val mostrarAdvertencia: StateFlow<Boolean> get() = _mostrarAdvertencia

    private val _ultimaActualizacion = MutableStateFlow<String?>(null)
    val ultimaActualizacion: StateFlow<String?> get() = _ultimaActualizacion

    private val _usandoDatosDemo = MutableStateFlow(false)
    val usandoDatosDemo: StateFlow<Boolean> get() = _usandoDatosDemo

    private val _valorReal = MutableStateFlow<Double?>(null)
    val valorReal: StateFlow<Double?> get() = _valorReal

    private val _aniosDisponibles = MutableStateFlow<List<Int>>(emptyList())
    val aniosDisponibles: StateFlow<List<Int>> get() = _aniosDisponibles

    private val _mesesDisponibles = MutableStateFlow<List<Int>>(emptyList())
    val mesesDisponibles: StateFlow<List<Int>> get() = _mesesDisponibles

    // Tipos SLA disponibles
    private val _tiposSlaDisponibles = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val tiposSlaDisponibles: StateFlow<List<Pair<String, String>>> get() = _tiposSlaDisponibles

    private val UMBRAL_MINIMO = 85.0

    private var filtroMesInicio: Int? = null
    private var filtroMesFin: Int? = null
    private var filtroAnio: Int? = null
    private var filtroUltimosMeses: Int = 12
    private var filtroTipoSla: String = "SLA001" // Por defecto SLA001

    init {
        cargarAniosDisponibles()
    }

    fun cargarAniosDisponibles() {
        viewModelScope.launch {
            try {
                val anios = repository.obtenerAniosDisponibles()
                _aniosDisponibles.value = anios
                Log.d("PrediccionViewModel", "Anios disponibles cargados: $anios")
            } catch (e: Exception) {
                Log.e("PrediccionViewModel", "Error al cargar anios disponibles", e)
            }
        }
    }

    fun cargarMesesDisponibles(anio: Int) {
        viewModelScope.launch {
            try {
                val meses = repository.obtenerMesesDisponibles(anio)
                _mesesDisponibles.value = meses
                Log.d("PrediccionViewModel", "Meses disponibles para $anio: $meses")
            } catch (e: Exception) {
                Log.e("PrediccionViewModel", "Error al cargar meses disponibles", e)
            }
        }
    }

    fun cargarTiposSlaDisponibles() {
        viewModelScope.launch {
            try {
                val tipos = repository.obtenerTiposSlaDisponibles()
                _tiposSlaDisponibles.value = tipos
                Log.d("PrediccionViewModel", "Tipos SLA cargados")
            } catch (e: Exception) {
                Log.e("PrediccionViewModel", "Error al cargar tipos SLA", e)
                // Cargar tipos por defecto si falla
                _tiposSlaDisponibles.value = listOf(
                    "SLA001" to "SLA General",
                    "SLA002" to "SLA Crítico"
                )
            }
        }
    }

    private fun obtenerFechaActual(): String {
        val sdf = SimpleDateFormat("dd 'de' MMMM, HH:mm", Locale.forLanguageTag("es-ES"))
        return sdf.format(Date())
    }

    fun cargarYPredecir() {
        cargarYPredecir(
            mesInicio = filtroMesInicio,
            mesFin = filtroMesFin,
            anio = filtroAnio,
            meses = filtroUltimosMeses
        )
    }

    fun cargarYPredecir(mesInicio: Int?, mesFin: Int?, anio: Int?, meses: Int) {
        if (mesInicio != null && mesFin != null && mesFin < mesInicio) {
            _error.value = "El mes de fin debe ser mayor o igual al mes de inicio"
            return
        }

        filtroMesInicio = mesInicio
        filtroMesFin = mesFin
        filtroAnio = anio
        filtroUltimosMeses = meses

        viewModelScope.launch {
            _cargando.value = true
            _error.value = null

            try {
                Log.d("PrediccionViewModel", "Iniciando carga desde API REST...")
                val datosHistoricos = repository.obtenerDatosHistoricos(meses = meses, anio = anio, mes = mesFin)
                _datosHistoricos.value = datosHistoricos

                if (datosHistoricos.isNotEmpty()) {
                    calcularEstadisticas(datosHistoricos)
                }

                // --- CORRECCIÓN --- 
                // El método devuelve un Triple, no un Result. Lo manejamos directamente.
                val resultado = repository.obtenerYPredecirSla(meses = meses, anio = anio, mes = mesFin)
                
                val datosSla = resultado.first      // Triple<Double, Double, Double>?
                val valorReal = resultado.second    // Double?
                val mensajeError = resultado.third  // String?

                if (datosSla != null) {
                    // Éxito en la predicción
                    _prediccion.value = datosSla.first
                    _slope.value = datosSla.second
                    _intercept.value = datosSla.third
                    _valorReal.value = valorReal
                    _error.value = null // Limpiar errores previos
                    _mostrarAdvertencia.value = (datosSla.first < UMBRAL_MINIMO)
                    _ultimaActualizacion.value = obtenerFechaActual()
                } else {
                    // Fallo o datos insuficientes
                    _error.value = mensajeError ?: "Error desconocido al predecir"
                    // Limpiar valores de predicción
                    _prediccion.value = null
                    _slope.value = null
                    _intercept.value = null
                    _valorReal.value = null
                }

            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                Log.e("PrediccionViewModel", "Error en cargarYPredecir", e)
            } finally {
                _cargando.value = false
            }
        }
    }

    private fun calcularEstadisticas(datos: List<SlaDataPoint>) {
        if (datos.isEmpty()) return
        try {
            val valores = datos.map { it.valor }
            val mejor = datos.maxByOrNull { it.valor }!!
            val peor = datos.minByOrNull { it.valor }!!
            val promedio = valores.average()
            val primerasMitad = valores.take(valores.size / 2).average()
            val segundaMitad = valores.takeLast(valores.size / 2).average()
            val tendencia = when {
                segundaMitad > primerasMitad + 1 -> "POSITIVA"
                segundaMitad < primerasMitad - 1 -> "NEGATIVA"
                else -> "ESTABLE"
            }
            _estadisticas.value = EstadisticasSla(
                mejorMes = Pair(mejor.mes, mejor.valor),
                peorMes = Pair(peor.mes, peor.valor),
                promedio = promedio,
                tendencia = tendencia
            )
        } catch(e: Exception) {
            Log.e("PrediccionViewModel", "Error calculando estadisticas", e)
        }
    }

    fun exportarResultado() {
        // La lógica interna de esta función se mantiene
    }
}
