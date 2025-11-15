package com.example.proyecto1.presentation.prediccion

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colores según especificación de las imágenes
private val AzulDatos = Color(0xFF2196F3)
private val GrisTendencia = Color(0xFF9E9E9E)
private val Verde = Color(0xFF4CAF50)
private val VerdeClaro = Color(0xFFE8F5E9)
private val Rojo = Color(0xFFE53935)
private val RojoClaro = Color(0xFFFFEBEE)
private val FondoGris = Color(0xFFF5F7FA)
private val GrisTexto = Color(0xFF616161)
private val GrisBorde = Color(0xFFE0E0E0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TendenciaScreen(
    vm: PrediccionViewModel
) {
    val datosHistoricos by vm.datosHistoricos.collectAsState()
    val estadisticas by vm.estadisticas.collectAsState()
    val prediccion by vm.prediccion.collectAsState()
    val slope by vm.slope.collectAsState()
    val intercept by vm.intercept.collectAsState()
    val cargando by vm.cargando.collectAsState()

    LaunchedEffect(Unit) {
        vm.cargarYPredecir()
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoGris)
    ) {
        // Columna izquierda: Filtros
        PanelFiltros(
            onAplicarFiltros = { vm.cargarYPredecir() },
            modifier = Modifier
                .width(280.dp)
                .fillMaxHeight()
        )

        // Columna derecha: Contenido principal
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Encabezado
            EncabezadoTendencia()

            if (cargando) {
                CargandoGrafico()
            } else {
                // Tabla de datos históricos
                TablaHistorico(datosHistoricos)

                // Tarjetas Mejor/Peor mes
                estadisticas?.let {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TarjetaMejorMes(
                            mes = it.mejorMes.first,
                            porcentaje = it.mejorMes.second,
                            modifier = Modifier.weight(1f)
                        )
                        TarjetaPeorMes(
                            mes = it.peorMes.first,
                            porcentaje = it.peorMes.second,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Gráfico principal
                GraficoTendenciaCompleto(
                    datosHistoricos = datosHistoricos,
                    prediccion = prediccion,
                    slope = slope,
                    intercept = intercept
                )
            }
        }
    }
}

// Panel de filtros lateral
@Composable
private fun PanelFiltros(
    onAplicarFiltros: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.White)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Filtros de Análisis",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF202124)
        )

        Text(
            text = "Personaliza el análisis de tendencia (170 de 170 registros)",
            fontSize = 11.sp,
            color = GrisTexto
        )

        HorizontalDivider(color = GrisBorde, thickness = 1.dp)

        // Tipo SLA
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Tipo SLA",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF202124)
            )
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Todos",
                        fontSize = 13.sp,
                        color = GrisTexto
                    )
                }
            }
        }

        // Rol / Área
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Rol / Área",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF202124)
            )
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Todos",
                        fontSize = 13.sp,
                        color = GrisTexto
                    )
                }
            }
        }

        // Periodo
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Periodo",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF202124)
            )
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Todos los periodos",
                        fontSize = 13.sp,
                        color = GrisTexto
                    )
                }
            }
        }
    }
}

@Composable
private fun EncabezadoTendencia() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Tendencia y Proyección del Cumplimiento SLA",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF202124)
        )
        Text(
            text = "Análisis del comportamiento histórico y estimación futura del nivel de servicio.",
            fontSize = 13.sp,
            color = GrisTexto
        )
    }
}

// Tabla de datos históricos
@Composable
private fun TablaHistorico(datos: List<SlaDataPoint>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Detalle del desempeño por mes con datos históricos",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF202124),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Encabezado de tabla
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FondoGris, RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Mes/Año", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Total Casos", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("Cumplidos", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("No Cumplidos", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("% Cumplimiento", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }

            // Filas de datos
            datos.takeLast(8).forEach { punto ->
                // Simular datos (en producción, obtener de Firestore)
                val totalCasos = (punto.valor * 1.2).toInt()
                val cumplidos = (totalCasos * punto.valor / 100).toInt()
                val noCumplidos = totalCasos - cumplidos

                val colorFondo = if (punto.valor >= 95) VerdeClaro else if (punto.valor < 85) RojoClaro else Color.White

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorFondo)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(punto.mes, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text("$totalCasos", fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("$cumplidos", fontSize = 12.sp, color = Verde, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
                    Text("$noCumplidos", fontSize = 12.sp, color = Rojo, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
                    Text("%.1f%%".format(punto.valor), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }
                HorizontalDivider(color = GrisBorde, thickness = 0.5.dp)
            }
        }
    }
}

// Tarjeta Mejor mes
@Composable
private fun TarjetaMejorMes(
    mes: String,
    porcentaje: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Mejor mes",
                    fontSize = 13.sp,
                    color = GrisTexto
                )
                Text(
                    text = "%.0f%%".format(porcentaje),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Verde
                )
                Text(
                    text = mes,
                    fontSize = 11.sp,
                    color = GrisTexto
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(VerdeClaro, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Mejor",
                    tint = Verde,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// Tarjeta Peor mes
@Composable
private fun TarjetaPeorMes(
    mes: String,
    porcentaje: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Peor mes",
                    fontSize = 13.sp,
                    color = GrisTexto
                )
                Text(
                    text = "%.0f%%".format(porcentaje),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Rojo
                )
                Text(
                    text = mes,
                    fontSize = 11.sp,
                    color = GrisTexto
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(RojoClaro, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Peor",
                    tint = Rojo,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// Gráfico completo con título y leyenda
@Composable
private fun GraficoTendenciaCompleto(
    datosHistoricos: List<SlaDataPoint>,
    prediccion: Double?,
    slope: Double?,
    intercept: Double?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Título
            Text(
                text = "Evolución Histórica y Predicción",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF202124),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Tendencia del cumplimiento SLA a lo largo del tiempo",
                fontSize = 11.sp,
                color = GrisTexto,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Gráfico
            if (datosHistoricos.isNotEmpty()) {
                GraficoLineasMejorado(
                    datos = datosHistoricos,
                    prediccion = prediccion,
                    slope = slope,
                    intercept = intercept,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay datos disponibles",
                        color = GrisTexto
                    )
                }
            }
        }
    }
}

// Gráfico de líneas mejorado
@Composable
private fun GraficoLineasMejorado(
    datos: List<SlaDataPoint>,
    prediccion: Double?,
    slope: Double?,
    intercept: Double?,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val paddingLeft = 50f
        val paddingRight = 30f
        val paddingTop = 30f
        val paddingBottom = 40f

        val dataWidth = width - paddingLeft - paddingRight
        val dataHeight = height - paddingTop - paddingBottom

        // Calcular escalas
        val maxY = 100.0
        val minY = 60.0
        val rangoY = maxY - minY

        val numPuntos = datos.size + 1 // +1 para predicción
        val espacioX = dataWidth / (numPuntos - 1).coerceAtLeast(1)

        // Funciones de conversión
        fun valorAY(valor: Double): Float {
            val proporcion = ((valor - minY) / rangoY).toFloat()
            return height - paddingBottom - (proporcion * dataHeight)
        }

        fun indiceAX(indice: Int): Float {
            return paddingLeft + (indice * espacioX)
        }

        // Dibujar grid horizontal
        for (i in 0..4) {
            val y = paddingTop + (i * dataHeight / 4)
            val valor = maxY - (i * (maxY - minY) / 4)

            // Línea de guía
            drawLine(
                color = Color(0xFFE0E0E0),
                start = Offset(paddingLeft, y),
                end = Offset(width - paddingRight, y),
                strokeWidth = 1f
            )

            // Etiqueta del eje Y
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawText(
                    "%.0f%%".format(valor),
                    10f,
                    y + 5f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#9E9E9E")
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.LEFT
                    }
                )
            }
        }

        // Dibujar eje X
        drawLine(
            color = Color(0xFFBDBDBD),
            start = Offset(paddingLeft, height - paddingBottom),
            end = Offset(width - paddingRight, height - paddingBottom),
            strokeWidth = 2f
        )

        // Dibujar línea de datos históricos
        val pathHistorico = Path()
        datos.forEachIndexed { index, punto ->
            val x = indiceAX(index)
            val y = valorAY(punto.valor)

            if (index == 0) {
                pathHistorico.moveTo(x, y)
            } else {
                pathHistorico.lineTo(x, y)
            }

            // Punto
            drawCircle(
                color = AzulDatos,
                center = Offset(x, y),
                radius = 5f
            )

            // Etiqueta del mes
            if (index == datos.size - 1 || index == 0) {
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(
                        punto.mes.substring(5),
                        x,
                        height - 10f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#757575")
                            textSize = 26f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }

        drawPath(
            path = pathHistorico,
            color = AzulDatos,
            style = Stroke(width = 3f)
        )

        // Dibujar línea de tendencia
        if (slope != null && intercept != null && datos.isNotEmpty()) {
            val pathTendencia = Path()
            val inicioX = indiceAX(0)
            val finX = indiceAX(datos.size)

            val inicioY = valorAY((intercept + slope * 1).coerceIn(minY, maxY))
            val finY = valorAY((intercept + slope * (datos.size + 1)).coerceIn(minY, maxY))

            pathTendencia.moveTo(inicioX, inicioY)
            pathTendencia.lineTo(finX, finY)

            drawPath(
                path = pathTendencia,
                color = GrisTendencia,
                style = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            )
        }

        // Punto de predicción
        if (prediccion != null) {
            val xPred = indiceAX(datos.size)
            val yPred = valorAY(prediccion.coerceIn(minY, maxY))

            // Línea punteada hacia predicción
            val pathPrediccion = Path()
            if (datos.isNotEmpty()) {
                val ultimoX = indiceAX(datos.size - 1)
                val ultimoY = valorAY(datos.last().valor)
                pathPrediccion.moveTo(ultimoX, ultimoY)
                pathPrediccion.lineTo(xPred, yPred)

                drawPath(
                    path = pathPrediccion,
                    color = Verde.copy(alpha = 0.6f),
                    style = Stroke(
                        width = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )
                )
            }

            // Círculo de predicción resaltado
            drawCircle(
                color = Verde.copy(alpha = 0.15f),
                center = Offset(xPred, yPred),
                radius = 18f
            )
            drawCircle(
                color = Verde,
                center = Offset(xPred, yPred),
                radius = 8f
            )

            // Etiqueta "Pred"
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawText(
                    "Pred",
                    xPred,
                    height - 10f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#4CAF50")
                        textSize = 26f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                )
            }
        }
    }
}

@Composable
private fun CargandoGrafico() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = AzulDatos)
            Text(
                text = "Generando análisis de tendencias...",
                fontSize = 14.sp,
                color = GrisTexto
            )
        }
    }
}

