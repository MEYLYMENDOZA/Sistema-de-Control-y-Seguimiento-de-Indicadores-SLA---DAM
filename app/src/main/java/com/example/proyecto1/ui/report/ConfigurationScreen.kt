package com.example.proyecto1.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyecto1.ui.theme.Black
import com.example.proyecto1.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(openDrawer: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = openDrawer) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menú de navegación")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White, titleContentColor = Black)
            )
        }
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
            item { SlaConfigurationCard() }
            item { CurrentValuesCard() }
        }
    }
}

@Composable
fun SlaConfigurationCard() {
    var slaTi by remember { mutableStateOf("25") }
    var slaTs by remember { mutableStateOf("20") }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Configuración de SLA", style = MaterialTheme.typography.titleMedium)
            Text("Ingresa los límites de días para cada tipo de SLA", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = slaTi,
                onValueChange = { slaTi = it },
                label = { Text("SLA-TI (Límite de Días)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = slaTs,
                onValueChange = { slaTs = it },
                label = { Text("SLA-TS (Límite de Días)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Info, contentDescription = "Información")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Información", fontWeight = FontWeight.Bold)
            }
            Text(
                text = "• El SLA-TI representa el tiempo máximo que debe transcurrir desde que se registra un incidente hasta que se soluciona.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "• El SLA-TS corresponde al tiempo de respuesta y seguimiento que se debe realizar sobre un ticket o incidencia.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Black)) {
                Text("Guardar Cambios")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth()) {
                Text("Restaurar Valores")
            }
        }
    }
}

@Composable
fun CurrentValuesCard() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Valores Actuales", style = MaterialTheme.typography.titleMedium)
            Text("Límites de días configurados actualmente", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CurrentValueItem(value = "35", label = "SLA-TI")
                CurrentValueItem(value = "20", label = "SLA-TS")
            }
        }
    }
}

@Composable
fun CurrentValueItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}