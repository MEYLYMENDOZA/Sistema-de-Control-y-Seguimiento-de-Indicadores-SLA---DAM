package com.example.proyecto1.presentation.carga

import android.content.Context
import android.net.Uri
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

// El ViewModel ya NO importa nada de org.apache.poi

@HiltViewModel
class CargaViewModel @Inject constructor(
    private val slaRepository: SlaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CargaUiState())
    val uiState = _uiState.asStateFlow()

    fun onFileSelected(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, summary = null, items = emptyList(), errorMessage = null) }

            ExcelHelper.parseExcelFile(context, uri).onSuccess { parsedItems ->
                if (parsedItems.isNotEmpty()) {
                    val rolCounts = parsedItems.groupingBy { it.rol }.eachCount()
                    val finalItems = parsedItems.map { it.copy(cantidadPorRol = rolCounts[it.rol] ?: 0) }

                    val total = finalItems.size
                    val cumplen = finalItems.count { it.estado == "Cumple" }
                    val noCumplen = total - cumplen
                    val porcCumplimiento = if (total > 0) finalItems.map { it.cumplimiento }.average().toFloat() else 0f

                    val summary = CargaSummaryData(total, cumplen, noCumplen, porcCumplimiento)

                    _uiState.update { it.copy(summary = summary, items = finalItems, isLoading = false) }
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
