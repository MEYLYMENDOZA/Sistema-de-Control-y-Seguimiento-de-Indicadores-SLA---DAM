package com.example.proyecto1.data.repository

import android.util.Log
import com.example.proyecto1.data.remote.api.SlaApiService
import com.example.proyecto1.data.remote.dto.AreaFiltroDto
import com.example.proyecto1.data.remote.dto.PeriodoDto
import com.example.proyecto1.data.remote.dto.TendenciaDatosDto
import com.example.proyecto1.data.remote.dto.TipoSlaDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para manejar la lógica de tendencia SLA, adaptado para Hilt.
 */
@Singleton
class TendenciaRepository @Inject constructor(private val apiService: SlaApiService) {

    private val TAG = "TendenciaRepository"

    suspend fun obtenerDatosCrudos(
        anio: Int?,
        tipoSla: String,
        idArea: Int? = null
    ): Result<TendenciaDatosDto> {
        return try {
            Log.d(TAG, "📡 Solicitando datos crudos: año=$anio, tipoSla=$tipoSla, área=$idArea")
            val response = apiService.obtenerSolicitudesTendencia(anio = anio, tipoSla = tipoSla, idArea = idArea)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerAniosDisponibles(): List<Int> {
        return try {
            val response = apiService.obtenerAniosDisponibles()
            if (response.isSuccessful && response.body() != null) response.body()!! else emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener años", e)
            emptyList()
        }
    }

    suspend fun obtenerMesesDisponibles(anio: Int): List<Int> {
        return try {
            val response = apiService.obtenerMesesDisponibles(anio)
            if (response.isSuccessful && response.body() != null) response.body()!! else emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener meses", e)
            emptyList()
        }
    }

    suspend fun obtenerAreasDisponibles(): List<AreaFiltroDto> {
        return try {
            val response = apiService.obtenerAreasDisponibles()
            if (response.isSuccessful && response.body() != null) response.body()!! else emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener áreas", e)
            emptyList()
        }
    }

    suspend fun obtenerTiposSlaDisponibles(): List<TipoSlaDto> {
        return try {
            val response = apiService.obtenerTiposSlaDisponibles()
            if (response.isSuccessful && response.body() != null) response.body()!! else emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener tipos SLA", e)
            emptyList()
        }
    }

    suspend fun obtenerPeriodosSugeridos(): List<PeriodoDto> {
        return try {
            val response = apiService.obtenerPeriodosSugeridos()
            if (response.isSuccessful && response.body() != null) response.body()!! else emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener períodos", e)
            emptyList()
        }
    }
}
