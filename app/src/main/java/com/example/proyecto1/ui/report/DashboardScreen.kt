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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.proyecto1.ui.home.HomeViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter

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
                roles = uiState.roles, selectedRole = uiState.selectedRole, onRoleSelected = homeViewModel::onRoleSelected,
                slaTypes = uiState.slaTypes, selectedSlaType = uiState.selectedSlaType, onSlaTypeSelected = homeViewModel::onSlaTypeSelected,
                periods = uiState.periods, selectedPeriod = uiState.selectedPeriod, onPeriodSelected = homeViewModel::onPeriodSelected
            )
        }
        item { KpiRow(uiState.compliancePercentage, uiState.averageDays) }
        
        item {
            val compliantPercent = if (uiState.totalCases > 0) (uiState.compliantCases.toFloat() / uiState.totalCases) * 100 else 0f
            val nonCompliantPercent = if (uiState.totalCases > 0) (uiState.nonCompliantCases.toFloat() / uiState.totalCases) * 100 else 0f

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SummaryKpiCard("Total de casos registrados", uiState.totalCases.toString(), "${uiState.compliantCases} cumplen / ${uiState.nonCompliantCases} no cumplen", Icons.Filled.Leaderboard, Color(0xFF6B21A8), Color(0xFFE9D5FF))
                SummaryKpiCard("Casos que Cumplen", uiState.compliantCases.toString(), "${String.format("%.1f", compliantPercent)}% del total", Icons.Filled.CheckCircle, Color(0xFF16A34A), Color(0xFFDCFCE7))
                SummaryKpiCard("Casos que No Cumplen", uiState.nonCompliantCases.toString(), "${String.format("%.1f", nonCompliantPercent)}% del total", Icons.Filled.Warning, Color(0xFFB91C1C), Color(0xFFFEE2E2))
            }
        }

        // CORRECCIÓN: Se añade el gráfico circular
        item {
            PieChartCard(
                compliant = uiState.compliantCases,
                nonCompliant = uiState.nonCompliantCases
            )
        }
    }
}

@Composable
private fun PieChartCard(compliant: Int, nonCompliant: Int) {
    if (compliant == 0 && nonCompliant == 0) return

    val chartBlue = Color(0xFF3B82F6)
    val chartRed = Color(0xFFEF4444)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Porcentaje de Cumplimiento General", style = MaterialTheme.typography.titleMedium)
            Text("Distribución entre casos que cumplen y no cumplen", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            
            AndroidView(
                factory = { context ->
                    PieChart(context).apply {
                        description.isEnabled = false
                        isDrawHoleEnabled = true
                        setHoleColor(android.graphics.Color.TRANSPARENT)
                        setUsePercentValues(true)
                        legend.isEnabled = false
                    }
                },
                update = { chart ->
                    val entries = listOf(
                        PieEntry(compliant.toFloat(), "Cumplen"),
                        PieEntry(nonCompliant.toFloat(), "No Cumplen")
                    )
                    val dataSet = PieDataSet(entries, "").apply {
                        colors = listOf(chartBlue.toArgb(), chartRed.toArgb())
                        valueTextColor = android.graphics.Color.WHITE
                        valueTextSize = 12f
                        valueFormatter = PercentFormatter(chart)
                    }
                    chart.data = PieData(dataSet)
                    chart.invalidate()
                },
                modifier = Modifier.fillMaxWidth().height(250.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterCard(roles: List<String>, selectedRole: String, onRoleSelected: (String) -> Unit, slaTypes: List<String>, selectedSlaType: String, onSlaTypeSelected: (String) -> Unit, periods: List<String>, selectedPeriod: String, onPeriodSelected: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FilterList, contentDescription = "Filtros"); Spacer(modifier = Modifier.width(8.dp)); Text("Filtros", style = MaterialTheme.typography.titleLarge)
            }
            Text("Filtra los datos por rol, tipo de SLA y periodo", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            FilterDropdown("Rol", roles, selectedRole, onRoleSelected)
            Spacer(modifier = Modifier.height(8.dp))
            FilterDropdown("Tipo SLA", slaTypes, selectedSlaType, onSlaTypeSelected)
            Spacer(modifier = Modifier.height(8.dp))
            FilterDropdown("Periodo", periods, selectedPeriod, onPeriodSelected)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdown(label: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(value = selected, onValueChange = {}, label = { Text(label) }, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) { options.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { onSelected(it); expanded = false }) } }
    }
}

@Composable
private fun KpiRow(compliancePercentage: Float, averageDays: Float) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        KpiCard(Modifier.weight(1f), "% de Cumplimiento del SLA", "${String.format("%.1f", compliancePercentage)}%", "En alerta", Icons.Default.ArrowDownward, Color(0xFFFFF3E0), Color(0xFFFFA000))
        KpiCard(Modifier.weight(1f), "Promedio de días de atención", "${String.format("%.1f", averageDays)}", "días promedio", Icons.Default.CalendarToday, Color(0xFFE3F2FD), Color(0xFF1976D2))
    }
}

@Composable
private fun KpiCard(modifier: Modifier = Modifier, title: String, value: String, subtitle: String, icon: ImageVector, iconBackgroundColor: Color, iconColor: Color) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Icon(icon, null, tint = iconColor, modifier = Modifier.background(iconBackgroundColor, CircleShape).padding(4.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
private fun SummaryKpiCard(title: String, value: String, subtitle: String, icon: ImageVector, iconColor: Color, iconBackgroundColor: Color) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(value, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(icon, title, tint = iconColor, modifier = Modifier.size(48.dp).background(iconBackgroundColor, RoundedCornerShape(8.dp)).padding(8.dp))
        }
    }
}