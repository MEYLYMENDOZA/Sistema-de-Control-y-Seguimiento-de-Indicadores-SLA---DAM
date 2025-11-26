package com.example.proyecto1.data.model

/**
 * Representa la configuración de los límites de SLA en la base de datos (Firestore).
 * El constructor vacío es necesario para que Firestore pueda deserializar los documentos a este objeto.
 */
data class SlaConfig(
    val sla1Limit: Int = 35, // Límite de días para SLA1
    val sla2Limit: Int = 20  // Límite de días para SLA2
) {
    // Constructor sin argumentos requerido por Firestore para la deserialización
    constructor() : this(35, 20)
}