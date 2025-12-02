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
    // PUERTO CORRECTO: 5120 (NO 5210)
    private val BASE_URL: String = try {
        BuildConfig.API_BASE_URL
    } catch (_: Exception) {
        // Fallback: 10.0.2.2 es localhost del PC desde el emulador Android - Puerto 5120
        "http://10.0.2.2:5120/"
    }

    
    init {
        android.util.Log.d("RetrofitClient", "🌐 API Base URL configurada: $BASE_URL")
        android.util.Log.d("RetrofitClient", "📱 Conectando al backend en puerto 5120")
        android.util.Log.d("RetrofitClient", "⚠️ IMPORTANTE: Backend debe estar ejecutándose en http://localhost:5120")
    }

    /**
     * Cliente HTTP con logging para debug
     * Timeouts aumentados para evitar errores de conexión
     */
    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)  // Aumentado a 30 segundos
            .readTimeout(30, TimeUnit.SECONDS)     // Aumentado a 30 segundos
            .writeTimeout(30, TimeUnit.SECONDS)    // Aumentado a 30 segundos
            .retryOnConnectionFailure(true)         // Activar reintentos
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
