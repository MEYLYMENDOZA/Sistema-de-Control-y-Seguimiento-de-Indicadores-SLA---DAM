package com.example.proyecto1.data.model

/**
 * Representa el documento único de configuración de límites de SLA en Firestore.
 * El constructor vacío es necesario para la deserialización de Firestore.
 */
data class SlaLimits(
    val limite_sla1: Int = 35,
    val limite_sla2: Int = 20
) {
    // Constructor sin argumentos requerido por Firestore
    constructor() : this(35, 20)
}