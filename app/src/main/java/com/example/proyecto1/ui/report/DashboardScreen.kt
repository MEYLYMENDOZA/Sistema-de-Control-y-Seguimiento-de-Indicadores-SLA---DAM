package com.example.proyecto1.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
                onSlaTypeSelected = { homeViewModel.onSlaTypeSelected(it) },
                periods = uiState.periods,
                selectedPeriod = uiState.selectedPeriod,
                onPeriodSelected = { homeViewModel.onPeriodSelected(it) }
            )
        }
        item {
            KpiRow(
                compliancePercentage = uiState.compliancePercentage,
                averageDays = uiState.averageDays
            )
        }
        
        // Se añaden las nuevas tarjetas de KPIs
        item {
            val compliantPercent = if (uiState.totalCases > 0) (uiState.compliantCases.toFloat() / uiState.totalCases) * 100 else 0f
            val nonCompliantPercent = if (uiState.totalCases > 0) (uiState.nonCompliantCases.toFloat() / uiState.totalCases) * 100 else 0f

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SummaryKpiCard(
                    title = "Total de casos registrados",
                    value = uiState.totalCases.toString(),
                    subtitle = "${uiState.compliantCases} cumplen / ${uiState.nonCompliantCases} no cumplen",
                    icon = Icons.Filled.Leaderboard,
                    iconBackgroundColor = Color(0xFFE9D5FF),
                    iconColor = Color(0xFF6B21A8)
                )
                SummaryKpiCard(
                    title = "Casos que Cumplen",
                    value = uiState.compliantCases.toString(),
                    subtitle = "${String.format("%.1f", compliantPercent)}% del total",
                    icon = Icons.Filled.CheckCircle,
                    iconBackgroundColor = Color(0xFFDCFCE7),
                    iconColor = Color(0xFF16A34A)
                )
                SummaryKpiCard(
                    title = "Casos que No Cumplen",
                    value = uiState.nonCompliantCases.toString(),
                    subtitle = "${String.format("%.1f", nonCompliantPercent)}% del total",
                    icon = Icons.Filled.Warning,
                    iconBackgroundColor = Color(0xFFFEE2E2),
                    iconColor = Color(0xFFB91C1C)
                )
            }
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
    onSlaTypeSelected: (String) -> Unit,
    periods: List<String>,
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit
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

            FilterDropdown(label = "Rol", options = roles, selected = selectedRole, onSelected = onRoleSelected)
            Spacer(modifier = Modifier.height(8.dp))
            FilterDropdown(label = "Tipo SLA", options = slaTypes, selected = selectedSlaType, onSelected = onSlaTypeSelected)
            Spacer(modifier = Modifier.height(8.dp))
            FilterDropdown(label = "Periodo", options = periods, selected = selectedPeriod, onSelected = onPeriodSelected)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdown(label: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected, onValueChange = {}, label = { Text(label) }, readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { onSelected(it); expanded = false }) }
        }
    }
}

@Composable
private fun KpiRow(compliancePercentage: Float, averageDays: Float) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        KpiCard(modifier = Modifier.weight(1f), title = "% de Cumplimiento del SLA", value = "${String.format("%.1f", compliancePercentage)}%", subtitle = "En alerta", icon = Icons.Default.ArrowDownward, iconBackgroundColor = Color(0xFFFFF3E0), iconColor = Color(0xFFFFA000))
        KpiCard(modifier = Modifier.weight(1f), title = "Promedio de días de atención", value = "${String.format("%.1f", averageDays)}", subtitle = "días promedio", icon = Icons.Default.CalendarToday, iconBackgroundColor = Color(0xFFE3F2FD), iconColor = Color(0xFF1976D2))
    }
}

@Composable
private fun KpiCard(modifier: Modifier = Modifier, title: String, value: String, subtitle: String, icon: ImageVector, iconBackgroundColor: Color, iconColor: Color) {
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

// Nuevo Composable para las tarjetas de resumen
@Composable
private fun SummaryKpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    iconBackgroundColor: Color
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(value, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBackgroundColor, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            )
        }
    }
}
