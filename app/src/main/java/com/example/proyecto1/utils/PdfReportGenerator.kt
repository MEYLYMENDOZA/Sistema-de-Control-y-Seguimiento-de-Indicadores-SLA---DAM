package com.example.proyecto1.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.proyecto1.data.repository.KpiResult
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfReportGenerator {

    private val TITLE_FONT = Font(Font.FontFamily.HELVETICA, 20f, Font.BOLD)
    private val SUBTITLE_FONT = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD)
    private val BODY_FONT = Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL)
    private val TABLE_HEADER_FONT = Font(Font.FontFamily.HELVETICA, 11f, Font.BOLD, BaseColor.WHITE)

    /**
     * Genera un PDF y lo guarda en la carpeta de descargas del dispositivo.
     */
    fun saveToFile(context: Context, kpiResult: KpiResult): String {
        val fileName = "Reporte_SLA_${System.currentTimeMillis()}.pdf"
        try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw IOException("Failed to create new MediaStore entry.")

            resolver.openOutputStream(uri)?.use { outputStream ->
                writeToStream(outputStream, kpiResult)
            }
            return "PDF guardado en Descargas/$fileName"

        } catch (e: Exception) {
            e.printStackTrace()
            return "Error al guardar PDF: ${e.message}"
        }
    }

    /**
     * Escribe el contenido del reporte PDF a un OutputStream genérico (para impresión o guardado).
     */
    fun writeToStream(outputStream: OutputStream, kpiResult: KpiResult) {
        val document = Document()
        try {
            PdfWriter.getInstance(document, outputStream)
            document.open()
            // Lógica de generación del contenido del PDF
            addTitleAndHeader(document)
            addResumenEjecutivo(document, kpiResult)
            addCumplimientoPorTipoSLA(document, kpiResult)
            addCumplimientoPorRol(document, kpiResult)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e // Relanza la excepción para que sea manejada por quien llamó a la función
        } finally {
            if (document.isOpen) {
                document.close()
            }
        }
    }

    private fun addTitleAndHeader(document: Document) {
        val title = Paragraph("Sistema de Control SLA", TITLE_FONT).apply { alignment = Element.ALIGN_CENTER }
        val subtitle = Paragraph("Reporte de Indicadores", BODY_FONT).apply { alignment = Element.ALIGN_CENTER }
        document.add(title)
        document.add(subtitle)

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val headerInfo = "Fecha de generación: ${sdf.format(Date())}\nGenerado por: admin\nRol: Administrador"
        document.add(Paragraph(headerInfo, BODY_FONT).apply { spacingBefore = 20f; spacingAfter = 20f })
    }

    private fun addResumenEjecutivo(document: Document, kpiResult: KpiResult) {
        document.add(Paragraph("Resumen Ejecutivo", SUBTITLE_FONT).apply { spacingAfter = 10f })
        val table = PdfPTable(2).apply { widthPercentage = 100f }
        addTableHeader(table, listOf("Indicador", "Valor"))

        table.addCell("Total de Casos Analizados")
        table.addCell(kpiResult.totalCasosCerrados.toString())
        table.addCell("Casos que Cumplen SLA")
        table.addCell(kpiResult.casosCumplen.toString())
        table.addCell("Casos que No Cumplen SLA")
        table.addCell(kpiResult.casosNoCumplen.toString())
        table.addCell("Porcentaje de Cumplimiento")
        val percentage = if(kpiResult.totalCasosCerrados > 0) (kpiResult.casosCumplen.toFloat() / kpiResult.totalCasosCerrados) * 100 else 0f
        table.addCell(String.format("%.1f%%", percentage))
        table.addCell("Promedio de Días")
        table.addCell(String.format("%.1f días", kpiResult.promedioDiasResolucion))

        document.add(table)
    }

    private fun addCumplimientoPorTipoSLA(document: Document, kpiResult: KpiResult) {
        document.add(Paragraph("Cumplimiento por Tipo de SLA", SUBTITLE_FONT).apply { spacingBefore = 20f; spacingAfter = 10f })
        val table = PdfPTable(4).apply { widthPercentage = 100f }
        addTableHeader(table, listOf("Tipo SLA", "Total", "Cumplen", "% Cumplimiento"))

        kpiResult.cumplimientoPorTipoSla.forEach { (tipoSla, porcentaje) ->
            table.addCell(tipoSla)
            table.addCell("N/A")
            table.addCell("N/A")
            table.addCell(String.format("%.1f%%", porcentaje))
        }
        document.add(table)
    }

    private fun addCumplimientoPorRol(document: Document, kpiResult: KpiResult) {
        document.add(Paragraph("Cumplimiento por Rol", SUBTITLE_FONT).apply { spacingBefore = 20f; spacingAfter = 10f })
        val table = PdfPTable(4).apply { widthPercentage = 100f }
        addTableHeader(table, listOf("Rol", "Total", "Cumplen", "% Cumplimiento"))

        kpiResult.cumplimientoPorRol.forEach { (rol, porcentaje) ->
            table.addCell(rol)
            table.addCell("N/A")
            table.addCell("N/A")
            table.addCell(String.format("%.1f%%", porcentaje))
        }
        document.add(table)
    }

    private fun addTableHeader(table: PdfPTable, headers: List<String>) {
        headers.forEach { header ->
            val cell = PdfPCell(Phrase(header, TABLE_HEADER_FONT)).apply {
                horizontalAlignment = Element.ALIGN_CENTER
                verticalAlignment = Element.ALIGN_MIDDLE
                backgroundColor = BaseColor(0, 77, 153) // Azul oscuro
                setPadding(8f)
            }
            table.addCell(cell)
        }
    }
}