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
        val cumplen = solicitudes.count { it.numDiasSla != null && it.diasUmbral != null && it.numDiasSla <= it.diasUmbral }
        val noCumplen = totalCasos - cumplen
        val porcentajeCumplimiento = if (totalCasos > 0) (cumplen.toDouble() / totalCasos) * 100 else 0.0
        val promedioDias = solicitudes.mapNotNull { it.numDiasSla }.average()

        val resumen = ResumenEjecutivoDto(totalCasos, cumplen, noCumplen, porcentajeCumplimiento, promedioDias)

        val cumplimientoPorTipo = solicitudes.groupBy { it.codigoSla ?: "Sin Tipo" }.map { (tipo, lista) ->
            val total = lista.size
            val cumplenTipo = lista.count { it.numDiasSla != null && it.diasUmbral != null && it.numDiasSla <= it.diasUmbral }
            CumplimientoPorTipoDto(tipo, total, cumplenTipo, if (total > 0) (cumplenTipo.toDouble() / total) * 100 else 0.0)
        }

        val cumplimientoPorRol = solicitudes.groupBy { it.rol?.nombre ?: "Sin Rol" }.map { (rol, lista) ->
            val total = lista.size
            val cumplenRol = lista.count { it.numDiasSla != null && it.diasUmbral != null && it.numDiasSla <= it.diasUmbral }
            CumplimientoPorRolDto(rol, cumplenRol, total, if (total > 0) (cumplenRol.toDouble() / total) * 100 else 0.0)
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS")
        val displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val ultimosRegistros = solicitudes.sortedByDescending { it.fechaSolicitud }.take(10).map { sol ->
            val fechaSol = sol.fechaSolicitud?.let { try { LocalDateTime.parse(it, formatter).format(displayFormatter) } catch (_: Exception) { "Fecha Inv." } } ?: "N/A"
            val fechaIng = sol.fechaIngreso?.let { try { LocalDateTime.parse(it, formatter).format(displayFormatter) } catch (_: Exception) { "Fecha Inv." } } ?: "N/A"
            val estado = if (sol.numDiasSla != null && sol.diasUmbral != null) {
                if (sol.numDiasSla <= sol.diasUmbral) "Cumple" else "No Cumple"
            } else {
                "N/A"
            }
            UltimoRegistroDto(sol.rol?.nombre ?: "Sin Rol", fechaSol, fechaIng, sol.codigoSla ?: "N/A", sol.numDiasSla, estado)
        }

        return ReporteGeneralDto(resumen, cumplimientoPorTipo, cumplimientoPorRol, ultimosRegistros)
    }

    // --- Métodos para la pantalla de Predicción ---

    @Suppress("UNUSED_PARAMETER")
    suspend fun obtenerYPredecirSla(meses: Int = 12, anio: Int? = null, mes: Int? = null): Triple<Triple<Double, Double, Double>?, Double?, String?> {
        return try {
            Log.d(TAG, "[Predicción] 🔍 Obteniendo datos: meses=$meses, anio=$anio")
            val response = apiService.obtenerSolicitudes(meses = meses, anio = anio, mes = null, idArea = null)

            if (!response.isSuccessful) {
                Log.e(TAG, "[Predicción] ❌ Error HTTP: ${response.code()}")
                return Triple(null, null, "Error del servidor: ${response.code()}")
            }

            if (response.body().isNullOrEmpty()) {
                Log.w(TAG, "[Predicción] ⚠️ No hay datos en la respuesta")
                return Triple(null, null, "No hay datos para el período.")
            }

            val solicitudes = response.body()!!
            Log.d(TAG, "[Predicción] 📊 Solicitudes recibidas: ${solicitudes.size}")

            val todasLasEstadisticas = calcularEstadisticasPorMes(solicitudes)
            Log.d(TAG, "[Predicción] 📈 Meses con estadísticas: ${todasLasEstadisticas.size}")

            todasLasEstadisticas.forEach { est ->
                Log.d(TAG, "[Predicción]   • ${est.mes}: ${est.total} casos, ${est.porcentajeCumplimiento}%")
            }

            if (todasLasEstadisticas.size < 2) {
                Log.w(TAG, "[Predicción] ⚠️ Datos insuficientes: ${todasLasEstadisticas.size} meses (se necesitan al menos 2)")
                return Triple(null, null, "Datos insuficientes (se necesitan al menos 2 meses).")
            }

            val x = todasLasEstadisticas.mapIndexed { index, _ -> (index + 1).toDouble() }.toDoubleArray()
            val y = todasLasEstadisticas.map { it.porcentajeCumplimiento }.toDoubleArray()
            val model = LinearRegression(x, y)
            val prediccion = model.predict((x.maxOrNull() ?: 0.0) + 1.0)

            Log.d(TAG, "[Predicción] ✅ Predicción calculada: $prediccion% (slope=${model.slope}, intercept=${model.intercept})")
            Triple(Triple(prediccion, model.slope, model.intercept), null, null)

        } catch (e: Exception) {
            Log.e(TAG, "[Predicción] ❌ Error inesperado", e)
            Triple(null, null, "Error de conexión: ${e.message}")
        }
    }

    private fun calcularEstadisticasPorMes(solicitudes: List<SolicitudReporteDto>): List<EstadisticaMes> {
        // Probar múltiples formatos de fecha comunes
        val formatters = listOf(
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        )

        val grouped = solicitudes.groupBy { solicitud ->
            val fechaStr = solicitud.fechaSolicitud
            if (fechaStr.isNullOrBlank()) {
                Log.w(TAG, "[Calcular] ⚠️ Solicitud sin fecha: ID=${solicitud.idSolicitud}")
                return@groupBy "UNKNOWN"
            }

            // Intentar parsear con cada formato
            for (formatter in formatters) {
                try {
                    val fecha = LocalDateTime.parse(fechaStr, formatter)
                    return@groupBy String.format(Locale.US, "%04d-%02d", fecha.year, fecha.monthValue)
                } catch (_: DateTimeParseException) {
                    continue
                }
            }

            Log.w(TAG, "[Calcular] ⚠️ No se pudo parsear fecha: '$fechaStr' (ID=${solicitud.idSolicitud})")
            "UNKNOWN"
        }

        val unknown = grouped["UNKNOWN"]?.size ?: 0
        if (unknown > 0) {
            Log.w(TAG, "[Calcular] ⚠️ $unknown solicitudes con fechas inválidas o nulas")
        }

        val estadisticas = grouped
            .filter { it.key != "UNKNOWN" }
            .map { (mes, sols) ->
                // Debug: ver los valores de las primeras 3 solicitudes de este mes
                if (sols.isNotEmpty()) {
                    Log.d(TAG, "[Calcular] 🔍 Mes $mes - Analizando ${sols.size} solicitudes:")
                    sols.take(3).forEach { sol ->
                        val diasCalculados = calcularDiasSla(sol.fechaSolicitud, sol.fechaIngreso)
                        Log.d(TAG, "[Calcular]   ID=${sol.idSolicitud}: fechaSol=${sol.fechaSolicitud?.take(10)}, " +
                                "fechaIng=${sol.fechaIngreso?.take(10)}, diasCalculados=$diasCalculados, " +
                                "diasUmbral=${sol.diasUmbral}, codigoSla=${sol.codigoSla}, numDiasSla=${sol.numDiasSla}")
                    }
                }

                var cumplidas = 0
                var detalleConteo = 0

                sols.forEach { sol ->
                    val umbral = sol.diasUmbral
                    detalleConteo++

                    if (umbral == null) {
                        Log.w(TAG, "[Calcular] ⚠️ ID=${sol.idSolicitud}: Sin umbral SLA")
                        return@forEach
                    }

                    // ✅ USAR numDiasSla del backend (ya viene calculado correctamente)
                    val diasTranscurridos = sol.numDiasSla

                    if (diasTranscurridos == null) {
                        Log.w(TAG, "[Calcular] ⚠️ ID=${sol.idSolicitud}: numDiasSla es NULL")
                        return@forEach
                    }

                    val cumple = diasTranscurridos <= umbral

                    if (detalleConteo <= 3) {
                        Log.d(TAG, "[Calcular]   ► ID=${sol.idSolicitud}: numDiasSla=$diasTranscurridos <= umbral=$umbral = $cumple")
                    }

                    if (cumple) {
                        cumplidas++
                    }
                }

                val total = sols.size
                val porcentaje = if (total > 0) (cumplidas.toDouble() / total) * 100.0 else 0.0

                Log.d(TAG, "[Calcular] 📊 Mes $mes: $cumplidas/$total cumplidas = $porcentaje%")
                EstadisticaMes(mes, total, cumplidas, total - cumplidas, porcentaje)
            }
            .sortedBy { it.mes }

        Log.d(TAG, "[Calcular] ✅ Calculadas estadísticas para ${estadisticas.size} meses")
        return estadisticas
    }

    /**
     * Calcula los días transcurridos entre fechaSolicitud y fechaIngreso
     * Retorna null si alguna fecha es nula o no se puede parsear
     */
    private fun calcularDiasSla(fechaSolicitudStr: String?, fechaIngresoStr: String?): Int? {
        if (fechaSolicitudStr.isNullOrBlank() || fechaIngresoStr.isNullOrBlank()) {
            return null
        }

        val formatters = listOf(
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        )

        var fechaSolicitud: LocalDateTime? = null
        var fechaIngreso: LocalDateTime? = null

        // Parsear fechaSolicitud
        for (formatter in formatters) {
            try {
                fechaSolicitud = LocalDateTime.parse(fechaSolicitudStr, formatter)
                break
            } catch (_: DateTimeParseException) {
                continue
            }
        }

        // Parsear fechaIngreso
        for (formatter in formatters) {
            try {
                fechaIngreso = LocalDateTime.parse(fechaIngresoStr, formatter)
                break
            } catch (_: DateTimeParseException) {
                continue
            }
        }

        if (fechaSolicitud == null || fechaIngreso == null) {
            return null
        }

        // Calcular diferencia en días
        val duration = java.time.Duration.between(fechaSolicitud, fechaIngreso)
        return duration.toDays().toInt()
    }


    suspend fun obtenerDatosHistoricos(meses: Int = 12, anio: Int? = null, mes: Int? = null): List<SlaDataPoint> {
        try {
            val response = apiService.obtenerSolicitudes(meses = meses, anio = anio, mes = mes, idArea = null)
            if (!response.isSuccessful || response.body().isNullOrEmpty()) return emptyList()
            val estadisticas = calcularEstadisticasPorMes(response.body()!!)
            return estadisticas.mapIndexed { index, est ->
                SlaDataPoint(est.mes, est.porcentajeCumplimiento, index + 1)
            }
        } catch (_: Exception) {
            return emptyList()
        }
    }

    suspend fun obtenerAniosDisponibles(): List<Int> {
        return try {
            apiService.obtenerAniosDisponibles().body() ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun obtenerMesesDisponibles(anio: Int): List<Int> {
        return try {
            apiService.obtenerMesesDisponibles(anio).body() ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun obtenerTiposSlaDisponibles(): List<Pair<String, String>> {
        return try {
            val response = apiService.obtenerTiposSlaDisponibles()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { it.codigo to it.descripcion }
            } else {
                // Fallback si el endpoint no existe
                listOf("SLA001" to "SLA Tipo 1", "SLA002" to "SLA Tipo 2")
            }
        } catch (_: Exception) {
            // Fallback si hay error de conexión
            listOf("SLA001" to "SLA Tipo 1", "SLA002" to "SLA Tipo 2")
        }
    }

    /**
     * Obtiene datos y calcula predicción FILTRANDO por tipo de SLA
     * Usa el endpoint /api/reporte/solicitudes-tendencia que ya filtra en backend
     */
    suspend fun obtenerYPredecirSlaPorTipo(
        tipoSla: String,
        anio: Int? = null,
        idArea: Int? = null
    ): Triple<Triple<Double, Double, Double>?, Double?, String?> {
        return try {
            Log.d(TAG, "[Predicción] 🔍 Obteniendo datos: tipoSla=$tipoSla, anio=$anio")
            val response = apiService.obtenerSolicitudesTendencia(
                anio = anio,
                tipoSla = tipoSla,
                idArea = idArea
            )

            if (!response.isSuccessful) {
                Log.e(TAG, "[Predicción] ❌ Error HTTP: ${response.code()}")
                return Triple(null, null, "Error del servidor: ${response.code()}")
            }

            val body = response.body()
            if (body == null || body.datosMensuales.isEmpty()) {
                Log.w(TAG, "[Predicción] ⚠️ No hay datos en la respuesta")
                return Triple(null, null, "No hay datos para el período seleccionado.")
            }

            Log.d(TAG, "[Predicción] 📊 Meses recibidos: ${body.datosMensuales.size}")

            val datosMensuales = body.datosMensuales
            datosMensuales.forEach { mes ->
                Log.d(TAG, "[Predicción]   • ${mes.mesNombre}: ${mes.totalCasos} casos, ${mes.porcentajeCumplimiento}%")
            }

            if (datosMensuales.size < 2) {
                Log.w(TAG, "[Predicción] ⚠️ Datos insuficientes: ${datosMensuales.size} meses (se necesitan al menos 2)")
                return Triple(null, null, "Datos insuficientes (se necesitan al menos 2 meses con datos).")
            }

            // Calcular regresión lineal
            val x = datosMensuales.mapIndexed { index, _ -> (index + 1).toDouble() }.toDoubleArray()
            val y = datosMensuales.map { it.porcentajeCumplimiento }.toDoubleArray()
            val model = LinearRegression(x, y)
            val prediccion = model.predict((x.maxOrNull() ?: 0.0) + 1.0)

            Log.d(TAG, "[Predicción] ✅ Predicción calculada: $prediccion% (slope=${model.slope}, intercept=${model.intercept})")
            Triple(Triple(prediccion, model.slope, model.intercept), null, null)

        } catch (e: Exception) {
            Log.e(TAG, "[Predicción] ❌ Error inesperado", e)
            Triple(null, null, "Error de conexión: ${e.message}")
        }
    }

    /**
     * Obtiene datos históricos FILTRADOS por tipo de SLA
     */
    suspend fun obtenerDatosHistoricosPorTipo(
        tipoSla: String,
        anio: Int? = null,
        idArea: Int? = null
    ): List<SlaDataPoint> {
        return try {
            val response = apiService.obtenerSolicitudesTendencia(
                anio = anio,
                tipoSla = tipoSla,
                idArea = idArea
            )

            if (!response.isSuccessful || response.body() == null) return emptyList()

            val datosMensuales = response.body()!!.datosMensuales
            datosMensuales.mapIndexed { index, mes ->
                SlaDataPoint(mes.mesNombre, mes.porcentajeCumplimiento, index + 1)
            }
        } catch (_: Exception) {
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
