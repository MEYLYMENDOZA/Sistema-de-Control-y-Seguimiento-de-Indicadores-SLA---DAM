package com.example.proyecto1.data.remote

import com.example.proyecto1.data.remote.dto.SlaHistoricoDto
import retrofit2.http.GET

interface ApiService {

    @GET("api/sla/historico")
    suspend fun obtenerHistoricoSla(): List<SlaHistoricoDto>
}
