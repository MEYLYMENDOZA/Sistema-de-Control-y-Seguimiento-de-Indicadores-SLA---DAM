package com.example.proyecto1.ui.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Esta será la nueva pantalla de Inicio (Dashboard)

@Composable
fun InicioScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Tarjeta de Filtros
        item { FiltersCard() }

        // 2. KPIs Principales
        item { KpiCard("73.3%", "% de Cumplimiento del SLA", "En alerta", Icons.Outlined.TrendingDown, MaterialTheme.colorScheme.error) }
        item { KpiCard("19.8", "Promedio de días de atención", "días promedio", Icons.Outlined.CalendarToday, MaterialTheme.colorScheme.onSurface) }
        item { KpiCard("150", "Total de casos registrados", "110 cumplen / 40 no cumplen", Icons.Outlined.BarChart, MaterialTheme.colorScheme.onSurface) }

        // 3. Gráficos (con placeholders por ahora)
        item { ChartPlaceholderCard("Porcentaje de Cumplimiento General") }
        item { ChartPlaceholderCard("Cumplimiento por Rol") }
        item { ChartPlaceholderCard("Tendencia de Cumplimiento") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersCard() {
    var expandedRole by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("Todos los roles") }

    var expandedType by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf("Todos los tipos") }

    var expandedPeriod by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf("Todo el periodo") }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Filtros", style = MaterialTheme.typography.titleMedium)
            Text("Filtra los datos por rol, tipo de SLA y periodo", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))

            // Filtro por Rol
            ExposedDropdownMenuBox(expanded = expandedRole, onExpandedChange = { expandedRole = !expandedRole }) {
                OutlinedTextField(
                    value = selectedRole,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Rol") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expandedRole, onDismissRequest = { expandedRole = false }) {
                    DropdownMenuItem(text = { Text("Todos los roles") }, onClick = { selectedRole = "Todos los roles"; expandedRole = false })
                    // ... agregar más roles ...
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Filtro por Tipo SLA
             ExposedDropdownMenuBox(expanded = expandedType, onExpandedChange = { expandedType = !expandedType }) {
                OutlinedTextField(
                    value = selectedType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo SLA") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                    DropdownMenuItem(text = { Text("Todos los tipos") }, onClick = { selectedType = "Todos los tipos"; expandedType = false })
                    // ... agregar más tipos ...
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Filtro por Periodo
            ExposedDropdownMenuBox(expanded = expandedPeriod, onExpandedChange = { expandedPeriod = !expandedPeriod }) {
                OutlinedTextField(
                    value = selectedPeriod,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Periodo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPeriod) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expandedPeriod, onDismissRequest = { expandedPeriod = false }) {
                    DropdownMenuItem(text = { Text("Todo el periodo") }, onClick = { selectedPeriod = "Todo el periodo"; expandedPeriod = false })
                    // ... agregar más periodos ...
                }
            }
        }
    }
}

@Composable
fun KpiCard(value: String, title: String, subtitle: String, icon: ImageVector, accentColor: Color) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(value, style = MaterialTheme.typography.headlineLarge, color = accentColor)
                Text(title, style = MaterialTheme.typography.bodyMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(icon, contentDescription = title, modifier = Modifier.size(48.dp), tint = accentColor)
        }
    }
}

@Composable
fun ChartPlaceholderCard(title: String) {
    Card(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
