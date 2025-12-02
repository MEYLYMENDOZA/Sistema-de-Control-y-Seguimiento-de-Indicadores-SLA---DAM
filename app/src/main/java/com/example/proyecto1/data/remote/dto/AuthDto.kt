package com.example.proyecto1.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para login request
 */
data class LoginRequestDto(
    @SerializedName("Username")
    val username: String,

    @SerializedName("Password")
    val password: String
)

/**
 * DTO para login response
 */
data class LoginResponseDto(
    @SerializedName("Success")
    val success: Boolean,

    @SerializedName("Message")
    val message: String,

    @SerializedName("Token")
    val token: String?,

    @SerializedName("Usuario")
    val usuario: UsuarioDto?
)

/**
 * DTO para Usuario
 */
data class UsuarioDto(
    @SerializedName("IdUsuario")
    val idUsuario: Int,

    @SerializedName("Username")
    val username: String,

    @SerializedName("Correo")
    val correo: String,

    @SerializedName("IdRolSistema")
    val idRolSistema: Int,

    @SerializedName("RolNombre")
    val rolNombre: String?,

    @SerializedName("IdEstadoUsuario")
    val idEstadoUsuario: Int,

    @SerializedName("EstadoNombre")
    val estadoNombre: String?,

    @SerializedName("CreadoEn")
    val creadoEn: String?,

    @SerializedName("UltimoLogin")
    val ultimoLogin: String?,

    @SerializedName("Personal")
    val personal: PersonalDto?
)

/**
 * DTO para Personal
 */
data class PersonalDto(
    @SerializedName("IdPersonal")
    val idPersonal: Int,

    @SerializedName("Nombres")
    val nombres: String?,

    @SerializedName("Apellidos")
    val apellidos: String?,

    @SerializedName("Documento")
    val documento: String?,

    @SerializedName("Estado")
    val estado: String?
)

/**
 * DTO para crear usuario
 */
data class CrearUsuarioDto(
    @SerializedName("Username")
    val username: String,

    @SerializedName("Correo")
    val correo: String,

    @SerializedName("Password")
    val password: String,

    @SerializedName("IdRolSistema")
    val idRolSistema: Int,

    @SerializedName("IdEstadoUsuario")
    val idEstadoUsuario: Int,

    @SerializedName("Nombres")
    val nombres: String?,

    @SerializedName("Apellidos")
    val apellidos: String?,

    @SerializedName("Documento")
    val documento: String?,

    @SerializedName("Telefono")
    val telefono: String?
)

/**
 * DTO para lista de usuarios
 */
data class ListaUsuariosResponseDto(
    @SerializedName("Success")
    val success: Boolean,

    @SerializedName("Usuarios")
    val usuarios: List<UsuarioDto>,

    @SerializedName("Total")
    val total: Int
)

/**
 * DTO para roles del sistema
 */
data class RolSistemaDto(
    @SerializedName("IdRolSistema")
    val idRolSistema: Int,

    @SerializedName("Codigo")
    val codigo: String,

    @SerializedName("Nombre")
    val nombre: String,

    @SerializedName("Descripcion")
    val descripcion: String?,

    @SerializedName("EsActivo")
    val esActivo: Boolean
)

/**
 * DTO para estados de usuario
 */
data class EstadoUsuarioDto(
    @SerializedName("IdEstadoUsuario")
    val idEstadoUsuario: Int,

    @SerializedName("Codigo")
    val codigo: String,

    @SerializedName("Descripcion")
    val descripcion: String
)
