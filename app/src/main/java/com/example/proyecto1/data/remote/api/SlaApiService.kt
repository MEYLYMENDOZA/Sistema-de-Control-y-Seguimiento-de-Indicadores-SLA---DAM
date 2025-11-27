package com.example.proyecto1.data.remote.api

import com.example.proyecto1.data.remote.dto.SolicitudSlaDto
import com.example.proyecto1.data.remote.dto.TendenciaDatosDto
import com.example.proyecto1.data.remote.dto.AreaFiltroDto
import com.example.proyecto1.data.remote.dto.TipoSlaDto
import com.example.proyecto1.data.remote.dto.PeriodoDto
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
}
