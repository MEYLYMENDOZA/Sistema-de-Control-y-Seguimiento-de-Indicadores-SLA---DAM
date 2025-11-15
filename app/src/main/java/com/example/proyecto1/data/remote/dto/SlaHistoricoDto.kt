package com.example.proyecto1.data.remote.dto

data class SlaHistoricoDto(
    val mes: String,
    val totalSolicitudes: Int,
    val cumplidas: Int,
    val noCumplidas: Int,
    val porcentajeSla: Double
)
