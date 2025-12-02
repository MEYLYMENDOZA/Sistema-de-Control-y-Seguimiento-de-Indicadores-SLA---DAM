package com.example.proyecto1.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para login request
 */
data class LoginRequestDto(
    @SerializedName("username")
    val username: String,

    @SerializedName("password")
    val password: String
)

/**
 * DTO para login response
 */
data class LoginResponseDto(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("token")
    val token: String?,

    @SerializedName("usuario")
    val usuario: UsuarioDto?
)

/**
 * DTO para Usuario
 */
data class UsuarioDto(
    @SerializedName("idUsuario")
    val idUsuario: Int,

    @SerializedName("username")
    val username: String,

    @SerializedName("correo")
    val correo: String,

    @SerializedName("idRolSistema")
    val idRolSistema: Int,

    @SerializedName("rolNombre")
    val rolNombre: String?,

    @SerializedName("idEstadoUsuario")
    val idEstadoUsuario: Int,

    @SerializedName("estadoNombre")
    val estadoNombre: String?,

    @SerializedName("creadoEn")
    val creadoEn: String?,

    @SerializedName("ultimoLogin")
    val ultimoLogin: String?,

    @SerializedName("personal")
    val personal: PersonalDto?
)

/**
 * DTO para Personal
 */
data class PersonalDto(
    @SerializedName("idPersonal")
    val idPersonal: Int,

    @SerializedName("nombres")
    val nombres: String?,

    @SerializedName("apellidos")
    val apellidos: String?,

    @SerializedName("documento")
    val documento: String?,

    @SerializedName("estado")
    val estado: String?
)

/**
 * DTO para crear usuario
 */
data class CrearUsuarioDto(
    @SerializedName("username")
    val username: String,

    @SerializedName("correo")
    val correo: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("idRolSistema")
    val idRolSistema: Int,

    @SerializedName("idEstadoUsuario")
    val idEstadoUsuario: Int,

    @SerializedName("nombres")
    val nombres: String?,

    @SerializedName("apellidos")
    val apellidos: String?,

    @SerializedName("documento")
    val documento: String?,

    @SerializedName("telefono")
    val telefono: String?
)

/**
 * DTO para lista de usuarios
 */
data class ListaUsuariosResponseDto(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("usuarios")
    val usuarios: List<UsuarioDto>,

    @SerializedName("total")
    val total: Int
)

/**
 * DTO para roles del sistema
 */
data class RolSistemaDto(
    @SerializedName("idRolSistema")
    val idRolSistema: Int,

    @SerializedName("codigo")
    val codigo: String,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("descripcion")
    val descripcion: String?,

    @SerializedName("esActivo")
    val esActivo: Boolean
)

/**
 * DTO para estados de usuario
 */
data class EstadoUsuarioDto(
    @SerializedName("idEstadoUsuario")
    val idEstadoUsuario: Int,

    @SerializedName("codigo")
    val codigo: String,

    @SerializedName("descripcion")
    val descripcion: String
)

