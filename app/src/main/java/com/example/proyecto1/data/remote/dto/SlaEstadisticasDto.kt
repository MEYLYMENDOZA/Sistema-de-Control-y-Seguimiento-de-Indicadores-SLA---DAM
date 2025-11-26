package com.example.proyecto1.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * DTO para una solicitud individual con sus datos SLA
 * La app Android calculará las estadísticas a partir de estas solicitudes
 */
data class SolicitudSlaDto(
    @SerializedName("idSolicitud")
    val idSolicitud: Int,

    @SerializedName("fechaSolicitud")
    val fechaSolicitud: String, // Formato: "2024-11-25T10:30:00"

    @SerializedName("numDiasSla")
    val numDiasSla: Int,

    @SerializedName("diasUmbral")
    val diasUmbral: Int,

    @SerializedName("idArea")
    val idArea: Int,

    @SerializedName("codigoSla")
    val codigoSla: String
)

