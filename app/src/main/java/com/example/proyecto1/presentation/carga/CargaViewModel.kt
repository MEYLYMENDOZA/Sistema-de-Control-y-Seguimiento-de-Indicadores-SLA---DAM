package com.example.proyecto1.presentation.carga

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
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

    private val TAG = "CargaViewModel"
    private val _uiState = MutableStateFlow(CargaUiState())
    val uiState = _uiState.asStateFlow()

    // PASO 1: El usuario selecciona el archivo. Solo guardamos su información.
    fun onFileSelected(context: Context, uri: Uri) {
        Log.d(TAG, "🔵 onFileSelected: uri=$uri")
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            val name = cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) it.getString(nameIndex) else "Archivo seleccionado"
                } else {
                    "Archivo seleccionado"
                }
            } ?: "Archivo seleccionado"

            Log.d(TAG, "✅ Archivo seleccionado: $name")
            _uiState.update {
                it.copy(
                    selectedFileUri = uri,
                    selectedFileName = name,
                    summary = null, // Limpiamos resultados anteriores
                    items = emptyList(),
                    errorMessage = null
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al seleccionar archivo", e)
            _uiState.update { it.copy(errorMessage = "Error al seleccionar archivo: ${e.message}") }
        }
    }

    // PASO 2: El usuario confirma y pulsa el botón "Procesar".
    fun procesarArchivoSeleccionado(context: Context) {
        val uri = _uiState.value.selectedFileUri
        Log.d(TAG, "🔵 procesarArchivoSeleccionado: uri=$uri")
        
        if (uri == null) {
            Log.e(TAG, "❌ No hay archivo seleccionado")
            _uiState.update { it.copy(errorMessage = "No hay archivo seleccionado") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "⏳ Iniciando procesamiento...")
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            ExcelHelper.parseExcelFile(context, uri).onSuccess { parsedItems ->
                Log.d(TAG, "✅ Archivo parseado: ${parsedItems.size} items")
                
                if (parsedItems.isNotEmpty()) {
                    val rolCounts = parsedItems.groupingBy { it.rol }.eachCount()
                    val finalItems = parsedItems.map { it.copy(cantidadPorRol = rolCounts[it.rol] ?: 0) }

                    val total = finalItems.size
                    val cumplen = finalItems.count { it.estado == "Cumple" }
                    val noCumplen = total - cumplen
                    val porcCumplimiento = if (total > 0) finalItems.map { it.cumplimiento }.average().toFloat() else 0f

                    val summary = CargaSummaryData(total, cumplen, noCumplen, porcCumplimiento)
                    
                    Log.d(TAG, "📊 Resumen: total=$total, cumplen=$cumplen, noCumplen=$noCumplen, cumplimiento=$porcCumplimiento%")

                    _uiState.update { it.copy(summary = summary, items = finalItems, isLoading = false, userMessage = "Archivo procesado. Vaya a Gestión para editar y subir los datos.") }
                    slaRepository.replaceItemsWith(finalItems)
                    Log.d(TAG, "✅ Datos guardados en repositorio")

                } else {
                    Log.w(TAG, "⚠️ No se encontraron datos válidos")
                    _uiState.update { it.copy(errorMessage = "No se encontraron datos válidos en el archivo.", isLoading = false) }
                    slaRepository.clearAll()
                }
            }.onFailure { error ->
                Log.e(TAG, "❌ Error al procesar archivo", error)
                _uiState.update { it.copy(errorMessage = error.message, isLoading = false) }
                slaRepository.clearAll()
            }
        }
    }

    fun downloadTemplate(context: Context) {
        Log.d(TAG, "🔵 downloadTemplate")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                ExcelHelper.downloadTemplate(context).onSuccess {
                    Log.d(TAG, "✅ Plantilla descargada exitosamente")
                    _uiState.update { it.copy(userMessage = "Plantilla guardada en Descargas") }
                }.onFailure { error ->
                    Log.e(TAG, "❌ Error al descargar plantilla", error)
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción al descargar plantilla", e)
                _uiState.update { it.copy(errorMessage = "Error inesperado: ${e.message}") }
            }
        }
    }

    fun clearData() {
        Log.d(TAG, "🔵 clearData")
        _uiState.value = CargaUiState()
        slaRepository.clearAll()
    }
    
    fun userMessageShown() { _uiState.update { it.copy(userMessage = null, errorMessage = null) } }

    internal fun setUiStateForPreview(newState: CargaUiState) { _uiState.value = newState }
}
