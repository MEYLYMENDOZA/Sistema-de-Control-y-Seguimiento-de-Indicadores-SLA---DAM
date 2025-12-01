package com.example.proyecto1.features.notifications.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto1.features.notifications.presentation.dashboard.components.SummaryCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsDashboardScreen(
    viewModel: AlertsDashboardViewModel = viewModel(),
    // Estas son las funciones de navegación
    onNavigateToAlertsHistory: () -> Unit,
    onNavigateToCriticalCases: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onMenuClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alertas") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                }
            )
        }
    ) { paddingValues ->

        // Usamos LazyColumn para que la pantalla sea "scroleable"
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- Tarjeta 1: Alertas Activas ---
            item {
                SummaryCard(
                    title = "Alertas Activas",
                    value = state.activeAlertsCount.toString(),
                    icon = Icons.Default.NotificationsActive,
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToAlertsHistory // Navega al historial
                )
            }

            // --- Tarjeta 2: Casos Críticos ---
            item {
                SummaryCard(
                    title = "Casos Críticos",
                    value = state.criticalCasesCount.toString(),
                    icon = Icons.Default.Error,
                    iconColor = Color(0xFFEA4335), // Rojo
                    onClick = onNavigateToCriticalCases // Navega al detalle de críticos
                )
            }

            // --- Tarjeta 3: Cerca del Límite ---
            item {
                SummaryCard(
                    title = "Cerca del Límite",
                    value = state.nearLimitCount.toString(),
                    icon = Icons.Default.Warning,
                    iconColor = Color(0xFFFBBC05), // Amarillo
                    onClick = { /* TODO: Navegar al detalle de "Cerca del Límite" */ }
                )
            }

            // --- Tarjeta 4: Cumplimiento ---
            item {
                SummaryCard(
                    title = "Cumplimiento",
                    value = "${state.compliancePercentage}%",
                    icon = Icons.Default.CheckCircleOutline, // O usa el ícono de "Error" como en tu figma
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = { /* TODO: Navegar a la pantalla de Dashboard General */ }
                )
            }

            // --- Tarjeta 5: Configuración de Alertas ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                AlertConfigCard(
                    onClick = onNavigateToSettings // Navega a la configuración
                )
            }
        }
    }
}

// Este es el Composable para la tarjeta de "Configuración" (Figma 1)
@Composable
private fun AlertConfigCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Configuración de Alertas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Define los umbrales para las notificaciones automáticas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Fila de Alerta Crítica
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.padding(start = 4.dp, end = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Crítica",
                        tint = Color(0xFFEA4335),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column {
                    Text(text = "Alerta Crítica", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(text = "Cumplimiento < 70%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fila de Alerta de Advertencia
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.padding(start = 4.dp, end = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Advertencia",
                        tint = Color(0xFFFBBC05),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column {
                    Text(text = "Alerta de Advertencia", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(text = "Cumplimiento < 85%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}