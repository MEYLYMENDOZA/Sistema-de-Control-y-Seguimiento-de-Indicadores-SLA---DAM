package com.example.proyecto1.domain.model

data class Usuario(
    val id: String = "",
    val username: String = "",
    val correo: String = "",
    val idRolSistema: String = "",
    val idEstadoUsuario: String = "",
    val firebaseUid: String = "" // Referencia a Firebase Auth UID
)

