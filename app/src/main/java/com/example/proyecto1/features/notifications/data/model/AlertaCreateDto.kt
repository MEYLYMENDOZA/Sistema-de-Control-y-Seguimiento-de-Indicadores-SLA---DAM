package com.example.proyecto1.features.notifications.data.model

data class AlertaCreateDto(
    val idSolicitud: Int,
    val idTipoAlerta: Int, // 1=SLA, 2=Critica, etc.
    val idEstadoAlerta: Int, // 1=Nueva
    val nivel: String, // "Alto", "Medio"
    val mensaje: String,
    val enviadoEmail: Boolean = false
)