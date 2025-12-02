package com.example.proyecto1.presentation.usuarios

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.repository.AuthRepository
import com.example.proyecto1.data.remote.dto.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UsuariosUiState(
    val isLoading: Boolean = false,
    val usuarios: List<UsuarioDto> = emptyList(),
    val usuariosFiltrados: List<UsuarioDto> = emptyList(),
    val total: Int = 0,
    val error: String? = null,
    val roles: List<RolSistemaDto> = emptyList(),
    val estados: List<EstadoUsuarioDto> = emptyList(),
    val mostrarFormulario: Boolean = false,
    val usuarioEnEdicion: UsuarioDto? = null,
    val terminoBusqueda: String = ""
)

class UsuariosViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val TAG = "UsuariosViewModel"

    private val _uiState = MutableStateFlow(UsuariosUiState())
    val uiState: StateFlow<UsuariosUiState> = _uiState

    init {
        cargarDatosIniciales()
    }

    private fun cargarDatosIniciales() {
        cargarUsuarios()
        cargarRoles()
        cargarEstados()
    }

    fun cargarUsuarios() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                Log.d(TAG, "📋 Cargando usuarios...")
                val result = authRepository.obtenerUsuarios()

                result.onSuccess { response ->
                    Log.d(TAG, "✅ ${response.total} usuarios cargados")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        usuarios = response.usuarios,
                        usuariosFiltrados = response.usuarios,
                        total = response.total,
                        error = null
                    )
                    aplicarFiltro()
                }.onFailure { exception ->
                    Log.e(TAG, "❌ Error al cargar usuarios", exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar usuarios"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción al cargar usuarios", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No se pudo conectar con el servidor"
                )
            }
        }
    }

    private fun cargarRoles() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "📊 Cargando roles del sistema...")
                val result = authRepository.obtenerRoles()
                result.onSuccess { roles ->
                    Log.d(TAG, "✅ ${roles.size} roles cargados")
                    roles.forEach { rol ->
                        Log.d(TAG, "  - Rol ID: ${rol.idRolSistema}, Código: ${rol.codigo}, Nombre: ${rol.nombre}")
                    }
                    _uiState.value = _uiState.value.copy(roles = roles)
                }.onFailure { exception ->
                    Log.e(TAG, "❌ Error al cargar roles: ${exception.message}", exception)
                    // Si falla, usar roles hardcodeados
                    Log.d(TAG, "⚠️ Usando roles por defecto (hardcoded)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción al cargar roles", e)
                // Continuar con roles hardcodeados en el formulario
            }
        }
    }

    private fun cargarEstados() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "📊 Cargando estados de usuario...")
                val result = authRepository.obtenerEstadosUsuario()
                result.onSuccess { estados ->
                    Log.d(TAG, "✅ ${estados.size} estados cargados")
                    _uiState.value = _uiState.value.copy(estados = estados)
                }.onFailure { exception ->
                    Log.e(TAG, "❌ Error al cargar estados: ${exception.message}", exception)
                    // Si falla, continuar con estado por defecto (Activo = 1)
                    Log.d(TAG, "⚠️ Usando estado por defecto (Activo = 1)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción al cargar estados", e)
                // Continuar con estado por defecto
            }
        }
    }

    fun mostrarFormularioCrear() {
        _uiState.value = _uiState.value.copy(
            mostrarFormulario = true,
            usuarioEnEdicion = null
        )
    }

    fun mostrarFormularioEditar(usuario: UsuarioDto) {
        _uiState.value = _uiState.value.copy(
            mostrarFormulario = true,
            usuarioEnEdicion = usuario
        )
    }

    fun cerrarFormulario() {
        _uiState.value = _uiState.value.copy(
            mostrarFormulario = false,
            usuarioEnEdicion = null
        )
    }

    fun crearUsuario(usuario: CrearUsuarioDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                Log.d(TAG, "➕ Creando usuario: ${usuario.username}")
                val result = authRepository.crearUsuario(usuario)

                result.onSuccess {
                    Log.d(TAG, "✅ Usuario creado exitosamente")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        mostrarFormulario = false,
                        error = null
                    )
                    cargarUsuarios()
                }.onFailure { exception ->
                    Log.e(TAG, "❌ Error al crear usuario", exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al crear usuario"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción al crear usuario", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No se pudo conectar con el servidor"
                )
            }
        }
    }

    fun actualizarUsuario(id: Int, usuario: CrearUsuarioDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                Log.d(TAG, "✏️ Actualizando usuario ID: $id")
                val result = authRepository.actualizarUsuario(id, usuario)

                result.onSuccess {
                    Log.d(TAG, "✅ Usuario actualizado exitosamente")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        mostrarFormulario = false,
                        usuarioEnEdicion = null,
                        error = null
                    )
                    cargarUsuarios()
                }.onFailure { exception ->
                    Log.e(TAG, "❌ Error al actualizar usuario", exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al actualizar usuario"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción al actualizar usuario", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No se pudo conectar con el servidor"
                )
            }
        }
    }

    fun eliminarUsuario(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                Log.d(TAG, "🗑️ Eliminando usuario ID: $id")
                val result = authRepository.eliminarUsuario(id)

                result.onSuccess {
                    Log.d(TAG, "✅ Usuario eliminado exitosamente")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                    cargarUsuarios()
                }.onFailure { exception ->
                    Log.e(TAG, "❌ Error al eliminar usuario", exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al eliminar usuario"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción al eliminar usuario", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No se pudo conectar con el servidor"
                )
            }
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun buscarUsuarios(termino: String) {
        _uiState.value = _uiState.value.copy(terminoBusqueda = termino)
        aplicarFiltro()
    }

    private fun aplicarFiltro() {
        val termino = _uiState.value.terminoBusqueda.lowercase().trim()

        val filtrados = if (termino.isEmpty()) {
            _uiState.value.usuarios
        } else {
            _uiState.value.usuarios.filter { usuario ->
                val nombres = usuario.personal?.nombres?.lowercase() ?: ""
                val apellidos = usuario.personal?.apellidos?.lowercase() ?: ""
                val nombreCompleto = "$nombres $apellidos".trim()
                val username = usuario.username.lowercase()
                val correo = usuario.correo.lowercase()

                // Buscar en: nombre, apellido, nombre completo, username o correo
                nombres.contains(termino) ||
                apellidos.contains(termino) ||
                nombreCompleto.contains(termino) ||
                username.contains(termino) ||
                correo.contains(termino)
            }
        }

        _uiState.value = _uiState.value.copy(usuariosFiltrados = filtrados)
        Log.d(TAG, "🔍 Búsqueda '$termino': ${filtrados.size} de ${_uiState.value.usuarios.size} usuarios")
    }
}

class UsuariosViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UsuariosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UsuariosViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

