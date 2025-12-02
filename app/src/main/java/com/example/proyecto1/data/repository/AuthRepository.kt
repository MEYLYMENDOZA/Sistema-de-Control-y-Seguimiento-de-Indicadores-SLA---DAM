package com.example.proyecto1.data.repository

import android.util.Log
import com.example.proyecto1.data.remote.api.RetrofitClient
import com.example.proyecto1.data.remote.api.SlaApiService
import com.example.proyecto1.data.remote.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.TimeUnit

/**
 * Repositorio para autenticación y gestión de usuarios
 */
class AuthRepository {

    private val TAG = "AuthRepository"
    // No mantener apiService en una propiedad: usar RetrofitClient.slaApiService por llamada
    private val baseUrl: String
        get() = try { RetrofitClient.baseUrlPublic } catch (_: Exception) { "http://10.0.2.2:5120/" }

    // Candidatos completos para pruebas rápidas (usar IPs conocidas)
    private val FULL_CANDIDATES = listOf(
        "http://172.19.9.109:5120/",
        "http://192.168.100.4:5120/",
        "http://172.19.5.121:5120/",
        "http://10.0.2.2:5120/"
    )

    // Valores comunes de últimos octetos para probar en la misma subred
    private val COMMON_HOSTS = listOf("1", "4", "100", "109", "121")

    private val okHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(6, TimeUnit.SECONDS)
            .build()
    }

    private val gson = Gson()

    /**
     * Realiza el login del usuario
     */
    suspend fun login(username: String, password: String): Result<LoginResponseDto> {
        return withContext(Dispatchers.IO) {
            try {
                // Forzar detección rápida de base URL en la subred antes de llamar
                RetrofitClient.detectAndSetBaseSync()
                Log.d(TAG, "🔐 Intentando login para: $username")
                val request = LoginRequestDto(username, password)
                val response = RetrofitClient.slaApiService.login(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        Log.d(TAG, "✅ Login exitoso para: $username")
                        Result.success(body)
                    } else {
                        val message = body?.message ?: "Credenciales inválidas"
                        Log.w(TAG, "⚠️ Login fallido: $message")
                        Result.failure(Exception(message))
                    }
                } else {
                    val errorMsg = "Error ${response.code()}: ${response.message()}"
                    Log.e(TAG, "❌ Error en login: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción en login", e)
                Result.failure(Exception("No se pudo conectar al servidor: ${e.message}"))
            }
        }
    }

    /**
     * Realiza el logout del usuario
     */
    suspend fun logout(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                RetrofitClient.detectAndSetBaseSync()
                val response = RetrofitClient.slaApiService.logout()
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Error al cerrar sesión"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Obtiene la lista de todos los usuarios
     */
    suspend fun obtenerUsuarios(): Result<ListaUsuariosResponseDto> {
        return withContext(Dispatchers.IO) {
            try {
                RetrofitClient.detectAndSetBaseSync()
                Log.d(TAG, "📋 Obteniendo lista de usuarios desde /api/User...")
                val response = RetrofitClient.slaApiService.obtenerUsuarios()

                if (response.isSuccessful) {
                    val usuarios = response.body()
                    if (usuarios != null) {
                        Log.d(TAG, "✅ ${usuarios.size} usuarios obtenidos")
                        Log.d(TAG, "   Usuarios: ${usuarios.joinToString { it.username }}")

                        // Convertir array a objeto wrapper
                        val wrapped = ListaUsuariosResponseDto(
                            success = true,
                            usuarios = usuarios,
                            total = usuarios.size
                        )
                        Result.success(wrapped)
                    } else {
                        Log.w(TAG, "⚠️ Respuesta vacía del servidor")
                        Result.failure(Exception("Respuesta vacía del servidor"))
                    }
                } else {
                    val errorMsg = "Error ${response.code()}: ${response.message()}"
                    Log.e(TAG, "❌ $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al obtener usuarios: ${e.message}", e)
                Result.failure(Exception("No se pudo conectar al servidor: ${e.message}"))
            }
        }
    }

    /**
     * Crea un nuevo usuario
     */
    suspend fun crearUsuario(usuario: CrearUsuarioDto): Result<UsuarioDto> {
        return withContext(Dispatchers.IO) {
            try {
                RetrofitClient.detectAndSetBaseSync()
                Log.d(TAG, "➕ Creando usuario: ${usuario.username}")
                val response = RetrofitClient.slaApiService.crearUsuario(usuario)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Log.d(TAG, "✅ Usuario creado: ${body.username}")
                        Result.success(body)
                    } else {
                        Result.failure(Exception("Respuesta vacía del servidor"))
                    }
                } else {
                    Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al crear usuario", e)
                Result.failure(Exception("No se pudo crear el usuario: ${e.message}"))
            }
        }
    }

    /**
     * Actualiza un usuario existente
     */
    suspend fun actualizarUsuario(id: Int, usuario: CrearUsuarioDto): Result<UsuarioDto> {
        return withContext(Dispatchers.IO) {
            try {
                RetrofitClient.detectAndSetBaseSync()
                Log.d(TAG, "✏️ Actualizando usuario ID: $id")
                val response = RetrofitClient.slaApiService.actualizarUsuario(id, usuario)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Log.d(TAG, "✅ Usuario actualizado: ${body.username}")
                        Result.success(body)
                    } else {
                        Result.failure(Exception("Respuesta vacía del servidor"))
                    }
                } else {
                    Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al actualizar usuario", e)
                Result.failure(Exception("No se pudo actualizar el usuario: ${e.message}"))
            }
        }
    }

    /**
     * Elimina un usuario
     */
    suspend fun eliminarUsuario(id: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                RetrofitClient.detectAndSetBaseSync()
                Log.d(TAG, "🗑️ Eliminando usuario ID: $id")
                val response = RetrofitClient.slaApiService.eliminarUsuario(id)

                if (response.isSuccessful) {
                    Log.d(TAG, "✅ Usuario eliminado")
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al eliminar usuario", e)
                Result.failure(Exception("No se pudo eliminar el usuario: ${e.message}"))
            }
        }
    }

    /**
     * Obtiene todos los roles del sistema
     * Si la llamada via Retrofit falla (404), intenta rutas alternas sobre la misma base URL
     */
    suspend fun obtenerRoles(): Result<List<RolSistemaDto>> {
        return withContext(Dispatchers.IO) {
            try {
                RetrofitClient.detectAndSetBaseSync()
                val response = RetrofitClient.slaApiService.obtenerRoles()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) return@withContext Result.success(body)
                    return@withContext Result.failure(Exception("Respuesta vacía"))
                }

                Log.w(TAG, "obtenerRoles() retrofit no exitoso: ${response.code()} - intentar rutas alternas")
                val alternatesRelative = listOf(
                    "api/User/roles",
                    "api/roles",
                    "api/roles-sistema",
                    "api/roles_sistema"
                )

                // 1) intentar rutas relativas con base actual
                for (path in alternatesRelative) {
                    val url = if (baseUrl.endsWith("/")) baseUrl + path else "$baseUrl/$path"
                    try {
                        val req = Request.Builder().url(url).get().build()
                        val resp = okHttp.newCall(req).execute()
                        val code = resp.code
                        val bodyStr = resp.body?.string()
                        resp.close()

                        Log.d(TAG, "Intento alterno GET $url -> $code")

                        if (code in 200..299 && !bodyStr.isNullOrBlank()) {
                            val listType = object : TypeToken<List<RolSistemaDto>>() {}.type
                            val roles: List<RolSistemaDto> = gson.fromJson(bodyStr, listType)
                            return@withContext Result.success(roles)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error probando ruta alterna relativa $path: ${e.message}")
                    }
                }

                // 2) intentar rutas completas con IPs candidatas (útil en dispositivo físico)
                // construir lista de candidatos ampliada (FULL_CANDIDATES + candidatos dinámicos en la misma subred)
                val dynamicCandidates = buildDynamicCandidates()
                val allCandidates = (FULL_CANDIDATES + dynamicCandidates).distinct()

                for (candidateBase in allCandidates) {
                    for (path in listOf("api/User/roles", "api/roles", "api/roles-sistema", "api/roles_sistema")) {
                        val url = if (candidateBase.endsWith("/")) candidateBase + path else "$candidateBase/$path"
                        try {
                            val req = Request.Builder().url(url).get().build()
                            val resp = okHttp.newCall(req).execute()
                            val code = resp.code
                            val bodyStr = resp.body?.string()
                            resp.close()

                            Log.d(TAG, "Intento full GET $url -> $code")
                            if (code in 200..299 && !bodyStr.isNullOrBlank()) {
                                val listType = object : TypeToken<List<RolSistemaDto>>() {}.type
                                val roles: List<RolSistemaDto> = gson.fromJson(bodyStr, listType)
                                return@withContext Result.success(roles)
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Error probando full candidate $url: ${e.message}")
                        }
                    }
                }

                return@withContext Result.failure(Exception("Error ${response.code()}"))
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error en obtenerRoles", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Construye candidatos dinámicos basados en la IP local del dispositivo
     * Intenta formar "http://{prefix}.{host}:5120/" donde prefix = first 3 octets de la IP local
     */
    private fun buildDynamicCandidates(): List<String> {
        try {
            val localIp = getLocalIpv4Address() ?: return emptyList()
            val prefix = localIp.substringBeforeLast('.')
            val deviceLast = localIp.substringAfterLast('.')
            val hosts = (COMMON_HOSTS + listOf(deviceLast)).distinct()
            return hosts.map { host -> "http://$prefix.$host:5120/" }
        } catch (t: Throwable) {
            Log.w(TAG, "buildDynamicCandidates fallo: ${t.message}")
            return emptyList()
        }
    }

    private fun getLocalIpv4Address(): String? {
        try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val ni = interfaces.nextElement()
                if (!ni.isUp || ni.isLoopback) continue
                val addrs = ni.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (addr is java.net.Inet4Address && !addr.isLoopbackAddress) {
                        val ip = addr.hostAddress
                        // ignorar APIPA y link-local
                        if (!ip.startsWith("169.254.")) return ip
                    }
                }
            }
        } catch (t: Throwable) {
            Log.w(TAG, "getLocalIpv4Address error: ${t.message}")
        }
        return null
    }

    /**
     * Obtiene todos los estados de usuario
     * Similar a obtenerRoles: reintenta rutas alternativas en caso de fallo
     */
    suspend fun obtenerEstadosUsuario(): Result<List<EstadoUsuarioDto>> {
        return withContext(Dispatchers.IO) {
            try {
                RetrofitClient.detectAndSetBaseSync()
                val response = RetrofitClient.slaApiService.obtenerEstadosUsuario()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) return@withContext Result.success(body)
                    return@withContext Result.failure(Exception("Respuesta vacía"))
                }

                Log.w(TAG, "obtenerEstadosUsuario() retrofit no exitoso: ${response.code()} - intentar rutas alternas")
                val alternatesRelative = listOf(
                    "api/User/estados",
                    "api/estados-usuario",
                    "api/estados"
                )

                for (path in alternatesRelative) {
                    val url = if (baseUrl.endsWith("/")) baseUrl + path else "$baseUrl/$path"
                    try {
                        val req = Request.Builder().url(url).get().build()
                        val resp = okHttp.newCall(req).execute()
                        val code = resp.code
                        val bodyStr = resp.body?.string()
                        resp.close()

                        Log.d(TAG, "Intento alterno GET $url -> $code")

                        if (code in 200..299 && !bodyStr.isNullOrBlank()) {
                            val listType = object : TypeToken<List<EstadoUsuarioDto>>() {}.type
                            val estados: List<EstadoUsuarioDto> = gson.fromJson(bodyStr, listType)
                            return@withContext Result.success(estados)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error probando ruta alterna relativa $path: ${e.message}")
                    }
                }

                val dynamicCandidates = buildDynamicCandidates()
                val allCandidates = (FULL_CANDIDATES + dynamicCandidates).distinct()

                for (candidateBase in allCandidates) {
                    for (path in listOf("api/User/estados", "api/estados", "api/estados-usuario")) {
                        val url = if (candidateBase.endsWith("/")) candidateBase + path else "$candidateBase/$path"
                        try {
                            val req = Request.Builder().url(url).get().build()
                            val resp = okHttp.newCall(req).execute()
                            val code = resp.code
                            val bodyStr = resp.body?.string()
                            resp.close()

                            Log.d(TAG, "Intento full GET $url -> $code")
                            if (code in 200..299 && !bodyStr.isNullOrBlank()) {
                                val listType = object : TypeToken<List<EstadoUsuarioDto>>() {}.type
                                val estados: List<EstadoUsuarioDto> = gson.fromJson(bodyStr, listType)
                                return@withContext Result.success(estados)
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Error probando full candidate $url: ${e.message}")
                        }
                    }
                }

                return@withContext Result.failure(Exception("Error ${response.code()}"))
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error en obtenerEstadosUsuario", e)
                Result.failure(e)
            }
        }
    }
}
