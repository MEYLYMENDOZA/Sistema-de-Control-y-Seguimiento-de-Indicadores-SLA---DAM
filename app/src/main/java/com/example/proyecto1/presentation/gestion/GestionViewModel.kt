package com.example.proyecto1.presentation.gestion

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.SlaRepository
import com.example.proyecto1.presentation.carga.CargaItemData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.max

data class GestionUiState(
    val isLoading: Boolean = false,
    val allItems: List<CargaItemData> = emptyList(),
    val displayedItems: List<CargaItemData> = emptyList(),
    val searchQuery: String = "",
    val selectedItemCodes: Set<String> = emptySet(),
    val itemToEdit: CargaItemData? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null, // Para mensajes de éxito
    // Estado para el diálogo de edición
    val editedRol: String = "",
    val editedFechaSolicitud: String = "",
    val editedFechaIngreso: String = "",
    val editedTipoSla: String = ""
)

@HiltViewModel
class GestionViewModel @Inject constructor(
    private val slaRepository: SlaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GestionUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            slaRepository.slaItems.collect { allItems ->
                _uiState.update {
                    it.copy(
                        allItems = allItems,
                        displayedItems = filterItems(allItems, it.searchQuery)
                    )
                }
            }
        }
    }

    // --- FUNCIÓN NUEVA PARA SUBIR DATOS ---
    fun subirDatosAGuardar() {
        val itemsToSave = _uiState.value.allItems
        if (itemsToSave.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "No hay datos para subir.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            slaRepository.subirSolicitudes(itemsToSave).onSuccess {
                _uiState.update { it.copy(isLoading = false, successMessage = "¡${itemsToSave.size} registros guardados con éxito en la base de datos!") }
                // Opcional: Limpiar la lista después de guardar
                slaRepository.clearAll()
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }

    private fun filterItems(items: List<CargaItemData>, query: String): List<CargaItemData> {
        if (query.isBlank()) return items
        return items.filter {
            it.codigo.contains(query, ignoreCase = true) ||
            it.rol.contains(query, ignoreCase = true) ||
            it.tipoSla.contains(query, ignoreCase = true)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { 
            it.copy(
                searchQuery = query,
                displayedItems = filterItems(it.allItems, query) 
            )
        }
    }

    fun onToggleAllSelected(isSelected: Boolean) {
        _uiState.update { state ->
            val newSelectedCodes = if (isSelected) state.displayedItems.map { it.codigo }.toSet() else emptySet()
            state.copy(selectedItemCodes = newSelectedCodes)
        }
    }

    fun onToggleItemSelected(code: String, isSelected: Boolean) {
        _uiState.update { state ->
            val newSelectedCodes = if (isSelected) state.selectedItemCodes + code else state.selectedItemCodes - code
            state.copy(selectedItemCodes = newSelectedCodes)
        }
    }

    fun deleteSelectedItems() {
        slaRepository.deleteItems(_uiState.value.selectedItemCodes)
        _uiState.update { it.copy(selectedItemCodes = emptySet()) } 
    }

    fun onEditItem(item: CargaItemData) {
        _uiState.update { 
            it.copy(
                itemToEdit = item,
                editedRol = item.rol,
                editedFechaSolicitud = item.fechaSolicitud,
                editedFechaIngreso = item.fechaIngreso,
                editedTipoSla = item.tipoSla
            )
        }
    }

    fun onDismissEditDialog() {
        _uiState.update { it.copy(itemToEdit = null) }
    }
    
    fun onEditRolChanged(newRol: String) { _uiState.update { it.copy(editedRol = newRol) } }
    fun onEditFechaSolicitudChanged(newDate: String) { _uiState.update { it.copy(editedFechaSolicitud = newDate) } }
    fun onEditFechaIngresoChanged(newDate: String) { _uiState.update { it.copy(editedFechaIngreso = newDate) } }
    fun onEditTipoSlaChanged(newSla: String) { _uiState.update { it.copy(editedTipoSla = newSla) } }
    

    fun onSaveChanges() {
        val originalItem = _uiState.value.itemToEdit ?: return
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        
        try {
            val fechaSolicitud = LocalDate.parse(_uiState.value.editedFechaSolicitud, formatter)
            val fechaIngreso = LocalDate.parse(_uiState.value.editedFechaIngreso, formatter)

            val diasTranscurridos = ChronoUnit.DAYS.between(fechaSolicitud, fechaIngreso)
            val slaTargets = mapOf("SLA1" to 35L, "SLA2" to 20L)
            val targetDays = slaTargets[_uiState.value.editedTipoSla] ?: 0L
            val cumple = diasTranscurridos >= 0 && diasTranscurridos < targetDays
            val estado = if (cumple) "Cumple" else "No Cumple"
            val cumplimiento = when {
                cumple -> 100.0f
                targetDays <= 0 -> 0.0f
                else -> max(0f, (2f - diasTranscurridos.toFloat() / targetDays.toFloat()) * 50f)
            }

            val updatedItem = originalItem.copy(
                rol = _uiState.value.editedRol,
                fechaSolicitud = _uiState.value.editedFechaSolicitud,
                fechaIngreso = _uiState.value.editedFechaIngreso,
                tipoSla = _uiState.value.editedTipoSla,
                diasTranscurridos = diasTranscurridos.toInt(),
                estado = estado,
                cumplimiento = cumplimiento
            )
            
            slaRepository.updateSingleItem(updatedItem)
            onDismissEditDialog()

        } catch (e: Exception) {
            Log.e("GestionViewModel", "Error al guardar cambios", e)
             _uiState.update { it.copy(errorMessage = "Formato de fecha inválido. Use dd/MM/yyyy.") }
        }
    }
    
    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}