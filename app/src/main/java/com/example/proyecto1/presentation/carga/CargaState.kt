package com.example.proyecto1.presentation.carga

import android.net.Uri

// --- UNIFIED DATA CLASSES FOR CARGA FEATURE ---

data class CargaItemData(
    val codigo: String,
    val rol: String,
    val tipoSla: String,
    val cumplimiento: Float,
    val diasTranscurridos: Int,
    val diasObjetivo: Int, // Campo para almacenar el umbral de días del SLA
    val estado: String,
    val fechaSolicitud: String,
    val fechaIngreso: String,
    val cantidadPorRol: Int = 0 // Campo para el conteo por rol
)

data class CargaSummaryData(
    val totalRegistros: Int,
    val cumplen: Int,
    val noCumplen: Int,
    val porcCumplimiento: Float
)

data class CargaUiState(
    val isLoading: Boolean = false,
    val selectedFileUri: Uri? = null,
    val selectedFileName: String? = null,
    val summary: CargaSummaryData? = null,
    val items: List<CargaItemData> = emptyList(),
    val errorMessage: String? = null,
    val userMessage: String? = null
)
