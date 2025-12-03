package com.example.proyecto1.presentation.prediccion

import android.app.Application
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.repository.SlaRepository
import com.example.proyecto1.utils.PdfExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    private val repository: SlaRepository,
    private val application: Application
) : ViewModel() {

    private val pdfExporter = PdfExporter(application)

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
                Log.d("PrediccionViewModel", "🔍 Cargando tipos SLA desde la BD...")
                val tipos = repository.obtenerTiposSlaDisponibles()
                _tiposSlaDisponibles.value = tipos
                Log.d("PrediccionViewModel", "✅ Tipos SLA cargados desde BD: ${tipos.size}")
                tipos.forEach { (codigo, descripcion) ->
                    Log.d("PrediccionViewModel", "   📋 $codigo: $descripcion")
                }
            } catch (e: Exception) {
                Log.e("PrediccionViewModel", "❌ Error al cargar tipos SLA desde BD", e)
                // NO usar fallback - dejar vacío para que el usuario sepa que hay problema
                _tiposSlaDisponibles.value = emptyList()
                Log.w("PrediccionViewModel", "⚠️ Lista de tipos SLA está vacía - verifica la conexión a la BD")
            }
        }
    }

    private fun obtenerFechaActual(): String {
        val sdf = SimpleDateFormat("dd 'de' MMMM, HH:mm", Locale.forLanguageTag("es-ES"))
        return sdf.format(Date())
    }

    fun cargarYPredecir(tipoSla: String = "SLA001") {
        cargarYPredecir(
            mesInicio = filtroMesInicio,
            mesFin = filtroMesFin,
            anio = filtroAnio,
            meses = filtroUltimosMeses,
            tipoSla = tipoSla
        )
    }

    fun cargarYPredecir(
        mesInicio: Int? = null,
        mesFin: Int? = null,
        anio: Int? = null,
        meses: Int = 12,
        tipoSla: String = "SLA001"
    ) {
        Log.d("PrediccionViewModel", "🔵 cargarYPredecir llamado con: mesInicio=$mesInicio, mesFin=$mesFin, anio=$anio, meses=$meses, tipoSla=$tipoSla")

        if (mesInicio != null && mesFin != null && mesFin < mesInicio) {
            _error.value = "El mes de fin debe ser mayor o igual al mes de inicio"
            Log.e("PrediccionViewModel", "❌ Error de validación: mes fin < mes inicio")
            return
        }

        filtroMesInicio = mesInicio
        filtroMesFin = mesFin
        filtroAnio = anio
        filtroUltimosMeses = meses
        filtroTipoSla = tipoSla

        viewModelScope.launch {
            _cargando.value = true
            _error.value = null
            Log.d("PrediccionViewModel", "⏳ Iniciando carga...")

            try {
                Log.d("PrediccionViewModel", "📡 Solicitando datos históricos...")
                val datosHistoricos = repository.obtenerDatosHistoricos(meses = meses, anio = anio, mes = mesFin)
                Log.d("PrediccionViewModel", "✅ Datos históricos recibidos: ${datosHistoricos.size} puntos")
                _datosHistoricos.value = datosHistoricos

                if (datosHistoricos.isNotEmpty()) {
                    Log.d("PrediccionViewModel", "📊 Calculando estadísticas...")
                    calcularEstadisticas(datosHistoricos)
                } else {
                    Log.w("PrediccionViewModel", "⚠️ No se recibieron datos históricos")
                }

                // --- CORRECCIÓN --- 
                // El método devuelve un Triple, no un Result. Lo manejamos directamente.
                Log.d("PrediccionViewModel", "🔮 Solicitando predicción...")
                val resultado = repository.obtenerYPredecirSla(meses = meses, anio = anio, mes = mesFin)

                val datosSla = resultado.first      // Triple<Double, Double, Double>?
                val valorReal = resultado.second    // Double?
                val mensajeError = resultado.third  // String?

                Log.d("PrediccionViewModel", "📊 Resultado predicción: datos=${datosSla != null}, valorReal=$valorReal, error=$mensajeError")

                if (datosSla != null) {
                    // Éxito en la predicción
                    _prediccion.value = datosSla.first
                    _slope.value = datosSla.second
                    _intercept.value = datosSla.third
                    _valorReal.value = valorReal
                    _error.value = null // Limpiar errores previos
                    _mostrarAdvertencia.value = (datosSla.first < UMBRAL_MINIMO)
                    _ultimaActualizacion.value = obtenerFechaActual()
                    Log.d("PrediccionViewModel", "✅ Predicción exitosa: ${datosSla.first}%")
                } else {
                    // Fallo o datos insuficientes
                    _error.value = mensajeError ?: "Error desconocido al predecir"
                    Log.e("PrediccionViewModel", "❌ Error en predicción: $mensajeError")
                    // Limpiar valores de predicción
                    _prediccion.value = null
                    _slope.value = null
                    _intercept.value = null
                    _valorReal.value = null
                }

            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                Log.e("PrediccionViewModel", "❌ Excepción en cargarYPredecir", e)
            } finally {
                _cargando.value = false
                Log.d("PrediccionViewModel", "⏹️ Carga finalizada. Cargando=${_cargando.value}")
            }
        }
    }

    private fun calcularEstadisticas(datos: List<SlaDataPoint>) {
        if (datos.isEmpty()) return
        try {
            val valores = datos.map { it.valor }
            val mejor = datos.maxByOrNull { it.valor } ?: return
            val peor = datos.minByOrNull { it.valor } ?: return
            val promedio = valores.average()
            val primerasMitad = valores.take(valores.size / 2).average()
            val segundaMitad = valores.takeLast(valores.size / 2).average()
            val tendencia = when {
                segundaMitad > primerasMitad + 1 -> "POSITIVA"
                segundaMitad < primerasMitad - 1 -> "NEGATIVA"
                else -> "ESTABLE"
            }
            // Estadísticas calculadas pero no se usan actualmente
            Log.d("PrediccionViewModel", "📊 Estadísticas: mejor=${mejor.mes}(${mejor.valor}), peor=${peor.mes}(${peor.valor}), promedio=$promedio, tendencia=$tendencia")
        } catch(e: Exception) {
            Log.e("PrediccionViewModel", "Error calculando estadisticas", e)
        }
    }

    fun exportarResultado() {
        viewModelScope.launch {
            try {
                // Validar que hay datos para exportar
                val pred = _prediccion.value
                val slp = _slope.value
                val icp = _intercept.value
                val hist = _datosHistoricos.value

                if (pred == null || slp == null || icp == null) {
                    Toast.makeText(application, "No hay datos de predicción para exportar", Toast.LENGTH_SHORT).show()
                    Log.w("PrediccionViewModel", "⚠️ Intento de exportar sin datos de predicción")
                    return@launch
                }

                // Convertir datos históricos al formato esperado por PdfExporter
                val datosHistoricosParaPdf = hist.map { punto ->
                    Triple(punto.mes, punto.valor, punto.orden)
                }

                // Calcular estadísticas para el PDF
                val estadisticas = if (hist.isNotEmpty()) {
                    val mejor = hist.maxByOrNull { it.valor }
                    val peor = hist.minByOrNull { it.valor }
                    val promedio = hist.map { it.valor }.average()
                    val primerasMitad = hist.take(hist.size / 2).map { it.valor }.average()
                    val segundaMitad = hist.takeLast(hist.size / 2).map { it.valor }.average()
                    val tendencia = when {
                        segundaMitad > primerasMitad + 1 -> "MEJORANDO"
                        segundaMitad < primerasMitad - 1 -> "EMPEORANDO"
                        else -> "ESTABLE"
                    }

                    PdfExporter.EstadisticasReporte(
                        mejorMes = mejor?.mes ?: "N/A",
                        mejorValor = mejor?.valor ?: 0.0,
                        peorMes = peor?.mes ?: "N/A",
                        peorValor = peor?.valor ?: 0.0,
                        promedio = promedio,
                        tendencia = tendencia
                    )
                } else {
                    PdfExporter.EstadisticasReporte(
                        mejorMes = "N/A",
                        mejorValor = 0.0,
                        peorMes = "N/A",
                        peorValor = 0.0,
                        promedio = 0.0,
                        tendencia = "N/A"
                    )
                }

                Log.d("PrediccionViewModel", "📄 Exportando PDF con predicción: $pred%")

                // Exportar a PDF
                val archivoPdf = pdfExporter.exportarPrediccionSLA(
                    prediccion = pred,
                    valorReal = _valorReal.value,
                    slope = slp,
                    intercept = icp,
                    datosHistoricos = datosHistoricosParaPdf,
                    estadisticas = estadisticas
                )

                if (archivoPdf != null && archivoPdf.exists()) {
                    Log.d("PrediccionViewModel", "✅ PDF creado: ${archivoPdf.absolutePath}")

                    // Abrir el PDF automáticamente
                    try {
                        val uri = FileProvider.getUriForFile(
                            application,
                            "${application.packageName}.fileprovider",
                            archivoPdf
                        )
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        application.startActivity(intent)

                        Toast.makeText(
                            application,
                            "PDF exportado: ${archivoPdf.name}",
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: Exception) {
                        Log.e("PrediccionViewModel", "❌ Error abriendo PDF", e)
                        Toast.makeText(
                            application,
                            "PDF creado en: ${archivoPdf.absolutePath}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Log.e("PrediccionViewModel", "❌ Error al crear el PDF")
                    Toast.makeText(application, "Error al crear el PDF", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("PrediccionViewModel", "❌ Error en exportarResultado", e)
                Toast.makeText(application, "Error al exportar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
