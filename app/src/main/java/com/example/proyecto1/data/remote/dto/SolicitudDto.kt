package com.example.proyecto1.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO principal que se recibe desde el backend con los datos para el reporte.
 * ACTUALIZADO: El backend envía configSla y rol como objetos anidados
 */
data class SolicitudReporteDto(
    @SerializedName("idSolicitud")
    val idSolicitud: Int,
    @SerializedName("fechaSolicitud")
    val fechaSolicitud: String?,
    @SerializedName("fechaIngreso")
    val fechaIngreso: String?,
    @SerializedName("numDiasSla")
    val numDiasSla: Int?,
    @SerializedName("resumenSla")
    val resumenSla: String?,
    @SerializedName("idArea")
    val idArea: Int?,
    // Objetos anidados que vienen del backend
    @SerializedName("configSla")
    val configSla: ConfigSlaDto?,
    @SerializedName("rol")
    val rol: RolDto?
) {
    // Propiedades calculadas para compatibilidad con código existente
    val diasUmbral: Int? get() = configSla?.diasUmbral
    val codigoSla: String? get() = configSla?.codigoSla
}

/**
 * DTO para la configuración del SLA.
 * Coincide con ConfigSlaDTO.cs del backend.
 */
data class ConfigSlaDto(
    @SerializedName("codigoSla")
    val codigoSla: String?,
    @SerializedName("diasUmbral")
    val diasUmbral: Int?
)

/**
 * DTO para el Rol.
 * El backend envía 'nombre' directamente
 */
data class RolDto(
    @SerializedName("nombre")
    val nombre: String?
)


