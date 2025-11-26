package com.example.proyecto1.ui.gestion

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class SlaRecord(
    val id: String,
    val codigo: String,
    val rol: String,
    val fechaSolicitud: String,
    val fechaIngreso: String,
    val tipoSla: String,
    val diasSla: Int,
    val cumple: Boolean
)

data class GestionUiState(
    val isLoading: Boolean = false,
    val dataLoaded: Boolean = false,
    val records: List<SlaRecord> = emptyList(),
    val selectedRecordIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val totalRecords: Int = 0,
    val compliant: Int = 0,
    val nonCompliant: Int = 0,
    val snackbarMessage: String? = null
)

class GestionDatosViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(GestionUiState())
    val uiState: StateFlow<GestionUiState> = _uiState.asStateFlow()

    fun onFileSelected(uri: Uri?) {
        if (uri == null) return
        _uiState.update { it.copy(isLoading = true, dataLoaded = false) }

        viewModelScope.launch {
            try {
                val records = withContext(Dispatchers.IO) { parseExcelFile(uri) }
                if (records.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, snackbarMessage = "Error: No se encontraron datos válidos.") }
                    return@launch
                }
                val total = records.size
                val compliant = records.count { it.cumple }
                val nonCompliant = total - compliant
                delay(1000)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        dataLoaded = true,
                        records = records,
                        totalRecords = total,
                        compliant = compliant,
                        nonCompliant = nonCompliant,
                        snackbarMessage = "¡Archivo procesado con ${records.size} registros!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, dataLoaded = false, snackbarMessage = "Error crítico: ${e.message}") }
            }
        }
    }

    private fun parseExcelFile(uri: Uri): List<SlaRecord> {
        val records = mutableListOf<SlaRecord>()
        getApplication<Application>().contentResolver.openInputStream(uri)?.use { inputStream ->
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)
            val outputDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            for (i in 1..sheet.lastRowNum) {
                try {
                    val row = sheet.getRow(i) ?: continue
                    val rol = row.getCell(0)?.stringCellValue?.trim() ?: ""
                    val tipoSla = row.getCell(3)?.stringCellValue?.trim() ?: ""
                    if (rol.isBlank() || tipoSla.isBlank() || tipoSla !in listOf("SLA1", "SLA2")) continue
                    
                    val fechaSolicitudDate = getCellDateValue(row.getCell(1))
                    val fechaIngresoDate = getCellDateValue(row.getCell(2))
                    if (fechaSolicitudDate == null || fechaIngresoDate == null) continue

                    val diffMillis = fechaIngresoDate.time - fechaSolicitudDate.time
                    val diasSla = TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS).toInt() + 1

                    val limiteSla1 = 35
                    val limiteSla2 = 20
                    val cumple = when (tipoSla) {
                        "SLA1" -> diasSla < limiteSla1
                        "SLA2" -> diasSla < limiteSla2
                        else -> false
                    }

                    records.add(SlaRecord(
                        id = UUID.randomUUID().toString(),
                        codigo = row.getCell(4)?.stringCellValue?.trim() ?: "N/A-${UUID.randomUUID().toString().substring(0, 4)}",
                        rol = rol,
                        fechaSolicitud = outputDateFormat.format(fechaSolicitudDate),
                        fechaIngreso = outputDateFormat.format(fechaIngresoDate),
                        tipoSla = tipoSla,
                        diasSla = diasSla,
                        cumple = cumple
                    ))
                } catch (e: Exception) { continue }
            }
        }
        return records
    }

    private fun getCellDateValue(cell: Cell?): Date? {
        if (cell == null) return null
        return try {
            when (cell.cellType) {
                CellType.NUMERIC -> if (DateUtil.isCellDateFormatted(cell)) cell.dateCellValue else null
                CellType.STRING -> {
                    val dateFormats = listOf("yyyy-MM-dd", "dd/MM/yyyy", "d/M/yy", "d-MMM-yy")
                    for (format in dateFormats) {
                        try {
                            return SimpleDateFormat(format, Locale.getDefault()).parse(cell.stringCellValue)
                        } catch (e: Exception) { /* Intenta el siguiente formato */ }
                    }
                    null
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun onCleanDataClicked() {
        _uiState.value = GestionUiState()
        _uiState.update { it.copy(snackbarMessage = "Datos limpiados.") }
    }

    fun onSnackbarShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onDeleteSelectedClicked() {
        if (_uiState.value.selectedRecordIds.isEmpty()) return
        val remainingRecords = _uiState.value.records.filterNot { it.id in _uiState.value.selectedRecordIds }
        val deletedCount = _uiState.value.records.size - remainingRecords.size
        _uiState.update {
            it.copy(
                records = remainingRecords,
                selectedRecordIds = emptySet(),
                totalRecords = remainingRecords.size,
                compliant = remainingRecords.count { r -> r.cumple },
                nonCompliant = remainingRecords.count { r -> !r.cumple },
                snackbarMessage = "$deletedCount registro(s) eliminado(s)."
            )
        }
    }

    fun onRecordSelectionChanged(recordId: String, isSelected: Boolean) {
        val newSelection = _uiState.value.selectedRecordIds.toMutableSet()
        if (isSelected) newSelection.add(recordId) else newSelection.remove(recordId)
        _uiState.update { it.copy(selectedRecordIds = newSelection) }
    }

    fun onSelectAllFiltered(filteredIds: List<String>, shouldSelect: Boolean) {
        val currentSelection = _uiState.value.selectedRecordIds.toMutableSet()
        if (shouldSelect) currentSelection.addAll(filteredIds) else currentSelection.removeAll(filteredIds.toSet())
        _uiState.update { it.copy(selectedRecordIds = currentSelection) }
    }

    fun onSaveRecord(updatedRecord: SlaRecord) {
        val updatedList = _uiState.value.records.map { if (it.id == updatedRecord.id) updatedRecord else it }
        _uiState.update {
            it.copy(
                records = updatedList,
                compliant = updatedList.count { r -> r.cumple },
                nonCompliant = updatedList.count { r -> !r.cumple },
                snackbarMessage = "Registro ${updatedRecord.codigo} actualizado."
            )
        }
    }
}