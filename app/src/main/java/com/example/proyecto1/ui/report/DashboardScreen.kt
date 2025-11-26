package com.example.proyecto1.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.* 
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue // CORRECCIÓN 1: Se añade el import que faltaba
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb // CORRECCIÓN 2: Se importa toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.proyecto1.ui.gestion.GestionDatosViewModel
import com.example.proyecto1.ui.gestion.SlaRecord
import com.example.proyecto1.ui.theme.White
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.DecimalFormat

@Composable
fun DashboardScreen(viewModel: GestionDatosViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray.copy(alpha = 0.2f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ReportSummaryCard(uiState.totalRecords, uiState.compliant, uiState.nonCompliant) }
        item { ComplianceByRoleChart(uiState.records) } // Gráfico de barras por Rol
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
fun ComplianceByRoleChart(records: List<SlaRecord>) {
    val dataByRole = records.groupBy { it.rol }
        .mapValues { (_, records) ->
            val total = records.size
            val compliant = records.count { it.cumple }
            if (total > 0) compliant.toFloat() / total.toFloat() * 100f else 0f
        }

    // CORRECCIÓN 2: Se obtiene el color fuera del bloque no-composable.
    val barColor = MaterialTheme.colorScheme.primary.toArgb()

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Cumplimiento por Rol", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            if (dataByRole.isEmpty()) {
                Text("No hay datos para mostrar.", style = MaterialTheme.typography.bodyMedium)
            } else {
                AndroidView(
                    factory = { context ->
                        BarChart(context).apply {
                            description.isEnabled = false
                            legend.isEnabled = false
                            xAxis.position = XAxis.XAxisPosition.BOTTOM
                            xAxis.setDrawGridLines(false)
                            axisLeft.axisMinimum = 0f
                            axisLeft.axisMaximum = 100f
                            axisRight.isEnabled = false
                        }
                    },
                    update = { chart ->
                        val entries = dataByRole.values.mapIndexed { index, value ->
                            BarEntry(index.toFloat(), value)
                        }
                        val labels = dataByRole.keys.toList()

                        val dataSet = BarDataSet(entries, "Cumplimiento por Rol").apply {
                            // CORRECCIÓN 2: Se usa el color ya resuelto.
                            color = barColor
                        }
                        chart.data = BarData(dataSet)
                        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                        chart.invalidate()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }
        }
    }
}