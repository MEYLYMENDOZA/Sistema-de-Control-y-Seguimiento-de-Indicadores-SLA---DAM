package com.example.proyecto1.presentation.carga

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.max

object ExcelHelper {

    fun parseExcelFile(context: Context, uri: Uri): Result<List<CargaItemData>> {
        return try {
            // CORRECCIÓN: Se usan los valores correctos para mantener la consistencia.
            // La solución ideal a futuro sería pasar la configuración desde el ViewModel
            // para no tener estos valores en varios sitios.
            val slaTargets = mapOf("SLA1" to 10L, "SLA2" to 5L)
            val parsedItems = mutableListOf<CargaItemData>()

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val workbook = XSSFWorkbook(inputStream)
                val sheet = workbook.getSheetAt(0)

                // Formateadores de fecha
                val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

                for (i in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(i) ?: continue
                    try {
                        val codigo = row.getCell(0)?.stringCellValue ?: ""
                        val rol = row.getCell(1)?.stringCellValue ?: ""
                        val fechaSolicitudStr = row.getCell(2)?.stringCellValue ?: ""
                        val fechaIngresoStr = row.getCell(3)?.stringCellValue ?: ""
                        val tipoSla = row.getCell(4)?.stringCellValue ?: ""

                        // Parsear las fechas con el formato de entrada
                        val fechaSolicitud = LocalDate.parse(fechaSolicitudStr, inputFormatter)
                        val fechaIngreso = LocalDate.parse(fechaIngresoStr, inputFormatter)

                        val diasTranscurridos = ChronoUnit.DAYS.between(fechaSolicitud, fechaIngreso)
                        val targetDays = slaTargets[tipoSla] ?: 0L

                        val cumple = diasTranscurridos >= 0 && diasTranscurridos <= targetDays
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
                                diasObjetivo = targetDays.toInt(), // <-- PARÁMETRO AÑADIDO
                                cantidadPorRol = 0, // Se calculará después
                                estado = estado,
                                fechaSolicitud = fechaSolicitud.format(outputFormatter),
                                fechaIngreso = fechaIngreso.format(outputFormatter)
                            )
                        )
                    } catch (e: Exception) {
                        Log.e("ExcelHelper", "Error procesando fila $i: ${e.message}")
                        return Result.failure(Exception("Error en formato de fila $i. Verifique las fechas (yyyy-MM-dd)."))
                    }
                }
            }
            Result.success(parsedItems)
        } catch (e: Exception) {
            Log.e("ExcelHelper", "Error al procesar el archivo", e)
            Result.failure(Exception("No se pudo leer el archivo. ¿Es un .xlsx válido?"))
        }
    }

    fun downloadTemplate(context: Context): Result<Unit> {
        return try {
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
            }
            workbook.close()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ExcelHelper", "Error al descargar la plantilla", e)
            Result.failure(Exception("Error al crear la plantilla"))
        }
    }
}
