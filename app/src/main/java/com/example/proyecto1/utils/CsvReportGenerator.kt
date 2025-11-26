package com.example.proyecto1.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.proyecto1.data.repository.KpiResult
import java.io.IOException

object CsvReportGenerator {

    fun generate(context: Context, kpiResult: KpiResult): String {
        val fileName = "Reporte_SLA_${System.currentTimeMillis()}.csv"
        val csvContent = buildCsvContent(kpiResult)

        try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
            }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw IOException("Failed to create new MediaStore entry.")

            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(csvContent.toByteArray())
            }

            return "CSV guardado en Descargas/$fileName"

        } catch (e: Exception) {
            e.printStackTrace()
            return "Error al generar CSV: ${e.message}"
        }
    }

    private fun buildCsvContent(kpiResult: KpiResult): String {
        val builder = StringBuilder()

        // Resumen Ejecutivo
        builder.append("Resumen Ejecutivo\n")
        builder.append("Indicador,Valor\n")
        builder.append("Total de Casos Analizados,${kpiResult.totalCasosCerrados}\n")
        builder.append("Casos que Cumplen SLA,${kpiResult.casosCumplen}\n")
        builder.append("Casos que No Cumplen SLA,${kpiResult.casosNoCumplen}\n")
        builder.append("Porcentaje de Cumplimiento,${String.format("%.1f%%", if(kpiResult.totalCasosCerrados > 0) (kpiResult.casosCumplen.toFloat() / kpiResult.totalCasosCerrados) * 100 else 0f)}\n")
        builder.append("Promedio de Días,${String.format("%.1f", kpiResult.promedioDiasResolucion)}\n\n")

        // Cumplimiento por Tipo de SLA
        builder.append("Cumplimiento por Tipo de SLA\n")
        builder.append("Tipo SLA,Total,Cumplen,% Cumplimiento\n")
        kpiResult.cumplimientoPorTipoSla.forEach { (tipoSla, porcentaje) ->
            builder.append("$tipoSla,N/A,N/A,${String.format("%.1f%%", porcentaje)}\n")
        }
        builder.append("\n")

        // Cumplimiento por Rol
        builder.append("Cumplimiento por Rol\n")
        builder.append("Rol,Total,Cumplen,% Cumplimiento\n")
        kpiResult.cumplimientoPorRol.forEach { (rol, porcentaje) ->
            builder.append("$rol,N/A,N/A,${String.format("%.1f%%", porcentaje)}\n")
        }

        return builder.toString()
    }
}