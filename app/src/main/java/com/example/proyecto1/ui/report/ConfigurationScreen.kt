package com.example.proyecto1.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
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
import com.example.proyecto1.data.remote.dto.ConfigSlaResponseDto
import com.example.proyecto1.data.remote.dto.ConfigSlaUpdateDto
import com.example.proyecto1.data.repository.SlaRepository
import com.example.proyecto1.ui.theme.Black
import com.example.proyecto1.ui.theme.White


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(
    openDrawer: () -> Unit,
    viewModel: ConfigurationViewModel = viewModel(factory = ConfigurationViewModelFactory(SlaRepository()))
) {
    val uiState by viewModel.uiState.collectAsState()
    val saveStatus by viewModel.saveStatus.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(saveStatus) {
        saveStatus?.onSuccess {
            snackbarHostState.showSnackbar("Cambios guardados exitosamente")
            viewModel.resetSaveStatus()
        }?.onFailure {
            snackbarHostState.showSnackbar("Error al guardar: ${it.message}")
            viewModel.resetSaveStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = { IconButton(onClick = openDrawer) { Icon(Icons.Filled.Menu, "Menú") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White, titleContentColor = Black)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when (val state = uiState) {
            is ConfigUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ConfigUiState.Success -> {
                ConfigurationContent(padding = innerPadding, configs = state.configs, onSave = {
                    viewModel.saveConfigSla(it)
                })
            }
            is ConfigUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.Warning, "Error", tint = Color.Red, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(state.message, color = Color.Red, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun ConfigurationContent(padding: PaddingValues, configs: List<ConfigSlaResponseDto>, onSave: (List<ConfigSlaUpdateDto>) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(Color.LightGray.copy(alpha = 0.2f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp) // Aumentar espacio entre tarjetas
    ) {
        item {
            SlaConfigurationCard(configs, onSave)
        }
    }
}

@Composable
fun SlaConfigurationCard(configs: List<ConfigSlaResponseDto>, onSave: (List<ConfigSlaUpdateDto>) -> Unit) {
    val textFieldsState = remember { mutableStateMapOf<String, String>() }
    val slasToEdit = listOf("SLA1", "SLA2")
    val filteredConfigs = configs.filter { config ->
        slasToEdit.any { it.equals(config.codigoSla.trim(), ignoreCase = true) }
    }

    // Este efecto inicializa el estado la primera vez que los datos se cargan.
    LaunchedEffect(configs) {
        filteredConfigs.forEach { config ->
            textFieldsState[config.codigoSla] = config.diasUmbral.toString()
        }
    }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Settings, contentDescription = "Icono de configuración", modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Configuración de SLA", style = MaterialTheme.typography.titleLarge)
                    Text("Ajusta los límites de días para cada tipo de SLA", style = MaterialTheme.typography.bodySmall)
                }
            }

            filteredConfigs.forEach { config ->
                 SlaInput(label = "${config.codigoSla} - Límite de días", value = textFieldsState[config.codigoSla] ?: "", onValueChange = {
                    textFieldsState[config.codigoSla] = it
                }, description = "El ${config.codigoSla} se cumple si el número de días es menor a este límite.")
            }
            
            InfoCard()

            Button(onClick = {
                val updates = textFieldsState.map { (codigo, dias) ->
                    ConfigSlaUpdateDto(codigo, dias.toIntOrNull() ?: 0)
                }
                onSave(updates)
            }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Black)) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Guardar Cambios")
            }

            // CORRECCIÓN FINAL: Restaurar a valores por defecto (35 y 20)
            OutlinedButton(onClick = {
                textFieldsState.keys.forEach { key ->
                    if (key.equals("SLA1", ignoreCase = true)) {
                        textFieldsState[key] = "35"
                    }
                    if (key.equals("SLA2", ignoreCase = true)) {
                        textFieldsState[key] = "20"
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Restore, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Restaurar Valores")
            }
        }
    }
}

@Composable
private fun SlaInput(label: String, value: String, onValueChange: (String) -> Unit, description: String) {
    Column {
        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("días")
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(description, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun InfoCard() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Filled.Info, contentDescription = "Icono de información", tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Información", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("• El cálculo de días SLA es: Fecha Ingreso - Fecha Solicitud", style = MaterialTheme.typography.bodySmall)
                Text("• SLA1 normalmente tiene un límite mayor que SLA2", style = MaterialTheme.typography.bodySmall)
                Text("• Los cambios afectarán cómo se evalúan futuros datos cargados", style = MaterialTheme.typography.bodySmall)
                Text("• Los datos ya cargados mantienen su evaluación original", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
