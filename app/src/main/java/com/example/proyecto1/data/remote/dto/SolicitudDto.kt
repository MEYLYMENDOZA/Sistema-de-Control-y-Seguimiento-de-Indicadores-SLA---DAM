package com.example.proyecto1.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO principal que se recibe desde el backend con los datos para el reporte.
 * Coincide con SolicitudReporteDTO.cs del backend.
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
    @SerializedName("configSla")
    val configSla: ConfigSlaDto?,
    // El backend nombra esta propiedad 'rolRegistro'
    @SerializedName("rolRegistro")
    val rol: RolDto?
)

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
 * Coincide con RolDTO.cs del backend.
 */
data class RolDto(
    // El backend nombra esta propiedad 'nombreRol'
    @SerializedName("nombreRol")
    val nombre: String?
)
