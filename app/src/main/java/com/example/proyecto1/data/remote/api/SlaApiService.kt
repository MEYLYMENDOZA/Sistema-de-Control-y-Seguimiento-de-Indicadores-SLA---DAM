package com.example.proyecto1.data.remote.api

import com.example.proyecto1.data.remote.dto.SolicitudSlaDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interfaz de API REST para consumir el backend de SQL Server
 *
 * IMPORTANTE: Este endpoint debe estar implementado en tu API de Visual Studio 2022
 */
interface SlaApiService {

    /**
     * Obtiene las solicitudes crudas desde la API
     * La app Android calculará las estadísticas y predicción
     *
     * Endpoint en tu API:
     * GET /api/sla/solicitudes
     *
     * Retorna una lista de solicitudes con:
     * - idSolicitud
     * - fechaSolicitud
     * - numDiasSla (días que tardó la solicitud)
     * - diasUmbral (días máximos permitidos del SLA)
     * - idArea
     * - codigoSla
     */
    @GET("api/sla/solicitudes")
    suspend fun obtenerSolicitudes(
        @Query("meses") meses: Int? = 12,
        @Query("anio") anio: Int? = null,
        @Query("mes") mes: Int? = null,
        @Query("idArea") idArea: Int? = null
    ): Response<List<SolicitudSlaDto>>

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
