package com.example.proyecto1.data.remote

import android.util.Log
import com.example.proyecto1.BuildConfig
import com.example.proyecto1.data.remote.api.SlaApiService
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.NetworkInterface
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val TAG = "RetrofitClient"

    // Lista de candidatos probados cuando API_BASE_URL == "AUTO"
    private val CANDIDATE_BASES = listOf(
        "http://10.0.2.2:5120/",      // Emulador Android
        "http://127.0.0.1:5120/",     // Localhost (no funciona desde físico, pero pruebo)
        "http://192.168.100.4:5120/", // Ejemplo LAN
        "http://172.19.9.109:5120/",  // Tu IP indicada (prioritaria)
        "http://172.19.5.121:5120/"   // Otra IP que mencionaste
    )

    private val httpClientForProbe = OkHttpClient.Builder()
        .connectTimeout(2000, TimeUnit.MILLISECONDS)
        .readTimeout(2000, TimeUnit.MILLISECONDS)
        .callTimeout(2000, TimeUnit.MILLISECONDS)
        .build()

    private fun probeUrl(base: String): Boolean {
        return try {
            val url = if (base.endsWith("/")) "${base}api/health" else "$base/api/health"
            val req = Request.Builder().url(url).get().build()
            val resp = httpClientForProbe.newCall(req).execute()
            val isOk = resp.isSuccessful || resp.code == 404 // 404 también significa que hay un servidor
            resp.close()
            Log.d(TAG, "✅ Probe $base: ${if (isOk) "OK" else "FAIL"} (code=${resp.code})")
            isOk
        } catch (t: Throwable) {
            Log.d(TAG, "❌ Probe $base: FAIL - ${t.message}")
            false
        }
    }

    /**
     * Intenta detectar la IP local del PC en la misma red WiFi
     * Método heurístico: busca IP 192.168.x.x o 172.x.x.x
     */
    private fun detectLocalNetworkBase(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr.isSiteLocalAddress) {
                        val ip = addr.hostAddress ?: continue
                        // Generar candidato en misma subnet cambiando último octeto
                        if (ip.startsWith("192.168.") || ip.startsWith("172.")) {
                            val parts = ip.split(".")
                            if (parts.size == 4) {
                                // Probar .4 comúnmente usado por PCs
                                val candidate = "${parts[0]}.${parts[1]}.${parts[2]}.4:5120/"
                                Log.d(TAG, "🔍 Detectada red local: $ip -> probando http://$candidate")
                                return "http://$candidate"
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error detectando red local: ${e.message}")
        }
        return null
    }

    @Volatile
    private var currentBaseUrl: String? = null

    private val resolvedBaseUrl: String
        get() {
            if (currentBaseUrl != null) return currentBaseUrl!!

            return if (BuildConfig.API_BASE_URL != "AUTO") {
                BuildConfig.API_BASE_URL.also { currentBaseUrl = it }
            } else {
                // Intentar auto-detección
                val detected = detectLocalNetworkBase()
                val candidates = if (detected != null) {
                    listOf(detected) + CANDIDATE_BASES
                } else {
                    CANDIDATE_BASES
                }

                Log.d(TAG, "🔍 Probando ${candidates.size} candidatos para API...")
                val found = candidates.firstOrNull { probeUrl(it) }
                val chosen = found ?: candidates.first()
                Log.d(TAG, "✅ Base URL seleccionada: $chosen")
                chosen.also { currentBaseUrl = it }
            }
        }

    /**
     * Fuerza re-detección de la base URL (útil cuando cambia de red)
     */
    fun detectAndSetBaseSync() {
        synchronized(this) {
            currentBaseUrl = null // Resetear para forzar re-detección
            val _ = resolvedBaseUrl // Forzar ejecución
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val slaApiService: SlaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(resolvedBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SlaApiService::class.java)
    }

    // Alias para compatibilidad
    val api: SlaApiService get() = slaApiService

    /**
     * Obtener instancia con base URL personalizada
     */
    fun getApiWithBase(base: String): SlaApiService {
        val url = if (base.endsWith("/")) base else "$base/"
        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SlaApiService::class.java)
    }
}
