package com.example.proyecto1.data.repository

import android.util.Log
import com.example.proyecto1.data.remote.api.RetrofitClient
import com.example.proyecto1.data.remote.api.SlaApiService
import com.example.proyecto1.data.remote.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio para autenticación y gestión de usuarios
 */
class AuthRepository {

    private val TAG = "AuthRepository"
    private val apiService: SlaApiService = RetrofitClient.slaApiService

    /**
     * Realiza el login del usuario
     */
    suspend fun login(username: String, password: String): Result<LoginResponseDto> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔐 Intentando login para: $username")
                val request = LoginRequestDto(username, password)
                val response = apiService.login(request)

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
                val response = apiService.logout()
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
                Log.d(TAG, "📋 Obteniendo lista de usuarios desde /api/User...")
                val response = apiService.obtenerUsuarios()

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
                Log.d(TAG, "➕ Creando usuario: ${usuario.username}")
                val response = apiService.crearUsuario(usuario)

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
                Log.d(TAG, "✏️ Actualizando usuario ID: $id")
                val response = apiService.actualizarUsuario(id, usuario)

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
                Log.d(TAG, "🗑️ Eliminando usuario ID: $id")
                val response = apiService.eliminarUsuario(id)

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
     */
    suspend fun obtenerRoles(): Result<List<RolSistemaDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.obtenerRoles()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Result.success(body)
                    } else {
                        Result.failure(Exception("Respuesta vacía"))
                    }
                } else {
                    Result.failure(Exception("Error ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Obtiene todos los estados de usuario
     */
    suspend fun obtenerEstadosUsuario(): Result<List<EstadoUsuarioDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.obtenerEstadosUsuario()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Result.success(body)
                    } else {
                        Result.failure(Exception("Respuesta vacía"))
                    }
                } else {
                    Result.failure(Exception("Error ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

