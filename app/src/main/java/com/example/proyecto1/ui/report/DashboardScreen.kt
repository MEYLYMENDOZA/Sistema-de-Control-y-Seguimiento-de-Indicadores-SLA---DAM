package com.example.proyecto1.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyecto1.ui.theme.GreenProgress
import com.example.proyecto1.ui.theme.RedProgress
import com.example.proyecto1.ui.theme.SurfaceWhite
import com.example.proyecto1.ui.theme.TextBlack
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, openDrawer: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes") },
                navigationIcon = {
                    IconButton(onClick = openDrawer) {
                        Icon(Icons.Filled.Menu, contentDescription = "Navigation menu")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite, titleContentColor = TextBlack)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* showConfirmationDialog = true */ },
                icon = { Icon(Icons.Filled.Download, "Download") },
                text = { Text(text = "Exportar PDF") },
                containerColor = TextBlack,
                contentColor = SurfaceWhite
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.LightGray.copy(alpha = 0.2f))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GenerateReportCard(
                    navController = navController,
                    showSnackbar = { message ->
                        scope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                )
            }
            item { ReportPreviewCard() }
            item { ComplianceByTypeCard() }
            item { ComplianceByRoleCard() }
            item { Last10RecordsCard() }
        }
    }
}


// ... Rest of the card Composables remain the same ...
@Composable
fun GenerateReportCard(navController: NavController, showSnackbar: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceWhite)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Generar Reportes", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate("report_preview") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = TextBlack, contentColor = SurfaceWhite)
            ) {
                Text("Descargar PDF")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showSnackbar("Funcionalidad de descarga de CSV no implementada.") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Descargar CSV")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showSnackbar("Funcionalidad de impresión no implementada.") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Imp. ahora")
            }
        }
    }
}

@Composable
fun ReportPreviewCard() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceWhite)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Vista Previa del Reporte", style = MaterialTheme.typography.titleMedium)
            Text("Resumen de los indicadores SLA", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Sistema de Control SLA", fontWeight = FontWeight.Bold)
            Text("Fecha de generación: 14/11/2025", style = MaterialTheme.typography.bodySmall)
            Text("Generado por: admin (Administrador)", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                KpiCard("150", "Total Casos")
                KpiCard("116", "Cumplen", Color(0xFFE8F5E9))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                KpiCard("34", "No Cumplen", Color(0xFFFFEBEE))
                KpiCard("77.3%", "% Cumplimiento", Color(0xFFE3F2FD))
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
fun ComplianceByTypeCard() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceWhite)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Cumplimiento por Tipo de SLA", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            val data = listOf(
                "SLA-TI" to 79.2f,
                "SLA-TS" to 77.5f
            )
            data.forEach { (label, value) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = label, modifier = Modifier.weight(1f))
                    LinearProgressIndicator(progress = { value / 100 }, modifier = Modifier.weight(1f))
                    Text(text = "$value%", modifier = Modifier.padding(start = 8.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ComplianceByRoleCard() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceWhite)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Cumplimiento por Rol", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            val roles = listOf(
                "Scrum Master" to 79.1f,
                "Soporte" to 94.1f,
                "Desarrollador" to 85.0f,
                "Gerente" to 78.8f,
                "DevOps" to 96.1f,
                "Analista" to 73.7f,
                "QA" to 88.0f
            )
            roles.forEach { (role, value) ->
                val progressColor = if (value > 80) GreenProgress else RedProgress
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(text = role, modifier = Modifier.weight(1.5f))
                    LinearProgressIndicator(
                        progress = { value / 100 },
                        modifier = Modifier.weight(1f),
                        color = progressColor,
                        trackColor = progressColor.copy(alpha = 0.3f)
                    )
                    Text(text = "$value%", modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun Last10RecordsCard() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceWhite)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Últimos 10 Registros", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Rol", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Solicitud", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("F. Ingreso", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            val records = listOf(
                Triple("Scrum Master", "11/11/2025", "11/11/2025"),
                Triple("Soporte", "11/11/2025", "11/11/2025"),
                Triple("Desarrollador", "11/11/2025", "11/11/2025"),
                Triple("Gerente", "11/11/2025", "11/11/2025"),
                Triple("DevOps", "11/11/2025", "11/11/2025")
            )

            records.forEach { (role, requestDate, entryDate) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(role, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(requestDate, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(entryDate, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
