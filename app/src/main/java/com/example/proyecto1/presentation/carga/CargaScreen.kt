package com.example.proyecto1.presentation.carga

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// Data classes
data class CargaSummaryData(
    val total: Int, val cumplen: Int, val noCumplen: Int, val cumplimiento: Float
)

// **MODELO DE DATOS ACTUALIZADO**
data class CargaItemData(
    val codigo: String, val rol: String, val tipoSla: String, val cumplimiento: Float,
    val diasTranscurridos: Int, // <-- Nuevo nombre
    val cantidadPorRol: Int, val estado: String
)

@Composable
fun CargaScreen(cargaViewModel: CargaViewModel = viewModel()) {
    val uiState by cargaViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage, uiState.errorMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            cargaViewModel.userMessageShown()
        }
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar("Error: $it")
            cargaViewModel.userMessageShown()
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { cargaViewModel.onFileSelected(context, it) }
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F2F5))
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { 
                CargaExcelSection(
                    viewModel = cargaViewModel,
                    onDownloadTemplate = { cargaViewModel.downloadTemplate(context) },
                    onSelectFile = { filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") }
                )
             }

            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            uiState.summary?.let { summary ->
                item { SummarySection(data = summary) }
                item {
                    Text(
                        text = "Datos Cargados",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 4.dp, top = 16.dp)
                    )
                    Text(
                        text = "Resumen de ${summary.total} registros procesados",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                item { DataTableHeader() }
                items(uiState.items) { item ->
                    DataTableRow(item = item)
                }
                item {
                    Text(
                        text = "Mostrando los últimos ${uiState.items.size} registros de ${summary.total} totales",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CargaExcelSection(viewModel: CargaViewModel, onDownloadTemplate: () -> Unit, onSelectFile: () -> Unit) {
    Card(elevation = CardDefaults.cardElevation(0.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Cargar Archivo Excel", style = MaterialTheme.typography.titleMedium)
            Text("Sube un archivo (.xlsx o .csv) con los indicadores SLA.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onSelectFile, modifier = Modifier.weight(1.5f), colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                    Icon(Icons.Default.UploadFile, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Seleccionar Archivo")
                }
                OutlinedButton(onClick = onDownloadTemplate, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Plantilla")
                }
                Button(onClick = { viewModel.clearData() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Text("Limpiar")
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Formato esperado del archivo Excel:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text("Columnas Requeridas:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Text("• Rol: Nombre del rol o área (texto)\n• Fecha Solicitud: fecha de la solicitud (fecha válida)\n• Fecha Ingreso: fecha de ingreso (fecha válida)\n• Tipo SLA: Debe ser exactamente \"SLA1\" o \"SLA2\"", style = MaterialTheme.typography.bodySmall, lineHeight = 20.sp)
            Spacer(Modifier.height(8.dp))
            Text("Columnas Opcionales:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Text("• Código: Código único de solicitud", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color(0xFFFFF3E0), RoundedCornerShape(4.dp)).padding(8.dp)) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFFA726))
                Spacer(Modifier.width(8.dp))
                Text("Importante: El sistema validará todas las columnas y datos. Si se detecta algún error, el archivo será rechazado completamente.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF616161), lineHeight = 18.sp)
            }
        }
    }
}

@Composable
fun SummarySection(data: CargaSummaryData) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        SummaryCard("Total Registros", data.total.toString(), Icons.Default.Article, Color(0xFF42A5F5), modifier = Modifier.weight(1f))
        SummaryCard("Cumplen", data.cumplen.toString(), Icons.Default.CheckCircle, Color(0xFF66BB6A), modifier = Modifier.weight(1f))
        SummaryCard("No Cumplen", data.noCumplen.toString(), Icons.Default.Cancel, Color(0xFFEF5350), modifier = Modifier.weight(1f))
        SummaryCard("% Cumplimiento", String.format("%.1f%%", data.cumplimiento), Icons.Default.Analytics, Color(0xFF42A5F5), modifier = Modifier.weight(1f))
    }
}

@Composable
fun SummaryCard(title: String, value: String, icon: ImageVector, iconColor: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(0.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun DataTableHeader() {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Código", modifier = Modifier.weight(1.5f), color = Color.Gray, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Text("Rol", modifier = Modifier.weight(1.5f), color = Color.Gray, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Text("Tipo SLA", modifier = Modifier.weight(1f), color = Color.Gray, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Text("% Cumplimiento", modifier = Modifier.weight(1.5f), color = Color.Gray, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        // **CABECERA ACTUALIZADA**
        Text("Días Transcurridos", modifier = Modifier.weight(1.5f), color = Color.Gray, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Text("Cantidad por Rol", modifier = Modifier.weight(1.5f), color = Color.Gray, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Text("Estado", modifier = Modifier.weight(1f), color = Color.Gray, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DataTableRow(item: CargaItemData) {
    Surface(shadowElevation = 0.dp, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(vertical = 4.dp), color = Color.White) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(item.codigo, modifier = Modifier.weight(1.5f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(item.rol, modifier = Modifier.weight(1.5f), fontSize = 14.sp)
            Text(item.tipoSla, modifier = Modifier.weight(1f), fontSize = 14.sp)
            Text(String.format("%.1f%%", item.cumplimiento), modifier = Modifier.weight(1.5f), fontSize = 14.sp)
            // **LÓGICA DE LA PÍLDORA ACTUALIZADA**
            Pill(text = "${item.diasTranscurridos} días", color = if (item.estado == "Cumple") Color(0xFFE8F5E9) else Color(0xFFFFEBEE), textColor = if (item.estado == "Cumple") Color(0xFF2E7D32) else Color(0xFFC62828), modifier = Modifier.weight(1.5f))
            Text("${item.cantidadPorRol} personas", modifier = Modifier.weight(1.5f), fontSize = 14.sp)
            Pill(text = item.estado, color = if (item.estado == "Cumple") Color(0xFFE8F5E9) else Color(0xFFFFEBEE), textColor = if (item.estado == "Cumple") Color(0xFF2E7D32) else Color(0xFFC62828), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun Pill(text: String, color: Color, textColor: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(color).padding(horizontal = 8.dp, vertical = 4.dp), contentAlignment = Alignment.Center) {
        Text(text, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F2F5)
@Composable
fun CargaScreenPreview() {
    CargaScreen(cargaViewModel = CargaViewModel())
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F2F5)
@Composable
fun CargaScreenWithDataPreview() {
    val cargaViewModel = CargaViewModel()
    val previewSummary = CargaSummaryData(total = 2, cumplen = 1, noCumplen = 1, cumplimiento = 73.3f)
    val previewData = listOf(
        CargaItemData("SOL-001", "Desarrollador", "SLA1", 100.0f, 26, 1, "Cumple"),
        CargaItemData("SOL-002", "Analista", "SLA2", 46.7f, 16, 1, "No Cumple")
    )
    cargaViewModel.setUiStateForPreview(CargaUiState(summary = previewSummary, items = previewData))
    CargaScreen(cargaViewModel = cargaViewModel)
}
