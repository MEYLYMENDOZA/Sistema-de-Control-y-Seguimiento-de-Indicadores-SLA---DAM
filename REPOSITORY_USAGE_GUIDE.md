# Guía de Uso de Repositorios

Este documento muestra ejemplos prácticos de cómo usar los repositorios implementados en el proyecto.

## AreaRepository

### Obtener todas las áreas
```kotlin
class MiViewModel : ViewModel() {
    private val areaRepo = AreaRepository()
    private val _areas = MutableStateFlow<List<Area>>(emptyList())
    val areas: StateFlow<List<Area>> = _areas
    
    fun cargarAreas() {
        viewModelScope.launch {
            val listaAreas = areaRepo.getAll()
            _areas.value = listaAreas
        }
    }
}
```

### Crear una nueva área
```kotlin
fun crearArea(nombre: String, descripcion: String) {
    viewModelScope.launch {
        val nuevaArea = Area(
            nombreArea = nombre,
            descripcion = descripcion
        )
        val id = areaRepo.create(nuevaArea)
        if (id != null) {
            Log.d("TAG", "Área creada con ID: $id")
        } else {
            Log.e("TAG", "Error al crear área")
        }
    }
}
```

### Actualizar un área
```kotlin
fun actualizarArea(id: String, nombre: String, descripcion: String) {
    viewModelScope.launch {
        val areaActualizada = Area(
            nombreArea = nombre,
            descripcion = descripcion
        )
        val exito = areaRepo.update(id, areaActualizada)
        if (exito) {
            Log.d("TAG", "Área actualizada")
        }
    }
}
```

### Eliminar un área
```kotlin
fun eliminarArea(id: String) {
    viewModelScope.launch {
        val exito = areaRepo.delete(id)
        if (exito) {
            Log.d("TAG", "Área eliminada")
        }
    }
}
```

## UsuarioRepository

### Buscar usuario por Firebase UID
```kotlin
fun cargarUsuarioActual() {
    viewModelScope.launch {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            val usuario = usuarioRepo.getByFirebaseUid(firebaseUser.uid)
            if (usuario != null) {
                _usuarioActual.value = usuario
            }
        }
    }
}
```

### Crear un nuevo usuario (después de registrarse con Firebase Auth)
```kotlin
fun crearUsuario(firebaseUid: String, email: String, username: String, idRol: String) {
    viewModelScope.launch {
        val nuevoUsuario = Usuario(
            username = username,
            correo = email,
            firebaseUid = firebaseUid,
            idRolSistema = idRol,
            idEstadoUsuario = "ACTIVO" // ID del estado activo
        )
        val id = usuarioRepo.create(nuevoUsuario)
        if (id != null) {
            Log.d("TAG", "Usuario creado en Firestore: $id")
        }
    }
}
```

### Buscar usuario por username
```kotlin
fun buscarPorUsername(username: String) {
    viewModelScope.launch {
        val usuario = usuarioRepo.getByUsername(username)
        if (usuario != null) {
            _usuarioEncontrado.value = usuario
        } else {
            _mensaje.value = "Usuario no encontrado"
        }
    }
}
```

## SolicitudRepository

### Obtener todas las solicitudes (últimas 50)
```kotlin
fun cargarSolicitudes() {
    viewModelScope.launch {
        val solicitudes = solicitudRepo.getAll(limit = 50)
        _solicitudes.value = solicitudes
    }
}
```

### Filtrar solicitudes por área
```kotlin
fun cargarSolicitudesPorArea(idArea: String) {
    viewModelScope.launch {
        val solicitudes = solicitudRepo.getByArea(idArea)
        _solicitudes.value = solicitudes
    }
}
```

### Filtrar solicitudes por estado
```kotlin
fun cargarSolicitudesPendientes(idEstadoPendiente: String) {
    viewModelScope.launch {
        val pendientes = solicitudRepo.getByEstado(idEstadoPendiente)
        _solicitudesPendientes.value = pendientes
    }
}
```

### Crear una nueva solicitud
```kotlin
fun crearSolicitud(
    idPersonal: String,
    idRolRegistro: String,
    idSla: String,
    idArea: String,
    resumen: String
) {
    viewModelScope.launch {
        val ahora = Timestamp.now()
        val nuevaSolicitud = Solicitud(
            idPersonal = idPersonal,
            idRolRegistro = idRolRegistro,
            idSla = idSla,
            idArea = idArea,
            idEstadoSolicitud = "PENDIENTE_ID", // ID del estado pendiente
            fechaSolicitud = ahora,
            fechaIngreso = ahora,
            numDiasSla = 5,
            resumenSla = resumen,
            origenDato = "MOBILE_APP",
            creadoPor = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            creadoEn = ahora
        )
        
        val id = solicitudRepo.create(nuevaSolicitud)
        if (id != null) {
            Log.d("TAG", "Solicitud creada: $id")
            _mensaje.value = "Solicitud creada exitosamente"
        } else {
            _mensaje.value = "Error al crear solicitud"
        }
    }
}
```

### Actualizar estado de solicitud
```kotlin
fun cambiarEstadoSolicitud(idSolicitud: String, nuevoEstado: String) {
    viewModelScope.launch {
        val solicitud = solicitudRepo.getById(idSolicitud)
        if (solicitud != null) {
            val actualizada = solicitud.copy(
                idEstadoSolicitud = nuevoEstado,
                actualizadoPor = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            )
            val exito = solicitudRepo.update(idSolicitud, actualizada)
            if (exito) {
                _mensaje.value = "Estado actualizado"
            }
        }
    }
}
```

## PrediccionRepository

### Calcular predicción SLA
```kotlin
class PrediccionViewModel : ViewModel() {
    private val repo = PrediccionRepository()
    
    private val _prediccion = MutableStateFlow<Double?>(null)
    val prediccion: StateFlow<Double?> = _prediccion
    
    private val _slope = MutableStateFlow<Double?>(null)
    val slope: StateFlow<Double?> = _slope
    
    private val _intercept = MutableStateFlow<Double?>(null)
    val intercept: StateFlow<Double?> = _intercept
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    fun calcularPrediccion() {
        viewModelScope.launch {
            try {
                val (pred, m, b) = repo.calcularPrediccion()
                _prediccion.value = pred
                _slope.value = m
                _intercept.value = b
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
```

## Ejemplo completo: Pantalla de gestión de áreas

```kotlin
// ViewModel
class AreasViewModel : ViewModel() {
    private val repo = AreaRepository()
    
    private val _areas = MutableStateFlow<List<Area>>(emptyList())
    val areas: StateFlow<List<Area>> = _areas
    
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando
    
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje
    
    init {
        cargarAreas()
    }
    
    fun cargarAreas() {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val lista = repo.getAll()
                _areas.value = lista
            } catch (e: Exception) {
                _mensaje.value = "Error al cargar áreas: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }
    
    fun crearArea(nombre: String, descripcion: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val nuevaArea = Area(nombreArea = nombre, descripcion = descripcion)
                val id = repo.create(nuevaArea)
                if (id != null) {
                    _mensaje.value = "Área creada exitosamente"
                    cargarAreas() // Recargar lista
                } else {
                    _mensaje.value = "Error al crear área"
                }
            } catch (e: Exception) {
                _mensaje.value = "Error: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }
    
    fun eliminarArea(id: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val exito = repo.delete(id)
                if (exito) {
                    _mensaje.value = "Área eliminada"
                    cargarAreas()
                } else {
                    _mensaje.value = "Error al eliminar"
                }
            } catch (e: Exception) {
                _mensaje.value = "Error: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }
}

// Screen
@Composable
fun AreasScreen(viewModel: AreasViewModel = viewModel()) {
    val areas by viewModel.areas.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Gestión de Áreas", style = MaterialTheme.typography.headlineMedium)
        
        if (cargando) {
            CircularProgressIndicator()
        } else {
            LazyColumn {
                items(areas) { area ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(area.nombreArea, style = MaterialTheme.typography.titleMedium)
                            Text(area.descripcion, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
        
        mensaje?.let {
            Snackbar { Text(it) }
        }
    }
}
```

## Buenas prácticas

1. **Siempre usar try-catch** en las operaciones de repositorio
2. **Mostrar indicadores de carga** mientras se ejecutan operaciones
3. **Verificar null** antes de usar resultados
4. **Usar StateFlow** para exponer datos del ViewModel a la UI
5. **Ejecutar en viewModelScope.launch** para operaciones suspend
6. **Logging** para debugging (usa Log.d/Log.e)
7. **Validar datos** antes de crear/actualizar
8. **Manejar errores de red/Firebase** apropiadamente

## Testing

### Test unitario de repositorio (ejemplo)
```kotlin
@Test
fun `crear area retorna ID valido`() = runTest {
    val repo = AreaRepository()
    val area = Area(nombreArea = "Test", descripcion = "Test desc")
    
    val id = repo.create(area)
    
    assertNotNull(id)
    assertTrue(id!!.isNotEmpty())
}
```

## Recursos adicionales

- [Documentación de Firestore](https://firebase.google.com/docs/firestore)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [StateFlow en Compose](https://developer.android.com/jetpack/compose/state)

