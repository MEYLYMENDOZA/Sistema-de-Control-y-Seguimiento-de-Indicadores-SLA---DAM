package com.example.proyecto1.features.notifications.presentation.alert_history

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto1.features.notifications.presentation.alert_history.components.AlertHistoryItem
import com.example.proyecto1.features.notifications.presentation.alert_history.components.CreateAlertDialog
import com.example.proyecto1.features.notifications.presentation.alert_history.components.AlertDetailsDialog
import com.example.proyecto1.features.notifications.domain.model.VisualAlert
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsHistoryScreen(
    viewModel: AlertsHistoryViewModel = viewModel(),
    onMenuClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    // Estados para los diálogos
    var showCreateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var alertToDeleteId by remember { mutableStateOf<String?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false) }  // ✅ NUEVO
    var selectedAlert by remember { mutableStateOf<VisualAlert?>(null) }  // ✅ NUEVO

    // --- CONTENEDOR PRINCIPAL (BOX) PARA PERMITIR ELEMENTOS FLOTANTES LIBRES ---
    Box(modifier = Modifier.fillMaxSize()) {

        // 1. EL SCAFFOLD (La pantalla normal de fondo)
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Historial de Alertas") },
                    navigationIcon = {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    },
                    actions = {
                        BadgedBox(
                            badge = {
                                if (state.unreadCount > 0) { Badge { Text("${state.unreadCount}") } }
                            },
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Text(text = "No Leídas", fontWeight = FontWeight.Medium)
                        }
                    }
                )
            }
            // NOTA: Ya no usamos floatingActionButton aquí dentro
        ) { paddingValues ->

            if (state.isLoading) {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Alertas Activas", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
                    }

                    if (state.alerts.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Text("No hay alertas activas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        items(state.alerts, key = { it.id }) { alert ->
                            AlertHistoryItem(
                                alert = alert,
                                onDismiss = {
                                    alertToDeleteId = it
                                    showDeleteDialog = true
                                },
                                onClick = {  // ✅ NUEVO: Al presionar la alerta
                                    selectedAlert = it
                                    showDetailsDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // 2. LOS BOTONES FLOTANTES ARRASTRABLES (Z-Index superior)
        // Usamos BoxScope para alinearlos inicialmente abajo a la derecha

        // Botón A: Verificar SLA (Arrastrable)
        DraggableExtendedFab(
            text = "Verificar SLA",
            icon = Icons.Default.Refresh,
            onClick = { viewModel.verificarCumplimientoSla() },
            // Posición inicial: Un poco más arriba del botón +
            initialOffsetY = -80f
        )

        // Botón B: Crear Alerta (Arrastrable)
        DraggableFab(
            icon = Icons.Default.Add,
            onClick = { showCreateDialog = true },
            // Posición inicial: Esquina inferior derecha
            initialOffsetY = 0f
        )

        // 3. DIÁLOGOS (Siempre encima de todo)
        if (showCreateDialog) {
            CreateAlertDialog(
                personalList = state.personalList,
                onDismiss = { showCreateDialog = false },
                onConfirm = { mensaje, responsable, nivel ->
                    viewModel.crearAlertaPersonalizada(mensaje, responsable, nivel)
                    showCreateDialog = false
                }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("¿Resolver Alerta?") },
                text = { Text("¿Estás seguro de marcar esta alerta como solucionada?") },
                confirmButton = {
                    Button(
                        onClick = {
                            alertToDeleteId?.let { viewModel.onDismissAlert(it) }
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Sí, Eliminar") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
                }
            )
        }

        // ✅ NUEVO: DIÁLOGO DE DETALLES DE ALERTA
        if (showDetailsDialog && selectedAlert != null) {
            AlertDetailsDialog(
                alert = selectedAlert!!,
                onDismiss = {
                    showDetailsDialog = false
                    selectedAlert = null
                }
            )
        }
    }
}

// --- COMPONENTES PERSONALIZADOS ARRASTRABLES ---

@Composable
fun BoxScope.DraggableFab(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    initialOffsetY: Float = 0f
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(initialOffsetY) }

    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .align(Alignment.BottomEnd) // Alineación base
            .padding(16.dp)
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        Icon(icon, contentDescription = null)
    }
}

@Composable
fun BoxScope.DraggableExtendedFab(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    initialOffsetY: Float = 0f
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(initialOffsetY) }

    ExtendedFloatingActionButton(
        onClick = onClick,
        text = { Text(text) },
        icon = { Icon(icon, contentDescription = null) },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier
            .align(Alignment.BottomEnd) // Alineación base
            .padding(16.dp)
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    )
}