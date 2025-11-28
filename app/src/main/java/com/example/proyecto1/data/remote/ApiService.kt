package com.example.proyecto1.data.remote

import com.example.proyecto1.data.remote.dto.SlaHistoricoDto
// Tus Modelos (DTOs)
import com.example.proyecto1.features.notifications.data.model.AlertaDto
import com.example.proyecto1.features.notifications.data.model.ReporteDto
import com.example.proyecto1.features.notifications.data.model.AlertaCreateDto // <--- FALTABA ESTE IMPORT
import com.example.proyecto1.features.notifications.data.model.PersonalDto

// Imports de Retrofit (Necesarios para que @GET, @POST, @Body funcionen)
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    // --- Endpoints de tus compañeros ---
    @GET("api/sla/historico")
    suspend fun obtenerHistoricoSla(): List<SlaHistoricoDto>

    // --- TUS ENDPOINTS (BRAYAN) ---

    // 1. Obtener lista de alertas
    @GET("api/Alerta")
    suspend fun getAlertas(): List<AlertaDto>

    // 2. Obtener lista de reportes
    @GET("api/Reporte")
    suspend fun getReportes(): List<ReporteDto>

    // 3. Ejecutar el Motor de SLA (Botón Verificar)
    @POST("api/Solicitud/procesar-slas")
    suspend fun procesarSlas(): Response<Unit>

    // 4. Borrar una alerta (Botón X)
    @DELETE("api/Alerta/{id}")
    suspend fun deleteAlerta(@Path("id") id: Int): Response<Unit>

    // 5. Crear Alerta Manual (Botón +)
    @POST("api/Alerta")
    suspend fun createAlerta(@Body alerta: AlertaCreateDto): Response<Unit>
    @GET("api/Personal")
    suspend fun getPersonal(): List<PersonalDto>

}