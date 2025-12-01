package com.example.proyecto1.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para recibir datos crudos del backend simplificado
 * US-12: Arquitectura Simplificada - Backend retorna datos crudos
 * La APP calcula: regresión lineal, proyección, tendencia
 */
data class TendenciaDatosDto(
    @SerializedName("tipoSla")
    val tipoSla: String,

    @SerializedName("diasUmbral")
    val diasUmbral: Int,

    @SerializedName("fechaInicio")
    val fechaInicio: String,

    @SerializedName("fechaFin")
    val fechaFin: String,

    @SerializedName("totalSolicitudes")
    val totalSolicitudes: Int,

    @SerializedName("totalMeses")
    val totalMeses: Int,

    @SerializedName("datosMensuales")
    val datosMensuales: List<DatoMensualCrudoDto>
)

/**
 * Datos crudos por mes (sin cálculos de tendencia)
 */
data class DatoMensualCrudoDto(
    @SerializedName("año")
    val anio: Int,

    @SerializedName("mes")
    val mes: Int,

    @SerializedName("mesNombre")
    val mesNombre: String,

    @SerializedName("totalCasos")
    val totalCasos: Int,

    @SerializedName("cumplidos")
    val cumplidos: Int,

    @SerializedName("noCumplidos")
    val noCumplidos: Int,

    @SerializedName("porcentajeCumplimiento")
    val porcentajeCumplimiento: Double
)

/**
 * Resultado de calcular tendencia LOCALMENTE en la app
 */
data class TendenciaCalculadaLocal(
    val historico: List<PuntoHistoricoDto>,
    val lineaTendencia: List<PuntoTendenciaDto>,
    val proyeccion: Double,
    val pendiente: Double,
    val intercepto: Double,
    val estadoTendencia: EstadoTendencia
)

/**
 * Estados posibles de tendencia
 */
enum class EstadoTendencia {
    MEJORANDO,    // Pendiente positiva (m > 0.5)
    EMPEORANDO,   // Pendiente negativa (m < -0.5)
    ESTABLE       // Pendiente cercana a 0 (|m| <= 0.5)
}

