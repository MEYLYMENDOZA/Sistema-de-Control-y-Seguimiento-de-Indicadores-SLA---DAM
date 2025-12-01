package com.example.proyecto1.data.repository

import android.util.Log
import com.example.proyecto1.data.remote.api.RetrofitClient
import com.example.proyecto1.data.remote.dto.TendenciaDatosDto

/**
 * Repositorio para manejar la lógica de tendencia SLA
 * US-12: Tendencia y Proyección de cumplimiento SLA
 */
class TendenciaRepository {

    private val apiService = RetrofitClient.slaApiService

    /**
     * Obtiene datos crudos desde el backend (NUEVO ENDPOINT SIMPLIFICADO)
     * Los cálculos de regresión y proyección se harán en la app con TendenciaCalculator
     */
    suspend fun obtenerDatosCrudos(
        anio: Int?,
        tipoSla: String,
        idArea: Int? = null
    ): Result<TendenciaDatosDto> {
        return try {
            Log.d("TendenciaRepository", "📡 Solicitando datos crudos: año=$anio, tipoSla=$tipoSla, área=$idArea")

            val response = apiService.obtenerSolicitudesTendencia(
                anio = anio,
                tipoSla = tipoSla,
                idArea = idArea
            )

            if (response.isSuccessful && response.body() != null) {
                val datos = response.body()!!
                Log.d("TendenciaRepository", "✅ Datos crudos recibidos: ${datos.totalMeses} meses, ${datos.totalSolicitudes} solicitudes")
                Result.success(datos)
            } else {
                val errorMsg = "Error HTTP ${response.code()}: ${response.message()}"
                Log.e("TendenciaRepository", "❌ $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("TendenciaRepository", "❌ Error al obtener datos", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene años disponibles desde la base de datos
     */
    suspend fun obtenerAniosDisponibles(): List<Int> {
        return try {
            Log.d("TendenciaRepository", "📅 Solicitando años disponibles desde BD...")
            val response = apiService.obtenerAniosDisponibles()
            if (response.isSuccessful && response.body() != null) {
                val anios = response.body()!!
                Log.d("TendenciaRepository", "✅ Años obtenidos: ${anios.joinToString(", ")}")
                anios
            } else {
                Log.e("TendenciaRepository", "❌ Error HTTP ${response.code()} al obtener años")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("TendenciaRepository", "❌ Error al obtener años", e)
            emptyList()
        }
    }

    /**
     * Obtiene meses disponibles para un año desde la base de datos
     */
    suspend fun obtenerMesesDisponibles(anio: Int): List<Int> {
        return try {
            Log.d("TendenciaRepository", "📅 Solicitando meses disponibles para año $anio...")
            val response = apiService.obtenerMesesDisponibles(anio)
            if (response.isSuccessful && response.body() != null) {
                val meses = response.body()!!
                Log.d("TendenciaRepository", "✅ Meses obtenidos: ${meses.joinToString(", ")}")
                meses
            } else {
                Log.e("TendenciaRepository", "❌ Error HTTP ${response.code()} al obtener meses")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("TendenciaRepository", "❌ Error al obtener meses", e)
            emptyList()
        }
    }

    /**
     * Obtiene las áreas disponibles desde la base de datos
     */
    suspend fun obtenerAreasDisponibles(): List<com.example.proyecto1.data.remote.dto.AreaFiltroDto> {
        return try {
            Log.d("TendenciaRepository", "🏢 Solicitando áreas disponibles desde BD...")
            val response = apiService.obtenerAreasDisponibles()
            if (response.isSuccessful && response.body() != null) {
                val areas = response.body()!!
                Log.d("TendenciaRepository", "✅ Áreas obtenidas: ${areas.size} registros")
                areas
            } else {
                Log.e("TendenciaRepository", "❌ Error HTTP ${response.code()} al obtener áreas")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("TendenciaRepository", "❌ Error al obtener áreas", e)
            emptyList()
        }
    }

    /**
     * Obtiene los tipos de SLA disponibles desde la configuración
     */
    suspend fun obtenerTiposSlaDisponibles(): List<com.example.proyecto1.data.remote.dto.TipoSlaDto> {
        return try {
            Log.d("TendenciaRepository", "📋 Solicitando tipos SLA disponibles desde BD...")
            val response = apiService.obtenerTiposSlaDisponibles()
            if (response.isSuccessful && response.body() != null) {
                val tipos = response.body()!!
                Log.d("TendenciaRepository", "✅ Tipos SLA obtenidos: ${tipos.map { it.codigo }.joinToString(", ")}")
                tipos
            } else {
                Log.e("TendenciaRepository", "❌ Error HTTP ${response.code()} al obtener tipos SLA")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("TendenciaRepository", "❌ Error al obtener tipos SLA", e)
            emptyList()
        }
    }

    /**
     * Obtiene los períodos sugeridos basados en datos disponibles
     */
    suspend fun obtenerPeriodosSugeridos(): List<com.example.proyecto1.data.remote.dto.PeriodoDto> {
        return try {
            Log.d("TendenciaRepository", "⏱️ Solicitando períodos sugeridos desde BD...")
            val response = apiService.obtenerPeriodosSugeridos()
            if (response.isSuccessful && response.body() != null) {
                val periodos = response.body()!!
                Log.d("TendenciaRepository", "✅ Períodos obtenidos: ${periodos.size} opciones")
                periodos
            } else {
                Log.e("TendenciaRepository", "❌ Error HTTP ${response.code()} al obtener períodos")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("TendenciaRepository", "❌ Error al obtener períodos", e)
            emptyList()
        }
    }
}

