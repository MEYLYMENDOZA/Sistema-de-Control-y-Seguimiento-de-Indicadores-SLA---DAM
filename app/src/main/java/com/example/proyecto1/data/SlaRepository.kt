package com.example.proyecto1.data

import android.util.Log
import com.example.proyecto1.data.remote.api.SlaApiService
import com.example.proyecto1.data.remote.dto.ConfigSlaResponseDto
import com.example.proyecto1.data.remote.dto.ConfigSlaUpdateDto
import com.example.proyecto1.data.remote.dto.SolicitudReporteDto
import com.example.proyecto1.domain.math.LinearRegression
import com.example.proyecto1.presentation.carga.CargaItemData
import com.example.proyecto1.presentation.prediccion.SlaDataPoint
import com.example.proyecto1.ui.report.CumplimientoPorRolDto
import com.example.proyecto1.ui.report.CumplimientoPorTipoDto
import com.example.proyecto1.ui.report.ReporteGeneralDto
import com.example.proyecto1.ui.report.ResumenEjecutivoDto
import com.example.proyecto1.ui.report.UltimoRegistroDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class SlaRepository @Inject constructor(private val apiService: SlaApiService) {

    private val TAG = "SlaRepository"

    // --- StateFlow from the 'new' repository for CargaScreen ---
    private val _slaItems = MutableStateFlow<List<CargaItemData>>(emptyList())
    val slaItems = _slaItems.asStateFlow()

    // --- Methods for CargaScreen ---
    suspend fun refreshDataFromApi() {
        try {
            val response = apiService.obtenerSolicitudes()
            if (response.isSuccessful) {
                val solicitudDtos = response.body() ?: emptyList()
                val cargaItems = solicitudDtos.map { it.toCargaItemData() }
                _slaItems.update { cargaItems }
            } else {
                Log.e(TAG, "Error de API: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener datos de la API", e)
        }
    }

    fun replaceItemsWith(localItems: List<CargaItemData>) {
        _slaItems.update { localItems }
    }

    fun clearAll() {
        _slaItems.update { emptyList() }
    }

    fun updateSingleItem(updatedItem: CargaItemData) {
        _slaItems.update { currentList -> currentList.map { if (it.codigo == updatedItem.codigo) updatedItem else it } }
    }

    fun deleteItems(itemCodes: Set<String>) {
        _slaItems.update { currentList -> currentList.filterNot { it.codigo in itemCodes } }
    }

    private fun SolicitudReporteDto.toCargaItemData(): CargaItemData {
        val slaTargets = mapOf("SLA1" to 35L, "SLA2" to 20L)
        val apiFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS")
        val displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val fechaSolicitud = try { this.fechaSolicitud?.let { LocalDate.parse(it, apiFormatter) } } catch (e: Exception) { null }
        val fechaIngreso = try { this.fechaIngreso?.let { LocalDate.parse(it, apiFormatter) } } catch (e: Exception) { null }
        val diasTranscurridos = if (fechaSolicitud != null && fechaIngreso != null) ChronoUnit.DAYS.between(fechaSolicitud, fechaIngreso) else this.numDiasSla?.toLong() ?: 0L
        val targetSlaCode = this.codigoSla?.uppercase()?.replace("00", "") ?: ""
        val targetDays = slaTargets[targetSlaCode] ?: 35L
        val cumple = diasTranscurridos >= 0 && diasTranscurridos < targetDays
        val estado = if (cumple) "Cumple" else "No Cumple"
        val cumplimiento = when {
            cumple -> 100.0f
            targetDays <= 0 -> 0.0f
            else -> max(0f, (2f - (diasTranscurridos.toFloat() / targetDays.toFloat())) * 50f)
        }
        return CargaItemData(this.idSolicitud.toString(), this.rol?.nombre ?: "N/A", targetSlaCode, cumplimiento, diasTranscurridos.toInt(), 0, estado, fechaSolicitud?.format(displayFormatter) ?: "", fechaIngreso?.format(displayFormatter) ?: "")
    }

    // --- Methods from the 'old' repository, now part of the unified repository ---

    suspend fun obtenerReporteGeneral(): Result<Pair<ReporteGeneralDto, List<SolicitudReporteDto>>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.obtenerSolicitudes()
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val processedReport = procesarSolicitudesParaReporte(body)
                    Result.success(Pair(processedReport, body))
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
            val estado = if (sol.numDiasSla != null && sol.diasUmbral != null) if (sol.numDiasSla <= sol.diasUmbral) "Cumple" else "No Cumple" else "N/A"
            UltimoRegistroDto(sol.rol?.nombre ?: "Sin Rol", fechaSol, fechaIng, sol.codigoSla ?: "N/A", sol.numDiasSla, estado)
        }
        return ReporteGeneralDto(resumen, cumplimientoPorTipo, cumplimientoPorRol, ultimosRegistros)
    }

    suspend fun obtenerYPredecirSla(meses: Int = 12, anio: Int? = null, mes: Int? = null): Triple<Triple<Double, Double, Double>?, Double?, String?> {
        return try {
            val response = apiService.obtenerSolicitudes(meses = meses, anio = anio, mes = null, idArea = null)
            if (!response.isSuccessful || response.body().isNullOrEmpty()) return Triple(null, null, "Error o no hay datos: ${response.code()}")
            val solicitudes = response.body()!!
            val todasLasEstadisticas = calcularEstadisticasPorMes(solicitudes)
            if (todasLasEstadisticas.size < 2) return Triple(null, null, "Datos insuficientes (se necesitan al menos 2 meses).")
            val x = todasLasEstadisticas.mapIndexed { index, _ -> (index + 1).toDouble() }.toDoubleArray()
            val y = todasLasEstadisticas.map { it.porcentajeCumplimiento }.toDoubleArray()
            val model = LinearRegression(x, y)
            val prediccion = model.predict((x.maxOrNull() ?: 0.0) + 1.0)
            Triple(Triple(prediccion, model.slope, model.intercept), null, null)
        } catch (e: Exception) {
            Triple(null, null, "Error de conexión: ${e.message}")
        }
    }

    private data class EstadisticaMes(val mes: String, val total: Int, val cumplidas: Int, val incumplidas: Int, val porcentajeCumplimiento: Double)

    private fun calcularEstadisticasPorMes(solicitudes: List<SolicitudReporteDto>): List<EstadisticaMes> {
        val formatters = listOf(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS"), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val grouped = solicitudes.groupBy { solicitud ->
            solicitud.fechaSolicitud?.let { fechaStr ->
                formatters.forEach { formatter -> try { return@groupBy LocalDateTime.parse(fechaStr, formatter).run { String.format(Locale.US, "%04d-%02d", year, monthValue) } } catch (_: DateTimeParseException) {} }
            }
            "UNKNOWN"
        }
        return grouped.filter { it.key != "UNKNOWN" }.map { (mes, sols) ->
            val cumplidas = sols.count { s -> s.numDiasSla != null && s.diasUmbral != null && s.numDiasSla <= s.diasUmbral }
            val total = sols.size
            val porcentaje = if (total > 0) (cumplidas.toDouble() / total) * 100.0 else 0.0
            EstadisticaMes(mes, total, cumplidas, total - cumplidas, porcentaje)
        }.sortedBy { it.mes }
    }
    
    suspend fun obtenerDatosHistoricos(meses: Int = 12, anio: Int? = null, mes: Int? = null): List<SlaDataPoint> {
        try {
            val response = apiService.obtenerSolicitudes(meses = meses, anio = anio, mes = mes, idArea = null)
            if (!response.isSuccessful || response.body().isNullOrEmpty()) return emptyList()
            return calcularEstadisticasPorMes(response.body()!!).mapIndexed { index, est -> SlaDataPoint(est.mes, est.porcentajeCumplimiento, index + 1) }
        } catch (_: Exception) {
            return emptyList()
        }
    }

    suspend fun obtenerAniosDisponibles(): List<Int> {
        return try { apiService.obtenerAniosDisponibles().body() ?: emptyList() } catch (_: Exception) { emptyList() }
    }

    suspend fun obtenerMesesDisponibles(anio: Int): List<Int> {
        return try { apiService.obtenerMesesDisponibles(anio).body() ?: emptyList() } catch (_: Exception) { emptyList() }
    }

    suspend fun getConfigSla(): Result<List<ConfigSlaResponseDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getConfigSla()
                if (response.isSuccessful && response.body() != null) Result.success(response.body()!!) else Result.failure(Exception("Error ${response.code()}"))
            } catch (e: Exception) {
                Result.failure(Exception("Error de conexión: ${e.message}"))
            }
        }
    }

    suspend fun updateConfigSla(configs: List<ConfigSlaUpdateDto>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateConfigSla(configs)
                if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Error ${response.code()}"))
            } catch (e: Exception) {
                Result.failure(Exception("Error de conexión: ${e.message}"))
            }
        }
    }
}
