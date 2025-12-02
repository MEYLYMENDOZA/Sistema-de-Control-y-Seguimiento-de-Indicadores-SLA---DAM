package com.example.proyecto1.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para LEER la configuración de SLA desde el backend.
 */
data class ConfigSlaResponseDto(
    @SerializedName("idSla")
    val idSla: Int,
    @SerializedName("codigoSla")
    val codigoSla: String,
    @SerializedName("diasUmbral")
    val diasUmbral: Int
)

/**
 * DTO para ENVIAR actualizaciones de configuración de SLA al backend.
 */
data class ConfigSlaUpdateDto(
    @SerializedName("idSla")
    val idSla: Int,
    @SerializedName("codigoSla")
    val codigoSla: String,
    @SerializedName("diasUmbral")
    val diasUmbral: Int
)

/**
 * Wrapper object para enviar la lista de DTOs de actualización,
 * como lo requiere el endpoint del backend.
 */
data class ConfigSlaUpdateWrapper(
    @SerializedName("dto")
    val dto: List<ConfigSlaUpdateDto>
)
