package com.example.proyecto1.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para la respuesta del endpoint /api/reportes/tendencia
 * US-12: Tendencia y Proyección SLA
 */
data class TendenciaReporteDto(
    @SerializedName("historico")
    val historico: List<PuntoHistoricoDto>,

    @SerializedName("tendencia")
    val tendencia: List<PuntoTendenciaDto>,

    @SerializedName("proyeccion")
    val proyeccion: Double,

    @SerializedName("pendiente")
    val pendiente: Double,

    @SerializedName("intercepto")
    val intercepto: Double,

    @SerializedName("estadoTendencia")
    val estadoTendencia: String, // "positiva", "negativa", "estable"

    @SerializedName("metadata")
    val metadata: MetadataDto?
)

data class PuntoHistoricoDto(
    @SerializedName("mes")
    val mes: String,

    @SerializedName("valor")
    val valor: Double,

    @SerializedName("orden")
    val orden: Int,

    @SerializedName("totalCasos")
    val totalCasos: Int,

    @SerializedName("cumplidos")
    val cumplidos: Int,

    @SerializedName("noCumplidos")
    val noCumplidos: Int
)

data class PuntoTendenciaDto(
    @SerializedName("mes")
    val mes: String,

    @SerializedName("valor")
    val valor: Double,

    @SerializedName("orden")
    val orden: Int
)

data class MetadataDto(
    @SerializedName("totalRegistros")
    val totalRegistros: Int,

    @SerializedName("fechaGeneracion")
    val fechaGeneracion: String
)

