package com.example.proyecto1.data.repository

import android.util.Log
import com.example.proyecto1.data.remote.api.RetrofitClient
import com.example.proyecto1.data.remote.api.SlaApiService
import com.example.proyecto1.data.remote.dto.ConfigSlaResponseDto
import com.example.proyecto1.data.remote.dto.ConfigSlaUpdateDto
import com.example.proyecto1.data.remote.dto.SolicitudReporteDto
import com.example.proyecto1.domain.math.LinearRegression
import com.example.proyecto1.presentation.prediccion.SlaDataPoint
import com.example.proyecto1.ui.report.CumplimientoPorRolDto
import com.example.proyecto1.ui.report.CumplimientoPorTipoDto
import com.example.proyecto1.ui.report.ReporteGeneralDto
import com.example.proyecto1.ui.report.ResumenEjecutivoDto
import com.example.proyecto1.ui.report.UltimoRegistroDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Repositorio UNIFICADO para obtener datos de SLA para las pantallas de Reportes, Predicción y Configuración.
 */
class SlaRepository {

    private val TAG = "SlaRepository"
    private val apiService: SlaApiService = RetrofitClient.slaApiService

    // --- Métodos para la pantalla de Reportes ---

    // MODIFICADO: Ahora devuelve también la lista de datos crudos
    suspend fun obtenerReporteGeneral(): Result<Pair<ReporteGeneralDto, List<SolicitudReporteDto>>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "[Reportes] 🔍 Llamando a la API: obtenerSolicitudes()")
                val response = apiService.obtenerSolicitudes()

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Log.d(TAG, "[Reportes] ✅ API call successful. ${body.size} solicitudes recibidas.")
                        val processedReport = procesarSolicitudesParaReporte(body)
                        // Devolver tanto los datos procesados como los crudos
                        Result.success(Pair(processedReport, body))
                    } else {
                        Result.failure(Exception("El cuerpo de la respuesta es nulo."))
                    }
                } else {
                    Result.failure(Exception("Error ${response.code()}: ${response.errorBody()?.string()}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("No se pudo conectar al servidor: ${e.message}"))
            }
        }
    }

    private fun procesarSolicitudesParaReporte(solicitudes: List<SolicitudReporteDto>): ReporteGeneralDto {
        val totalCasos = solicitudes.size
        val cumplen = solicitudes.count { it.numDiasSla != null && it.configSla?.diasUmbral != null && it.numDiasSla <= it.configSla.diasUmbral }
        val noCumplen = totalCasos - cumplen
        val porcentajeCumplimiento = if (totalCasos > 0) (cumplen.toDouble() / totalCasos) * 100 else 0.0
        val promedioDias = solicitudes.mapNotNull { it.numDiasSla }.average()

        val resumen = ResumenEjecutivoDto(totalCasos, cumplen, noCumplen, porcentajeCumplimiento, promedioDias)

        val cumplimientoPorTipo = solicitudes.groupBy { it.configSla?.codigoSla ?: "Sin Tipo" }.map { (tipo, lista) ->
            val total = lista.size
            val cumplenTipo = lista.count { it.numDiasSla != null && it.configSla?.diasUmbral != null && it.numDiasSla <= it.configSla.diasUmbral }
            CumplimientoPorTipoDto(tipo, total, cumplenTipo, if (total > 0) (cumplenTipo.toDouble() / total) * 100 else 0.0)
        }

        val cumplimientoPorRol = solicitudes.groupBy { it.rol?.nombre ?: "Sin Rol" }.map { (rol, lista) ->
            val total = lista.size
            val cumplenRol = lista.count { it.numDiasSla != null && it.configSla?.diasUmbral != null && it.numDiasSla <= it.configSla.diasUmbral }
            CumplimientoPorRolDto(rol, cumplenRol, total, if (total > 0) (cumplenRol.toDouble() / total) * 100 else 0.0)
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS")
        val displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val ultimosRegistros = solicitudes.sortedByDescending { it.fechaSolicitud }.take(10).map { sol ->
            val fechaSol = sol.fechaSolicitud?.let { try { LocalDateTime.parse(it, formatter).format(displayFormatter) } catch (_: Exception) { "Fecha Inv." } } ?: "N/A"
            val fechaIng = sol.fechaIngreso?.let { try { LocalDateTime.parse(it, formatter).format(displayFormatter) } catch (_: Exception) { "Fecha Inv." } } ?: "N/A"
            val estado = if (sol.numDiasSla != null && sol.configSla?.diasUmbral != null) {
                if (sol.numDiasSla <= sol.configSla.diasUmbral) "Cumple" else "No Cumple"
            } else {
                "N/A"
            }
            UltimoRegistroDto(sol.rol?.nombre ?: "Sin Rol", fechaSol, fechaIng, sol.configSla?.codigoSla ?: "N/A", sol.numDiasSla, estado)
        }

        return ReporteGeneralDto(resumen, cumplimientoPorTipo, cumplimientoPorRol, ultimosRegistros)
    }

    // --- Métodos para la pantalla de Predicción ---

    /**
     * Obtiene solicitudes desde la API y calcula predicción con regresión lineal
     * Retorna Triple(predicción, realidad si existe, mensaje de error)
     */
    suspend fun obtenerYPredecirSla(meses: Int = 12, anio: Int? = null, mes: Int? = null): Triple<Triple<Double, Double, Double>?, Double?, String?> {
        try {
            Log.d(TAG, "🔄 [Predicción] Intentando conectar con la API...")
            Log.d(TAG, "📡 Endpoint: /api/sla/solicitudes?meses=$meses&anio=$anio&mes=$mes")

            // Primer intento
            val resultado = intentarPredecir(meses, anio, mes)
            if (resultado != null) return Triple(resultado.first, resultado.second, null)

            // Reintento rápido
            Log.w(TAG, "⚠️ Reintentando conexión con la API...")
            val resultado2 = intentarPredecir(meses, anio, mes)
            if (resultado2 != null) return Triple(resultado2.first, resultado2.second, null)

            Log.w(TAG, "⚠️ No se pudo obtener datos después de reintento")
            return Triple(null, null, "No hay datos disponibles para el período seleccionado.")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error: ${e.message}", e)
            return Triple(null, null, "Error de conexión: ${e.message}")
        }
    }

    private suspend fun intentarPredecir(meses: Int, anio: Int?, mes: Int?): Pair<Triple<Double, Double, Double>, Double?>? {
        val response = apiService.obtenerSolicitudes(meses = meses, anio = anio, mes = null, idArea = null)
        Log.d(TAG, "📡 Respuesta HTTP: ${response.code()} ${response.message()}")

        if (!response.isSuccessful) return null
        val solicitudes = response.body()
        Log.d(TAG, "📦 Solicitudes recibidas: ${solicitudes?.size ?: 0}")
        if (solicitudes.isNullOrEmpty()) return null

        val todasLasEstadisticas = calcularEstadisticasPorMesSlaDto(solicitudes)
        Log.d(TAG, "📊 Meses procesados: ${todasLasEstadisticas.size}")

        // Si se especificó un mes, filtrar hasta ese mes para la predicción
        val estadisticasParaPrediccion = if (mes != null && anio != null) {
            val mesFiltro = String.format(Locale.US, "%04d-%02d", anio, mes)
            todasLasEstadisticas.filter { it.mes <= mesFiltro }.sortedBy { it.mes }
        } else {
            todasLasEstadisticas.sortedBy { it.mes }
        }

        if (estadisticasParaPrediccion.size < 2) {
            Log.w(TAG, "⚠️ Insuficientes datos: se necesitan al menos 2 meses")
            return null
        }

        val x = estadisticasParaPrediccion.mapIndexed { index, _ -> (index + 1).toDouble() }.toDoubleArray()
        val y = estadisticasParaPrediccion.map { it.porcentajeCumplimiento }.toDoubleArray()

        Log.d(TAG, "🔢 Calculando regresión lineal...")
        val model = LinearRegression(x, y)
        val prediccion = model.predict((x.maxOrNull() ?: 0.0) + 1.0)
        Log.d(TAG, "✅ Predicción: $prediccion%")

        // Buscar el mes siguiente para comparar
        var realidadMesSiguiente: Double? = null
        if (mes != null && anio != null) {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, anio)
            cal.set(Calendar.MONTH, mes - 1)
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
            }
        }

        return Pair(Triple(prediccion, model.slope, model.intercept), realidadMesSiguiente)
    }

    /**
     * Calcula estadísticas por mes para SolicitudReporteDto (usado en predicción)
     */
    private fun calcularEstadisticasPorMesSlaDto(solicitudes: List<SolicitudReporteDto>): List<EstadisticaMes> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS")

        val porMes = solicitudes.groupBy {
            try {
                it.fechaSolicitud?.let { fechaStr ->
                    val fecha = LocalDateTime.parse(fechaStr, formatter)
                    String.format(Locale.US, "%04d-%02d", fecha.year, fecha.monthValue)
                } ?: "UNKNOWN"
            } catch (_: Exception) {
                "UNKNOWN"
            }
        }.filter { it.key != "UNKNOWN" }

        return porMes.map { (mes, sols) ->
            val cumplidas = sols.count {
                it.numDiasSla != null && it.configSla?.diasUmbral != null &&
                it.numDiasSla <= it.configSla.diasUmbral
            }
            val total = sols.size
            val porcentaje = if (total > 0) (cumplidas.toDouble() / total) * 100.0 else 0.0
            EstadisticaMes(mes, total, cumplidas, total - cumplidas, porcentaje)
        }.sortedBy { it.mes }
    }

    suspend fun obtenerDatosHistoricos(meses: Int = 12, anio: Int? = null, mes: Int? = null): List<SlaDataPoint> {
        return try {
            val response = apiService.obtenerSolicitudes(meses = meses, anio = anio, mes = mes, idArea = null)
            if (!response.isSuccessful || response.body().isNullOrEmpty()) {
                emptyList()
            } else {
                val estadisticas = calcularEstadisticasPorMesSlaDto(response.body()!!)
                estadisticas.mapIndexed { index, est ->
                    SlaDataPoint(est.mes, est.porcentajeCumplimiento, index + 1)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener datos históricos", e)
            emptyList()
        }
    }

    suspend fun obtenerAniosDisponibles(): List<Int> {
        return try {
            val response = apiService.obtenerAniosDisponibles()
            response.body() ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener años disponibles", e)
            emptyList()
        }
    }

    suspend fun obtenerMesesDisponibles(anio: Int): List<Int> {
        return try {
            val response = apiService.obtenerMesesDisponibles(anio)
            response.body() ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener meses disponibles", e)
            emptyList()
        }
    }

    private data class EstadisticaMes(
        val mes: String,
        val total: Int,
        val cumplidas: Int,
        val incumplidas: Int,
        val porcentajeCumplimiento: Double
    )

    // --- Métodos para la pantalla de Configuración ---

    suspend fun getConfigSla(): Result<List<ConfigSlaResponseDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getConfigSla()
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Error ${response.code()}: ${response.errorBody()?.string()}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("No se pudo conectar al servidor: ${e.message}"))
            }
        }
    }

    suspend fun updateConfigSla(configs: List<ConfigSlaUpdateDto>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateConfigSla(configs)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Error ${response.code()}: ${response.errorBody()?.string()}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("No se pudo conectar al servidor: ${e.message}"))
            }
        }
    }
}
