package com.example.proyecto1.features.notifications.presentation.alert_history.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AlertDetailsDialog(
    alert: VisualAlert,
    onDismiss: () -> Unit
) {
    // Determina el color y el ícono según la criticidad
    val (icon, tintColor) = remember(alert.criticality) {
        when (alert.criticality) {
            AlertCriticality.CRITICAL -> Icons.Default.Error to Color(0xFFEA4335) // Rojo
            AlertCriticality.WARNING -> Icons.Default.Warning to Color(0xFFFBBC05) // Amarillo
            AlertCriticality.INFO -> Icons.Default.CheckCircle to Color(0xFF34A853) // Verde
        }
    }

    // Obtener fecha/hora actual como fecha de detección
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val currentDate = dateFormat.format(Date())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Criticidad",
                    tint = tintColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Detalles de Alerta",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ===== SECCIÓN 1: INFORMACIÓN GENERAL =====
                DetailSection(
                    title = "Información General",
                    backgroundColor = Color(0xFFF5F5F5)
                ) {
                    DetailRow(label = "ID Alerta", value = alert.id)
                    DetailRow(label = "Tipo", value = alert.typeSLA)
                    DetailRow(label = "Estado", value = alert.status)
                }

                // ===== SECCIÓN 2: CRITICIDAD =====
                DetailSection(
                    title = "Nivel de Criticidad",
                    backgroundColor = tintColor.copy(alpha = 0.1f)
                ) {
                    val criticidadTexto = when (alert.criticality) {
                        AlertCriticality.CRITICAL -> "🔴 CRÍTICA (Alto impacto)"
                        AlertCriticality.WARNING -> "🟡 ADVERTENCIA (Medio impacto)"
                        AlertCriticality.INFO -> "🟢 INFORMATIVA (Bajo impacto)"
                        else -> "Estado desconocido"
                    }
                    DetailRow(label = "Nivel", value = criticidadTexto)
                }

                // ===== SECCIÓN 3: DETALLE DEL PROBLEMA =====
                DetailSection(
                    title = "Detalles del Problema",
                    backgroundColor = Color(0xFFF5F5F5)
                ) {
                    DetailRow(label = "Descripción", value = alert.delayDays, multiline = true)
                    DetailRow(label = "Rol/Área Afectada", value = alert.roleAffected)
                }

                // ===== SECCIÓN 4: FECHAS =====
                DetailSection(
                    title = "Fechas",
                    backgroundColor = Color(0xFFF5F5F5)
                ) {
                    DetailRow(label = "Detectada", value = currentDate)
                    DetailRow(label = "Última actualización", value = currentDate)
                }

                // ===== SECCIÓN 5: ACCIONES RECOMENDADAS =====
                DetailSection(
                    title = "Acciones Recomendadas",
                    backgroundColor = Color(0xFFFFF3E0)
                ) {
                    Text(
                        text = when (alert.criticality) {
                            AlertCriticality.CRITICAL -> "⚠️ Esta alerta requiere atención inmediata. Contacta al responsable del área afectada."
                            AlertCriticality.WARNING -> "⚠️ Monitorea esta situación. Puede escalar si no se toman medidas."
                            AlertCriticality.INFO -> "ℹ️ Alerta informativa. Mantén esta información registrada para análisis futuro."
                            else -> "Revisa esta alerta y toma las acciones necesarias."
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = tintColor
                )
            ) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun DetailSection(
    title: String,
    backgroundColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            content()
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    multiline: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = if (multiline) Modifier
                .fillMaxWidth()
                .padding(start = 8.dp) else Modifier.fillMaxWidth()
        )
    }
}

