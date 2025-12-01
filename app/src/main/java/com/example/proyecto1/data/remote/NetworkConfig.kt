package com.example.proyecto1.data.remote

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.NetworkInterface
import java.net.URL

/**
 * Configuración automática de red que detecta el servidor API
 * en cualquier red WiFi sin necesidad de cambiar código
 */
object NetworkConfig {
    private const val TAG = "NetworkConfig"
    private const val API_PORT = 5120
    private const val HEALTH_ENDPOINT = "/api/reporte/tipos-sla-disponibles"
    private const val PREFS_NAME = "api_config"
    private const val KEY_LAST_IP = "last_working_ip"
    private const val CONNECTION_TIMEOUT = 2000 // 2 segundos

    // IPs comunes para probar primero (más rápido)
    private val COMMON_IPS = listOf(
        "172.19.5.121",     // Red actual
        "192.168.100.4",    // Red anterior
        "192.168.1.100",    // Común en redes domésticas
        "192.168.0.100",    // Común en redes domésticas
        "10.0.0.100"        // Común en redes corporativas
    )

    /**
     * Obtiene la URL base de la API automáticamente
     * 1. Intenta la última IP exitosa
     * 2. Busca en IPs comunes
     * 3. Escanea la subred local
     */
    suspend fun getApiBaseUrl(context: Context): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "🔍 Buscando servidor API en la red local...")

        // 1. Intentar última IP exitosa
        val lastIp = getLastWorkingIp(context)
        if (lastIp != null && testConnection(lastIp)) {
            Log.d(TAG, "✅ Usando última IP exitosa: $lastIp")
            return@withContext formatUrl(lastIp)
        }

        // 2. Probar IPs comunes
        for (ip in COMMON_IPS) {
            if (testConnection(ip)) {
                Log.d(TAG, "✅ Servidor encontrado en: $ip")
                saveLastWorkingIp(context, ip)
                return@withContext formatUrl(ip)
            }
        }

        // 3. Escanear subred local
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

        // 4. Fallback a última IP conocida o localhost
        Log.w(TAG, "⚠️ No se pudo detectar el servidor, usando fallback")
        lastIp?.let { formatUrl(it) } ?: "http://172.19.5.121:$API_PORT/"
    }

    /**
     * Prueba si el servidor está disponible en una IP
     */
    private fun testConnection(ip: String): Boolean {
        return try {
            val url = URL("http://$ip:$API_PORT$HEALTH_ENDPOINT")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = CONNECTION_TIMEOUT
            connection.readTimeout = CONNECTION_TIMEOUT
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            connection.disconnect()

            val isSuccess = responseCode in 200..299
            if (isSuccess) {
                Log.d(TAG, "✓ Conexión exitosa a $ip (HTTP $responseCode)")
            }
            isSuccess
        } catch (e: Exception) {
            // Silencioso, es normal que falle en muchas IPs
            false
        }
    }

    /**
     * Obtiene la IP local del dispositivo Android
     */
    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses

                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()

                    // Solo IPv4 y no loopback
                    if (!address.isLoopbackAddress && address is java.net.Inet4Address) {
                        val ip = address.hostAddress
                        if (ip != null && !ip.startsWith("127.")) {
                            return ip
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo IP local: ${e.message}")
        }
        return null
    }

    /**
     * Extrae la subred de una IP (ej: 192.168.1.100 -> 192.168.1)
     */
    private fun getSubnet(ip: String): String {
        val parts = ip.split(".")
        return if (parts.size >= 3) {
            "${parts[0]}.${parts[1]}.${parts[2]}"
        } else {
            "192.168.1" // Fallback
        }
    }

    /**
     * Escanea la subred buscando el servidor
     * Optimizado: solo prueba IPs comunes para servidores
     */
    private suspend fun scanSubnet(subnet: String): String? = withContext(Dispatchers.IO) {
        Log.d(TAG, "🔍 Escaneando subred: $subnet.x")

        // Rango común para servidores locales
        val commonServerIps = listOf(1, 100, 101, 102, 200, 201, 254)

        for (i in commonServerIps) {
            val ip = "$subnet.$i"
            if (testConnection(ip)) {
                return@withContext ip
            }
        }

        null
    }

    /**
     * Formatea la URL completa
     */
    private fun formatUrl(ip: String): String {
        return "http://$ip:$API_PORT/"
    }

    /**
     * Guarda la última IP exitosa en SharedPreferences
     */
    private fun saveLastWorkingIp(context: Context, ip: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_LAST_IP, ip)
        }
    }

    /**
     * Obtiene la última IP exitosa guardada
     */
    private fun getLastWorkingIp(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LAST_IP, null)
    }

    /**
     * Fuerza la actualización de la IP (útil si el usuario cambia de red)
     */
    suspend fun refreshApiUrl(context: Context): String = withContext(Dispatchers.IO) {
        // Limpia la caché y busca de nuevo
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            remove(KEY_LAST_IP)
        }

        getApiBaseUrl(context)
    }
}

