package com.example.proyecto1.data.remote.api

import com.example.proyecto1.data.remote.dto.TendenciaDatosDto
import com.example.proyecto1.data.remote.dto.AreaFiltroDto
import com.example.proyecto1.data.remote.dto.TipoSlaDto
import com.example.proyecto1.data.remote.dto.PeriodoDto

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


    /**
     * US-12: Obtiene datos crudos para análisis de tendencia
     * El backend SOLO retorna datos agrupados por mes
     * LA APP calcula: regresión lineal, proyección, tendencia
     * GET /api/reporte/solicitudes-tendencia
     * @param anio Año de análisis - opcional
     * @param tipoSla Código SLA desde ConfigSla - obligatorio (ej: "SLA001", "SLA002")
     * @param idArea ID del área - opcional
     * @return Datos mensuales crudos para cálculo local
     */
    @GET("api/reporte/solicitudes-tendencia")
    suspend fun obtenerSolicitudesTendencia(
        @Query("anio") anio: Int? = null,
        @Query("tipoSla") tipoSla: String,
        @Query("idArea") idArea: Int? = null
    ): Response<TendenciaDatosDto>

    /**
     * Obtiene los años disponibles desde la base de datos
     * GET /api/reporte/anios-disponibles
     */
    @GET("api/reporte/anios-disponibles")
    suspend fun obtenerAniosDisponibles(): Response<List<Int>>

    /**
     * Obtiene los meses disponibles para un año específico desde la base de datos
     * GET /api/reporte/meses-disponibles?anio=2024
     */
    @GET("api/reporte/meses-disponibles")
    suspend fun obtenerMesesDisponibles(
        @Query("anio") anio: Int
    ): Response<List<Int>>

    /**
     * Obtiene las áreas disponibles desde la base de datos
     * GET /api/reporte/areas-disponibles
     */
    @GET("api/reporte/areas-disponibles")
    suspend fun obtenerAreasDisponibles(): Response<List<AreaFiltroDto>>

    /**
     * Obtiene los tipos de SLA disponibles desde la configuración
     * GET /api/reporte/tipos-sla-disponibles
     */
    @GET("api/reporte/tipos-sla-disponibles")
    suspend fun obtenerTiposSlaDisponibles(): Response<List<TipoSlaDto>>

    /**
     * Obtiene los períodos sugeridos basados en datos disponibles
     * GET /api/reporte/periodos-sugeridos
     */
    @GET("api/reporte/periodos-sugeridos")
    suspend fun obtenerPeriodosSugeridos(): Response<List<PeriodoDto>>

    // --- Endpoints para Configuración ---

    @GET("api/ConfigSla")
    suspend fun getConfigSla(): Response<List<ConfigSlaResponseDto>>

    @PUT("api/ConfigSla")
    suspend fun updateConfigSla(@Body configs: List<ConfigSlaUpdateDto>): Response<Unit>
 
}
