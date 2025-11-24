package com.example.proyecto1.ui.prediccion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PrediccionSLAScreen() {
    var ticketInput by remember { mutableStateOf("") }
    var predictionResult by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Predicción de Cumplimiento SLA",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Introduce los datos de un nuevo ticket para predecir si cumplirá el SLA.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = ticketInput,
                        onValueChange = { ticketInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Descripción del Ticket") },
                        placeholder = { Text("Ej: Falla en el sistema de facturación") },
                        minLines = 3
                    )

                    Button(
                        onClick = {
                            // Lógica de predicción (simulada)
                            predictionResult = if (ticketInput.isNotBlank()) {
                                if (ticketInput.length % 2 == 0) "CUMPLIRÁ EL SLA" else "NO CUMPLIRÁ EL SLA"
                            } else {
                                null
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Predecir Cumplimiento")
                    }
                }
            }
        }

        if (predictionResult != null) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Resultado de la Predicción:", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = predictionResult!!,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (predictionResult == "CUMPLIRÁ EL SLA") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
