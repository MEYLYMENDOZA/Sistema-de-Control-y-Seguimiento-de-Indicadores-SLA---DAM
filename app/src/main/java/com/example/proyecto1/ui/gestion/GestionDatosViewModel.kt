package com.example.proyecto1.ui.gestion

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
}

data class GestionDatosState(
    val isLoading: Boolean = false,
    val dataLoaded: Boolean = false,
    val records: List<SlaRecord> = emptyList(),
    val selectedRecordIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val totalRecords: Int = 0,
    val compliant: Int = 0,
    val nonCompliant: Int = 0
)

class GestionDatosViewModel(application: Application) : AndroidViewModel(application) {

    var uiState by mutableStateOf(GestionDatosState())
        private set

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onFileSelected(uri: Uri?) {
        if (uri == null) return
        uiState = uiState.copy(isLoading = true, dataLoaded = false)

        viewModelScope.launch {
            try {
                val records = withContext(Dispatchers.IO) {
                    parseExcelFile(uri)
                }

                if (records.isEmpty()) {
                    _eventFlow.emit(UiEvent.ShowSnackbar("Error: No se encontraron filas con datos válidos. Verifique el formato del archivo."))
                    uiState = uiState.copy(isLoading = false)
                    return@launch
                }

                val total = records.size
                val compliant = records.count { it.cumple }
                val nonCompliant = total - compliant

                delay(1000)

                uiState = uiState.copy(
                    isLoading = false,
                    dataLoaded = true,
                    records = records,
                    totalRecords = total,
                    compliant = compliant,
                    nonCompliant = nonCompliant
                )
                _eventFlow.emit(UiEvent.ShowSnackbar("¡Archivo procesado con ${records.size} registros!"))

            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Error crítico al leer el archivo: ${e.message}"))
                uiState = uiState.copy(isLoading = false, dataLoaded = false)
            }
        }
    }

    private fun parseExcelFile(uri: Uri): List<SlaRecord> {
        val records = mutableListOf<SlaRecord>()
        val contentResolver = getApplication<Application>().contentResolver
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        contentResolver.openInputStream(uri)?.use { inputStream ->
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)

            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i) ?: continue

                try {
                    val rol = row.getCell(0)?.stringCellValue?.trim() ?: ""
                    val tipoSla = row.getCell(3)?.stringCellValue?.trim() ?: ""
                    var codigo = row.getCell(4)?.stringCellValue?.trim() ?: ""

                    if (rol.isBlank() || tipoSla.isBlank()) continue
                    if (tipoSla !in listOf("SLA1", "SLA2")) continue
                    if (codigo.isBlank()) codigo = "N/A-${UUID.randomUUID().toString().substring(0, 4)}"

                    val fechaSolicitudDate = getCellDateValue(row.getCell(1))
                    val fechaIngresoDate = getCellDateValue(row.getCell(2))

                    if (fechaSolicitudDate == null || fechaIngresoDate == null) continue

                    val diffMillis = fechaIngresoDate.time - fechaSolicitudDate.time
                    val diasSla = TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS).toInt()

                    val cumple = when (tipoSla) {
                        "SLA1" -> diasSla < 35
                        "SLA2" -> diasSla < 20
                        else -> false
                    }

                    records.add(SlaRecord(
                        id = UUID.randomUUID().toString(),
                        codigo = codigo,
                        rol = rol,
                        fechaSolicitud = dateFormat.format(fechaSolicitudDate),
                        fechaIngreso = dateFormat.format(fechaIngresoDate),
                        tipoSla = tipoSla,
                        diasSla = diasSla,
                        cumple = cumple
                    ))
                } catch (e: Exception) {
                    continue
                }
            }
        }
        return records
    }

    private fun getCellDateValue(cell: org.apache.poi.ss.usermodel.Cell?): Date? {
        if (cell == null) return null
        return try {
            when (cell.cellType) {
                CellType.NUMERIC -> if (DateUtil.isCellDateFormatted(cell)) cell.dateCellValue else null
                CellType.STRING -> {
                    // Intenta parsear diferentes formatos de fecha comunes
                    val dateFormats = listOf("dd/MM/yyyy", "d/M/yy", "d-MMM-yy", "yyyy-MM-dd")
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
        uiState = GestionDatosState()
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowSnackbar("Datos limpiados. Puede cargar un nuevo archivo."))
        }
    }

    fun onDeleteSelectedClicked() {
        if (uiState.selectedRecordIds.isEmpty()) return
        val remainingRecords = uiState.records.filterNot { it.id in uiState.selectedRecordIds }
        val deletedCount = uiState.records.size - remainingRecords.size
        val total = remainingRecords.size
        val compliant = remainingRecords.count { it.cumple }
        val nonCompliant = total - compliant
        uiState = uiState.copy(
            records = remainingRecords,
            selectedRecordIds = emptySet(),
            totalRecords = total,
            compliant = compliant,
            nonCompliant = nonCompliant
        )
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowSnackbar("$deletedCount registro(s) eliminado(s)."))
        }
    }

    fun onSearchQueryChanged(query: String) {
        uiState = uiState.copy(searchQuery = query)
    }

    fun onRecordSelectionChanged(recordId: String, isSelected: Boolean) {
        val newSelection = uiState.selectedRecordIds.toMutableSet()
        if (isSelected) newSelection.add(recordId) else newSelection.remove(recordId)
        uiState = uiState.copy(selectedRecordIds = newSelection)
    }

    fun onSelectAllFiltered(filteredIds: List<String>, shouldSelect: Boolean) {
        val currentSelection = uiState.selectedRecordIds.toMutableSet()
        if (shouldSelect) currentSelection.addAll(filteredIds) else currentSelection.removeAll(filteredIds.toSet())
        uiState = uiState.copy(selectedRecordIds = currentSelection)
    }

    fun onSaveRecord(updatedRecord: SlaRecord) {
        val updatedList = uiState.records.map { if (it.id == updatedRecord.id) updatedRecord else it }
        val total = updatedList.size
        val compliant = updatedList.count { it.cumple }
        val nonCompliant = total - compliant

        uiState = uiState.copy(
            records = updatedList,
            totalRecords = total,
            compliant = compliant,
            nonCompliant = nonCompliant
        )
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowSnackbar("Registro ${updatedRecord.codigo} actualizado."))
        }
    }
}