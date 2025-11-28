package com.example.proyecto1.presentation.gestion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.data.SlaRepository
import com.example.proyecto1.presentation.carga.CargaItemData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.max


data class GestionUiState(
    val isLoading: Boolean = false,
    val allItems: List<CargaItemData> = emptyList(),
    val displayedItems: List<CargaItemData> = emptyList(),
    val searchQuery: String = "",
    val selectedItemCodes: Set<String> = emptySet(),
    val itemToEdit: CargaItemData? = null
)

class GestionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GestionUiState())
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedItemCodes = MutableStateFlow<Set<String>>(emptySet())

    init {
        viewModelScope.launch {
            // Combine repository data, search query, and selections to build the final UI state
            combine(
                SlaRepository.slaItems,
                _searchQuery,
                _selectedItemCodes
            ) { allItems, query, selectedCodes ->

                val filteredItems = if (query.isBlank()) {
                    allItems
                } else {
                    allItems.filter {
                        it.codigo.contains(query, ignoreCase = true) ||
                        it.rol.contains(query, ignoreCase = true) ||
                        it.tipoSla.contains(query, ignoreCase = true)
                    }
                }

                GestionUiState(
                    allItems = allItems,
                    displayedItems = filteredItems,
                    searchQuery = query,
                    selectedItemCodes = selectedCodes,
                    isLoading = false // Assuming loading is handled by repository flow
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onToggleAllSelected(isSelected: Boolean) {
        if (isSelected) {
            _selectedItemCodes.update { _uiState.value.displayedItems.map { it.codigo }.toSet() }
        } else {
            _selectedItemCodes.update { emptySet() }
        }
    }

    fun onToggleItemSelected(code: String, isSelected: Boolean) {
        _selectedItemCodes.update { currentCodes ->
            if (isSelected) {
                currentCodes + code
            } else {
                currentCodes - code
            }
        }
    }

    fun deleteSelectedItems() {
        SlaRepository.deleteItems(_selectedItemCodes.value)
        _selectedItemCodes.value = emptySet() // Clear selection after deletion
    }

    fun onEditItem(item: CargaItemData) {
        _uiState.update { it.copy(itemToEdit = item) }
    }

    fun onDismissEditDialog() {
        _uiState.update { it.copy(itemToEdit = null) }
    }

    fun onSaveChanges(editedItem: CargaItemData) {
        // Recalculate SLA based on potentially new dates or SLA type
        val slaTargets = mapOf("SLA1" to 35L, "SLA2" to 20L)
        val targetDays = slaTargets[editedItem.tipoSla] ?: 0L
        
        val cumple = editedItem.diasTranscurridos >= 0 && editedItem.diasTranscurridos < targetDays
        val estado = if (cumple) "Cumple" else "No Cumple"
        
        val cumplimiento = when {
            cumple -> 100.0f
            targetDays <= 0 -> 0.0f
            else -> {
                val ratio = editedItem.diasTranscurridos.toFloat() / targetDays.toFloat()
                max(0f, (2f - ratio) * 50f)
            }
        }

        val fullyUpdatedItem = editedItem.copy(estado = estado, cumplimiento = cumplimiento)

        SlaRepository.updateSingleItem(fullyUpdatedItem)
        onDismissEditDialog()
    }
}
