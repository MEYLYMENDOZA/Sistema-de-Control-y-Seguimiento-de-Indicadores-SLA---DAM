package com.example.proyecto1.data.remote.api

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.net.NetworkInterface
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit con DETECCIÓN AUTOMÁTICA de IP del servidor
 */
object RetrofitClient {

    private const val TAG = "RetrofitClient_API"
    private const val API_PORT = 5120
    private const val HEALTH_ENDPOINT = "/api/reporte/tipos-sla-disponibles"
    private const val PREFS_NAME = "api_config"
    private const val KEY_LAST_IP = "last_working_ip"
    private const val CONNECTION_TIMEOUT = 2000

    private val COMMON_IPS = listOf(
        "172.19.7.121",
        "172.19.5.121",
        "192.168.100.4",
        "192.168.1.100",
        "192.168.0.100",
        "10.0.0.100"
    )

    private var currentBaseUrl: String? = null
    private var retrofitInstance: Retrofit? = null
    @Volatile
    private var isInitializing = false

    /**
     * Inicializa el cliente de forma asíncrona (suspendible)
     * Llámalo desde una coroutine
     */
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

    /**
     * Refresca la conexión buscando el servidor de nuevo
     */
    suspend fun refresh(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            remove(KEY_LAST_IP)
        }
        currentBaseUrl = detectServerIp(context.applicationContext)
        Log.d(TAG, "🔄 API actualizada a: $currentBaseUrl")
        retrofitInstance = createRetrofit(currentBaseUrl!!)
    }

    /**
     * Obtiene la instancia de Retrofit, inicializándola con IP por defecto si es necesario
     */
    private fun getRetrofitInstance(): Retrofit {
        if (retrofitInstance == null) {
            Log.w(TAG, "⚠️ Retrofit no inicializado, usando IP por defecto")
            currentBaseUrl = "http://172.19.5.121:$API_PORT/"
            retrofitInstance = createRetrofit(currentBaseUrl!!)
        }
        return retrofitInstance!!
    }

    private suspend fun detectServerIp(context: Context): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "🔍 Buscando servidor API en la red local...")

        val lastIp = getLastWorkingIp(context)
        if (lastIp != null && testConnection(lastIp)) {
            Log.d(TAG, "✅ Usando última IP exitosa: $lastIp")
            return@withContext formatUrl(lastIp)
        }

        for (ip in COMMON_IPS) {
            if (testConnection(ip)) {
                Log.d(TAG, "✅ Servidor encontrado en: $ip")
                saveLastWorkingIp(context, ip)
                return@withContext formatUrl(ip)
            }
        }

        val localIp = getLocalIpAddress()
        if (localIp != null) {
            Log.d(TAG, "📱 IP del dispositivo: $localIp")
            val subnet = getSubnet(localIp)
            val foundIp = scanSubnet(subnet)

            if (foundIp != null) {
                Log.d(TAG, "✅ Servidor encontrado en subred: $foundIp")
                saveLastWorkingIp(context, foundIp)
                return@withContext formatUrl(foundIp)
            }
        }

        Log.w(TAG, "⚠️ No se pudo detectar el servidor")
        lastIp?.let { formatUrl(it) } ?: "http://172.19.5.121:$API_PORT/"
    }

    private fun testConnection(ip: String): Boolean {
        return try {
            val url = URL("http://$ip:$API_PORT$HEALTH_ENDPOINT")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = CONNECTION_TIMEOUT
            connection.readTimeout = CONNECTION_TIMEOUT
            connection.requestMethod = "GET"
            val responseCode = connection.responseCode
            connection.disconnect()
            responseCode in 200..299
        } catch (_: Exception) {
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
        val commonServerIps = listOf(1, 100, 101, 102, 121, 200, 201, 254)
        for (i in commonServerIps) {
            val ip = "$subnet.$i"
            if (testConnection(ip)) {
                return@withContext ip
            }
        }
        null
    }

    private fun formatUrl(ip: String) = "http://$ip:$API_PORT/"

    private fun saveLastWorkingIp(context: Context, ip: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_LAST_IP, ip)
        }
    }

    private fun getLastWorkingIp(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LAST_IP, null)
    }

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val slaApiService: SlaApiService by lazy {
        getRetrofitInstance().create(SlaApiService::class.java)
    }

    fun getCurrentBaseUrl(): String? = currentBaseUrl
}
