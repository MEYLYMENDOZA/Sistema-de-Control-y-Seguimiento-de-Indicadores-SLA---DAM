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
import java.time.format.DateTimeParseException
import java.util.*

/**
 * Repositorio UNIFICADO para obtener datos de SLA para las pantallas de Reportes y Predicción.
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
            val fechaSol = sol.fechaSolicitud?.let { try { LocalDateTime.parse(it, formatter).format(displayFormatter) } catch (e: Exception) { "Fecha Inv." } } ?: "N/A"
            val fechaIng = sol.fechaIngreso?.let { try { LocalDateTime.parse(it, formatter).format(displayFormatter) } catch (e: Exception) { "Fecha Inv." } } ?: "N/A"
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

    suspend fun obtenerYPredecirSla(meses: Int = 12, anio: Int? = null, mes: Int? = null): Triple<Triple<Double, Double, Double>?, Double?, String?> {
         try {
            val response = apiService.obtenerSolicitudes(meses = meses, anio = anio, mes = null, idArea = null)
            if (!response.isSuccessful || response.body().isNullOrEmpty()) {
                 return Triple(null, null, "No hay datos para el período.")
            }
            val solicitudes = response.body()!!
            val todasLasEstadisticas = calcularEstadisticasPorMes(solicitudes)
            if (todasLasEstadisticas.size < 2) {
                 return Triple(null, null, "Datos insuficientes (se necesitan al menos 2 meses).")
            }

            val x = todasLasEstadisticas.mapIndexed { index, _ -> (index + 1).toDouble() }.toDoubleArray()
            val y = todasLasEstadisticas.map { it.porcentajeCumplimiento }.toDoubleArray()
            val model = LinearRegression(x, y)
            val prediccion = model.predict((x.maxOrNull() ?: 0.0) + 1.0)

            return Triple(Triple(prediccion, model.slope, model.intercept), null, null)
        } catch (e: Exception) {
            return Triple(null, null, "Error de conexión: ${e.message}")
        }
    }

    private fun calcularEstadisticasPorMes(solicitudes: List<SolicitudReporteDto>): List<EstadisticaMes> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS")
        return solicitudes.groupBy { 
            try {
                it.fechaSolicitud?.let { fechaStr ->
                    val fecha = LocalDateTime.parse(fechaStr, formatter)
                    String.format(Locale.US, "%04d-%02d", fecha.year, fecha.monthValue)
                } ?: "UNKNOWN"
            } catch (e: DateTimeParseException) { "UNKNOWN" }
        }.filter { it.key != "UNKNOWN" }.map { (mes, sols) ->
            val cumplidas = sols.count { it.numDiasSla != null && it.configSla?.diasUmbral != null && it.numDiasSla <= it.configSla.diasUmbral }
            val total = sols.size
            EstadisticaMes(mes, total, cumplidas, total - cumplidas, if (total > 0) (cumplidas.toDouble() / total) * 100.0 else 0.0)
        }.sortedBy { it.mes }
    }

    suspend fun obtenerDatosHistoricos(meses: Int = 12, anio: Int? = null, mes: Int? = null): List<SlaDataPoint> {
        try {
            val response = apiService.obtenerSolicitudes(meses = meses, anio = anio, mes = mes, idArea = null)
            if (!response.isSuccessful || response.body().isNullOrEmpty()) return emptyList()
            val estadisticas = calcularEstadisticasPorMes(response.body()!!)
            return estadisticas.mapIndexed { index, est ->
                SlaDataPoint(est.mes, est.porcentajeCumplimiento, index + 1)
            }
        } catch (e: Exception) {
            return emptyList()
        }
    }

    suspend fun obtenerAñosDisponibles(): List<Int> {
        return try {
            apiService.obtenerAñosDisponibles().body() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun obtenerMesesDisponibles(anio: Int): List<Int> {
        return try {
            apiService.obtenerMesesDisponibles(anio).body() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private data class EstadisticaMes(val mes: String, val total: Int, val cumplidas: Int, val incumplidas: Int, val porcentajeCumplimiento: Double)

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
