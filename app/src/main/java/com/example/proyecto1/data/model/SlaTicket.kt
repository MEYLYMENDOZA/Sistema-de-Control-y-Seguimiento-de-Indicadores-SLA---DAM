package com.example.proyecto1.data.model

import com.google.firebase.Timestamp

/**
 * Representa un ticket de SLA en la base de datos (Firestore).
 * El constructor vacío es necesario para que Firestore pueda deserializar los documentos a este objeto.
 */
data class SlaTicket(
    val rol: String = "",
    val fechaSolicitud: Timestamp? = null,
    val fechaIngreso: Timestamp? = null,
    val tipo: String = "", // "SLA1" o "SLA2"
    val dias: Int = 0,
    val estado: String = "" // "Cumple" o "No Cumple"
) {
    // Constructor sin argumentos requerido por Firestore para la deserialización
    constructor() : this("", null, null, "", 0, "")
}