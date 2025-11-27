package com.example.proyecto1.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * ⚠️ OBSOLETO: Este RetrofitClient ya no se usa
 * Usa com.example.proyecto1.data.remote.api.RetrofitClient en su lugar
 *
 * Este archivo debe ser eliminado manualmente
 */
@Deprecated(
    "Usar com.example.proyecto1.data.remote.api.RetrofitClient con detección automática de IP",
    ReplaceWith("com.example.proyecto1.data.remote.api.RetrofitClient")
)
object RetrofitClient {

    // Cambia esto por la URL real de tu API .NET
    private const val BASE_URL = "http://TU_IP_O_DOMINIO:PUERTO/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
