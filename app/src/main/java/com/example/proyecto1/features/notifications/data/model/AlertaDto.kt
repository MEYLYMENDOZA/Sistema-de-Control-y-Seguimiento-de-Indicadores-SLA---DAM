package com.example.proyecto1.features.notifications.data.model

import com.example.proyecto1.features.notifications.domain.model.AlertCriticality
import com.example.proyecto1.features.notifications.domain.model.VisualAlert

data class AlertaDto(
    // Estos nombres deben ser IDÉNTICOS a los de tu imagen del navegador
    val idAlerta: Int,
    val idSolicitud: Int,
    val nivel: String,
    val mensaje: String,
    val fechaCreacion: String
) {
    fun toDomain(): VisualAlert {
        // Lógica visual mejorada:
        // Si es una alerta manual (idSolicitud == 1), mostrar "Alerta Manual #idAlerta"
        // Si no, mostrar "Solicitud #idSolicitud - Activa"
        val typeSLATitle = if (idSolicitud == 1) {
            "Alerta Manual #$idAlerta"
        } else {
            "Solicitud #$idSolicitud - Activa"
        }

        return VisualAlert(
            id = idAlerta.toString(),
            typeSLA = typeSLATitle,
            roleAffected = "Sistema",
            status = "Activa",
            delayDays = mensaje,
            criticality = when (nivel.uppercase()) {
                "ALTO" -> AlertCriticality.CRITICAL
                "MEDIO" -> AlertCriticality.WARNING
                "BAJO" -> AlertCriticality.INFO
                else -> AlertCriticality.INFO
            }
        )
    }
}