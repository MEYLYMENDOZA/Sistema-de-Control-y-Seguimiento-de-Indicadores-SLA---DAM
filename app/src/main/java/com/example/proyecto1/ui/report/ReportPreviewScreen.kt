package com.example.proyecto1.ui.report

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyecto1.data.model.SlaHistorico
import com.example.proyecto1.ui.viewmodel.SlaDashboardViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private fun formatDate(timestamp: Timestamp?): String {
    if (timestamp == null) return "N/A"
    val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(navController: NavController, viewModel: SlaDashboardViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            scope.launch {
                val message = if (isGranted) "Permiso concedido. Intenta la descarga de nuevo." else "Permiso denegado. No se puede guardar el archivo."
                snackbarHostState.showSnackbar(message)
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("Error: ${state.error}", textAlign = TextAlign.Center)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Reportes", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))

                // --- TARJETA DE BOTONES RESTAURADA ---
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Generar Reportes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Exporta los datos y análisis en diferentes formatos", style = MaterialTheme.typography.bodyMedium)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button({ 
                                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                } else {
                                    viewModel.generatePdfReport(context)
                                } 
                            }, modifier = Modifier.weight(1f)) { Text("Descargar PDF") }
                            
                            Button({ 
                                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                } else {
                                    viewModel.generateCsvReport(context)
                                }
                            }, modifier = Modifier.weight(1f)) { Text("Descargar CSV") }
                            
                            Button({ 
                                scope.launch { snackbarHostState.showSnackbar("Funcionalidad de impresión no implementada.") }
                            }, modifier = Modifier.weight(1f)) { Text("Imprimir") }
                        }
                    }
                }

                // --- CONTENIDO DEL REPORTE ---
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text("Vista Previa del Reporte", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Resumen de los indicadores SLA", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Sistema de Control SLA")
                            Text("Fecha de generación: ${formatDate(Timestamp.now())}")
                            Text("Generado por: admin (Administrador)")
                        }

                        state.kpiResult?.let { kpis ->
                            Column {
                                Text("Resumen Ejecutivo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                                    KpiCard("Total Casos", kpis.totalCasosCerrados.toString(), Color(0xFFF0F0F0), Modifier.weight(1f))
                                    KpiCard("Cumplen", kpis.casosCumplen.toString(), Color(0xFFE8F5E9), Modifier.weight(1f))
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                                    KpiCard("No Cumplen", kpis.casosNoCumplen.toString(), Color(0xFFFFEBEE), Modifier.weight(1f))
                                    val ptje = if(kpis.totalCasosCerrados > 0) (kpis.casosCumplen.toDouble() / kpis.totalCasosCerrados) * 100 else 0.0
                                    KpiCard("% Cumplimiento", "${String.format("%.1f", ptje)}%", Color(0xFFE3F2FD), Modifier.weight(1f))
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(Modifier.fillMaxWidth()) {
                                    KpiCard("Promedio Días", String.format("%.1f", kpis.promedioDiasResolucion), Color(0xFFF3E5F5), Modifier.weight(1f))
                                    Spacer(Modifier.weight(1f).padding(8.dp))
                                }
                            }

                            Column {
                                Text("Cumplimiento por Tipo de SLA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                kpis.cumplimientoPorTipoSla.forEach { (tipo, prct) ->
                                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(tipo, modifier = Modifier.weight(1.5f))
                                        Text(String.format("%.1f%%", prct), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                    }
                                }
                            }

                            Column {
                                Text("Cumplimiento por Rol", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                kpis.cumplimientoPorRol.forEach { (rol, prct) ->
                                    ProgressBarRow(rol, (prct / 100).toFloat(), "${String.format("%.1f", prct)}%")
                                }
                            }
                        }

                        state.historicoResult?.let { historico ->
                            Column {
                                Text("Últimos 10 Registros (Histórico)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Table(historico.historico.take(10))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color)) {
        Column(Modifier.padding(16.dp).fillMaxWidth(), Arrangement.Center, Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ProgressBarRow(title: String, value: Float, text: String) {
    val color = if (value * 100 >= 80) Color(0xFF4CAF50) else Color(0xFFF44336)
    Column(Modifier.padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(progress = { value }, modifier = Modifier.fillMaxWidth().height(8.dp), color = color, trackColor = color.copy(alpha = 0.3f))
    }
}

@Composable
fun Table(data: List<SlaHistorico>) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Text("Mes", Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text("Orden", Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text("% SLA", Modifier.weight(2f), fontWeight = FontWeight.Bold)
        }
        data.forEach { row ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(row.mes.toString(), Modifier.weight(1f))
                Text(row.orden.toString(), Modifier.weight(1f))
                Text(String.format("%.1f%%", row.porcentajeSla), Modifier.weight(2f))
            }
        }
    }
}
