package com.example.proyecto1.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTOs para los filtros dinámicos de Tendencia
 * Todos los datos vienen desde la base de datos
 */

/**
 * DTO para representar un área disponible en filtros de tendencia
 */
data class AreaFiltroDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("nombre")
    val nombre: String
)

/**
 * DTO para representar un tipo de SLA disponible
 */
data class TipoSlaDto(
    @SerializedName("codigo")
    val codigo: String,

    @SerializedName("descripcion")
    val descripcion: String,

    @SerializedName("diasUmbral")
    val diasUmbral: Int
)

/**
 * DTO para representar un período sugerido
 */
data class PeriodoDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("descripcion")
    val descripcion: String,

    @SerializedName("meses")
    val meses: Int
)

