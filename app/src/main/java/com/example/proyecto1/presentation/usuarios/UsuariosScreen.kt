package com.example.proyecto1.presentation.usuarios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto1.data.remote.dto.CrearUsuarioDto
import com.example.proyecto1.data.remote.dto.UsuarioDto
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuariosScreen(
    viewModel: UsuariosViewModel = viewModel(factory = UsuariosViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Usuarios") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header con título
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Administración de Usuarios",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Gestiona los usuarios registrados en el sistema",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Buscador
            OutlinedTextField(
                value = uiState.terminoBusqueda,
                onValueChange = { viewModel.buscarUsuarios(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nombre, apellido, usuario o correo...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón Agregar Usuario (Azul)
            Button(
                onClick = { viewModel.mostrarFormularioCrear() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Agregar Usuario",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de Usuarios
            Text(
                "Lista de Usuarios",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Usuarios registrados en el sistema",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Contenido
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF2196F3))
                    }
                }
                uiState.usuariosFiltrados.isEmpty() -> {
                    EmptyUsuariosState(hayBusqueda = uiState.terminoBusqueda.isNotEmpty())
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.usuariosFiltrados) { usuario ->
                            UsuarioCard(
                                usuario = usuario,
                                roles = uiState.roles,
                                onEdit = { viewModel.mostrarFormularioEditar(usuario) },
                                onDelete = { viewModel.eliminarUsuario(usuario.idUsuario) }
                            )
                        }
                    }
                }
            }
        }

        // Formulario de creación/edición
        if (uiState.mostrarFormulario) {
            FormularioUsuarioDialog(
                usuario = uiState.usuarioEnEdicion,
                roles = uiState.roles,
                onDismiss = { viewModel.cerrarFormulario() },
                onSave = { usuarioDto ->
                    if (uiState.usuarioEnEdicion != null) {
                        viewModel.actualizarUsuario(uiState.usuarioEnEdicion!!.idUsuario, usuarioDto)
                    } else {
                        viewModel.crearUsuario(usuarioDto)
                    }
                },
                onRetryRoles = { viewModel.reintentarCargarRoles() }
            )
        }

        // Mostrar error si existe
        uiState.error?.let { error ->
            AlertDialog(
                onDismissRequest = { viewModel.limpiarError() },
                title = { Text("Error", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = { viewModel.limpiarError() }) { Text("OK") }
                }
            )
        }
    }
}

@Composable
fun UsuarioCard(
    usuario: UsuarioDto,
    roles: List<com.example.proyecto1.data.remote.dto.RolSistemaDto> = emptyList(),
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // Log para debug
    android.util.Log.d("UsuarioCard", "Renderizando usuario: ${usuario.username}, Personal: ${usuario.personal?.nombres ?: "NULL"}")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // DTO tiene username y correo como String (non-nullable), no necesitamos cast
                val username = usuario.username
                val correo = usuario.correo

                // Avatar con inicial
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2196F3)),
                    contentAlignment = Alignment.Center
                ) {
                    val inicial = when {
                        username.isNotBlank() -> username.first().uppercaseChar().toString()
                        correo.isNotBlank() -> correo.first().uppercaseChar().toString()
                        else -> "U"
                    }

                    Text(
                        text = inicial,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    // Nombre completo desde Personal (si existe)
                    val personal = usuario.personal
                    val nombres = personal?.nombres?.trim() ?: ""
                    val apellidos = personal?.apellidos?.trim() ?: ""

                    val nombreCompleto = buildString {
                        if (nombres.isNotBlank()) append(nombres)
                        if (nombres.isNotBlank() && apellidos.isNotBlank()) append(" ")
                        if (apellidos.isNotBlank()) append(apellidos)
                    }.trim()

                    if (nombreCompleto.isNotBlank()) {
                        // Mostrar nombre completo como título
                        Text(
                            text = nombreCompleto,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        // Username como subtítulo
                        if (username.isNotBlank()) {
                            Text(
                                text = "@$username",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF666666),
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        // Si no hay Personal, mostrar username como título
                        Text(
                            text = username.ifBlank { "Usuario" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Email siempre visible
                    if (correo.isNotBlank()) {
                        Text(
                            text = correo,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666)
                        )
                    }

                    // Resolver nombre de rol
                    val rolDisplay = usuario.rolNombre?.takeIf { it.isNotBlank() }
                        ?: roles.find { it.idRolSistema == usuario.idRolSistema }?.nombre
                        ?: "Sin rol"

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF2196F3).copy(alpha = 0.1f),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = rolDisplay,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2196F3),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Fecha de creación
                    usuario.creadoEn?.let { fecha ->
                        Text(
                            text = "Creado: ${formatearFecha(fecha)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Botones de acción
            Row {
                IconButton(
                    onClick = onEdit,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color(0xFF2196F3)
                    )
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color(0xFFE53935)
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Desactivar")
                }
            }
        }
    }
}

@Composable
fun EmptyUsuariosState(hayBusqueda: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            if (hayBusqueda) Icons.Default.Search else Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (hayBusqueda)
                "No se encontraron usuarios"
            else
                "No hay usuarios registrados",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        if (hayBusqueda) {
            Text(
                text = "Intenta con otro término de búsqueda",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioUsuarioDialog(
    usuario: UsuarioDto?,
    roles: List<com.example.proyecto1.data.remote.dto.RolSistemaDto>,
    onDismiss: () -> Unit,
    onSave: (CrearUsuarioDto) -> Unit,
    onRetryRoles: () -> Unit
) {
    var username by remember { mutableStateOf(usuario?.username ?: "") }
    // No pedimos nombres/apellidos en el formulario simple
    var correo by remember { mutableStateOf(usuario?.correo ?: "") }
    var password by remember { mutableStateOf("") }
    var mostrarPassword by remember { mutableStateOf(false) }

    // Si roles está vacío, mostramos un mensaje y botón de reintento
    if (roles.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Roles no disponibles") },
            text = { Text("No se cargaron los roles desde el servidor. Verifica la conexión con la API o presiona reintentar.") },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("Cerrar") }
            },
            dismissButton = {
                TextButton(onClick = onRetryRoles) { Text("Reintentar") }
            }
        )
        return
    }

    // Filtrar roles para mostrar dinámicamente (usar los que trae la API)
    val rolesFiltrados = remember(roles) { roles }

    var rolSeleccionado by remember {
        mutableStateOf(
            usuario?.idRolSistema ?: rolesFiltrados.firstOrNull()?.idRolSistema ?: 0
        )
    }
    var expandedRol by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (usuario == null) "Agregar Usuario" else "Editar Usuario", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                // Nombre de usuario
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nombre de usuario *") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true,
                    enabled = usuario == null,
                    placeholder = { Text("Ejemplo: jperez") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Correo
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo electrónico *") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    singleLine = true,
                    placeholder = { Text("ejemplo@correo.com") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (usuario == null) "Contraseña *" else "Nueva contraseña (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                            Icon(
                                if (mostrarPassword) Icons.Default.Lock else Icons.Default.Lock,
                                contentDescription = if (mostrarPassword) "Ocultar" else "Mostrar"
                            )
                        }
                    },
                    visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    placeholder = { Text("Mínimo 6 caracteres") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Selector de rol (simplificado dentro del dialog)
                ExposedDropdownMenuBox(
                    expanded = expandedRol,
                    onExpandedChange = { expandedRol = it }
                ) {
                    OutlinedTextField(
                        value = rolesFiltrados.find { it.idRolSistema == rolSeleccionado }?.nombre ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRol) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.AccountCircle, null) }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRol,
                        onDismissRequest = { expandedRol = false }
                    ) {
                        rolesFiltrados.forEach { rol ->
                            DropdownMenuItem(
                                text = { Text(rol.nombre) },
                                onClick = { rolSeleccionado = rol.idRolSistema; expandedRol = false },
                                leadingIcon = { Icon(if (rol.codigo.uppercase() == "TECNICO") Icons.Default.Build else Icons.Default.Person, contentDescription = null, tint = Color(0xFF2196F3)) }
                            )
                        }
                    }
                }

            }
        },
        confirmButton = {
            TextButton(onClick = {
                val passwordFinal = if (usuario != null && password.isBlank()) "sin_cambio" else password
                onSave(
                    CrearUsuarioDto(
                        username = username,
                        correo = correo,
                        password = passwordFinal,
                        idRolSistema = rolSeleccionado,
                        idEstadoUsuario = 1,
                        nombres = null,
                        apellidos = null,
                        documento = null,
                        telefono = null
                    )
                )
            }) { Text(if (usuario == null) "Crear" else "Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

}

// Función helper para formatear fechas
private fun formatearFecha(fechaISO: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(fechaISO)
        date?.let { outputFormat.format(it) } ?: fechaISO
    } catch (_: Exception) {
        fechaISO.substring(0, 10) // Fallback: solo la fecha
    }
}
