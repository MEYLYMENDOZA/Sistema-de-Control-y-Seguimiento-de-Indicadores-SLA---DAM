package com.example.proyecto1.data.remote.api

import com.example.proyecto1.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit para consumir la API REST de SQL Server
 *
 * IMPORTANTE: La BASE_URL se toma desde BuildConfig.API_BASE_URL (configurada en build.gradle)
 */
object RetrofitClient {

    // Base URL configurable por buildType (debug/release)
    private val BASE_URL: String = BuildConfig.API_BASE_URL
    
    init {
        android.util.Log.d("RetrofitClient", "🌐 API Base URL configurada: $BASE_URL")
        android.util.Log.d("RetrofitClient", "📱 Dispositivo: FÍSICO (celular conectado por USB)")
        android.util.Log.d("RetrofitClient", "⚠️ IMPORTANTE: PC y celular deben estar en la MISMA red WiFi")
    }

    /**
     * Cliente HTTP con logging para debug
     */
    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Instancia de Retrofit
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Servicio de API para SLA
     */
    val slaApiService: SlaApiService by lazy {
        retrofit.create(SlaApiService::class.java)
    }
}
