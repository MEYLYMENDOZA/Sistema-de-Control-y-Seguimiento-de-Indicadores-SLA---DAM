package com.example.proyecto1.data.remote.api

import com.example.proyecto1.data.remote.dto.ConfigSlaResponseDto
import com.example.proyecto1.data.remote.dto.ConfigSlaUpdateDto
import com.example.proyecto1.data.remote.dto.SolicitudReporteDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

/**
 * Interfaz de API REST para consumir el backend de SQL Server
 */
interface SlaApiService {

    // --- Endpoints para Reportes y Predicción ---

    @GET("api/sla/solicitudes")
    suspend fun obtenerSolicitudes(
        @Query("meses") meses: Int? = null,
        @Query("anio") anio: Int? = null,
        @Query("mes") mes: Int? = null,
        @Query("idArea") idArea: Int? = null
    ): Response<List<SolicitudReporteDto>>

    @GET("api/sla/años-disponibles")
    suspend fun obtenerAñosDisponibles(): Response<List<Int>>

    @GET("api/sla/meses-disponibles")
    suspend fun obtenerMesesDisponibles(
        @Query("anio") anio: Int
    ): Response<List<Int>>

    // --- Endpoints para Configuración ---

    @GET("api/ConfigSla")
    suspend fun getConfigSla(): Response<List<ConfigSlaResponseDto>>

    @PUT("api/ConfigSla")
    suspend fun updateConfigSla(@Body configs: List<ConfigSlaUpdateDto>): Response<Unit>
}
