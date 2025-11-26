package com.example.proyecto1.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyecto1.ui.home.HomeViewModel

@Composable
fun DashboardScreen(homeViewModel: HomeViewModel) {
    val uiState by homeViewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            FilterCard(
                roles = uiState.roles,
                selectedRole = uiState.selectedRole,
                onRoleSelected = { homeViewModel.onRoleSelected(it) },
                slaTypes = uiState.slaTypes,
                selectedSlaType = uiState.selectedSlaType,
                onSlaTypeSelected = { homeViewModel.onSlaTypeSelected(it) }
            )
        }
        item {
            KpiRow(
                compliancePercentage = uiState.compliancePercentage,
                averageDays = uiState.averageDays
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterCard(
    roles: List<String>,
    selectedRole: String,
    onRoleSelected: (String) -> Unit,
    slaTypes: List<String>,
    selectedSlaType: String,
    onSlaTypeSelected: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FilterList, contentDescription = "Filtros")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Filtros", style = MaterialTheme.typography.titleLarge)
            }
            Text("Filtra los datos por rol, tipo de SLA y periodo", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))

            FilterDropdown(
                label = "Rol",
                options = roles,
                selected = selectedRole,
                onSelected = onRoleSelected
            )
            Spacer(modifier = Modifier.height(8.dp))
            FilterDropdown(
                label = "Tipo SLA",
                options = slaTypes,
                selected = selectedSlaType,
                onSelected = onSlaTypeSelected
            )
            Spacer(modifier = Modifier.height(8.dp))
            FilterDropdown(
                label = "Periodo",
                options = listOf("Todo el periodo"),
                selected = "Todo el periodo",
                onSelected = {}
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        onSelected(it)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun KpiRow(compliancePercentage: Float, averageDays: Float) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        KpiCard(
            modifier = Modifier.weight(1f),
            title = "% de Cumplimiento del SLA",
            value = "${String.format("%.1f", compliancePercentage)}%",
            subtitle = "En alerta",
            icon = Icons.Default.ArrowDownward,
            iconBackgroundColor = Color(0xFFFFF3E0),
            iconColor = Color(0xFFFFA000)
        )
        KpiCard(
            modifier = Modifier.weight(1f),
            title = "Promedio de días de atención",
            value = "${String.format("%.1f", averageDays)}",
            subtitle = "días promedio",
            icon = Icons.Default.CalendarToday,
            iconBackgroundColor = Color(0xFFE3F2FD),
            iconColor = Color(0xFF1976D2)
        )
    }
}

@Composable
private fun KpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconColor: Color
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.background(iconBackgroundColor, CircleShape).padding(4.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}