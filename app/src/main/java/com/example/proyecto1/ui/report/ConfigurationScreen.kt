package com.example.proyecto1.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.* // Importa RowScope, ColumnScope, etc.
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Importa todos los iconos, incluyendo Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto1.ui.viewmodel.SlaLimitsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(viewModel: SlaLimitsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.error, uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            scope.launch { snackbarHostState.showSnackbar("¡Límites guardados con éxito!") }
            viewModel.resetUpdateStatus()
        }
        if (uiState.error != null) {
            scope.launch { snackbarHostState.showSnackbar("Error: ${uiState.error}") }
            viewModel.resetUpdateStatus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .background(Color.LightGray.copy(alpha = 0.2f))
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ConfigurationCard(
                    limits = uiState.limits, 
                    onSave = { sla1, sla2 -> viewModel.updateSlaLimits(sla1, sla2) },
                    onRestore = { viewModel.restoreDefaultLimits() }
                )
                CurrentValuesCards(uiState.limits)
            }
        }
    }
}

@Composable
fun ConfigurationCard(
    limits: com.example.proyecto1.data.model.SlaLimits?,
    onSave: (String, String) -> Unit,
    onRestore: () -> Unit
) {
    var sla1 by remember(limits) { mutableStateOf(limits?.limite_sla1?.toString() ?: "") }
    var sla2 by remember(limits) { mutableStateOf(limits?.limite_sla2?.toString() ?: "") }

    LaunchedEffect(limits) {
        sla1 = limits?.limite_sla1?.toString() ?: ""
        sla2 = limits?.limite_sla2?.toString() ?: ""
    }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings Icon", modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Configuración de SLA", style = MaterialTheme.typography.titleLarge)
                    Text("Ajusta los límites de días para cada tipo de SLA", style = MaterialTheme.typography.bodySmall)
                }
            }

            Text("SLA1 - Límite de días", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = sla1, onValueChange = { sla1 = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                Text("días", modifier = Modifier.padding(start = 8.dp))
            }
            Text("El SLA1 se cumple si el número de días es menor a este límite", style = MaterialTheme.typography.bodySmall)

            Text("SLA2 - Límite de días", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = sla2, onValueChange = { sla2 = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                Text("días", modifier = Modifier.padding(start = 8.dp))
            }
            Text("El SLA2 se cumple si el número de días es menor a este límite", style = MaterialTheme.typography.bodySmall)

            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Info, contentDescription = "Info")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Información", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• El cálculo de días SLA es: Fecha Ingreso - Fecha Solicitud", style = MaterialTheme.typography.bodySmall)
                    Text("• SLA1 normalmente tiene un límite mayor que SLA2", style = MaterialTheme.typography.bodySmall)
                    Text("• Los cambios afectarán cómo se evalúan futuros datos cargados", style = MaterialTheme.typography.bodySmall)
                    Text("• Los datos ya cargados mantienen su evaluación original", style = MaterialTheme.typography.bodySmall)
                }
            }

            Button(onClick = { onSave(sla1, sla2) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                Text("Guardar Cambios")
            }
            OutlinedButton(onClick = { onRestore() }, modifier = Modifier.fillMaxWidth()) { 
                Text("Restaurar Valores")
            }
        }
    }
}

@Composable
fun CurrentValuesCards(limits: com.example.proyecto1.data.model.SlaLimits?) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Valores Actuales", style = MaterialTheme.typography.titleLarge)
            Text("Límites de días configurados actualmente", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))
            
            val sla1Text = limits?.limite_sla1?.toString() ?: "N/A"
            val sla2Text = limits?.limite_sla2?.toString() ?: "N/A"

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("SLA1", style = MaterialTheme.typography.titleMedium)
                    Text(sla1Text, style = MaterialTheme.typography.displayMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Text("días máximos", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("SLA2", style = MaterialTheme.typography.titleMedium)
                    Text(sla2Text, style = MaterialTheme.typography.displayMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Text("días máximos", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}