// Reemplaza el contenido del archivo con esto
package com.example.proyecto1.features.notifications.domain.model

/**
 * Representa una alerta visual del sistema (US-13).
 */
data class VisualAlert(
    val id: String,
    val typeSLA: String,
    val roleAffected: String,
    val status: String, // Ej: "Incumplido", "Por Vencer"
    val delayDays: String, // Ej: "13 días de retraso"
    val criticality: AlertCriticality
)

enum class AlertCriticality {
    CRITICAL,
    WARNING,
    INFO
}