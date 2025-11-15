package com.example.proyecto1.ui.report

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    fun generatePdf(context: Context): Boolean {
        val timeStamp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val fileName = "Reporte_SLA_$timeStamp.pdf"

        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        var outputStream: OutputStream? = null
        try {
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: return false
            outputStream = resolver.openOutputStream(uri) ?: return false

            val document = Document()
            PdfWriter.getInstance(document, outputStream)
            document.open()

            addContent(document)

            document.close()
            Log.d("PdfGenerator", "PDF generated successfully at ${uri.path}")
            return true
        } catch (e: Exception) {
            Log.e("PdfGenerator", "Error generating PDF", e)
            return false
        } finally {
            outputStream?.close()
        }
    }

    private fun addContent(document: Document) {
        // --- Fuentes y Colores ---
        val titleFont = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD)
        val headerFont = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD)
        val normalFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL)
        val tableHeaderFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD, BaseColor.WHITE)
        val headerColor = BaseColor(23, 37, 84) // Un azul oscuro corporativo

        // --- Título del Documento ---
        val title = Paragraph("Sistema de Control SLA", titleFont)
        title.alignment = Element.ALIGN_CENTER
        document.add(title)

        val subtitle = Paragraph("Reporte de Indicadores", normalFont)
        subtitle.alignment = Element.ALIGN_CENTER
        document.add(subtitle)
        document.add(Paragraph(" "))

        // --- Resumen Ejecutivo ---
        document.add(Paragraph("Resumen Ejecutivo", headerFont))
        document.add(Paragraph(" "))
        val summaryTable = PdfPTable(5).apply {
            widthPercentage = 100f
            val data = mapOf(
                "Total de casos medidos este mes" to "150",
                "Indicadores que cumplen SLA" to "116",
                "Indicadores que no cumplen SLA" to "34",
                "Media de cumplimiento de SLA" to "76.0%",
                "Promedio días de cumplimiento" to "17.3"
            )
            data.forEach { (key, value) ->
                addCell(createSummaryCard(key, value))
            }
        }
        document.add(summaryTable)

        document.add(Paragraph(" ")) // Spacer

        // --- Cumplimiento por Tipo de SLA ---
        document.add(Paragraph("Cumplimiento por Tipo de SLA", headerFont))
        document.add(Paragraph(" "))
        val slaTypeTable = PdfPTable(4).apply {
            widthPercentage = 100f
            val headers = listOf("Tipo SLA", "Total Casos", "% Cumplimiento", "% No Cumplimiento")
            headers.forEach { addCell(createHeaderCell(it, tableHeaderFont, headerColor)) }
            val data = listOf(
                listOf("SLA-TI > 25 días", "87", "74.6%", "25.4%"),
                listOf("SLA-TS > 20 días", "63", "77.1%", "22.9%")
            )
            data.forEach { row -> row.forEach { addCell(createSimpleCell(it, normalFont)) } }
        }
        document.add(slaTypeTable)
        document.add(Paragraph(" "))

        // --- Cumplimiento por Rol ---
        document.add(Paragraph("Cumplimiento por Rol", headerFont))
        document.add(Paragraph(" "))
        val roleTable = PdfPTable(5).apply {
            widthPercentage = 100f
            val headers = listOf("Rol", "Total", "% Cumplimiento", "% No Cumplimiento", "Promedio Días")
            headers.forEach { addCell(createHeaderCell(it, tableHeaderFont, headerColor)) }
            val data = listOf(
                listOf("Desarrollador", "19", "84.2%", "15.8%", "22.2"),
                listOf("Soporte", "28", "77.1%", "22.9%", "28.0"),
                listOf("Analista", "22", "77.0%", "23.0%", "17.0"),
                listOf("DevOps", "15", "100%", "0%", "19.0"),
                listOf("QA", "17", "77.0%", "23.0%", "17.0")
            )
            data.forEach { row -> row.forEach { addCell(createSimpleCell(it, normalFont)) } }
        }
        document.add(roleTable)
    }

    private fun createHeaderCell(text: String, font: Font, backgroundColor: BaseColor): PdfPCell {
        val cell = PdfPCell(Phrase(text, font))
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.verticalAlignment = Element.ALIGN_MIDDLE
        cell.backgroundColor = backgroundColor
        cell.setPaddingTop(8f)
        cell.setPaddingBottom(8f)
        cell.setPaddingLeft(8f)
        cell.setPaddingRight(8f)
        cell.border = Rectangle.NO_BORDER
        return cell
    }

    private fun createSimpleCell(text: String, font: Font): PdfPCell {
        val cell = PdfPCell(Phrase(text, font))
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.verticalAlignment = Element.ALIGN_MIDDLE
        cell.setPaddingTop(8f)
        cell.setPaddingBottom(8f)
        cell.setPaddingLeft(8f)
        cell.setPaddingRight(8f)
        cell.borderWidth = 1f
        cell.borderColor = BaseColor.LIGHT_GRAY
        return cell
    }

    private fun createSummaryCard(title: String, value: String): PdfPCell {
        val titleFont = Font(Font.FontFamily.HELVETICA, 8f, Font.NORMAL, BaseColor.DARK_GRAY)
        val valueFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD)

        val cell = PdfPCell()
        cell.addElement(Paragraph(title, titleFont).apply { alignment = Element.ALIGN_CENTER })
        cell.addElement(Paragraph(value, valueFont).apply { alignment = Element.ALIGN_CENTER })
        cell.border = Rectangle.NO_BORDER
        cell.setPaddingTop(10f)
        cell.setPaddingBottom(10f)
        cell.setPaddingLeft(10f)
        cell.setPaddingRight(10f)
        return cell
    }
}
