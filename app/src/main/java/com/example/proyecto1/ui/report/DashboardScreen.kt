package com.example.proyecto1.ui.report

import android.app.Application
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
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
import com.example.proyecto1.data.repository.SlaRepository
import com.example.proyecto1.ui.theme.Black
import com.example.proyecto1.ui.theme.GreenProgress
import com.example.proyecto1.ui.theme.RedProgress
import com.example.proyecto1.ui.theme.White
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController, 
    openDrawer: () -> Unit,
    reportViewModel: ReportViewModel = viewModel(
        factory = ReportViewModelFactory(LocalContext.current.applicationContext as Application, SlaRepository())
    )
) {
    val uiState by reportViewModel.uiState.collectAsState()
    val exportState by reportViewModel.exportState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Efecto para manejar el resultado de la exportación
    LaunchedEffect(exportState) {
        when (val state = exportState) {
            is ExportState.Success -> {
                snackbarHostState.showSnackbar("PDF generado exitosamente")
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(state.fileUri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("No se encontró una app para abrir PDFs.")
                }
                reportViewModel.resetExportState()
            }
            is ExportState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                reportViewModel.resetExportState()
            }
            is ExportState.Exporting -> {
                snackbarHostState.showSnackbar("Generando PDF...")
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes") },
                navigationIcon = { IconButton(onClick = openDrawer) { Icon(Icons.Filled.Menu, "Menú") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White, titleContentColor = Black)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when (val state = uiState) {
            is ReportUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ReportUiState.Success -> {
                ReportContent(
                    paddingValues = innerPadding,
                    reportData = state.reportData,
                    onExportClick = { reportViewModel.exportarReportePdf() }
                )
            }
            is ReportUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.Warning, "Error", tint = Color.Red, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(state.message, color = Color.Red, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { reportViewModel.fetchReportData() }) {
                        Text("Reintentar")
                    }
                }
            }
        }
    }
}

@Composable
fun ReportContent(
    paddingValues: PaddingValues, 
    reportData: ReporteGeneralDto,
    onExportClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .background(Color.LightGray.copy(alpha = 0.2f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GenerateReportCard(onExportClick = onExportClick)
        }
        item { ReportPreviewCard(reportData.resumen) }
        item { ComplianceByTypeCard(reportData.cumplimientoPorTipo) }
        item { ComplianceByRoleCard(reportData.cumplimientoPorRol) }
        item { Last10RecordsCard(reportData.ultimosRegistros) }
    }
}

@Composable
fun GenerateReportCard(onExportClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Generar Reportes", style = MaterialTheme.typography.titleMedium)
            Button(
                onClick = onExportClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Black)
            ) {
                Icon(Icons.Filled.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Descargar PDF")
            }
        }
    }
}

@Composable
fun ReportPreviewCard(resumen: ResumenEjecutivoDto) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Vista Previa del Reporte", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                KpiCard(resumen.totalCasos.toString(), "Total Casos")
                KpiCard(resumen.cumplen.toString(), "Cumplen", Color(0xFFE8F5E9))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                KpiCard(resumen.noCumplen.toString(), "No Cumplen", Color(0xFFFFEBEE))
                KpiCard("${String.format("%.1f", resumen.porcentajeCumplimiento)}%", "% Cumplimiento", Color(0xFFE3F2FD))
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
fun ComplianceByTypeCard(data: List<CumplimientoPorTipoDto>) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Cumplimiento por Tipo de SLA", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            data.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = item.tipoSla, modifier = Modifier.weight(1f))
                    LinearProgressIndicator(progress = { item.porcentajeCumplimiento.toFloat() / 100f }, modifier = Modifier.weight(1f))
                    Text(text = "${String.format("%.1f", item.porcentajeCumplimiento)}%", modifier = Modifier.padding(start = 8.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ComplianceByRoleCard(roles: List<CumplimientoPorRolDto>) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Cumplimiento por Rol", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            roles.forEach { role ->
                val progressColor = if (role.porcentaje > 80) GreenProgress else RedProgress
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(text = role.rol, modifier = Modifier.weight(1.5f))
                    LinearProgressIndicator(
                        progress = { role.porcentaje.toFloat() / 100f },
                        modifier = Modifier.weight(1f),
                        color = progressColor,
                        trackColor = progressColor.copy(alpha = 0.3f)
                    )
                    Text(text = "${role.completados}/${role.total} (${String.format("%.1f", role.porcentaje)}%)", modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun Last10RecordsCard(records: List<UltimoRegistroDto>) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Últimos 10 Registros", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Rol", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Solicitud", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("F. Ingreso", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            records.forEach { record ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(record.rol, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(record.fechaSolicitud, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(record.fechaIngreso, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
