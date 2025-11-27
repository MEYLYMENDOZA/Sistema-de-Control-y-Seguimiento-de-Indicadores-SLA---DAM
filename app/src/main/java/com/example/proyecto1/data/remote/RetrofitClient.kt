package com.example.proyecto1.data.remote

import android.content.Context
import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit con detección automática de IP del servidor
 * ✅ Se adapta automáticamente a cualquier red WiFi
 * ✅ No requiere cambios manuales de IP
 * ✅ Funciona para todos los desarrolladores sin configuración
 */
object RetrofitClient {

    private const val TAG = "RetrofitClient"
    private var currentBaseUrl: String? = null
    private var retrofitInstance: Retrofit? = null

    /**
     * Inicializa Retrofit con detección automática de red
     */
    fun initialize(context: Context) {
        if (retrofitInstance == null) {
            currentBaseUrl = runBlocking {
                NetworkConfig.getApiBaseUrl(context.applicationContext)
            }
            Log.d(TAG, "🌐 API configurada en: $currentBaseUrl")
            retrofitInstance = createRetrofit(currentBaseUrl!!)
        }
    }

    /**
     * Actualiza la URL base (útil cuando cambias de red WiFi)
     */
    fun refresh(context: Context) {
        currentBaseUrl = runBlocking {
            NetworkConfig.refreshApiUrl(context.applicationContext)
        }
        Log.d(TAG, "🔄 API actualizada a: $currentBaseUrl")
        retrofitInstance = createRetrofit(currentBaseUrl!!)
    }

    /**
     * Obtiene la instancia del API
     */
    val api: ApiService
        get() {
            checkNotNull(retrofitInstance) {
                "❌ RetrofitClient no inicializado. Llama a initialize(context) primero."
            }
            return retrofitInstance!!.create(ApiService::class.java)
        }

    /**
     * Crea la instancia de Retrofit
     */
    private fun createRetrofit(baseUrl: String): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Obtiene la URL base actual (para debugging)
     */
    fun getCurrentBaseUrl(): String? = currentBaseUrl
}

