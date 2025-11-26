package com.example.proyecto1.data.model

/**
 * Representa un documento de la colección 'sla_historico' en Firestore.
 * El constructor vacío es necesario para la deserialización de Firestore.
 */
data class SlaHistorico(
    val mes: Long = 0, // Usamos Long para el número del mes
    val orden: Long = 0, // Usamos Long para el orden
    val porcentajeSla: Double = 0.0 // Usamos Double para el porcentaje
) {
    // Constructor sin argumentos requerido por Firestore
    constructor() : this(0, 0, 0.0)
}