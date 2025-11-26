package com.example.proyecto1.ui.prediction

import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream

@Composable
fun PredictionScreen(predictionViewModel: PredictionViewModel) {
    val uiState by predictionViewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Header() }
        item { InfoBanner(recordCount = uiState.recordCount) }
        item {
            // CORRECCIÓN: El botón de actualizar ahora resetea el período.
            FilterControls(
                uiState = uiState,
                onPeriodSelected = predictionViewModel::onPeriodSelected,
                onUpdateClicked = predictionViewModel::resetPeriodAndUpdate
            )
        }
        item {
            if (uiState.isPredictionReady) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ProjectionCard(
                            modifier = Modifier.weight(2f),
                            projectedSla = uiState.projectedSla,
                            isTrendNegative = uiState.isTrendNegative,
                            lastUpdated = uiState.lastUpdated
                        )
                        CoefficientsCard(
                            modifier = Modifier.weight(1f),
                            slope = uiState.slope,
                            intercept = uiState.intercept
                        )
                    }
                    if (uiState.showWarning) {
                        WarningBanner()
                    }
                    // CORRECCIÓN: El botón de recalcular ahora fuerza un refresco del cálculo actual.
                    ActionButtons(
                        uiState = uiState,
                        onRecalculate = predictionViewModel::forceRecalculation
                    )
                }
            } else {
                NotEnoughDataCard()
            }
        }
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { Footer(recordCount = uiState.recordCount) }
    }
}

@Composable
private fun Header() {
    Column {
        Text("Predicción de Cumplimiento SLA", style = MaterialTheme.typography.headlineSmall)
        Text("Estimación basada en datos históricos y regresión lineal simple (y = mx + b)", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoBanner(recordCount: Int) {
    Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Info, "Info", tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Predicción con Datos Demo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                AssistChip(onClick = {}, label = { Text("$recordCount registros") })
            }
            Text("Las predicciones mostradas se basan en datos de demostración. Carga tus propios datos en \"Carga de Datos\" para obtener proyecciones reales de tu organización.", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterControls(uiState: PredictionUiState, onPeriodSelected: (String) -> Unit, onUpdateClicked: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.weight(1f)) {
                OutlinedTextField(value = uiState.selectedPeriod, onValueChange = {}, label = { Text("Mes/Año") }, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    uiState.availablePeriods.forEach { period ->
                        DropdownMenuItem(text = { Text(period) }, onClick = { onPeriodSelected(period); expanded = false })
                    }
                }
            }
            Button(onClick = onUpdateClicked) { Icon(Icons.Default.Refresh, "Actualizar"); Spacer(Modifier.width(8.dp)); Text("Actualizar Datos") }
        }
    }
}

@Composable
private fun ProjectionCard(modifier: Modifier = Modifier, projectedSla: Float, isTrendNegative: Boolean, lastUpdated: String) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SLA Proyectado para el próximo mes", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${String.format("%.1f", projectedSla)}%", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(16.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(if (isTrendNegative) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward, "Tendencia", tint = if (isTrendNegative) Color.Red else Color(0xFF16A34A), modifier = Modifier.size(32.dp))
                    Text(if (isTrendNegative) "Tendencia negativa" else "Tendencia positiva", color = if (isTrendNegative) Color.Red else Color(0xFF16A34A), style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Última actualización: $lastUpdated", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
private fun CoefficientsCard(modifier: Modifier = Modifier, slope: Double, intercept: Double) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Coeficientes del Modelo", style = MaterialTheme.typography.titleMedium)
            Text("Parámetros de regresión lineal", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Divider()
            CoefficientItem("Pendiente (m)", String.format("%.4f", slope))
            CoefficientItem("Intersección (b)", String.format("%.4f", intercept))
            Spacer(Modifier.height(8.dp))
            Text("Modelo generado automáticamente", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
private fun CoefficientItem(label: String, value: String) { Column { Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray); Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) } }

@Composable
private fun WarningBanner() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF9C3))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Warning, "Advertencia", tint = Color(0xFFFACC15))
            Text("Advertencia: Predicción inferior al umbral mínimo de cumplimiento.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ActionButtons(uiState: PredictionUiState, onRecalculate: () -> Unit) {
    val context = LocalContext.current
    val onExport = {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val fileName = "Prediccion_SLA_${System.currentTimeMillis()}.pdf"
            val file = File(downloadsDir, fileName)
            val document = Document()
            PdfWriter.getInstance(document, FileOutputStream(file))
            document.open()

            document.add(Paragraph("Reporte de Predicción de Cumplimiento SLA"))
            document.add(Paragraph(" "))
            document.add(Paragraph("Fecha de Generación: ${uiState.lastUpdated}"))
            document.add(Paragraph("Período Base para la Predicción: ${uiState.selectedPeriod}"))
            document.add(Paragraph(" "))
            document.add(Paragraph("--- Resultados de la Predicción ---"))
            document.add(Paragraph("SLA Proyectado para el próximo mes: ${String.format("%.1f", uiState.projectedSla)}%"))
            document.add(Paragraph("Tendencia: ${if (uiState.isTrendNegative) "Negativa" else "Positiva"}"))
            document.add(Paragraph(" "))
            document.add(Paragraph("--- Coeficientes del Modelo de Regresión Lineal ---"))
            document.add(Paragraph("Pendiente (m): ${String.format("%.4f", uiState.slope)}"))
            document.add(Paragraph("Intersección (b): ${String.format("%.4f", uiState.intercept)}"))

            document.close()
            Toast.makeText(context, "Reporte exportado a Descargas/$fileName", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(onClick = onRecalculate, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Refresh, "Recalcular"); Spacer(Modifier.width(8.dp)); Text("Recalcular Predicción") }
        OutlinedButton(onClick = onExport, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Share, "Exportar"); Spacer(Modifier.width(8.dp)); Text("Exportar Resultado") }
    }
}

@Composable
private fun Footer(recordCount: Int) { Text(text = "Fuente de datos: Historial SLA mensual • $recordCount registros procesados", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }

@Composable
private fun NotEnoughDataCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Info, "Info", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Text("No hay suficientes datos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Se necesitan al menos dos meses de datos históricos para generar una predicción.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}
