package com.example.proyecto1.presentation.carga

import android.net.Uri

// Este archivo contiene todas las clases de datos que definen el estado de la UI de CargaScreen.

data class CargaUiState(
    // Estado del archivo seleccionado
    val selectedFileUri: Uri? = null,
    val selectedFileName: String? = null,

    // Estado del resumen y los datos cargados
    val summary: CargaSummaryData? = null,
    val items: List<CargaItemData> = emptyList(),

    // Estado de la UI
    val isLoading: Boolean = false,
    val userMessage: String? = null,
    val errorMessage: String? = null,
    val fileName: String? = null
)

data class CargaSummaryData(
    val total: Int, val cumplen: Int, val noCumplen: Int, val cumplimiento: Float
)

data class CargaItemData(
    val codigo: String,
    val rol: String,
    val tipoSla: String,
    val cumplimiento: Float,
    val diasTranscurridos: Int,
    val cantidadPorRol: Int,
    val estado: String,
    // CAMPOS NUEVOS PARA MOSTRAR EN LA TARJETA DE GESTIÓN
    val fechaSolicitud: String,
    val fechaIngreso: String
)
