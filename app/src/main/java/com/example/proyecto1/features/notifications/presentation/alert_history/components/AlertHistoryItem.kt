package com.example.proyecto1.features.notifications.presentation.alert_history.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto1.features.notifications.domain.model.AlertCriticality
import com.example.proyecto1.features.notifications.domain.model.VisualAlert

@Composable
fun AlertHistoryItem(
    modifier: Modifier = Modifier,
    alert: VisualAlert,
    onDismiss: (String) -> Unit,
    onClick: (VisualAlert) -> Unit  // ✅ NUEVO: Parámetro para ver detalles
) {
    // Determina el color y el ícono según la criticidad
    val (icon, tintColor) = remember(alert.criticality) {
        when (alert.criticality) {
            AlertCriticality.CRITICAL -> Icons.Default.Error to Color(0xFFEA4335) // Rojo
            AlertCriticality.WARNING -> Icons.Default.Warning to Color(0xFFFBBC05) // Amarillo
            AlertCriticality.INFO -> Icons.Default.CheckCircle to Color(0xFF34A853) // Verde
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(alert) }  // ✅ NUEVO: Clickeable para ver detalles
            .border(2.dp, tintColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Columna 1: Ícono de Criticidad
            Icon(
                imageVector = icon,
                contentDescription = "Criticidad",
                tint = tintColor,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Columna 2: Información de la Alerta
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${alert.typeSLA} - ${alert.status}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rol afectado: ${alert.roleAffected}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Detalle: ${alert.delayDays}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Columna 3: Botón de Cerrar (Dismiss)
            // (Cumple con "El usuario debe poder interactuar con las alertas (cerrar)")
            IconButton(
                onClick = { onDismiss(alert.id) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = "Cerrar Alerta",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}