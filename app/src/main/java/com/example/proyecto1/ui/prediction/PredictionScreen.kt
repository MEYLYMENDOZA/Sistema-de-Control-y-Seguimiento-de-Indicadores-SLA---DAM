package com.example.proyecto1.ui.prediction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyecto1.ui.gestion.GestionDatosViewModel

@Composable
fun PredictionScreen(
    gestionViewModel: GestionDatosViewModel,
    predictionViewModel: PredictionViewModel
) {
    val gestionState by gestionViewModel.uiState.collectAsState()
    val predictionState by predictionViewModel.uiState.collectAsState()

    LaunchedEffect(gestionState.records) {
        predictionViewModel.updateSlaRecords(gestionState.records)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Análisis Predictivo", style = MaterialTheme.typography.headlineSmall)

        Card(
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (predictionState.predictionEnabled) {
                    Text("Tendencia de Días SLA", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Pendiente (m): ${String.format("%.2f", predictionState.predictionSlope)}", style = MaterialTheme.typography.bodyLarge)
                    Text("Intersección (b): ${String.format("%.2f", predictionState.predictionIntercept)}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Predicción para el próximo registro:", style = MaterialTheme.typography.titleMedium)
                    Text("${String.format("%.1f", predictionState.nextSlaPrediction)} días", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                } else {
                    Text("No hay suficientes datos para realizar una predicción (se necesitan al menos 2 registros).")
                }
            }
        }
    }
}