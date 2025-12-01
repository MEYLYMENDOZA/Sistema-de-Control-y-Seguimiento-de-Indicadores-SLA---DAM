package com.example.proyecto1.features.notifications.presentation.email_notifications

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto1.features.notifications.domain.model.NotificationStatus
import com.example.proyecto1.features.notifications.presentation.email_notifications.components.EmailHistoryItem
import com.example.proyecto1.features.notifications.presentation.email_notifications.components.NotificationStatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailNotificationsScreen(
    // Inyecta el ViewModel
    viewModel: EmailNotificationsViewModel = viewModel(),
    onMenuClick: () -> Unit
) {
    // Observa el estado del ViewModel
    val state by viewModel.uiState.collectAsState()

    // Obtener contexto para abrir la app de correo
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                }
            )
        }
    ) { paddingValues ->

        if (state.isLoading) {
            // Muestra un indicador de carga mientras los datos se "cargan"
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Muestra el contenido principal
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // --- Cabecera ---
                item {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Notificaciones de Reportes",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Historial de reportes enviados por correo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // --- Tarjetas de Estadísticas ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NotificationStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Enviados",
                            count = state.sentCount,
                            icon = Icons.Default.CheckCircle,
                            color = Color(0xFF34A853) // Verde
                        )
                        NotificationStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Pendientes",
                            count = state.pendingCount,
                            icon = Icons.Default.HourglassTop,
                            color = Color(0xFFFBBC05) // Amarillo
                        )
                        NotificationStatCard(
                            modifier = Modifier.weight(1f),
                            title = "Fallidos",
                            count = state.failedCount,
                            icon = Icons.Default.Error,
                            color = Color(0xFFEA4335) // Rojo
                        )
                    }
                }

                // --- Cabecera de Historial ---
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Historial de envíos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = { viewModel.onClearHistoryClicked() },
                            enabled = state.historyList.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Limpiar todo",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Limpiar todo")
                        }
                    }
                }

                // --- Lista de Historial ---
                if (state.historyList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No hay envíos en el historial.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(state.historyList, key = { it.id }) { historyItem ->
                        EmailHistoryItem(
                            item = historyItem,
                            onOpenEmailClick = {
                                // ✅ NUEVO: Crear Intent para abrir app de correo
                                try {
                                    val emailIntent = Intent(Intent.ACTION_MAIN).apply {
                                        addCategory(Intent.CATEGORY_APP_EMAIL)
                                    }
                                    context.startActivity(emailIntent)
                                } catch (e: Exception) {
                                    // ✅ Mostrar Toast si no hay app de correo instalada
                                    Toast.makeText(
                                        context,
                                        "No hay aplicación de correo instalada",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }

                // Espacio al final
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}