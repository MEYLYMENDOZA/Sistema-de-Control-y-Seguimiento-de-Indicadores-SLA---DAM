package com.example.proyecto1.presentation.prediccion

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.repository.SlaRepository
import com.example.proyecto1.utils.PdfExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

class PrediccionViewModel(application: Application) : AndroidViewModel(application) {

    // Repositorio UNIFICADO que consume la API REST de SQL Server
    private val repository = SlaRepository()

    // PDF Exporter
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

    // Comparación predicción vs realidad
    private val _valorReal = MutableStateFlow<Double?>(null)
    val valorReal: StateFlow<Double?> get() = _valorReal

    // Filtros dinámicos desde la base de datos
    private val _aniosDisponibles = MutableStateFlow<List<Int>>(emptyList())
    val aniosDisponibles: StateFlow<List<Int>> get() = _aniosDisponibles

    private val _mesesDisponibles = MutableStateFlow<List<Int>>(emptyList())
    val mesesDisponibles: StateFlow<List<Int>> get() = _mesesDisponibles

    private val UMBRAL_MINIMO = 85.0 // SLA mínimo aceptable

    // Estado de filtros actuales (se conserva para reintentos/refrescos)
    private var filtroMesInicio: Int? = null // 1..12
    private var filtroMesFin: Int? = null    // 1..12
    private var filtroAnio: Int? = null
    private var filtroUltimosMeses: Int = 12

    init {
        // Cargar anios disponibles al iniciar
        cargarAniosDisponibles()
    }

    /**
     * Carga los años disponibles desde la base de datos
     */
    fun cargarAniosDisponibles() {
        viewModelScope.launch {
            try {
                val anios = repository.obtenerAniosDisponibles()
                _aniosDisponibles.value = anios
                Log.d("PrediccionViewModel", "✅ Anios disponibles cargados: $anios")
            } catch (e: Exception) {
                Log.e("PrediccionViewModel", "Error al cargar anios disponibles: ${e.message}")
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
                Log.d("PrediccionViewModel", "✅ Meses disponibles para $anio: $meses")
            } catch (e: Exception) {
                Log.e("PrediccionViewModel", "Error al cargar meses disponibles: ${e.message}")
            }
        }
    }

    private fun obtenerFechaActual(): String {
        val sdf = SimpleDateFormat("dd 'de' MMMM, HH:mm", Locale.forLanguageTag("es-ES"))
        return sdf.format(Date())
    }

    /**
     * API antiguo sin parámetros: usa defaults (12 últimos meses)
     */
    fun cargarYPredecir() {
        cargarYPredecir(mesInicio = filtroMesInicio, mesFin = filtroMesFin, anio = filtroAnio, meses = filtroUltimosMeses)
    }

    /**
     * Carga los datos desde la API REST con filtros y calcula la predicción
     * @param mesInicio Mes de inicio del rango (1-12)
     * @param mesFin Mes de fin del rango (1-12, debe ser >= mesInicio)
     * @param anio Año
     * @param meses Últimos N meses (si no se especifica rango)
     */
    fun cargarYPredecir(mesInicio: Int?, mesFin: Int?, anio: Int?, meses: Int) {
        // Validar que mesFin >= mesInicio
        if (mesInicio != null && mesFin != null && mesFin < mesInicio) {
            _error.value = "El mes de fin debe ser mayor o igual al mes de inicio"
            Log.w("PrediccionViewModel", "❌ Rango inválido: $mesInicio > $mesFin")
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
                Log.d("PrediccionViewModel", "Filtros: mesInicio=$mesInicio, mesFin=$mesFin, anio=$anio")

                // Obtener datos históricos desde la API (con filtros)
                val datosHistoricos = repository.obtenerDatosHistoricos(meses = meses, anio = anio, mes = mesFin)
                _datosHistoricos.value = datosHistoricos

                Log.d("PrediccionViewModel", "Datos históricos obtenidos: ${datosHistoricos.size} meses")

                // Calcular estadísticas
                if (datosHistoricos.isNotEmpty()) {
                    calcularEstadisticas(datosHistoricos)
                }

                // Calcular predicción usando regresión lineal
                val resultado = repository.obtenerYPredecirSla(meses = meses, anio = anio, mes = mesFin)
                val datosSla = resultado.first      // Triple<Double, Double, Double>? - puede ser null
                val valorReal = resultado.second    // Double? (null si no existe el mes siguiente)
                val mensajeError = resultado.third  // String? (mensaje de error si falló)

                if (datosSla == null) {
                    // No hay datos disponibles
                    _error.value = mensajeError ?: "No hay datos disponibles para el período seleccionado"
                    _prediccion.value = null
                    _slope.value = null
                    _intercept.value = null
                    _valorReal.value = null
                    _usandoDatosDemo.value = false
                    Log.w("PrediccionViewModel", "❌ Sin datos: $mensajeError")
                    return@launch
                }

                val prediccion = datosSla.first
                val pendiente = datosSla.second
                val intercepto = datosSla.third

                _prediccion.value = prediccion
                _slope.value = pendiente
                _intercept.value = intercepto
                _valorReal.value = valorReal
                _usandoDatosDemo.value = false
                _error.value = null

                // Verificar advertencia
                _mostrarAdvertencia.value = prediccion < UMBRAL_MINIMO

                // Actualizar fecha
                _ultimaActualizacion.value = obtenerFechaActual()

                Log.d("PrediccionViewModel", "✅ Predicción calculada exitosamente")
                Log.d("PrediccionViewModel", "Predicción: $prediccion%, Pendiente: $pendiente, Intercepto: $intercepto")
                if (valorReal != null) {
                    val diferencia = valorReal - prediccion
                    Log.d("PrediccionViewModel", "📊 Comparación - Real: $valorReal%, Diferencia: ${if (diferencia >= 0) "+" else ""}$diferencia%")
                }

            } catch (e: Exception) {
                _error.value = "Error al obtener datos: ${e.message}"
                Log.e("PrediccionViewModel", "❌ Error al calcular predicción", e)
            } finally {
                _cargando.value = false
            }
        }
    }

    /**
     * Calcula estadísticas basadas en los datos históricos
     */
    private fun calcularEstadisticas(datos: List<SlaDataPoint>) {
        try {
            val valores = datos.map { it.valor }
            val mejor = datos.maxByOrNull { it.valor }!!
            val peor = datos.minByOrNull { it.valor }!!
            val promedio = valores.average()

            // Determinar tendencia
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

            Log.d("PrediccionViewModel", "Estadísticas calculadas - Mejor: ${mejor.valor}%, Peor: ${peor.valor}%, Promedio: $promedio%, Tendencia: $tendencia")
        } catch (e: Exception) {
            Log.e("PrediccionViewModel", "Error calculando estadísticas", e)
        }
    }

    fun exportarResultado() {
        viewModelScope.launch {
            try {
                // Validar que haya datos para exportar
                if (_prediccion.value == null || _slope.value == null || _intercept.value == null) {
                    Log.w("PrediccionViewModel", "No hay datos para exportar")
                    return@launch
                }

                // Preparar datos históricos para el PDF
                val datosParaPdf = _datosHistoricos.value.map { punto ->
                    Triple(punto.mes, punto.valor, punto.orden)
                }

                // Preparar estadísticas
                val stats = _estadisticas.value?.let { est ->
                    PdfExporter.EstadisticasReporte(
                        mejorMes = est.mejorMes.first,
                        mejorValor = est.mejorMes.second,
                        peorMes = est.peorMes.first,
                        peorValor = est.peorMes.second,
                        promedio = est.promedio,
                        tendencia = est.tendencia
                    )
                } ?: PdfExporter.EstadisticasReporte(
                    mejorMes = "N/A",
                    mejorValor = 0.0,
                    peorMes = "N/A",
                    peorValor = 0.0,
                    promedio = 0.0,
                    tendencia = "N/A"
                )

                // Generar PDF
                Log.d("PrediccionViewModel", "Generando PDF...")
                val pdfFile = pdfExporter.exportarPrediccionSLA(
                    prediccion = _prediccion.value!!,
                    valorReal = _valorReal.value,
                    slope = _slope.value!!,
                    intercept = _intercept.value!!,
                    datosHistoricos = datosParaPdf,
                    estadisticas = stats
                )

                if (pdfFile != null && pdfFile.exists()) {
                    Log.d("PrediccionViewModel", "✅ PDF generado exitosamente: ${pdfFile.absolutePath}")

                    // Abrir PDF automáticamente
                    val context = getApplication<Application>()
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)

                    // Intent para visualizar
                    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }

                    // Intent para compartir como fallback
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }

                    // Otorgar permiso explícito a todas las actividades que puedan manejar estos intents
                    try {
                        val pm = context.packageManager
                        val resolvedView = pm.queryIntentActivities(viewIntent, 0)
                        for (ri in resolvedView) {
                            try {
                                context.grantUriPermission(ri.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            } catch (_: Exception) {}
                        }

                        val resolvedShare = pm.queryIntentActivities(shareIntent, 0)
                        for (ri in resolvedShare) {
                            try {
                                context.grantUriPermission(ri.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            } catch (_: Exception) {}
                        }

                        // Añadir ClipData para algunos launchers/receivers que requieren ClipData
                        viewIntent.clipData = android.content.ClipData.newUri(context.contentResolver, "PDF", uri)
                        shareIntent.clipData = android.content.ClipData.newUri(context.contentResolver, "PDF", uri)

                        // Intent chooser para abrir con visor; si falla, mostrar chooser de compartir
                        try {
                            context.startActivity(viewIntent)
                        } catch (e: Exception) {
                            Log.w("PrediccionViewModel", "No hay visor PDF, fallback a compartir: ${e.message}")
                            context.startActivity(Intent.createChooser(shareIntent, "Compartir PDF").apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                        }
                    } catch (e: Exception) {
                        Log.e("PrediccionViewModel", "Error otorgando permisos o abriendo PDF", e)
                    }
                } else {
                    Log.e("PrediccionViewModel", "❌ Error al generar PDF")
                }
            } catch (e: Exception) {
                Log.e("PrediccionViewModel", "❌ Error al exportar resultado", e)
            }
        }
    }
}
