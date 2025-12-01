package com.example.proyecto1.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO principal que se recibe desde el backend con los datos para el reporte.
 * El backend envía los datos de forma plana (diasUmbral y codigoSla en el root)
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
    @SerializedName("diasUmbral")
    val diasUmbral: Int?,
    @SerializedName("codigoSla")
    val codigoSla: String?,
    @SerializedName("idArea")
    val idArea: Int?,
    @SerializedName("resumenSla")
    val resumenSla: String?,
    @SerializedName("rolRegistro")
    val rol: RolDto? = null
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
