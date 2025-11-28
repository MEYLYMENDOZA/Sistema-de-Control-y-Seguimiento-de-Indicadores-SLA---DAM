package com.example.proyecto1.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Environment
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfExporter(private val context: Context) {

    private val colorAzul = BaseColor(33, 150, 243) // #2196F3
    private val colorGris = BaseColor(97, 97, 97)   // #616161
    private val colorRojo = BaseColor(229, 57, 53)   // #E53935
    private val colorVerde = BaseColor(76, 175, 80) // #4CAF50
    private val colorExcelente = BaseColor(76, 175, 80) // Verde
    private val colorBajo = BaseColor(229, 57, 53)      // Rojo

    /**
     * Exporta el reporte de predicción SLA a PDF
     */
    fun exportarPrediccionSLA(
        prediccion: Double,
        valorReal: Double?,
        slope: Double,
        intercept: Double,
        datosHistoricos: List<Triple<String, Double, Int>>, // (mes, porcentaje, total)
        estadisticas: EstadisticasReporte
    ): File? {
        try {
            // Crear directorio de reportes
            val reportsDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "SLA_Reports"
            )
            if (!reportsDir.exists()) {
                reportsDir.mkdirs()
            }

            // Nombre del archivo con fecha
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Reporte_Prediccion_SLA_$timestamp.pdf"
            val file = File(reportsDir, fileName)

            // Crear documento PDF
            val document = Document(PageSize.A4, 36f, 36f, 50f, 50f)
            PdfWriter.getInstance(document, FileOutputStream(file))

            document.open()

            // 1. Encabezado con logo
            agregarEncabezado(document)

            // 2. Título del reporte
            agregarTitulo(document, "Reporte de Análisis SLA")

            // 3. Subtítulo
            val subtitulo = Paragraph("Predicción de Cumplimiento", getFontSubtitulo())
            subtitulo.alignment = Element.ALIGN_CENTER
            subtitulo.spacingAfter = 20f
            document.add(subtitulo)

            // 4. Información de la predicción
            agregarInfoPrediccion(document, prediccion, slope, intercept)

            // 5. Comparación si existe valor real
            if (valorReal != null) {
                agregarComparacion(document, prediccion, valorReal)
            }

            // 6. Estadísticas
            agregarEstadisticas(document, estadisticas)

            // 7. Tabla de datos históricos (si hay datos)
            if (datosHistoricos.isNotEmpty()) {
                agregarTablaDatos(document, datosHistoricos)
            }

            // 8. Pie de página
            agregarPieDePagina(document)

            document.close()

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Exporta reporte de todos los SLA (formato tabla que mostraste)
     */
    fun exportarReporteSLACompleto(
        datosTabla: List<FilaSLA>
    ): File? {
        try {
            val reportsDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "SLA_Reports"
            )
            if (!reportsDir.exists()) {
                reportsDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Reporte_Analisis_SLA_$timestamp.pdf"
            val file = File(reportsDir, fileName)

            val document = Document(PageSize.A4, 36f, 36f, 50f, 50f)
            PdfWriter.getInstance(document, FileOutputStream(file))

            document.open()

            // Encabezado
            agregarEncabezado(document)

            // Título
            agregarTitulo(document, "Reporte de Análisis SLA")

            // Subtítulo
            val subtitulo = Paragraph("Todos los tipos SLA", getFontNormal())
            subtitulo.alignment = Element.ALIGN_LEFT
            subtitulo.spacingAfter = 20f
            document.add(subtitulo)

            // Tabla
            val table = PdfPTable(6) // 6 columnas
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(0.5f, 1.2f, 1.5f, 1f, 1f, 1.2f))

            // Encabezados
            val headers = listOf("ID", "Código SLA", "Rol", "SLA %", "Usuarios", "Nivel")
            headers.forEach { header ->
                val cell = PdfPCell(Phrase(header, getFontTablaHeader()))
                cell.backgroundColor = colorAzul
                cell.horizontalAlignment = Element.ALIGN_CENTER
                cell.setPadding(8f)
                table.addCell(cell)
            }

            // Datos
            datosTabla.forEach { fila ->
                // ID
                table.addCell(crearCeldaTabla(fila.id.toString()))
                // Código SLA
                table.addCell(crearCeldaTabla(fila.codigoSla))
                // Rol
                table.addCell(crearCeldaTabla(fila.rol))
                // SLA %
                val celdaSla = crearCeldaTabla(fila.slaPorcentaje)
                celdaSla.horizontalAlignment = Element.ALIGN_CENTER
                table.addCell(celdaSla)
                // Usuarios
                table.addCell(crearCeldaTabla(fila.usuarios))
                // Nivel
                val celdaNivel = crearCeldaTabla(fila.nivel)
                celdaNivel.backgroundColor = when (fila.nivel) {
                    "EXCELENTE" -> colorExcelente
                    "BAJO" -> colorBajo
                    else -> BaseColor.WHITE
                }
                if (fila.nivel in listOf("EXCELENTE", "BAJO")) {
                    celdaNivel.phrase = Phrase(fila.nivel, getFontTablaNivelBlanco())
                }
                celdaNivel.horizontalAlignment = Element.ALIGN_CENTER
                table.addCell(celdaNivel)
            }

            document.add(table)

            // Pie de página
            agregarPieDePagina(document)

            document.close()

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun agregarEncabezado(document: Document) {
        try {
            // Cargar logo TATA desde recursos drawable
            // Nota: El archivo se llama logo_TATA.png pero Android lo convierte a logo_tata
            val resourceId = context.resources.getIdentifier("logo_tata", "drawable", context.packageName)

            val logoBitmap = if (resourceId != 0) {
                BitmapFactory.decodeResource(context.resources, resourceId)
            } else {
                // Si no se encuentra el logo, crear uno programáticamente
                crearLogoBitmap()
            }

            val stream = ByteArrayOutputStream()
            logoBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()

            val image = Image.getInstance(byteArray)
            image.scaleToFit(150f, 80f)
            image.alignment = Element.ALIGN_RIGHT
            image.spacingAfter = 10f

            document.add(image)
        } catch (e: Exception) {
            // Si falla el logo, continuar sin él
            e.printStackTrace()
        }
    }

    private fun agregarTitulo(document: Document, titulo: String) {
        val paragraph = Paragraph(titulo, getFontTitulo())
        paragraph.alignment = Element.ALIGN_CENTER
        paragraph.spacingAfter = 10f
        document.add(paragraph)
    }

    private fun agregarInfoPrediccion(
        document: Document,
        prediccion: Double,
        slope: Double,
        intercept: Double
    ) {
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(1f, 1f))
        table.spacingBefore = 10f
        table.spacingAfter = 15f

        // Predicción
        val cellPrediccionLabel = PdfPCell(Phrase("SLA Proyectado (próximo mes):", getFontBold()))
        cellPrediccionLabel.border = Rectangle.NO_BORDER
        cellPrediccionLabel.paddingBottom = 10f
        table.addCell(cellPrediccionLabel)

        val cellPrediccionValue = PdfPCell(Phrase(String.format("%.1f%%", prediccion), getFontGrande()))
        cellPrediccionValue.border = Rectangle.NO_BORDER
        cellPrediccionValue.horizontalAlignment = Element.ALIGN_RIGHT
        cellPrediccionValue.paddingBottom = 10f
        table.addCell(cellPrediccionValue)

        // Pendiente
        val cellSlopeLabel = PdfPCell(Phrase("Pendiente (m):", getFontNormal()))
        cellSlopeLabel.border = Rectangle.NO_BORDER
        cellSlopeLabel.paddingTop = 5f
        table.addCell(cellSlopeLabel)

        val cellSlopeValue = PdfPCell(Phrase(String.format("%.4f", slope), getFontNormal()))
        cellSlopeValue.border = Rectangle.NO_BORDER
        cellSlopeValue.horizontalAlignment = Element.ALIGN_RIGHT
        cellSlopeValue.paddingTop = 5f
        table.addCell(cellSlopeValue)

        // Intercepto
        val cellInterceptLabel = PdfPCell(Phrase("Intercepto (b):", getFontNormal()))
        cellInterceptLabel.border = Rectangle.NO_BORDER
        cellInterceptLabel.paddingTop = 5f
        table.addCell(cellInterceptLabel)

        val cellInterceptValue = PdfPCell(Phrase(String.format("%.4f", intercept), getFontNormal()))
        cellInterceptValue.border = Rectangle.NO_BORDER
        cellInterceptValue.horizontalAlignment = Element.ALIGN_RIGHT
        cellInterceptValue.paddingTop = 5f
        table.addCell(cellInterceptValue)

        document.add(table)
    }

    private fun agregarComparacion(document: Document, prediccion: Double, valorReal: Double) {
        val diferencia = valorReal - prediccion
        val esPositivo = diferencia >= 0

        val chunk = Chunk("\n📊 Comparación Predicho vs Real\n", getFontBold())
        document.add(Paragraph(chunk))

        val table = PdfPTable(3)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(1f, 1f, 1f))
        table.spacingBefore = 5f
        table.spacingAfter = 15f

        // Headers
        table.addCell(crearCeldaComparacion("Predicho", colorAzul))
        table.addCell(crearCeldaComparacion("Real", colorVerde))
        table.addCell(crearCeldaComparacion("Diferencia", if (esPositivo) colorVerde else colorRojo))

        // Valores
        table.addCell(crearCeldaValor(String.format("%.1f%%", prediccion)))
        table.addCell(crearCeldaValor(String.format("%.1f%%", valorReal)))
        table.addCell(crearCeldaValor(String.format("%s%.2f%%", if (esPositivo) "+" else "", diferencia)))

        document.add(table)
    }

    private fun agregarEstadisticas(document: Document, stats: EstadisticasReporte) {
        val chunk = Chunk("\nEstadísticas del Período\n", getFontBold())
        document.add(Paragraph(chunk))

        val content = """
            • Mejor mes: ${stats.mejorMes} (${String.format("%.1f%%", stats.mejorValor)})
            • Peor mes: ${stats.peorMes} (${String.format("%.1f%%", stats.peorValor)})
            • Promedio: ${String.format("%.1f%%", stats.promedio)}
            • Tendencia: ${stats.tendencia}
        """.trimIndent()

        val paragraph = Paragraph(content, getFontNormal())
        paragraph.spacingBefore = 5f
        paragraph.spacingAfter = 15f
        document.add(paragraph)
    }

    private fun agregarTablaDatos(
        document: Document,
        datos: List<Triple<String, Double, Int>>
    ) {
        val chunk = Chunk("\nDatos Históricos\n", getFontBold())
        document.add(Paragraph(chunk))

        val table = PdfPTable(3)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(2f, 2f, 1f))
        table.spacingBefore = 5f

        // Headers
        table.addCell(crearCeldaHeader("Período"))
        table.addCell(crearCeldaHeader("Cumplimiento SLA"))
        table.addCell(crearCeldaHeader("Total"))

        // Datos
        datos.forEach { (mes, porcentaje, total) ->
            table.addCell(crearCeldaTabla(mes))
            table.addCell(crearCeldaTabla(String.format("%.1f%%", porcentaje)))
            table.addCell(crearCeldaTabla(total.toString()))
        }

        document.add(table)
    }

    private fun agregarPieDePagina(document: Document) {
        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val pie = Paragraph("\nGenerado: $fecha\nFuente: Sistema de Control SLA", getFontPequeno())
        pie.alignment = Element.ALIGN_CENTER
        pie.spacingBefore = 20f
        document.add(pie)
    }

    private fun crearCeldaHeader(texto: String): PdfPCell {
        val cell = PdfPCell(Phrase(texto, getFontTablaHeader()))
        cell.backgroundColor = colorAzul
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.setPadding(8f)
        return cell
    }

    private fun crearCeldaTabla(texto: String): PdfPCell {
        val cell = PdfPCell(Phrase(texto, getFontNormal()))
        cell.setPadding(6f)
        cell.horizontalAlignment = Element.ALIGN_LEFT
        return cell
    }

    private fun crearCeldaComparacion(texto: String, color: BaseColor): PdfPCell {
        val cell = PdfPCell(Phrase(texto, getFontTablaNivelBlanco()))
        cell.backgroundColor = color
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.setPadding(8f)
        return cell
    }

    private fun crearCeldaValor(texto: String): PdfPCell {
        val cell = PdfPCell(Phrase(texto, getFontBold()))
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.setPadding(8f)
        return cell
    }

    // Fonts
    private fun getFontTitulo(): Font {
        return Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD, colorAzul)
    }

    private fun getFontSubtitulo(): Font {
        return Font(Font.FontFamily.HELVETICA, 14f, Font.NORMAL, colorGris)
    }

    private fun getFontBold(): Font {
        return Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD, BaseColor.BLACK)
    }

    private fun getFontNormal(): Font {
        return Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL, BaseColor.BLACK)
    }

    private fun getFontGrande(): Font {
        return Font(Font.FontFamily.HELVETICA, 24f, Font.BOLD, colorAzul)
    }

    private fun getFontPequeno(): Font {
        return Font(Font.FontFamily.HELVETICA, 9f, Font.NORMAL, colorGris)
    }

    private fun getFontTablaHeader(): Font {
        return Font(Font.FontFamily.HELVETICA, 11f, Font.BOLD, BaseColor.WHITE)
    }

    private fun getFontTablaNivelBlanco(): Font {
        return Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD, BaseColor.WHITE)
    }

    /**
     * Crea el logo de TATA como bitmap
     */
    private fun crearLogoBitmap(): Bitmap {
        val width = 300
        val height = 150
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Color azul TATA
        val paint = Paint().apply {
            color = Color.rgb(33, 82, 139) // Azul TATA
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // Dibujar forma simplificada del logo TATA
        // Dos arcos superiores
        canvas.drawArc(50f, 20f, 130f, 80f, 180f, 180f, true, paint)
        canvas.drawArc(170f, 20f, 250f, 80f, 180f, 180f, true, paint)

        // Texto TATA
        val textPaint = Paint().apply {
            color = Color.rgb(33, 82, 139)
            textSize = 50f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("TATA", width / 2f, height - 30f, textPaint)

        return bitmap
    }

    // Data classes
    data class EstadisticasReporte(
        val mejorMes: String,
        val mejorValor: Double,
        val peorMes: String,
        val peorValor: Double,
        val promedio: Double,
        val tendencia: String
    )

    data class FilaSLA(
        val id: Int,
        val codigoSla: String,
        val rol: String,
        val slaPorcentaje: String,
        val usuarios: String,
        val nivel: String
    )
}

