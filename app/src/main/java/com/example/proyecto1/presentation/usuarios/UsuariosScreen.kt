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
                }
            )
        }

        // Mostrar error si existe
        uiState.error?.let { error ->
            AlertDialog(
                onDismissRequest = { viewModel.limpiarError() },
                title = { Text("Error") },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = { viewModel.limpiarError() }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun UsuarioCard(
    usuario: UsuarioDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

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
                // Avatar con inicial
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2196F3)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = usuario.username.firstOrNull()?.uppercase() ?: "U",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    // Nombre completo (si existe)
                    val nombreCompleto = buildString {
                        usuario.personal?.nombres?.let { append(it) }
                        if (usuario.personal?.nombres?.isNotBlank() == true &&
                            usuario.personal?.apellidos?.isNotBlank() == true) {
                            append(" ")
                        }
                        usuario.personal?.apellidos?.let { append(it) }
                    }.trim()

                    if (nombreCompleto.isNotBlank()) {
                        Text(
                            text = nombreCompleto,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        // Username como subtítulo
                        Text(
                            text = "@${usuario.username}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666),
                            fontSize = 12.sp
                        )
                    } else {
                        // Si no hay nombre, mostrar username como título
                        Text(
                            text = usuario.username,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Email
                    Text(
                        text = usuario.correo,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                    // Rol
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF2196F3).copy(alpha = 0.1f),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = usuario.rolNombre ?: "Sin rol",
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
                // Botón Editar
                IconButton(
                    onClick = onEdit,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color(0xFF2196F3)
                    )
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                // Botón Desactivar/Eliminar
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
    onSave: (CrearUsuarioDto) -> Unit
) {
    var username by remember { mutableStateOf(usuario?.username ?: "") }
    var nombres by remember { mutableStateOf(usuario?.personal?.nombres ?: "") }
    var apellidos by remember { mutableStateOf(usuario?.personal?.apellidos ?: "") }
    var correo by remember { mutableStateOf(usuario?.correo ?: "") }
    var password by remember { mutableStateOf("") }
    var mostrarPassword by remember { mutableStateOf(false) }

    // Filtrar roles para mostrar solo Técnico (1004) y Cliente (1005)
    val rolesFiltrados = remember(roles) {
        if (roles.isEmpty()) {
            // Si no hay roles de la API, crear roles por defecto
            listOf(
                com.example.proyecto1.data.remote.dto.RolSistemaDto(
                    idRolSistema = 1004,
                    codigo = "TECNICO",
                    nombre = "Técnico",
                    descripcion = "Soporte técnico",
                    esActivo = true
                ),
                com.example.proyecto1.data.remote.dto.RolSistemaDto(
                    idRolSistema = 1005,
                    codigo = "CLIENTE",
                    nombre = "Cliente",
                    descripcion = "Usuario solicitante",
                    esActivo = true
                )
            )
        } else {
            roles.filter { it.idRolSistema == 1004 || it.idRolSistema == 1005 }
        }
    }

    var rolSeleccionado by remember {
        mutableStateOf(
            usuario?.idRolSistema ?: rolesFiltrados.firstOrNull()?.idRolSistema ?: 1005
        )
    }
    var expandedRol by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título
                Text(
                    text = if (usuario == null) "Agregar Usuario" else "Editar Usuario",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider()

                // Nombre de usuario
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nombre de usuario *") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true,
                    enabled = usuario == null, // No editable si es edición
                    placeholder = { Text("Ejemplo: jperez") }
                )

                // Nombres
                OutlinedTextField(
                    value = nombres,
                    onValueChange = { nombres = it },
                    label = { Text("Nombres *") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true,
                    placeholder = { Text("Ejemplo: Juan Carlos") }
                )

                // Apellidos
                OutlinedTextField(
                    value = apellidos,
                    onValueChange = { apellidos = it },
                    label = { Text("Apellidos *") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true,
                    placeholder = { Text("Ejemplo: Pérez García") }
                )

                // Correo electrónico
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo electrónico *") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    singleLine = true,
                    placeholder = { Text("ejemplo@correo.com") }
                )

                // Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text(if (usuario == null) "Contraseña *" else "Nueva contraseña (opcional)")
                    },
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
                    visualTransformation = if (mostrarPassword)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    singleLine = true,
                    placeholder = { Text("Mínimo 6 caracteres") }
                )

                // Selector de Rol
                ExposedDropdownMenuBox(
                    expanded = expandedRol,
                    onExpandedChange = { expandedRol = it }
                ) {
                    OutlinedTextField(
                        value = rolesFiltrados.find { it.idRolSistema == rolSeleccionado }?.nombre ?: "Cliente",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRol) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        leadingIcon = { Icon(Icons.Default.AccountCircle, null) }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRol,
                        onDismissRequest = { expandedRol = false }
                    ) {
                        rolesFiltrados.forEach { rol ->
                            DropdownMenuItem(
                                text = { Text(rol.nombre) },
                                onClick = {
                                    rolSeleccionado = rol.idRolSistema
                                    expandedRol = false
                                },
                                leadingIcon = {
                                    Icon(
                                        if (rol.codigo == "TECNICO") Icons.Default.Build else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFF2196F3)
                                    )
                                }
                            )
                        }
                    }
                }

                // Fecha de creación (solo lectura si es edición)
                if (usuario != null) {
                    usuario.creadoEn?.let { fecha ->
                        OutlinedTextField(
                            value = formatearFecha(fecha),
                            onValueChange = {},
                            label = { Text("Fecha de creación") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.DateRange, null) },
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                HorizontalDivider()

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            val passwordFinal = if (usuario != null && password.isBlank()) {
                                "sin_cambio" // Contraseña sin cambio
                            } else {
                                password
                            }

                            onSave(
                                CrearUsuarioDto(
                                    username = username,
                                    correo = correo,
                                    password = passwordFinal,
                                    idRolSistema = rolSeleccionado,
                                    idEstadoUsuario = 1, // Activo por defecto
                                    nombres = nombres.ifBlank { null },
                                    apellidos = apellidos.ifBlank { null },
                                    documento = null,
                                    telefono = null
                                )
                            )
                        },
                        enabled = username.isNotBlank() &&
                                  nombres.isNotBlank() &&
                                  apellidos.isNotBlank() &&
                                  correo.isNotBlank() &&
                                  (usuario != null || password.length >= 6),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Icon(
                            if (usuario == null) Icons.Default.Add else Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (usuario == null) "Crear" else "Guardar")
                    }
                }
            }
        }
    }
}

// Función helper para formatear fechas
private fun formatearFecha(fechaISO: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(fechaISO)
        date?.let { outputFormat.format(it) } ?: fechaISO
    } catch (e: Exception) {
        fechaISO.substring(0, 10) // Fallback: solo la fecha
    }
}

