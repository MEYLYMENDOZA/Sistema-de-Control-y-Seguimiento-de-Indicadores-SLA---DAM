package com.example.proyecto1.data

import com.example.proyecto1.presentation.carga.CargaItemData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Singleton object to act as an in-memory database for SLA records.
// This allows data to be shared between different ViewModels (CargaViewModel and GestionViewModel).
object SlaRepository {
    private val _slaItems = MutableStateFlow<List<CargaItemData>>(emptyList())
    val slaItems = _slaItems.asStateFlow()

    fun updateItems(newItems: List<CargaItemData>) {
        _slaItems.update { newItems }
    }

    fun updateSingleItem(updatedItem: CargaItemData) {
        _slaItems.update { currentList ->
            currentList.map {
                if (it.codigo == updatedItem.codigo) {
                    updatedItem
                } else {
                    it
                }
            }
        }
    }

    fun deleteItems(itemCodes: Set<String>) {
        _slaItems.update { currentList ->
            currentList.filterNot { it.codigo in itemCodes }
        }
    }

    fun clearAll() {
        _slaItems.update { emptyList() }
    }
}
