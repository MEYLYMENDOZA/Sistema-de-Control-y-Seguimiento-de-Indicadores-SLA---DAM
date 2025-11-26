package com.example.proyecto1.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SolicitudDto(
    @SerializedName("id_solicitud")
    val idSolicitud: Int,

    @SerializedName("id_personal")
    val idPersonal: Int,

    @SerializedName("id_rol_registro")
    val idRolRegistro: Int,

    @SerializedName("id_sla")
    val idSla: Int,

    @SerializedName("id_area")
    val idArea: Int,

    @SerializedName("id_estado_solicitud")
    val idEstadoSolicitud: Int,

    @SerializedName("fecha_solicitud")
    val fechaSolicitud: String?,

    @SerializedName("fecha_ingreso")
    val fechaIngreso: String?,

    @SerializedName("num_dias_sla")
    val numDiasSla: Int?,

    @SerializedName("resumen_sla")
    val resumenSla: String?,

    @SerializedName("origen_dato")
    val origenDato: String?,

    @SerializedName("creado_en")
    val creadoEn: String?,

    @SerializedName("actualizado_en")
    val actualizadoEn: String?
)

data class SolicitudConDetallesDto(
    @SerializedName("id_solicitud")
    val idSolicitud: Int,

    @SerializedName("fecha_solicitud")
    val fechaSolicitud: String?,

    @SerializedName("fecha_ingreso")
    val fechaIngreso: String?,

    @SerializedName("num_dias_sla")
    val numDiasSla: Int?,

    @SerializedName("resumen_sla")
    val resumenSla: String?,

    @SerializedName("area")
    val area: AreaDto?,

    @SerializedName("estado_solicitud")
    val estadoSolicitud: EstadoSolicitudDto?,

    @SerializedName("config_sla")
    val configSla: ConfigSlaDto?
)

data class AreaDto(
    @SerializedName("id_area")
    val idArea: Int,

    @SerializedName("nombre_area")
    val nombreArea: String,

    @SerializedName("descripcion")
    val descripcion: String?
)

data class EstadoSolicitudDto(
    @SerializedName("id_estado_solicitud")
    val idEstadoSolicitud: Int,

    @SerializedName("codigo")
    val codigo: String,

    @SerializedName("descripcion")
    val descripcion: String?
)

data class ConfigSlaDto(
    @SerializedName("id_sla")
    val idSla: Int,

    @SerializedName("codigo_sla")
    val codigoSla: String,

    @SerializedName("descripcion")
    val descripcion: String?,

    @SerializedName("dias_umbral")
    val diasUmbral: Int?,

    @SerializedName("es_activo")
    val esActivo: Boolean
)

