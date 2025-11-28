package com.example.proyecto1.presentation.carga

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.max

data class CargaUiState(
    val summary: CargaSummaryData? = null,
    val items: List<CargaItemData> = emptyList(),
    val isLoading: Boolean = false,
    val userMessage: String? = null,
    val errorMessage: String? = null,
    val fileName: String? = null
)

class CargaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CargaUiState())
    val uiState = _uiState.asStateFlow()

    fun onFileSelected(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, summary = null, items = emptyList(), errorMessage = null) }
            try {
                val slaTargets = mapOf("SLA1" to 35L, "SLA2" to 20L)

                val parsedItems = mutableListOf<CargaItemData>()
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val workbook = XSSFWorkbook(inputStream)
                    val sheet = workbook.getSheetAt(0)

                    for (i in 1..sheet.lastRowNum) {
                        val row = sheet.getRow(i) ?: continue
                        try {
                            val codigo = row.getCell(0)?.stringCellValue ?: ""
                            val rol = row.getCell(1)?.stringCellValue ?: ""
                            val fechaSolicitudStr = row.getCell(2)?.stringCellValue ?: ""
                            val fechaIngresoStr = row.getCell(3)?.stringCellValue ?: ""
                            val tipoSla = row.getCell(4)?.stringCellValue ?: ""

                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            val fechaSolicitud = LocalDate.parse(fechaSolicitudStr, formatter)
                            val fechaIngreso = LocalDate.parse(fechaIngresoStr, formatter)

                            val diasTranscurridos = ChronoUnit.DAYS.between(fechaSolicitud, fechaIngreso)
                            val targetDays = slaTargets[tipoSla] ?: 0L

                            val cumple = diasTranscurridos >= 0 && diasTranscurridos < targetDays
                            val estado = if (cumple) "Cumple" else "No Cumple"

                            val cumplimiento = when {
                                cumple -> 100.0f
                                targetDays <= 0 -> 0.0f
                                else -> {
                                    val ratio = diasTranscurridos.toFloat() / targetDays.toFloat()
                                    max(0f, (2f - ratio) * 50f)
                                }
                            }

                            parsedItems.add(
                                CargaItemData(
                                    codigo = codigo, rol = rol, tipoSla = tipoSla,
                                    cumplimiento = cumplimiento,
                                    diasTranscurridos = diasTranscurridos.toInt(),
                                    cantidadPorRol = 0,
                                    estado = estado
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("CargaViewModel", "Error procesando fila $i: ${e.message}")
                            _uiState.update { it.copy(errorMessage = "Error en formato de fila $i. Verifique las fechas (yyyy-MM-dd).") }
                            return@launch
                        }
                    }
                }

                if (parsedItems.isNotEmpty()) {
                    val rolCounts = parsedItems.groupingBy { it.rol }.eachCount()
                    val finalItems = parsedItems.map { it.copy(cantidadPorRol = rolCounts[it.rol] ?: 0) }

                    val total = finalItems.size
                    val cumplen = finalItems.count { it.estado == "Cumple" }
                    val noCumplen = total - cumplen
                    val porcCumplimiento = if (total > 0) finalItems.map { it.cumplimiento }.average().toFloat() else 0f

                    val summary = CargaSummaryData(total, cumplen, noCumplen, porcCumplimiento)

                    _uiState.update { it.copy(summary = summary, items = finalItems) }
                } else {
                     _uiState.update { it.copy(errorMessage = "No se encontraron datos válidos en el archivo.") }
                }

            } catch (e: Exception) {
                Log.e("CargaViewModel", "Error al procesar el archivo", e)
                _uiState.update { it.copy(errorMessage = "No se pudo leer el archivo. ¿Es un .xlsx válido?") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun downloadTemplate(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val workbook: Workbook = XSSFWorkbook()
                val sheet: Sheet = workbook.createSheet("Plantilla SLA")

                val headers = listOf("Código", "Rol", "Fecha Solicitud", "Fecha Ingreso", "Tipo SLA")
                val data = listOf(
                    listOf("SOL-2024-001", "Desarrollador", "2024-01-15", "2024-02-10", "SLA1"),
                    listOf("SOL-2024-002", "Analista", "2024-01-20", "2024-02-05", "SLA2")
                )

                val headerRow = sheet.createRow(0)
                headers.forEachIndexed { index, header -> headerRow.createCell(index).setCellValue(header) }
                data.forEachIndexed { rowIndex, rowData ->
                    val row = sheet.createRow(rowIndex + 1)
                    rowData.forEachIndexed { cellIndex, cellData -> row.createCell(cellIndex).setCellValue(cellData) }
                }

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "plantilla_sla.xlsx")
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    resolver.openOutputStream(it).use { out -> workbook.write(out) }
                    _uiState.update { it.copy(userMessage = "Plantilla guardada en Descargas") }
                }
                workbook.close()
            } catch (e: Exception) {
                Log.e("CargaViewModel", "Error al descargar la plantilla", e)
                _uiState.update { it.copy(errorMessage = "Error al crear la plantilla") }
            }
        }
    }

    fun clearData() { _uiState.value = CargaUiState() }
    fun userMessageShown() { _uiState.update { it.copy(userMessage = null, errorMessage = null) } }

    internal fun setUiStateForPreview(newState: CargaUiState) { _uiState.value = newState }
}
