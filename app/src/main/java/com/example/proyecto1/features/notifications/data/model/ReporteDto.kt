package com.example.proyecto1.features.notifications.data.model

import com.example.proyecto1.features.notifications.domain.model.EmailNotificationHistory
import com.example.proyecto1.features.notifications.domain.model.NotificationStatus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

data class ReporteDto(
    val idReporte: Int,
    val tipoReporte: String?,
    val formato: String?,
    val fechaGeneracion: String?
) {
    // Convertimos estos datos al modelo que usa tu pantalla
    fun toDomain(): EmailNotificationHistory {
        // Formatear la fecha de ISO 8601 a formato legible
        val formattedDate = formatearFecha(fechaGeneracion)

        return EmailNotificationHistory(
            id = idReporte.toString(),
            // Título del reporte
            reportTitle = tipoReporte ?: "Reporte Generado",
            // Como la API no nos dice a quién se envió, simulamos que fue al admin
            recipient = "admin@empresa.com",
            // Simulamos el nombre del archivo basándonos en el tipo
            pdfName = "${tipoReporte?.lowercase()}.pdf",
            // Fecha formateada
            dateSent = formattedDate,
            // Si está en la lista, asumimos que fue enviado
            status = NotificationStatus.SENT
        )
    }

    private fun formatearFecha(fechaISO: String?): String {
        return try {
            if (fechaISO.isNullOrBlank()) {
                "Fecha desconocida"
            } else {
                // Parsear desde formato ISO 8601 (ej: "2025-11-26T22:30:51")
                val dateTime = LocalDateTime.parse(fechaISO, DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                // Formatear a formato legible (ej: "26 Nov 2025, 10:30 PM")
                val formatter = DateTimeFormatter
                    .ofPattern("dd MMM yyyy, hh:mm a", Locale.getDefault())
                dateTime.format(formatter)
            }
        } catch (e: DateTimeParseException) {
            // Si hay error al parsear, devolver la fecha original o mensaje de error
            println("Error al formatear fecha: ${e.message}")
            fechaISO ?: "Fecha desconocida"
        } catch (e: Exception) {
            // Capturar cualquier otra excepción
            println("Error inesperado al formatear fecha: ${e.message}")
            "Fecha desconocida"
        }
    }
}