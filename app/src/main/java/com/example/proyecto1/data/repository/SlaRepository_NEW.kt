package com.example.proyecto1.data.repository

import android.util.Log
import com.example.proyecto1.data.remote.api.RetrofitClient
import com.example.proyecto1.domain.math.LinearRegression
import com.example.proyecto1.presentation.prediccion.SlaDataPoint
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repositorio para obtener datos de SLA desde la API REST de SQL Server
 * CALCULA las estadísticas en Android, no en el backend
 */
class SlaRepository {

    private val TAG = "SlaRepository"
    private val apiService = RetrofitClient.slaApiService

    /**
     * Obtiene solicitudes crudas desde la API y calcula estadísticas + predicción en Android
     * Retorna Triple(predicción, realidad si existe, usandoDemo)
     * - predicción: Triple(valor predicho, slope, intercept)? - NULL si no hay datos
     * - realidad: Double? (null si no existe el mes siguiente)
     * - usandoDemo: Boolean (siempre false ahora, se eliminó fallback demo)
     */
    suspend fun obtenerYPredecirSla(meses: Int = 12, anio: Int? = null, mes: Int? = null): Triple<Triple<Double, Double, Double>?, Double?, String?> {
        try {
            Log.d(TAG, "🔄 Intentando conectar con la API...")
            Log.d(TAG, "📡 Endpoint: /api/sla/solicitudes?meses=$meses&anio=$anio&mes=$mes")

            // 1er intento
            val resultado = intentarPredecir(meses, anio, mes)
            if (resultado != null) return Triple(resultado.first, resultado.second, null)

            // Reintento rápido
            Log.w(TAG, "⚠️ Reintentando conexión con la API...")
            val resultado2 = intentarPredecir(meses, anio, mes)
            if (resultado2 != null) return Triple(resultado2.first, resultado2.second, null)

            Log.w(TAG, "⚠️ No se pudo obtener datos reales después de reintento")
            return Triple(null, null, "No hay datos disponibles para el período seleccionado. Verifica que la API esté corriendo y que existan datos en la base de datos.")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error: ${e.message}", e)
            return Triple(null, null, "Error de conexión: ${e.message}")
        }
    }

    private suspend fun intentarPredecir(meses: Int, anio: Int?, mes: Int?): Pair<Triple<Double, Double, Double>, Double?>? {
        // Obtener solicitudes sin filtro de mes para tener más datos
        val response = apiService.obtenerSolicitudes(meses = meses, anio = anio, mes = null, idArea = null)
        Log.d(TAG, "📡 Respuesta HTTP: ${response.code()} ${response.message()}")

        if (!response.isSuccessful) return null
        val solicitudes = response.body()
        Log.d(TAG, "📦 Solicitudes recibidas: ${solicitudes?.size ?: 0}")
        if (solicitudes.isNullOrEmpty()) return null

        val todasLasEstadisticas = calcularEstadisticasPorMes(solicitudes)
        Log.d(TAG, "📊 Meses procesados: ${todasLasEstadisticas.size}")

        // Si se especificó un mes, filtrar hasta ese mes para la predicción
        val estadisticasParaPrediccion = if (mes != null && anio != null) {
            val mesFiltro = String.format(Locale.US, "%04d-%02d", anio, mes)
            todasLasEstadisticas.filter { it.mes <= mesFiltro }.sortedBy { it.mes }
        } else {
            todasLasEstadisticas.sortedBy { it.mes }
        }

        // Validar que haya al menos 2 puntos de datos
        if (estadisticasParaPrediccion.size < 2) {
            Log.w(TAG, "⚠️ Insuficientes datos: se necesitan al menos 2 meses, solo hay ${estadisticasParaPrediccion.size}")
            return null
        }

        val x = estadisticasParaPrediccion.mapIndexed { index, _ -> (index + 1).toDouble() }.toDoubleArray()
        val y = estadisticasParaPrediccion.map { it.porcentajeCumplimiento }.toDoubleArray()

        estadisticasParaPrediccion.forEachIndexed { index, est ->
            Log.d(TAG, "📊 Mes ${index + 1} (${est.mes}): ${est.porcentajeCumplimiento}% (${est.cumplidas}/${est.total})")
        }

        Log.d(TAG, "🔢 Calculando regresión lineal...")
        val model = LinearRegression(x, y)
        val prediccion = model.predict((x.maxOrNull() ?: 0.0) + 1.0)
        Log.d(TAG, "✅ Predicción (DATOS REALES): $prediccion%")

        // Buscar el mes siguiente para comparar
        var realidadMesSiguiente: Double? = null
        if (mes != null && anio != null) {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, anio)
            cal.set(Calendar.MONTH, mes - 1) // Calendar usa 0-11
            cal.add(Calendar.MONTH, 1)

            val mesSiguiente = String.format(
                Locale.US,
                "%04d-%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1
            )

            val estadisticaMesSiguiente = todasLasEstadisticas.find { it.mes == mesSiguiente }
            if (estadisticaMesSiguiente != null) {
                realidadMesSiguiente = estadisticaMesSiguiente.porcentajeCumplimiento
                Log.d(TAG, "📈 Comparación - Predicho: $prediccion% vs Real: $realidadMesSiguiente%")
                val diferencia = realidadMesSiguiente - prediccion
                Log.d(TAG, "📊 Diferencia: ${if (diferencia >= 0) "+" else ""}$diferencia%")
            }
        }

        return Pair(Triple(prediccion, model.slope, model.intercept), realidadMesSiguiente)
    }

    /**
     * Calcula estadísticas por mes a partir de solicitudes crudas
     */
    private fun calcularEstadisticasPorMes(solicitudes: List<com.example.proyecto1.data.remote.dto.SolicitudSlaDto>): List<EstadisticaMes> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

        // Agrupar por mes
        val porMes = solicitudes.groupBy {
            try {
                val fecha = dateFormat.parse(it.fechaSolicitud)
                if (fecha != null) {
                    val cal = Calendar.getInstance()
                    cal.time = fecha
                    String.format(Locale.US, "%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
                } else {
                    "UNKNOWN"
                }
            } catch (_: Exception) {
                "UNKNOWN"
            }
        }.filter { it.key != "UNKNOWN" }

        // Calcular estadísticas
        return porMes.map { (mes, sols) ->
            val cumplidas = sols.count { it.numDiasSla <= it.diasUmbral }
            val total = sols.size
            val porcentaje = if (total > 0) (cumplidas.toDouble() / total) * 100.0 else 0.0

            EstadisticaMes(mes, total, cumplidas, total - cumplidas, porcentaje)
        }.sortedBy { it.mes }
    }

    private fun usarDatosDemostracion(): Triple<Double, Double, Double> {
        Log.d(TAG, "📊 Usando datos demo...")
        val datosSla = doubleArrayOf(95.0, 93.5, 94.2, 92.8, 91.5, 93.0, 92.3, 90.8, 91.2, 89.5, 90.0, 88.7)
        val x = DoubleArray(datosSla.size) { (it + 1).toDouble() }
        val model = LinearRegression(x, datosSla)
        val prediccion = model.predict(13.0)
        return Triple(prediccion, model.slope, model.intercept)
    }

    suspend fun obtenerDatosHistoricos(meses: Int = 12, anio: Int? = null, mes: Int? = null): List<SlaDataPoint> {
        try {
            val response = apiService.obtenerSolicitudes(meses = meses, anio = anio, mes = mes, idArea = null)
            if (!response.isSuccessful || response.body().isNullOrEmpty()) {
                return generarDatosDemo()
            }

            val estadisticas = calcularEstadisticasPorMes(response.body()!!)
            return estadisticas.mapIndexed { index, est ->
                SlaDataPoint(est.mes, est.porcentajeCumplimiento, index + 1)
            }
        } catch (e: Exception) {
            return generarDatosDemo()
        }
    }

    private fun generarDatosDemo(): List<SlaDataPoint> {
        val meses = listOf("2024-01", "2024-02", "2024-03", "2024-04", "2024-05", "2024-06",
                          "2024-07", "2024-08", "2024-09", "2024-10", "2024-11", "2024-12")
        val valores = doubleArrayOf(95.0, 93.5, 94.2, 92.8, 91.5, 93.0, 92.3, 90.8, 91.2, 89.5, 90.0, 88.7)
        return meses.mapIndexed { index, mes -> SlaDataPoint(mes, valores[index], index + 1) }
    }

    private data class EstadisticaMes(
        val mes: String,
        val total: Int,
        val cumplidas: Int,
        val incumplidas: Int,
        val porcentajeCumplimiento: Double
    )

    /**
     * Obtiene los años disponibles en la base de datos
     * NOTA: Este método usa endpoints que solo están disponibles en TendenciaRepository
     * Si necesitas años disponibles, usa TendenciaRepository en su lugar
     */
    suspend fun obtenerAñosDisponibles(): List<Int> {
        // Fallback: retorna últimos 3 años
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        return listOf(currentYear, currentYear - 1, currentYear - 2)
    }

    /**
     * Obtiene los meses disponibles para un año específico
     * NOTA: Este método usa endpoints que solo están disponibles en TendenciaRepository
     * Si necesitas meses disponibles, usa TendenciaRepository en su lugar
     */
    suspend fun obtenerMesesDisponibles(anio: Int): List<Int> {
        // Fallback: retorna todos los meses
        return (1..12).toList()
    }
}

// Este archivo ha sido unificado en SlaRepository.kt y ya no se utiliza.

