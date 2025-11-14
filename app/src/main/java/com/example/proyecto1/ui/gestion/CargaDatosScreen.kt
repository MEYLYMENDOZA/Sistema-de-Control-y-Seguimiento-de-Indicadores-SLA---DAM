package com.example.proyecto1.ui.gestion

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CargaDatosScreen(viewModel: GestionDatosViewModel) {
    val uiState = viewModel.uiState

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> viewModel.onFileSelected(uri) }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UploadControlCard(
                onSelectFileClick = { filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") },
                onCleanDataClick = { viewModel.onCleanDataClicked() }
            )

            AnimatedVisibility(visible = uiState.dataLoaded) {
                SummaryMetricsGrid(state = uiState)
            }
        }

        if (uiState.isLoading) {
            Surface(color = Color.Black.copy(alpha = 0.4f), modifier = Modifier.fillMaxSize()) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}


// =================================================================
// COMPONENTES DE UI (REUTILIZADOS Y NUEVOS)
// =================================================================

@Composable
fun UploadControlCard(onSelectFileClick: () -> Unit, onCleanDataClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Cargar Archivo", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Sube un archivo (.xlsx) con los indicadores SLA.", fontSize = 14.sp, color = Color.Gray)

            ActionButton(
                text = "Seleccionar Archivo",
                icon = Icons.Default.UploadFile,
                isPrimary = true,
                onClick = onSelectFileClick
            )
            ActionButton(
                text = "Limpiar Datos",
                icon = Icons.Default.DeleteSweep,
                backgroundColor = Color(0xFFFBE9E7),
                contentColor = Color(0xFFD32F2F),
                onClick = onCleanDataClick
            )
            FormatInfoCard()
        }
    }
}

@Composable
fun SummaryMetricsGrid(state: GestionDatosState) {
    val percentage = if (state.totalRecords > 0) (state.compliant.toFloat() / state.totalRecords) * 100 else 0f

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Resumen de Datos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricCard(
                label = "Total Registros",
                value = state.totalRecords.toString(),
                icon = Icons.Default.Article,
                iconColor = Color(0xFF42A5F5),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "Cumplen",
                value = state.compliant.toString(),
                icon = Icons.Default.CheckCircle,
                iconColor = Color(0xFF66BB6A),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricCard(
                label = "No Cumplen",
                value = state.nonCompliant.toString(),
                icon = Icons.Default.Cancel,
                iconColor = Color(0xFFEF5350),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "% Cumplimiento",
                value = "${String.format("%.1f", percentage)}%",
                icon = Icons.Default.PieChart,
                iconColor = if (percentage >= 70) Color(0xFF66BB6A) else Color(0xFFFFA726),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, icon: ImageVector, iconColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(), // Use the modifier here
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 14.sp, color = Color.Gray)
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ActionButton(text: String, icon: ImageVector, onClick: () -> Unit, isPrimary: Boolean = false, backgroundColor: Color = if (isPrimary) Color.Black else Color.White, contentColor: Color = if (isPrimary) Color.White else Color.Black) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor, contentColor = contentColor),
        border = if (!isPrimary) BorderStroke(1.dp, Color.LightGray) else null
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun FormatInfoCard() {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFF5F5F5)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = "Formato", tint = Color.DarkGray)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Formato esperado", fontWeight = FontWeight.Bold, color = Color.Black)
            }
            Text("• Columnas: Rol, Fecha Solicitud, Fecha Ingreso, Tipo SLA, Código (Opcional).", fontSize = 13.sp, color = Color.DarkGray, lineHeight = 18.sp)
            Text("• Tipo SLA debe ser \"SLA1\" o \"SLA2\".", fontSize = 13.sp, color = Color.DarkGray, lineHeight = 18.sp)
        }
    }
}
