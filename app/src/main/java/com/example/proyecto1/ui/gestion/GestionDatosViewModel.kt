package com.example.proyecto1.ui.gestion

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// Unica clase de estado para ambas pantallas
data class GestionUiState(
    // Comun
    val isLoading: Boolean = false,

    // Para CargaDatosScreen
    val dataLoaded: Boolean = false,
    val totalRecords: Int = 0,
    val compliant: Int = 0,
    val nonCompliant: Int = 0,

    // Para GestionDatosScreen
    val records: List<SlaRecord> = emptyList(),
    val selectedRecordIds: Set<String> = emptySet(),
    val searchQuery: String = ""
)

class GestionDatosViewModel(application: Application) : AndroidViewModel(application) {

    var uiState by mutableStateOf(GestionUiState())
        private set

    private val repository = SlaRepository()

    init {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            repository.getSlaRecords().collect { records ->
                uiState = uiState.copy(
                    records = records,
                    isLoading = false
                )
            }
        }
    }

    // --- Lógica de Carga de Archivos ---

    fun onFileSelected(uri: Uri?) {
        if (uri == null) return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
                val records = parseExcelFile(inputStream)
                repository.uploadSlaRecords(records)

                val total = records.size
                val compliantCount = records.count { it.cumple }

                uiState = uiState.copy(
                    dataLoaded = true,
                    totalRecords = total,
                    compliant = compliantCount,
                    nonCompliant = total - compliantCount
                )
            } catch (e: Exception) {
                e.printStackTrace()
                // Aquí se podría actualizar la UI con un mensaje de error
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    private fun parseExcelFile(inputStream: InputStream?): List<SlaRecord> {
        val records = mutableListOf<SlaRecord>()
        val dataFormatter = DataFormatter()
        inputStream.use { stream ->
            val workbook = WorkbookFactory.create(stream)
            val sheet = workbook.getSheetAt(0)
            val headerRow = sheet.getRow(0).cellIterator().asSequence().map { it.stringCellValue.trim() }.toList()

            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i) ?: continue
                val recordMap = headerRow.zip(row.cellIterator().asSequence().map { cell -> dataFormatter.formatCellValue(cell) }.toList()).toMap()

                val fechaSolicitud = recordMap["Fecha Solicitud"] ?: ""
                val fechaIngreso = recordMap["Fecha Ingreso"] ?: ""
                val tipoSla = recordMap["Tipo SLA"] ?: ""

                val (dias, cumple) = calculateSla(fechaSolicitud, fechaIngreso, tipoSla)

                records.add(
                    SlaRecord(
                        id = "", // Firestore generará el ID
                        codigo = recordMap["Código"] ?: "N/A",
                        rol = recordMap["Rol"] ?: "",
                        fechaSolicitud = fechaSolicitud,
                        fechaIngreso = fechaIngreso,
                        tipoSla = tipoSla,
                        diasSla = dias,
                        cumple = cumple
                    )
                )
            }
        }
        return records
    }

    private fun calculateSla(fechaSolicitud: String, fechaIngreso: String, tipoSla: String): Pair<Int, Boolean> {
        try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date1 = dateFormat.parse(fechaSolicitud)
            val date2 = dateFormat.parse(fechaIngreso)
            if (date1 != null && date2 != null) {
                val diff = date2.time - date1.time
                val dias = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
                val cumple = when (tipoSla) {
                    "SLA1" -> dias < 35
                    "SLA2" -> dias < 20
                    else -> false
                }
                return dias to cumple
            }
        } catch (e: Exception) { /* Silently fail */ }
        return 0 to false
    }

    fun onCleanDataClicked() {
        viewModelScope.launch {
            repository.deleteAllRecords()
            uiState = uiState.copy(
                dataLoaded = false,
                totalRecords = 0,
                compliant = 0,
                nonCompliant = 0
            )
        }
    }

    // --- Lógica de Gestión (existente) ---

    fun onSaveRecord(record: SlaRecord) {
        viewModelScope.launch { repository.updateSlaRecord(record) }
    }

    fun onDeleteSelectedClicked() {
        viewModelScope.launch {
            repository.deleteSlaRecords(uiState.selectedRecordIds.toList())
            uiState = uiState.copy(selectedRecordIds = emptySet())
        }
    }

    fun onSearchQueryChanged(query: String) {
        uiState = uiState.copy(searchQuery = query)
    }

    fun onRecordSelectionChanged(recordId: String, isSelected: Boolean) {
        val newSelectedIds = uiState.selectedRecordIds.toMutableSet()
        if (isSelected) newSelectedIds.add(recordId) else newSelectedIds.remove(recordId)
        uiState = uiState.copy(selectedRecordIds = newSelectedIds)
    }

    fun onSelectAllFiltered(filteredIds: List<String>, selectAll: Boolean) {
        val currentSelected = uiState.selectedRecordIds.toMutableSet()
        if (selectAll) currentSelected.addAll(filteredIds) else currentSelected.removeAll(filteredIds.toSet())
        uiState = uiState.copy(selectedRecordIds = currentSelected)
    }
}
