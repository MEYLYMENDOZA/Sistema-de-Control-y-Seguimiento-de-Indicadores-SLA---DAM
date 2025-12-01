package com.example.proyecto1.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import com.example.proyecto1.R
import com.example.proyecto1.data.remote.dto.PuntoHistoricoDto
import com.example.proyecto1.presentation.tendencia.FiltrosReporte
import com.example.proyecto1.presentation.tendencia.KPIsTendencia
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Exportador de reportes de tendencia a PDF usando iText
 * US-12: Exportar reporte PDF con grafica, tabla y KPIs
 * Formato igual al de predicciones con logo TATA
 */
class PdfExporterTendencia(private val context: Context) {

    // Colores corporativos
    private val colorAzul = BaseColor(33, 150, 243)      // #2196F3
    private val colorGris = BaseColor(97, 97, 97)        // #616161
    private val colorRojo = BaseColor(229, 57, 53)       // #E53935
    private val colorVerde = BaseColor(76, 175, 80)      // #4CAF50
    private val colorAmarillo = BaseColor(255, 193, 7)   // #FFC107
    private val colorNaranja = BaseColor(255, 152, 0)    // #FF9800

    fun exportar(
        historico: List<PuntoHistoricoDto>,
        proyeccion: Double,
        estadoTendencia: String,
        kpis: KPIsTendencia,
        filtros: FiltrosReporte,
        context: Context,
        compartir: Boolean = false
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
            val fileName = "Reporte_Tendencia_SLA_$timestamp.pdf"
            val file = File(reportsDir, fileName)

            // Crear documento PDF con iText
            val document = Document(PageSize.A4, 36f, 36f, 50f, 50f)
            PdfWriter.getInstance(document, FileOutputStream(file))

            document.open()

            // 1. Encabezado con logo
            agregarEncabezado(document)

            // 2. TÃ­tulo del reporte
            agregarTitulo(document, "Reporte de Tendencia SLA")

            // 3. Subtítulo con filtros
            agregarSubtitulo(document, filtros)

            // 4. KPIs principales
            agregarKPIs(document, kpis, proyeccion, estadoTendencia)

            // 5. Gráfico de tendencia
            if (historico.isNotEmpty()) {
                agregarGrafico(document, historico, proyeccion)
            }

            // 6. Tabla de datos históricos
            if (historico.isNotEmpty()) {
                agregarTablaHistorico(document, historico)
            }

            // 7. Información adicional
            agregarInformacionTendencia(document, proyeccion, estadoTendencia, filtros)

            // 8. Pie de página
            agregarPieDePagina(document)

            document.close()

            Log.d("PdfExporterTendencia", "✅ PDF creado: ${file.absolutePath}")

            // Abrir o compartir PDF
            if (compartir) {
                compartirPDF(context, file)
            } else {
                abrirPDF(context, file)
            }

            return file
        } catch (e: Exception) {
            Log.e("PdfExporterTendencia", "âŒ Error al crear PDF", e)
            throw e
        }
    }

    // ============================================================================
    // FUNCIONES AUXILIARES PARA CONSTRUIR EL PDF
    // ============================================================================

    private fun agregarEncabezado(document: Document) {
        try {
            // Cargar logo TATA desde recursos drawable
            val resourceId = context.resources.getIdentifier("logo_tata", "drawable", context.packageName)

            val logoBitmap = if (resourceId != 0) {
                BitmapFactory.decodeResource(context.resources, resourceId)
            } else {
                // Si no se encuentra el logo, crear uno programÃ¡ticamente
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
            // Si falla el logo, continuar sin Ã©l
            e.printStackTrace()
        }
    }

    private fun agregarTitulo(document: Document, titulo: String) {
        val paragraph = Paragraph(titulo, getFontTitulo())
        paragraph.alignment = Element.ALIGN_CENTER
        paragraph.spacingAfter = 10f
        document.add(paragraph)
    }

    private fun agregarSubtitulo(document: Document, filtros: FiltrosReporte) {
        val tipoSla = filtros.tipoSla ?: "Todos"
        val anio = filtros.anio?.toString() ?: "Todos los anos"
        val area = filtros.idArea?.toString() ?: "Todas las areas"

        val subtitulo = Paragraph(
            "Tipo SLA: $tipoSla | Ano: $anio | Area: $area",
            getFontSubtitulo()
        )
        subtitulo.alignment = Element.ALIGN_CENTER
        subtitulo.spacingAfter = 10f  // Reducido de 20f a 10f
        document.add(subtitulo)
    }

    private fun agregarKPIs(
        document: Document,
        kpis: KPIsTendencia,
        proyeccion: Double,
        estadoTendencia: String
    ) {
        val chunk = Chunk("\nIndicadores Clave de Rendimiento\n", getFontBold())
        document.add(Paragraph(chunk))

        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(1f, 1f))
        table.spacingBefore = 5f   // Reducido de 10f a 5f
        table.spacingAfter = 10f   // Reducido de 15f a 10f

        // Proyeccion
        val cellProyLabel = PdfPCell(Phrase("SLA Proyectado (proximo mes):", getFontBold()))
        cellProyLabel.border = Rectangle.NO_BORDER
        cellProyLabel.paddingBottom = 10f
        table.addCell(cellProyLabel)

        val cellProyValue = PdfPCell(Phrase(String.format("%.2f%%", proyeccion), getFontGrande()))
        cellProyValue.border = Rectangle.NO_BORDER
        cellProyValue.horizontalAlignment = Element.ALIGN_RIGHT
        cellProyValue.paddingBottom = 10f
        table.addCell(cellProyValue)

        // Estado de tendencia
        val cellTendLabel = PdfPCell(Phrase("Estado de tendencia:", getFontNormal()))
        cellTendLabel.border = Rectangle.NO_BORDER
        cellTendLabel.paddingTop = 5f
        table.addCell(cellTendLabel)

        val colorTendencia = when (estadoTendencia.lowercase()) {
            "mejorando" -> colorVerde
            "empeorando" -> colorRojo
            else -> colorAmarillo
        }
        val textoTendencia = when (estadoTendencia.lowercase()) {
            "mejorando" -> "â†‘ MEJORANDO"
            "empeorando" -> "â†“ EMPEORANDO"
            else -> "â†’ ESTABLE"
        }
        val fontTendencia = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD, colorTendencia)
        val cellTendValue = PdfPCell(Phrase(textoTendencia, fontTendencia))
        cellTendValue.border = Rectangle.NO_BORDER
        cellTendValue.horizontalAlignment = Element.ALIGN_RIGHT
        cellTendValue.paddingTop = 5f
        table.addCell(cellTendValue)

        // Mejor mes
        val cellMejorLabel = PdfPCell(Phrase("Mejor mes:", getFontNormal()))
        cellMejorLabel.border = Rectangle.NO_BORDER
        cellMejorLabel.paddingTop = 5f
        table.addCell(cellMejorLabel)

        val cellMejorValue = PdfPCell(Phrase("${kpis.mejorMes} (${String.format("%.2f%%", kpis.valorMejorMes)})", getFontNormal()))
        cellMejorValue.border = Rectangle.NO_BORDER
        cellMejorValue.horizontalAlignment = Element.ALIGN_RIGHT
        cellMejorValue.paddingTop = 5f
        table.addCell(cellMejorValue)

        // Peor mes
        val cellPeorLabel = PdfPCell(Phrase("Peor mes:", getFontNormal()))
        cellPeorLabel.border = Rectangle.NO_BORDER
        cellPeorLabel.paddingTop = 5f
        table.addCell(cellPeorLabel)

        val cellPeorValue = PdfPCell(Phrase("${kpis.peorMes} (${String.format("%.2f%%", kpis.valorPeorMes)})", getFontNormal()))
        cellPeorValue.border = Rectangle.NO_BORDER
        cellPeorValue.horizontalAlignment = Element.ALIGN_RIGHT
        cellPeorValue.paddingTop = 5f
        table.addCell(cellPeorValue)

        // Promedio historico
        val cellPromLabel = PdfPCell(Phrase("Promedio historico:", getFontNormal()))
        cellPromLabel.border = Rectangle.NO_BORDER
        cellPromLabel.paddingTop = 5f
        table.addCell(cellPromLabel)

        val cellPromValue = PdfPCell(Phrase(String.format("%.2f%%", kpis.promedioHistorico), getFontNormal()))
        cellPromValue.border = Rectangle.NO_BORDER
        cellPromValue.horizontalAlignment = Element.ALIGN_RIGHT
        cellPromValue.paddingTop = 5f
        table.addCell(cellPromValue)

        document.add(table)
    }

    private fun agregarGrafico(document: Document, historico: List<PuntoHistoricoDto>, proyeccion: Double) {
        try {
            val chunk = Chunk("\nGráfico de Tendencia\n", getFontBold())
            document.add(Paragraph(chunk))

            // Crear bitmap del gráfico
            val width = 500
            val height = 300
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)

            // Fondo blanco
            canvas.drawColor(android.graphics.Color.WHITE)

            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
            }

            // Configuración del gráfico
            val margin = 50f
            val graphWidth = width - margin * 2
            val graphHeight = height - margin * 2
            val graphX = margin
            val graphY = margin

            // Dibujar ejes
            paint.color = android.graphics.Color.LTGRAY
            paint.strokeWidth = 2f
            canvas.drawLine(graphX, graphY + graphHeight, graphX + graphWidth, graphY + graphHeight, paint) // Eje X
            canvas.drawLine(graphX, graphY, graphX, graphY + graphHeight, paint) // Eje Y

            // Encontrar valores máximo y mínimo
            val valores = historico.map { it.valor }
            val maxValor = maxOf(valores.maxOrNull() ?: 100.0, proyeccion, 100.0)
            val minValor = minOf(valores.minOrNull() ?: 0.0, 0.0)
            val rangoValor = maxValor - minValor

            // Dibujar líneas de cuadrícula
            paint.color = android.graphics.Color.rgb(240, 240, 240)
            for (i in 0..4) {
                val y = graphY + (graphHeight * i / 4)
                canvas.drawLine(graphX, y, graphX + graphWidth, y, paint)
            }

            // Dibujar línea de tendencia
            paint.color = android.graphics.Color.rgb(33, 150, 243) // Azul
            paint.strokeWidth = 3f
            paint.style = android.graphics.Paint.Style.STROKE

            val path = android.graphics.Path()
            historico.forEachIndexed { index, punto ->
                val x = graphX + (graphWidth * index / (historico.size - 1))
                val y = graphY + graphHeight - ((punto.valor - minValor) / rangoValor * graphHeight).toFloat()

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            canvas.drawPath(path, paint)

            // Dibujar puntos
            paint.style = android.graphics.Paint.Style.FILL
            historico.forEachIndexed { index, punto ->
                val x = graphX + (graphWidth * index / (historico.size - 1))
                val y = graphY + graphHeight - ((punto.valor - minValor) / rangoValor * graphHeight).toFloat()
                canvas.drawCircle(x, y, 5f, paint)
            }

            // Dibujar proyección
            if (historico.isNotEmpty()) {
                paint.color = android.graphics.Color.rgb(76, 175, 80) // Verde
                val xProyeccion = graphX + graphWidth + 10
                val yProyeccion = graphY + graphHeight - ((proyeccion - minValor) / rangoValor * graphHeight).toFloat()
                canvas.drawCircle(xProyeccion, yProyeccion, 6f, paint)

                // Texto proyección
                paint.textSize = 12f
                paint.color = android.graphics.Color.BLACK
                canvas.drawText(String.format("%.1f%%", proyeccion), xProyeccion - 20, yProyeccion - 10, paint)
            }

            // Convertir bitmap a Image de iText
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()

            val image = Image.getInstance(byteArray)
            image.scaleToFit(450f, 250f)
            image.alignment = Element.ALIGN_CENTER
            image.spacingBefore = 5f
            image.spacingAfter = 10f

            document.add(image)
        } catch (e: Exception) {
            Log.e("PdfExporterTendencia", "❌ Error al agregar gráfico", e)
            // Continuar sin gráfico si falla
        }
    }

    private fun agregarTablaHistorico(document: Document, historico: List<PuntoHistoricoDto>) {
        val chunk = Chunk("\nDatos Historicos Mensuales\n", getFontBold())
        document.add(Paragraph(chunk))

        val table = PdfPTable(5)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(1.5f, 1f, 1f, 1f, 1.2f))
        table.spacingBefore = 5f

        // Headers
        table.addCell(crearCeldaHeader("Periodo"))
        table.addCell(crearCeldaHeader("Total"))
        table.addCell(crearCeldaHeader("Cumplidos"))
        table.addCell(crearCeldaHeader("No Cumplidos"))
        table.addCell(crearCeldaHeader("% Cumplimiento"))

        // Datos
        historico.forEach { punto ->
            table.addCell(crearCeldaTabla(punto.mes))
            table.addCell(crearCeldaTabla(punto.totalCasos.toString()))
            table.addCell(crearCeldaTabla(punto.cumplidos.toString()))
            table.addCell(crearCeldaTabla(punto.noCumplidos.toString()))

            val celdaPorcentaje = crearCeldaTabla(String.format("%.2f%%", punto.valor))
            celdaPorcentaje.horizontalAlignment = Element.ALIGN_CENTER
            table.addCell(celdaPorcentaje)
        }

        document.add(table)
    }

    private fun agregarInformacionTendencia(
        document: Document,
        proyeccion: Double,
        estadoTendencia: String,
        filtros: FiltrosReporte
    ) {
        val chunk = Chunk("\n\nAnalisis de Tendencia\n", getFontBold())
        document.add(Paragraph(chunk))

        val interpretacion = when (estadoTendencia.lowercase()) {
            "mejorando" -> "La tendencia muestra una mejora constante en el cumplimiento del SLA. " +
                    "Se espera que el proximo periodo alcance un ${String.format("%.2f%%", proyeccion)} de cumplimiento."
            "empeorando" -> "La tendencia indica un deterioro en el cumplimiento del SLA. " +
                    "Se proyecta un ${String.format("%.2f%%", proyeccion)} para el proximo periodo. " +
                    "Se recomienda revisar los procesos y tomar medidas correctivas."
            else -> "La tendencia se mantiene estable. " +
                    "Se proyecta un ${String.format("%.2f%%", proyeccion)} para el proximo periodo, " +
                    "similar al rendimiento historico."
        }

        val paragraph = Paragraph(interpretacion, getFontNormal())
        paragraph.spacingBefore = 5f
        paragraph.spacingAfter = 10f  // Reducido de 15f a 10f
        paragraph.alignment = Element.ALIGN_JUSTIFIED
        document.add(paragraph)
    }

    private fun agregarPieDePagina(document: Document) {
        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val pie = Paragraph("\nGenerado: $fecha\nFuente: Sistema de Control SLA - Modulo de Tendencia", getFontPequeno())
        pie.alignment = Element.ALIGN_CENTER
        pie.spacingBefore = 10f  // Reducido de 20f a 10f
        document.add(pie)
    }

    // ============================================================================
    // FUNCIONES AUXILIARES PARA CELDAS Y FORMATO
    // ============================================================================

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

    // ============================================================================
    // FUNCIONES DE FONTS CON SOPORTE UTF-8 (CP1252)
    // ============================================================================

    private fun getFontTitulo(): Font {
        val baseFont = com.itextpdf.text.pdf.BaseFont.createFont(
            com.itextpdf.text.pdf.BaseFont.HELVETICA_BOLD,
            com.itextpdf.text.pdf.BaseFont.CP1252,
            com.itextpdf.text.pdf.BaseFont.EMBEDDED
        )
        return Font(baseFont, 18f, Font.BOLD, colorAzul)
    }

    private fun getFontSubtitulo(): Font {
        val baseFont = com.itextpdf.text.pdf.BaseFont.createFont(
            com.itextpdf.text.pdf.BaseFont.HELVETICA,
            com.itextpdf.text.pdf.BaseFont.CP1252,
            com.itextpdf.text.pdf.BaseFont.EMBEDDED
        )
        return Font(baseFont, 14f, Font.NORMAL, colorGris)
    }

    private fun getFontBold(): Font {
        val baseFont = com.itextpdf.text.pdf.BaseFont.createFont(
            com.itextpdf.text.pdf.BaseFont.HELVETICA_BOLD,
            com.itextpdf.text.pdf.BaseFont.CP1252,
            com.itextpdf.text.pdf.BaseFont.EMBEDDED
        )
        return Font(baseFont, 12f, Font.BOLD, BaseColor.BLACK)
    }

    private fun getFontNormal(): Font {
        val baseFont = com.itextpdf.text.pdf.BaseFont.createFont(
            com.itextpdf.text.pdf.BaseFont.HELVETICA,
            com.itextpdf.text.pdf.BaseFont.CP1252,
            com.itextpdf.text.pdf.BaseFont.EMBEDDED
        )
        return Font(baseFont, 11f, Font.NORMAL, BaseColor.BLACK)
    }

    private fun getFontGrande(): Font {
        val baseFont = com.itextpdf.text.pdf.BaseFont.createFont(
            com.itextpdf.text.pdf.BaseFont.HELVETICA_BOLD,
            com.itextpdf.text.pdf.BaseFont.CP1252,
            com.itextpdf.text.pdf.BaseFont.EMBEDDED
        )
        return Font(baseFont, 24f, Font.BOLD, colorAzul)
    }

    private fun getFontPequeno(): Font {
        val baseFont = com.itextpdf.text.pdf.BaseFont.createFont(
            com.itextpdf.text.pdf.BaseFont.HELVETICA,
            com.itextpdf.text.pdf.BaseFont.CP1252,
            com.itextpdf.text.pdf.BaseFont.EMBEDDED
        )
        return Font(baseFont, 9f, Font.NORMAL, colorGris)
    }

    private fun getFontTablaHeader(): Font {
        val baseFont = com.itextpdf.text.pdf.BaseFont.createFont(
            com.itextpdf.text.pdf.BaseFont.HELVETICA_BOLD,
            com.itextpdf.text.pdf.BaseFont.CP1252,
            com.itextpdf.text.pdf.BaseFont.EMBEDDED
        )
        return Font(baseFont, 11f, Font.BOLD, BaseColor.WHITE)
    }

    /**
     * Crea el logo de TATA como bitmap
     */
    private fun crearLogoBitmap(): Bitmap {
        val width = 300
        val height = 150
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        // Color azul TATA
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(33, 82, 139) // Azul TATA
            style = android.graphics.Paint.Style.FILL
            isAntiAlias = true
        }

        // Dibujar forma simplificada del logo TATA
        // Dos arcos superiores
        canvas.drawArc(50f, 20f, 130f, 80f, 180f, 180f, true, paint)
        canvas.drawArc(170f, 20f, 250f, 80f, 180f, 180f, true, paint)

        // Texto TATA
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(33, 82, 139)
            textSize = 50f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.drawText("TATA", width / 2f, height - 30f, textPaint)

        return bitmap
    }

    // ============================================================================
    // FUNCION PARA ABRIR PDF
    // ============================================================================

    private fun abrirPDF(context: Context, file: File) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    ),
                    "application/pdf"
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            val chooserIntent = Intent.createChooser(intent, "Abrir PDF con...").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            Log.e("PdfExporterTendencia", "❌ Error al abrir PDF", e)
        }
    }

    private fun compartirPDF(context: Context, file: File) {
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Reporte de Tendencia SLA")
                putExtra(Intent.EXTRA_TEXT, "Te comparto el reporte de tendencia SLA.\n\nGenerado por Sistema de Control SLA")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Compartir reporte con...").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(chooserIntent)
            Log.d("PdfExporterTendencia", "✅ Intent de compartir abierto correctamente")
        } catch (e: Exception) {
            Log.e("PdfExporterTendencia", "❌ Error al compartir PDF", e)
        }
    }
}