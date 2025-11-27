package com.example.proyecto1.data.remote.api

import com.example.proyecto1.data.remote.dto.SolicitudReporteDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interfaz de API REST para consumir el backend de SQL Server
 */
interface SlaApiService {

    /**
     * Obtiene las solicitudes con detalles. Usado por las pantallas de Reportes y Predicción.
     * GET /api/sla/solicitudes
     */
    @GET("api/sla/solicitudes")
    suspend fun obtenerSolicitudes(
        @Query("meses") meses: Int? = null,
        @Query("anio") anio: Int? = null,
        @Query("mes") mes: Int? = null,
        @Query("idArea") idArea: Int? = null
    ): Response<List<SolicitudReporteDto>>

    /**
     * Obtiene los años disponibles en la base de datos
     * GET /api/sla/años-disponibles
     */
    @GET("api/sla/años-disponibles")
    suspend fun obtenerAñosDisponibles(): Response<List<Int>>

    /**
     * Obtiene los meses disponibles para un año específico
     * GET /api/sla/meses-disponibles?anio=2024
     */
    @GET("api/sla/meses-disponibles")
    suspend fun obtenerMesesDisponibles(
        @Query("anio") anio: Int
    ): Response<List<Int>>
}
