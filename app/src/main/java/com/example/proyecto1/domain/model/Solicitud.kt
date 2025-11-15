package com.example.proyecto1.domain.model

import com.google.firebase.Timestamp

data class Solicitud(
    val id: String = "",
    val idPersonal: String = "",
    val idRolRegistro: String = "",
    val idSla: String = "",
    val idArea: String = "",
    val idEstadoSolicitud: String = "",
    val fechaSolicitud: Timestamp? = null,
    val fechaIngreso: Timestamp? = null,
    val numDiasSla: Int = 0,
    val resumenSla: String = "",
    val origenDato: String = "",
    val creadoPor: String = "",
    val creadoEn: Timestamp? = null,
    val actualizadoEn: Timestamp? = null,
    val actualizadoPor: String = ""
)

