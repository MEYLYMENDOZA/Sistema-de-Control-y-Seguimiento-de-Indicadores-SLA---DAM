package com.example.proyecto1.features.notifications.presentation.alert_history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto1.features.notifications.presentation.alert_history.components.AlertHistoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsHistoryScreen(
    // Inyecta el ViewModel
    viewModel: AlertsHistoryViewModel = viewModel()
) {
    // Observa el estado del ViewModel
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Alertas") },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Abrir menú lateral */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                },
                // Muestra el contador de alertas no leídas en la barra superior
                // (Cumple con "badge/contador de alertas no leídas")
                actions = {
                    BadgedBox(
                        badge = {
                            if (state.unreadCount > 0) {
                                Badge { Text("${state.unreadCount}") }
                            }
                        },
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        // El ícono sobre el que se mostrará el badge
                        // (Puedes cambiarlo por un ícono de campana si prefieres)
                        Text(
                            text = "No Leídas",
                            fontWeight = FontWeight.Medium
                        )
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
            // Muestra el contenido principal (la lista de alertas)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // --- Cabecera de la lista ---
                item {
                    Text(
                        text = "Alertas Activas",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // --- Lista de Alertas ---
                if (state.alerts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No hay alertas activas.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(state.alerts, key = { it.id }) { alert ->
                        AlertHistoryItem(
                            alert = alert,
                            onDismiss = { alertId ->
                                viewModel.onDismissAlert(alertId)
                            }
                        )
                    }
                }
            }
        }
    }
}