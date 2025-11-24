package com.example.proyecto1.ui.tendencia

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TendenciaSLAScreen() {
    val monthlyData = remember {
        listOf(
            "Ene" to 95f,
            "Feb" to 92f,
            "Mar" to 93f,
            "Abr" to 90f,
            "May" to 88f,
            "Jun" to 91f,
            "Jul" to 94f
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Tendencia de Cumplimiento SLA",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Evolución histórica del porcentaje de cumplimiento de SLA a lo largo del tiempo.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Cumplimiento Mensual (%)", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    LineChart(data = monthlyData)
                    Spacer(modifier = Modifier.height(16.dp))
                    ChartLegend()
                }
            }
        }
    }
}

@Composable
fun LineChart(data: List<Pair<String, Float>>) {
    val maxValue = 100f
    val minValue = 0f
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) { 
            val stepX = size.width / (data.size - 1)
            val stepY = size.height / (maxValue - minValue)

            // Draw grid lines
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            for (i in 0..4) {
                val y = size.height - (i * (size.height / 4))
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f,
                    pathEffect = pathEffect
                )
            }

            // Draw line chart
            for (i in 0 until data.size - 1) {
                val startX = i * stepX
                val startY = size.height - ((data[i].second - minValue) * stepY)
                val endX = (i + 1) * stepX
                val endY = size.height - ((data[i + 1].second - minValue) * stepY)
                
                drawLine(
                    color = primaryColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 3f
                )
                drawCircle(color = primaryColor, radius = 6f, center = Offset(startX, startY))
            }
            drawCircle(color = primaryColor, radius = 6f, center = Offset((data.size - 1) * stepX, size.height - ((data.last().second - minValue) * stepY)))
        }
    }
}

@Composable
fun ChartLegend() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Circle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("% de Cumplimiento", style = MaterialTheme.typography.bodySmall)
    }
}
