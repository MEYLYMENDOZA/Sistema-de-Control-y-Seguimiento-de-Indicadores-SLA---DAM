package com.example.proyecto1.di

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.example.proyecto1.data.remote.api.SlaApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.NetworkInterface
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TAG = "NetworkModule"

    // Función para detectar la IP del servidor automáticamente
    private fun detectServerIp(context: Context): String {
        return try {
            // Intentar obtener el gateway de WiFi primero
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val dhcp = wifiManager.dhcpInfo

            if (dhcp != null && dhcp.gateway != 0) {
                val gateway = String.format(
                    java.util.Locale.US,
                    "%d.%d.%d.%d",
                    (dhcp.gateway and 0xff),
                    (dhcp.gateway shr 8 and 0xff),
                    (dhcp.gateway shr 16 and 0xff),
                    (dhcp.gateway shr 24 and 0xff)
                )

                if (gateway != "0.0.0.0") {
                    Log.i(TAG, "✅ Gateway WiFi detectado: $gateway")
                    return gateway
                }
            }

            // Si no hay gateway válido, buscar la IP del dispositivo
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (intf in interfaces) {
                val addrs = intf.inetAddresses
                for (addr in addrs) {
                    val hostAddress = addr.hostAddress ?: continue

                    if (!addr.isLoopbackAddress && hostAddress.indexOf(':') < 0) {
                        Log.d(TAG, "IP del dispositivo detectada: $hostAddress")

                        // Si es IP local, intentar detectar el servidor en la misma subred
                        when {
                            hostAddress.startsWith("192.168.") || hostAddress.startsWith("172.") -> {
                                // Usar la misma subred pero cambiar el último octeto
                                val subnet = hostAddress.substringBeforeLast(".")
                                val serverIp = "$subnet.4"
                                Log.i(TAG, "🔍 Usando IP del mismo segmento: $serverIp")
                                return serverIp
                            }
                        }
                    }
                }
            }

            // Si no se detectó ninguna IP, usar localhost del emulador
            Log.w(TAG, "⚠️ No se detectó IP válida, usando emulador por defecto")
            "10.0.2.2"
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error detectando IP: ${e.message}")
            "10.0.2.2"
        }
    }

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(
            @SuppressLint("CustomX509TrustManager")
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        )
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory = sslContext.socketFactory

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        @ApplicationContext context: Context
    ): Retrofit {
        // Leer configuración desde strings.xml
        val configuredIp = try {
            context.getString(context.resources.getIdentifier("server_ip", "string", context.packageName))
        } catch (e: Exception) {
            "auto"
        }

        val configuredPort = try {
            context.getString(context.resources.getIdentifier("server_port", "string", context.packageName))
        } catch (e: Exception) {
            "5120"
        }

        // Si está en "auto", detectar automáticamente; si no, usar la IP configurada
        val serverIp = if (configuredIp == "auto") {
            detectServerIp(context)
        } else {
            Log.i(TAG, "📌 Usando IP configurada manualmente: $configuredIp")
            configuredIp
        }

        val baseUrl = "http://$serverIp:$configuredPort/"
        Log.i(TAG, "📡 URL Base final: $baseUrl")

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideSlaApiService(retrofit: Retrofit): SlaApiService {
        return retrofit.create(SlaApiService::class.java)
    }
}
