package com.example.proyecto1.ui.notificaciones

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.outlined.MarkAsUnread
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class Notificacion(
    val id: Int,
    val remitente: String,
    val asunto: String,
    val fecha: String,
    val leido: Boolean
)

@Composable
fun NotificacionesScreen() {
    val notificaciones = remember {
        listOf(
            Notificacion(1, "Sistema de Reportes", "Reporte Semanal de SLA (22/11/24) enviado", "Hace 5 minutos", true),
            Notificacion(2, "Sistema de Reportes", "Reporte Mensual de Cumplimiento (Octubre) enviado", "Ayer", true),
            Notificacion(3, "Juan Pérez", "Re: Duda sobre ticket #A-123", "21/11/2024", false),
            Notificacion(4, "Sistema de Alertas", "Alerta: Caída Drástica en SLA1", "20/11/2024", true)
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
                "Historial de Notificaciones",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Aquí encontrarás los reportes enviados y otras notificaciones importantes.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        items(notificaciones) { notificacion ->
            NotificacionCard(notificacion = notificacion)
        }
    }
}

@Composable
fun NotificacionCard(notificacion: Notificacion) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            val icon = if (notificacion.leido) Icons.Default.MarkEmailRead else Icons.Outlined.MarkAsUnread
            val iconColor = if (notificacion.leido) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary

            Icon(
                imageVector = icon,
                contentDescription = if (notificacion.leido) "Leído" else "No leído",
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = notificacion.remitente,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (!notificacion.leido) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = notificacion.fecha,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = notificacion.asunto,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (!notificacion.leido) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
