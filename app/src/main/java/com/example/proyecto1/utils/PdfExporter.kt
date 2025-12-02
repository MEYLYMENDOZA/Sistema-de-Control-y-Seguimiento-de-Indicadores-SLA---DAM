package com.example.proyecto1.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.proyecto1.ui.report.ReporteGeneralDto
import com.itextpdf.text.*
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfExporter(private val context: Context) {

    // --- Colores y Fuentes ---
    private val colorAzul = BaseColor(33, 150, 243)
    private val colorGrisClaro = BaseColor(245, 245, 245)
    private val colorTexto = BaseColor.BLACK
    private val colorCumple = BaseColor(76, 175, 80)
    private val colorNoCumple = BaseColor(229, 57, 53)

    // Usar BaseFont con CP1252 y embebida para evitar problemas con acentos
    private val baseFont: BaseFont = try {
        BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED)
    } catch (e: Exception) {
        // fallback a built-in
        BaseFont.createFont()
    }

    private val fontTitulo = Font(baseFont, 18f, Font.BOLD, colorTexto)
    private val fontSubtitulo = Font(baseFont, 14f, Font.BOLD, colorTexto)
    private val fontHeaderTabla = Font(baseFont, 10f, Font.BOLD, BaseColor.WHITE)
    private val fontNormal = Font(baseFont, 10f, Font.NORMAL, colorTexto)
    private val fontBold = Font(baseFont, 10f, Font.BOLD, colorTexto)
    private val fontGrande = Font(baseFont, 24f, Font.BOLD, colorAzul)
    private val fontPequeno = Font(baseFont, 9f, Font.ITALIC, BaseColor.GRAY)

    // --- Data Classes (públicas para ser accesibles desde ViewModels) ---
    data class EstadisticasReporte(val mejorMes: String, val mejorValor: Double, val peorMes: String, val peorValor: Double, val promedio: Double, val tendencia: String)
    data class FilaSLA(val id: Int, val codigoSla: String, val rol: String, val slaPorcentaje: String, val usuarios: String, val nivel: String)

    // --- Métodos de Exportación ---

    fun exportarReporteDeIndicadores(reporte: ReporteGeneralDto): File? {
        try {
            val file = crearArchivo("Reporte_Indicadores_SLA")
            val document = Document(PageSize.A4, 36f, 36f, 50f, 50f)
            PdfWriter.getInstance(document, FileOutputStream(file))
            document.open()

            agregarCabecera(document)
            document.add(Chunk.NEWLINE)

            agregarTablaResumen(document, reporte.resumen)
            document.add(Chunk.NEWLINE)

            agregarTablaCumplimientoPorTipo(document, reporte.cumplimientoPorTipo)
            document.add(Chunk.NEWLINE)

            agregarTablaCumplimientoPorRol(document, reporte.cumplimientoPorRol)
            document.add(Chunk.NEWLINE)

            agregarTablaUltimosRegistros(document, reporte.ultimosRegistros)
            
            agregarPieDePagina(document)

            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun exportarPrediccionSLA(prediccion: Double, valorReal: Double?, slope: Double, intercept: Double, datosHistoricos: List<Triple<String, Double, Int>>, estadisticas: EstadisticasReporte): File? {
        try {
            val file = crearArchivo("Reporte_Prediccion_SLA")
            val document = Document(PageSize.A4, 36f, 36f, 50f, 50f)
            PdfWriter.getInstance(document, FileOutputStream(file))
            document.open()

            agregarCabecera(document)
            agregarSubtitulo(document, "Predicción de Cumplimiento")
            agregarInfoPrediccion(document, prediccion, slope, intercept)
            if (valorReal != null) {
                agregarComparacion(document, prediccion, valorReal)
            }
            agregarEstadisticas(document, estadisticas)
            if (datosHistoricos.isNotEmpty()) {
                agregarTablaDatosHistoricos(document, datosHistoricos)
            }
            agregarPieDePagina(document)

            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // --- Componentes del PDF ---

    private fun crearArchivo(nombreBase: String): File {
        // Usar app-specific external directory para evitar problemas de permisos con FileProvider
        val externalDocs = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val reportsDir = File(externalDocs, "SLA_Reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(reportsDir, "${nombreBase}_$timestamp.pdf")
    }

    /**
     * Helper para obtener Uri via FileProvider (el caller debe usar FLAG_GRANT_READ_URI_PERMISSION en el Intent)
     */
    fun getPdfUri(file: File): Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun agregarCabecera(document: Document) {
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(3f, 1f))
        table.defaultCell.border = Rectangle.NO_BORDER

        val cellInfo = PdfPCell()
        cellInfo.border = Rectangle.NO_BORDER
        cellInfo.addElement(Paragraph("Sistema de Control SLA", fontTitulo))
        cellInfo.addElement(Paragraph("Reporte de Indicadores", fontSubtitulo))
        cellInfo.addElement(Paragraph("Fecha de generación: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}", fontNormal))
        cellInfo.addElement(Paragraph("Generado por: admin", fontNormal))
        cellInfo.addElement(Paragraph("Rol: Administrador", fontNormal))
        table.addCell(cellInfo)

        try {
            val resourceId = context.resources.getIdentifier("logo_tata", "drawable", context.packageName)
            if (resourceId != 0) {
                val logoBitmap = BitmapFactory.decodeResource(context.resources, resourceId)
                if (logoBitmap != null) {
                    val stream = ByteArrayOutputStream()
                    logoBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    val image = Image.getInstance(stream.toByteArray())
                    image.scaleToFit(120f, 60f)
                    val cellLogo = PdfPCell(image)
                    cellLogo.border = Rectangle.NO_BORDER
                    cellLogo.horizontalAlignment = Element.ALIGN_RIGHT
                    table.addCell(cellLogo)
                } else {
                    val emptyCell = PdfPCell(Phrase("", fontNormal))
                    emptyCell.border = Rectangle.NO_BORDER
                    table.addCell(emptyCell)
                }
            } else {
                val emptyCell = PdfPCell(Phrase("", fontNormal))
                emptyCell.border = Rectangle.NO_BORDER
                table.addCell(emptyCell)
            }
        } catch (e: Exception) {
            val emptyCell = PdfPCell(Phrase("", fontNormal))
            emptyCell.border = Rectangle.NO_BORDER
            table.addCell(emptyCell)
        }
        
        document.add(table)
    }

    private fun agregarSubtitulo(document: Document, texto: String) {
        val p = Paragraph(texto, fontSubtitulo)
        p.alignment = Element.ALIGN_CENTER
        p.spacingAfter = 20f
        document.add(p)
    }
    
    private fun agregarInfoPrediccion(document: Document, prediccion: Double, slope: Double, intercept: Double) {
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(1f, 1f))
        table.spacingBefore = 10f
        table.spacingAfter = 15f

        table.addCell(crearCeldaSinBorde("SLA Proyectado (próximo mes):", fontBold))
        table.addCell(crearCeldaSinBorde(String.format("%.1f%%", prediccion), fontGrande, Element.ALIGN_RIGHT))

        table.addCell(crearCeldaSinBorde("Pendiente (m):", fontNormal))
        table.addCell(crearCeldaSinBorde(String.format("%.4f", slope), fontNormal, Element.ALIGN_RIGHT))

        table.addCell(crearCeldaSinBorde("Intercepto (b):", fontNormal))
        table.addCell(crearCeldaSinBorde(String.format("%.4f", intercept), fontNormal, Element.ALIGN_RIGHT))

        document.add(table)
    }

    private fun agregarComparacion(document: Document, prediccion: Double, valorReal: Double) {
        val diferencia = valorReal - prediccion
        val esPositivo = diferencia >= 0
        val chunk = Chunk("\n📊 Comparación Predicho vs Real\n", fontBold)
        document.add(Paragraph(chunk))
        val table = PdfPTable(3)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(1f, 1f, 1f))
        table.spacingBefore = 5f
        table.spacingAfter = 15f
        table.addCell(crearCeldaDato("Predicho"))
        table.addCell(crearCeldaDato("Real"))
        table.addCell(crearCeldaDato("Diferencia"))
        table.addCell(crearCeldaDato(String.format("%.1f%%", prediccion)))
        table.addCell(crearCeldaDato(String.format("%.1f%%", valorReal)))
        table.addCell(crearCeldaDato(String.format("%s%.2f%%", if (esPositivo) "+" else "", diferencia)))
        document.add(table)
    }

    private fun agregarEstadisticas(document: Document, stats: EstadisticasReporte) {
        document.add(Paragraph("Estadísticas del Período", fontSubtitulo))
        document.add(Paragraph("• Mejor mes: ${stats.mejorMes} (${String.format("%.1f%%", stats.mejorValor)})", fontNormal))
        document.add(Paragraph("• Peor mes: ${stats.peorMes} (${String.format("%.1f%%", stats.peorValor)})", fontNormal))
        document.add(Paragraph("• Promedio: ${String.format("%.1f%%", stats.promedio)}", fontNormal))
        document.add(Paragraph("• Tendencia: ${stats.tendencia}", fontNormal))
    }

    private fun agregarTablaDatosHistoricos(document: Document, datos: List<Triple<String, Double, Int>>) {
        document.add(Paragraph("Datos Históricos", fontSubtitulo))
        val table = PdfPTable(3)
        table.widthPercentage = 100f
        table.spacingBefore = 10f
        table.setWidths(floatArrayOf(2f, 1f, 1f))
        table.addCell(crearCeldaHeader("Mes"))
        table.addCell(crearCeldaHeader("Cumplimiento"))
        table.addCell(crearCeldaHeader("Casos"))
        datos.forEach {
            table.addCell(crearCeldaDato(it.first))
            table.addCell(crearCeldaDato(String.format("%.1f%%", it.second), Element.ALIGN_CENTER))
            table.addCell(crearCeldaDato(it.third.toString(), Element.ALIGN_CENTER))
        }
        document.add(table)
    }

    private fun agregarPieDePagina(document: Document) {
        val pie = Paragraph("\nGenerado: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\nFuente: Sistema de Control SLA", fontPequeno)
        pie.alignment = Element.ALIGN_CENTER
        pie.spacingBefore = 20f
        document.add(pie)
    }

    private fun agregarTablaResumen(document: Document, resumen: com.example.proyecto1.ui.report.ResumenEjecutivoDto) {
        document.add(Paragraph("Resumen Ejecutivo", fontSubtitulo))
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.spacingBefore = 10f
        table.setWidths(floatArrayOf(3f, 1f))

        table.addCell(crearCeldaHeader("Indicador"))
        table.addCell(crearCeldaHeader("Valor"))

        table.addCell(crearCeldaDato("Total de Casos Analizados"))
        table.addCell(crearCeldaDato(resumen.totalCasos.toString(), Element.ALIGN_CENTER))
        table.addCell(crearCeldaDato("Casos que Cumplen SLA"))
        table.addCell(crearCeldaDato(resumen.cumplen.toString(), Element.ALIGN_CENTER))
        table.addCell(crearCeldaDato("Casos que No Cumplen SLA"))
        table.addCell(crearCeldaDato(resumen.noCumplen.toString(), Element.ALIGN_CENTER))
        table.addCell(crearCeldaDato("Porcentaje de Cumplimiento"))
        table.addCell(crearCeldaDato(String.format("%.1f%%", resumen.porcentajeCumplimiento), Element.ALIGN_CENTER))
        table.addCell(crearCeldaDato("Promedio de Días"))
        table.addCell(crearCeldaDato(String.format("%.1f días", resumen.promedioDias), Element.ALIGN_CENTER))

        document.add(table)
    }

    private fun agregarTablaCumplimientoPorTipo(document: Document, datos: List<com.example.proyecto1.ui.report.CumplimientoPorTipoDto>) {
        document.add(Paragraph("Cumplimiento por Tipo de SLA", fontSubtitulo))
        val table = PdfPTable(4)
        table.widthPercentage = 100f
        table.spacingBefore = 10f
        table.setWidths(floatArrayOf(2f, 1f, 1f, 1.5f))

        table.addCell(crearCeldaHeader("Tipo SLA"))
        table.addCell(crearCeldaHeader("Total"))
        table.addCell(crearCeldaHeader("Cumplen"))
        table.addCell(crearCeldaHeader("% Cumplimiento"))

        datos.forEach {
            table.addCell(crearCeldaDato(it.tipoSla))
            table.addCell(crearCeldaDato(it.total.toString(), Element.ALIGN_CENTER))
            table.addCell(crearCeldaDato(it.cumplen.toString(), Element.ALIGN_CENTER))
            table.addCell(crearCeldaDato(String.format("%.1f%%", it.porcentajeCumplimiento), Element.ALIGN_CENTER))
        }

        document.add(table)
    }

    private fun agregarTablaCumplimientoPorRol(document: Document, datos: List<com.example.proyecto1.ui.report.CumplimientoPorRolDto>) {
        document.add(Paragraph("Cumplimiento por Rol", fontSubtitulo))
        val table = PdfPTable(4)
        table.widthPercentage = 100f
        table.spacingBefore = 10f
        table.setWidths(floatArrayOf(2f, 1f, 1f, 1.5f))

        table.addCell(crearCeldaHeader("Rol"))
        table.addCell(crearCeldaHeader("Total"))
        table.addCell(crearCeldaHeader("Cumplen"))
        table.addCell(crearCeldaHeader("% Cumplimiento"))

        datos.forEach {
            table.addCell(crearCeldaDato(it.rol))
            table.addCell(crearCeldaDato(it.total.toString(), Element.ALIGN_CENTER))
            table.addCell(crearCeldaDato(it.completados.toString(), Element.ALIGN_CENTER))
            table.addCell(crearCeldaDato(String.format("%.1f%%", it.porcentaje), Element.ALIGN_CENTER))
        }
        
        document.add(table)
    }

    private fun agregarTablaUltimosRegistros(document: Document, datos: List<com.example.proyecto1.ui.report.UltimoRegistroDto>) {
        document.add(Paragraph("Últimos Registros", fontSubtitulo))
        val table = PdfPTable(6)
        table.widthPercentage = 100f
        table.spacingBefore = 10f
        table.setWidths(floatArrayOf(1.5f, 1f, 1f, 1.2f, 0.8f, 1f))

        table.addCell(crearCeldaHeader("Rol"))
        table.addCell(crearCeldaHeader("F. Solicitud"))
        table.addCell(crearCeldaHeader("F. Ingreso"))
        table.addCell(crearCeldaHeader("Tipo"))
        table.addCell(crearCeldaHeader("Días"))
        table.addCell(crearCeldaHeader("Estado"))

        datos.forEach { 
            table.addCell(crearCeldaDato(it.rol))
            table.addCell(crearCeldaDato(it.fechaSolicitud, Element.ALIGN_CENTER))
            table.addCell(crearCeldaDato(it.fechaIngreso, Element.ALIGN_CENTER))
            table.addCell(crearCeldaDato(it.tipo, Element.ALIGN_CENTER))
            table.addCell(crearCeldaDato(it.dias?.toString() ?: "N/A", Element.ALIGN_CENTER))
            
            // --- CORRECCIÓN ---
            val estadoFont = Font(fontNormal)
            val backgroundColor: BaseColor

            if (it.estado.equals("Cumple", ignoreCase = true)) {
                backgroundColor = colorCumple
                estadoFont.color = BaseColor.WHITE
            } else if (it.estado.equals("No Cumple", ignoreCase = true)) {
                backgroundColor = colorNoCumple
                estadoFont.color = BaseColor.WHITE
            } else {
                backgroundColor = BaseColor.WHITE
            }

            val phrase = Phrase(it.estado, estadoFont)
            val celdaEstado = PdfPCell(phrase)
            celdaEstado.backgroundColor = backgroundColor
            celdaEstado.horizontalAlignment = Element.ALIGN_CENTER
            celdaEstado.verticalAlignment = Element.ALIGN_MIDDLE
            celdaEstado.setPadding(6f)
            celdaEstado.borderWidth = 1f
            celdaEstado.borderColor = colorGrisClaro
            table.addCell(celdaEstado)
        }
        
        document.add(table)
    }

    private fun crearCeldaHeader(texto: String): PdfPCell {
        val cell = PdfPCell(Phrase(texto, fontHeaderTabla))
        cell.backgroundColor = colorAzul
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.verticalAlignment = Element.ALIGN_MIDDLE
        cell.setPadding(8f)
        return cell
    }

    private fun crearCeldaDato(texto: String, alignment: Int = Element.ALIGN_LEFT): PdfPCell {
        val cell = PdfPCell(Phrase(texto, fontNormal))
        cell.horizontalAlignment = alignment
        cell.verticalAlignment = Element.ALIGN_MIDDLE
        cell.setPadding(6f)
        cell.borderWidth = 1f
        cell.borderColor = colorGrisClaro
        return cell
    }
    
    private fun crearCeldaSinBorde(texto: String, font: Font, alignment: Int = Element.ALIGN_LEFT): PdfPCell {
        val cell = PdfPCell(Phrase(texto, font))
        cell.border = Rectangle.NO_BORDER
        cell.horizontalAlignment = alignment
        cell.paddingBottom = 5f
        return cell
    }
}
