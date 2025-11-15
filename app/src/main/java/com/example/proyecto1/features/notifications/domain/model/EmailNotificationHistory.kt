package com.example.proyecto1.features.notifications.domain.model

/**
 * Representa un registro en el historial de envíos de correo (US-14).
 */
data class EmailNotificationHistory(
    val id: String,
    val reportTitle: String, // Ej: "Reporte de Indicadores SLA – 10/11/2025"
    val recipient: String, // Ej: "admin@empresa.com"
    val pdfName: String, // Ej: "Reporte_SLA_2025-11-10.pdf"
    val dateSent: String, // Ej: "10 nov 2025, 16:16"
    val status: NotificationStatus
)

enum class NotificationStatus {
    SENT,
    PENDING,
    FAILED
}