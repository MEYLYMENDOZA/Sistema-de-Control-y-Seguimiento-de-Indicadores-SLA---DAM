package com.example.proyecto1.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.* 
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
// import androidx.compose.runtime.getValue // No es necesario con esta solución
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyecto1.ui.gestion.GestionDatosViewModel
import com.example.proyecto1.ui.gestion.SlaRecord
import com.example.proyecto1.ui.theme.GreenProgress
import com.example.proyecto1.ui.theme.RedProgress
import com.example.proyecto1.ui.theme.White
import java.text.DecimalFormat

@Composable
fun DashboardScreen(viewModel: GestionDatosViewModel) {
    // SOLUCIÓN: Se reemplaza el delegado "by" por el acceso explícito a ".value"
    // para esquivar el error de la caché del compilador.
    val uiState = viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray.copy(alpha = 0.2f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Se accede a los datos a través de uiState.value
        item { ReportSummaryCard(uiState.value.totalRecords, uiState.value.compliant, uiState.value.nonCompliant) }
        item { ComplianceByTypeCard(uiState.value.records) }
        item { ComplianceByRoleCard(uiState.value.records) }
        item { Last10RecordsCard(uiState.value.records) }
    }
}

@Composable
fun ReportSummaryCard(total: Int, compliant: Int, nonCompliant: Int) {
    val compliancePercentage = if (total > 0) (compliant.toDouble() / total.toDouble() * 100) else 0.0
    val percentageFormatter = DecimalFormat("#.##")

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Resumen de Indicadores SLA", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                KpiCard(total.toString(), "Total Casos")
                KpiCard(compliant.toString(), "Cumplen", Color(0xFFE8F5E9))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                KpiCard(nonCompliant.toString(), "No Cumplen", Color(0xFFFFEBEE))
                KpiCard("${percentageFormatter.format(compliancePercentage)}%", "% Cumplimiento", Color(0xFFE3F2FD))
            }
        }
    }
}

@Composable
fun KpiCard(value: String, label: String, backgroundColor: Color = Color.LightGray.copy(alpha = 0.2f)) {
    Card(colors = CardDefaults.cardColors(containerColor = backgroundColor)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ComplianceByTypeCard(records: List<SlaRecord>) {
    val data = records.groupBy { it.tipoSla }
        .mapValues { (_, records) ->
            val total = records.size
            val compliant = records.count { it.cumple }
            if (total > 0) compliant.toFloat() / total.toFloat() * 100f else 0f
        }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Cumplimiento por Tipo de SLA", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            if (data.isEmpty()) {
                Text("No hay datos para mostrar.", style = MaterialTheme.typography.bodyMedium)
            } else {
                data.forEach { (label, value) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = label, modifier = Modifier.weight(1f))
                        LinearProgressIndicator(progress = value / 100f, modifier = Modifier.weight(1f))
                        Text(text = "${value.toInt()}%", modifier = Modifier.padding(start = 8.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ComplianceByRoleCard(records: List<SlaRecord>) {
    val roles = records.groupBy { it.rol }
        .mapValues { (_, records) ->
            val total = records.size
            val compliant = records.count { it.cumple }
            if (total > 0) compliant.toFloat() / total.toFloat() * 100f else 0f
        }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Cumplimiento por Rol", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            if (roles.isEmpty()) {
                Text("No hay datos para mostrar.", style = MaterialTheme.typography.bodyMedium)
            } else {
                roles.forEach { (role, value) ->
                    val progressColor = if (value > 80) GreenProgress else RedProgress
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(text = role, modifier = Modifier.weight(1.5f))
                        LinearProgressIndicator(
                            progress = value / 100f,
                            modifier = Modifier.weight(1f),
                            color = progressColor,
                            trackColor = progressColor.copy(alpha = 0.3f)
                        )
                        Text(text = "${value.toInt()}%", modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun Last10RecordsCard(records: List<SlaRecord>) {
    val lastRecords = records.take(10)

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Últimos 10 Registros", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Rol", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("F. Solicitud", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("F. Ingreso", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            if (lastRecords.isEmpty()) {
                Text("No hay registros para mostrar.", style = MaterialTheme.typography.bodyMedium)
            } else {
                lastRecords.forEach { record ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(record.rol, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text(record.fechaSolicitud, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text(record.fechaIngreso, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
