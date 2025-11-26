package com.example.proyecto1.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

/**
 * Representa un documento de la colección 'solicitud' en Firestore.
 * El constructor vacío es necesario para la deserialización de Firestore.
 */
data class Solicitud(
    // Campos principales para KPIs
    val cumple_sla: Boolean = false,
    val rol_asignado: String = "",
    val tipo_sla: String = "",
    val dias_resolucion: Double = 0.0,
    val fecha_solicitud: Timestamp? = null,
    val fecha_cierre: Timestamp? = null,

    // --- CAMPOS CORREGIDOS ---
    // Estos campos son referencias a otros documentos, no Strings.
    val id_estado_solicitud: DocumentReference? = null,
    val id_usuario: DocumentReference? = null,

    // Otros campos
    val descripcion: String = "",
    val titulo: String = ""
) {
    // Constructor sin argumentos requerido por Firestore
    constructor() : this(false, "", "", 0.0, null, null, null, null, "", "")
}