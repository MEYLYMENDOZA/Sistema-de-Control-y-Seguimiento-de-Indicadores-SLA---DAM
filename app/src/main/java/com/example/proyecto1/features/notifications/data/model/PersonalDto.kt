package com.example.proyecto1.features.notifications.data.model

data class PersonalDto(
    val idPersonal: Int,
    val nombres: String,
    val apellidos: String
    // Puedes agregar más campos si la API los manda, pero con esto basta
) {
    // Propiedad auxiliar para mostrar el nombre completo bonito
    val nombreCompleto: String
        get() = "$nombres $apellidos"
}

