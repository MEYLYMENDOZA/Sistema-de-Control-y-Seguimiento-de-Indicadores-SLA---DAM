package com.example.proyecto1.data.remote.api

import android.content.Context
import android.util.Log
import com.example.proyecto1.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.net.URL
import java.net.NetworkInterface
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetrofitClient @Inject constructor(@ApplicationContext private val context: Context) {

    private val TAG = "RetrofitClient_API"
    private val API_PORT = 5120
    private val HEALTH_ENDPOINT = "/api/reporte/tipos-sla-disponibles"
    private val PREFS_NAME = "api_config"
    private val KEY_LAST_IP = "last_working_ip"
    private val CONNECTION_TIMEOUT = 2000

    private val COMMON_IPS = listOf(
        "192.168.100.4",    // 👈 IP ACTUAL DEL SERVIDOR - PRUEBA PRIMERO
        "172.19.9.109",     // IP anterior
        "172.19.5.121",
        "172.19.7.121",
        "172.19.7.213",
        "192.168.1.100",
        "192.168.0.100",
        "192.168.18.246",
        "10.0.0.100",
        "10.0.2.2"
    )

    private var currentBaseUrl: String? = null
    private var retrofitInstance: Retrofit? = null

    @Volatile
    private var isInitializing = false

    suspend fun initialize(context: Context) {
        if (isInitializing) {
            Log.d(TAG, "⏳ Inicialización ya en progreso...")
            return
        }

        isInitializing = true
        try {
            currentBaseUrl = detectServerIp(context.applicationContext)
            Log.d(TAG, "🌐 API configurada en: $currentBaseUrl")
            retrofitInstance = createRetrofit(currentBaseUrl!!)
        } finally {
            isInitializing = false
        }
    }

    suspend fun refresh(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().remove(KEY_LAST_IP).apply()
        currentBaseUrl = detectServerIp(context.applicationContext)
        Log.d(TAG, "🔄 API actualizada a: $currentBaseUrl")
        retrofitInstance = createRetrofit(currentBaseUrl!!)
    }

    private fun getRetrofitInstance(): Retrofit {
        if (retrofitInstance == null) {
            Log.w(TAG, "⚠️ Retrofit no inicializado, usando IP por defecto")
            currentBaseUrl = "http://192.168.100.4:$API_PORT/"
            retrofitInstance = createRetrofit(currentBaseUrl!!)
        }
        return retrofitInstance!!
    }

    private suspend fun detectServerIp(context: Context): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "🔍 ========================================")
        Log.d(TAG, "🔍 INICIANDO BÚSQUEDA DE SERVIDOR API")
        Log.d(TAG, "🔍 ========================================")

        val lastIp = getLastWorkingIp(context)
        if (lastIp != null) {
            Log.d(TAG, "📋 Última IP guardada: $lastIp")
            if (testConnection(lastIp)) {
                Log.d(TAG, "✅ ¡Conexión exitosa con IP guardada!")
                return@withContext formatUrl(lastIp)
            } else {
                Log.d(TAG, "❌ IP guardada no responde")
            }
        } else {
            Log.d(TAG, "📋 No hay IP guardada")
        }

        Log.d(TAG, "🔎 Probando IPs comunes...")
        for (ip in COMMON_IPS) {
            Log.d(TAG, "  Probando: $ip")
            if (testConnection(ip)) {
                Log.d(TAG, "✅ ¡SERVIDOR ENCONTRADO EN: $ip!")
                saveLastWorkingIp(context, ip)
                return@withContext formatUrl(ip)
            }
        }

        val localIp = getLocalIpAddress()
        if (localIp != null) {
            Log.d(TAG, "📱 IP del dispositivo: $localIp")
            val subnet = getSubnet(localIp)
            Log.d(TAG, "🌐 Subred detectada: $subnet.0/24")

            val foundIp = scanSubnet(subnet)
            if (foundIp != null) {
                Log.d(TAG, "✅ ¡SERVIDOR ENCONTRADO EN SUBRED: $foundIp!")
                saveLastWorkingIp(context, foundIp)
                return@withContext formatUrl(foundIp)
            }
        } else {
            Log.w(TAG, "⚠️ No se pudo obtener la IP local del dispositivo")
        }

        val fallbackUrl = lastIp?.let { formatUrl(it) } ?: "http://192.168.100.4:$API_PORT/"
        Log.w(TAG, "⚠️ ========================================")
        Log.w(TAG, "⚠️ NO SE ENCONTRÓ SERVIDOR")
        Log.w(TAG, "⚠️ Usando fallback: $fallbackUrl")
        Log.w(TAG, "⚠️ ========================================")
        fallbackUrl
    }

    private fun testConnection(ip: String): Boolean {
        return try {
            val url = URL("http://$ip:$API_PORT$HEALTH_ENDPOINT")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = CONNECTION_TIMEOUT
            connection.readTimeout = CONNECTION_TIMEOUT
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")

            val responseCode = connection.responseCode
            val isSuccess = responseCode in 200..299

            if (isSuccess) {
                Log.d(TAG, "    ✓ HTTP $responseCode - OK")
            } else {
                Log.d(TAG, "    ✗ HTTP $responseCode")
            }

            connection.disconnect()
            isSuccess
        } catch (e: Exception) {
            Log.d(TAG, "    ✗ ${e.message?.take(50) ?: "No responde"}")
            false
        }
    }

    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is java.net.Inet4Address) {
                        val ip = address.hostAddress
                        if (ip != null && !ip.startsWith("127.")) {
                            return ip
                        }
                    }
                }
            }
        } catch (_: Exception) { }
        return null
    }

    private fun getSubnet(ip: String): String {
        val parts = ip.split(".")
        return if (parts.size >= 3) "${parts[0]}.${parts[1]}.${parts[2]}" else "192.168.1"
    }

    private suspend fun scanSubnet(subnet: String): String? = withContext(Dispatchers.IO) {
        Log.d(TAG, "🔎 Escaneando subred: $subnet.*")

        val commonServerIps = listOf(
            1, 2, 100, 101, 102, 103, 104, 105, 110, 111,
            120, 121, 200, 201, 213, 246, 254
        )

        for (i in commonServerIps) {
            val ip = "$subnet.$i"
            Log.d(TAG, "  Probando: $ip")
            if (testConnection(ip)) {
                Log.d(TAG, "  ✅ ¡Encontrado!")
                return@withContext ip
            }
        }

        Log.d(TAG, "  ❌ No se encontró servidor en esta subred")
        null
    }

    private fun formatUrl(ip: String) = "http://$ip:$API_PORT/"

    private fun saveLastWorkingIp(context: Context, ip: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(KEY_LAST_IP, ip).apply()
    }

    private fun getLastWorkingIp(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LAST_IP, null)
    }

    private fun createRetrofit(baseUrl: String): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val slaApiService: SlaApiService by lazy {
        getRetrofitInstance().create(SlaApiService::class.java)
    }

    val apiService: com.example.proyecto1.data.remote.ApiService by lazy {
        getRetrofitInstance().create(com.example.proyecto1.data.remote.ApiService::class.java)
    }

    fun getCurrentBaseUrl(): String? = currentBaseUrl
}
