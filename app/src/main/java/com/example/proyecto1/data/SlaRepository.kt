package com.example.proyecto1.data

import android.util.Log
import com.example.proyecto1.network.SlaApiService
import com.example.proyecto1.network.dto.SolicitudReporteDTO
import com.example.proyecto1.presentation.carga.CargaItemData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class SlaRepository @Inject constructor(private val apiService: SlaApiService) {

    private val _slaItems = MutableStateFlow<List<CargaItemData>>(emptyList())
    val slaItems = _slaItems.asStateFlow()

    suspend fun refreshDataFromApi() {
        try {
            val solicitudDtos = apiService.getSolicitudes()
            val cargaItems = solicitudDtos.map { it.toCargaItemData() }
            _slaItems.update { cargaItems }
            Log.d("SlaRepository", "Datos actualizados desde la API: ${cargaItems.size} items.")
        } catch (e: Exception) {
            Log.e("SlaRepository", "Error al obtener datos de la API", e)
            _slaItems.update { emptyList() }
        }
    }

    fun replaceItemsWith(localItems: List<CargaItemData>) {
        Log.d("SlaRepository", "Reemplazando datos con ${localItems.size} items locales.")
        _slaItems.update { localItems }
    }

    fun clearAll() {
        _slaItems.update { emptyList() }
    }

    fun updateSingleItem(updatedItem: CargaItemData) {
        _slaItems.update { currentList ->
            currentList.map {
                if (it.codigo == updatedItem.codigo) updatedItem else it
            }
        }
    }

    fun deleteItems(itemCodes: Set<String>) {
        _slaItems.update { currentList ->
            currentList.filterNot { it.codigo in itemCodes }
        }
    }

    private fun SolicitudReporteDTO.toCargaItemData(): CargaItemData {
        val slaTargets = mapOf("SLA1" to 35L, "SLA2" to 20L)
        val apiFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS")
        val displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        
        val fechaSolicitud = try { this.fechaSolicitud?.let { LocalDate.parse(it, apiFormatter) } } catch (e: Exception) { null }
        val fechaIngreso = try { this.fechaIngreso?.let { LocalDate.parse(it, apiFormatter) } } catch (e: Exception) { null }

        val diasTranscurridos = if (fechaSolicitud != null && fechaIngreso != null) {
            ChronoUnit.DAYS.between(fechaSolicitud, fechaIngreso)
        } else {
            this.numDiasSla?.toLong() ?: 0L
        }

        // CORRECCIÓN: Formatear el código del SLA de "SLA001" a "SLA1"
        val targetSlaCode = this.configSla?.codigoSla?.uppercase()?.replace("00", "") ?: ""
        val targetDays = slaTargets[targetSlaCode] ?: 35L

        val cumple = diasTranscurridos >= 0 && diasTranscurridos < targetDays
        val estado = if (cumple) "Cumple" else "No Cumple"

        val cumplimiento = when {
            cumple -> 100.0f
            targetDays <= 0 -> 0.0f
            else -> {
                val ratio = diasTranscurridos.toFloat() / targetDays.toFloat()
                max(0f, (2f - ratio) * 50f)
            }
        }

        return CargaItemData(
            // CORRECCIÓN: Usar el idSolicitud como el código.
            codigo = this.idSolicitud.toString(),
            rol = this.rol?.nombre ?: "N/A",
            tipoSla = targetSlaCode,
            cumplimiento = cumplimiento,
            diasTranscurridos = diasTranscurridos.toInt(),
            cantidadPorRol = 0, 
            estado = estado,
            fechaSolicitud = fechaSolicitud?.format(displayFormatter) ?: "",
            fechaIngreso = fechaIngreso?.format(displayFormatter) ?: ""
        )
    }
}
