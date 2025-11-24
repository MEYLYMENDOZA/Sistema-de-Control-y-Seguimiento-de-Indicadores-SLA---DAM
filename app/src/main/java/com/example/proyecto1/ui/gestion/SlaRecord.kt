package com.example.proyecto1.ui.gestion

/**
 * Data class para los registros de SLA.
 * Se han añadido valores por defecto a todas las propiedades para asegurar que tenga
 * un constructor sin argumentos, lo cual es requerido por Firebase Firestore
 * para la deserialización con toObject().
 */
data class SlaRecord(
    val id: String = "",
    val codigo: String = "",
    val rol: String = "",
    val fechaSolicitud: String = "",
    val fechaIngreso: String = "",
    val tipoSla: String = "",
    val diasSla: Int = 0,
    val cumple: Boolean = false
)
