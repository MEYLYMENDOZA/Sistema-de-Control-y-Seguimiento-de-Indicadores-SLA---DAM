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

    private const val TAG = "ExcelHelper"

    fun parseExcelFile(context: Context, uri: Uri): Result<List<CargaItemData>> {
        Log.d(TAG, "🔵 parseExcelFile: uri=$uri")
        return try {
            // CORRECCIÓN: Se usan los valores correctos para mantener la consistencia.
            // La solución ideal a futuro sería pasar la configuración desde el ViewModel
            // para no tener estos valores en varios sitios.
            val slaTargets = mapOf("SLA1" to 10L, "SLA2" to 5L)
            val parsedItems = mutableListOf<CargaItemData>()

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                Log.d(TAG, "📖 Abriendo archivo Excel...")
                val workbook = XSSFWorkbook(inputStream)
                val sheet = workbook.getSheetAt(0)

                Log.d(TAG, "📊 Hojas en el archivo: ${workbook.numberOfSheets}, filas en la primera hoja: ${sheet.lastRowNum}")

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

                        Log.d(TAG, "📝 Fila $i: codigo=$codigo, rol=$rol, fechaSol=$fechaSolicitudStr, fechaIng=$fechaIngresoStr, tipoSla=$tipoSla")

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
                        Log.e(TAG, "❌ Error procesando fila $i: ${e.message}", e)
                        return Result.failure(Exception("Error en formato de fila $i. Verifique las fechas (yyyy-MM-dd)."))
                    }
                }
            } ?: run {
                Log.e(TAG, "❌ No se pudo abrir el archivo")
                return Result.failure(Exception("No se pudo abrir el archivo"))
            }

            Log.d(TAG, "✅ Archivo parseado exitosamente: ${parsedItems.size} items")
            Result.success(parsedItems)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al procesar el archivo", e)
            Result.failure(Exception("No se pudo leer el archivo. ¿Es un .xlsx válido? Error: ${e.message}"))
        }
    }

    fun downloadTemplate(context: Context): Result<Unit> {
        Log.d(TAG, "🔵 downloadTemplate")
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

            Log.d(TAG, "📝 Creando archivo en Descargas...")
            val resolver = context.contentResolver
            val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // Android 10+ (API 29+): Usar MediaStore
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "plantilla_sla.xlsx")
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            } else {
                // Android 9 y anteriores: Usar File
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                val file = java.io.File(downloadsDir, "plantilla_sla.xlsx")
                android.net.Uri.fromFile(file)
            }

            if (uri == null) {
                Log.e(TAG, "❌ No se pudo crear el archivo en MediaStore")
                return Result.failure(Exception("No se pudo crear el archivo en Descargas"))
            }

            Log.d(TAG, "✍️ Escribiendo contenido al archivo: $uri")
            resolver.openOutputStream(uri)?.use { out ->
                workbook.write(out)
                Log.d(TAG, "✅ Plantilla creada exitosamente")
            } ?: run {
                Log.e(TAG, "❌ No se pudo abrir el stream de salida")
                return Result.failure(Exception("No se pudo escribir el archivo"))
            }
            workbook.close()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al descargar la plantilla", e)
            Result.failure(Exception("Error al crear la plantilla: ${e.message}"))
        }
    }
}
