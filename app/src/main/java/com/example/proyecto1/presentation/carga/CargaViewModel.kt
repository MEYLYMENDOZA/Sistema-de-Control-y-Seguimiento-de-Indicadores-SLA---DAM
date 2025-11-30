package com.example.proyecto1.presentation.carga

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.SlaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CargaViewModel @Inject constructor(
    private val slaRepository: SlaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CargaUiState())
    val uiState = _uiState.asStateFlow()

    // PASO 1: El usuario selecciona el archivo. Solo guardamos su información.
    fun onFileSelected(context: Context, uri: Uri) {
        // Obtenemos el nombre del archivo desde la URI
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        val name = cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) it.getString(nameIndex) else "Archivo seleccionado"
            } else {
                "Archivo seleccionado"
            }
        } ?: "Archivo seleccionado"

        _uiState.update {
            it.copy(
                selectedFileUri = uri,
                selectedFileName = name,
                summary = null, // Limpiamos resultados anteriores
                items = emptyList(),
                errorMessage = null
            )
        }
    }

    // PASO 2: El usuario confirma y pulsa el botón "Procesar".
    // Los datos se cargan en el repositorio local para que GESTIÓN los pueda usar.
    fun procesarArchivoSeleccionado(context: Context) {
        val uri = _uiState.value.selectedFileUri ?: return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            ExcelHelper.parseExcelFile(context, uri).onSuccess { parsedItems ->
                if (parsedItems.isNotEmpty()) {
                    val rolCounts = parsedItems.groupingBy { it.rol }.eachCount()
                    val finalItems = parsedItems.map { it.copy(cantidadPorRol = rolCounts[it.rol] ?: 0) }

                    val total = finalItems.size
                    val cumplen = finalItems.count { it.estado == "Cumple" }
                    val noCumplen = total - cumplen
                    val porcCumplimiento = if (total > 0) finalItems.map { it.cumplimiento }.average().toFloat() else 0f

                    val summary = CargaSummaryData(total, cumplen, noCumplen, porcCumplimiento)

                    _uiState.update { it.copy(summary = summary, items = finalItems, isLoading = false, userMessage = "Archivo procesado. Vaya a Gestión para editar y subir los datos.") }
                    // Los datos se guardan en el repositorio para que la pantalla de Gestión los use.
                    slaRepository.replaceItemsWith(finalItems)

                } else {
                    _uiState.update { it.copy(errorMessage = "No se encontraron datos válidos en el archivo.", isLoading = false) }
                    slaRepository.clearAll()
                }
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message, isLoading = false) }
                slaRepository.clearAll()
            }
        }
    }

    // LA FUNCIÓN 'subirDatosAGuardar' HA SIDO ELIMINADA. NO PERTENECE A ESTA PANTALLA.

    fun downloadTemplate(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            ExcelHelper.downloadTemplate(context).onSuccess {
                _uiState.update { it.copy(userMessage = "Plantilla guardada en Descargas") }
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    fun clearData() {
        _uiState.value = CargaUiState()
        slaRepository.clearAll()
    }
    fun userMessageShown() { _uiState.update { it.copy(userMessage = null, errorMessage = null) } }

    internal fun setUiStateForPreview(newState: CargaUiState) { _uiState.value = newState }
}
