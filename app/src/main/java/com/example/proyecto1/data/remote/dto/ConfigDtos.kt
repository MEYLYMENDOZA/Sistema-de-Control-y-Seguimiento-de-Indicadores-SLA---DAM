package com.example.proyecto1.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para LEER la configuración de SLA desde el backend.
 */
data class ConfigSlaResponseDto(
    @SerializedName("codigoSla")
    val codigoSla: String,
    @SerializedName("diasUmbral")
    val diasUmbral: Int
)

/**
 * DTO para ENVIAR actualizaciones de configuración de SLA al backend.
 */
data class ConfigSlaUpdateDto(
    @SerializedName("codigoSla")
    val codigoSla: String,
    @SerializedName("diasUmbral")
    val diasUmbral: Int
)
