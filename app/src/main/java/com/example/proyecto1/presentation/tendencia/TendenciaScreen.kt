package com.example.proyecto1.presentation.tendencia

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap

// Colores corporativos según especificación
private val AzulCorporativo = Color(0xFF2196F3)
private val GrisClaro = Color(0xFFF4F6F8)
private val GrisTexto = Color(0xFF616161)
private val Verde = Color(0xFF4CAF50)
private val Rojo = Color(0xFFE53935)
private val Amarillo = Color(0xFFFFA726)
private val Naranja = Color(0xFFFF9800)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TendenciaScreen(
    vm: TendenciaViewModel
) {
    val historico by vm.historico.collectAsState()
    val tendencia by vm.tendencia.collectAsState()
    val proyeccion by vm.proyeccion.collectAsState()
    val estadoTendencia by vm.estadoTendencia.collectAsState()
    val cargando by vm.cargando.collectAsState()
    val error by vm.error.collectAsState()
    val ultimaActualizacion by vm.ultimaActualizacion.collectAsState()
    val aniosDisponibles by vm.aniosDisponibles.collectAsState()
    val areasDisponibles by vm.areasDisponibles.collectAsState()
    val tiposSlaDisponibles by vm.tiposSlaDisponibles.collectAsState()

    val mesesDisponibles by vm.mesesDisponibles.collectAsState()

    // Filtros seleccionados
    var tipoSlaSeleccionado by remember { mutableStateOf("") }
    var anioSeleccionado by remember { mutableStateOf("") }
    var mesSeleccionado by remember { mutableStateOf("") } // Nuevo: filtro de mes
    var areaSeleccionada by remember { mutableStateOf("") }

    // Seleccionar primer tipo SLA disponible cuando se carguen
    LaunchedEffect(tiposSlaDisponibles) {
        if (tiposSlaDisponibles.isNotEmpty() && tipoSlaSeleccionado.isEmpty()) {
            tipoSlaSeleccionado = tiposSlaDisponibles.first().codigo
        }
    }

    // Cuando se carguen los años, seleccionar el más reciente
    LaunchedEffect(aniosDisponibles) {
        if (aniosDisponibles.isNotEmpty() && anioSeleccionado.isEmpty()) {
            anioSeleccionado = aniosDisponibles.first().toString()
        }
    }

    // Cargar meses cuando se selecciona un año
    LaunchedEffect(anioSeleccionado) {
        val anioInt = anioSeleccionado.toIntOrNull()
        if (anioInt != null) {
            vm.cargarMesesDisponibles(anioInt)
        }
    }

    LaunchedEffect(Unit) {
        vm.cargarAniosDisponibles()
    }

    // Cargar datos cuando cambien los filtros principales
    LaunchedEffect(tipoSlaSeleccionado, anioSeleccionado, mesSeleccionado, areaSeleccionada) {
        Log.d("TendenciaScreen", "🔄 Filtros cambiados: tipoSla=$tipoSlaSeleccionado, anio=$anioSeleccionado, mes=$mesSeleccionado, area=$areaSeleccionada")
        if (tipoSlaSeleccionado.isNotEmpty() && anioSeleccionado.isNotEmpty()) {
            val anioInt = anioSeleccionado.toIntOrNull()
            val mesInt = if (mesSeleccionado.isNotEmpty()) mesSeleccionado.toIntOrNull() else null
            val areaInt = if (areaSeleccionada.isNotEmpty()) areaSeleccionada.toIntOrNull() else null

            if (anioInt != null) {
                Log.d("TendenciaScreen", "📡 Cargando reporte: mes=$mesInt, anio=$anioInt, tipoSla=$tipoSlaSeleccionado, area=$areaInt")
                vm.cargarReporteTendencia(
                    mes = mesInt,
                    anio = anioInt,
                    tipoSla = tipoSlaSeleccionado,
                    idArea = areaInt
                )
            }
        }
    }

    // Logging del estado de los datos
    LaunchedEffect(historico, cargando, error) {
        Log.d("TendenciaScreen", "📊 Estado: historico=${historico.size} puntos, cargando=$cargando, error=$error")
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GrisClaro)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Encabezado
            EncabezadoTendencia()

            // 2. Filtros de análisis
            FiltrosAnalisis(
                tipoSlaSeleccionado = tipoSlaSeleccionado,
                onTipoSlaChange = { tipoSlaSeleccionado = it },
                anioSeleccionado = anioSeleccionado,
                onAnioChange = { anioSeleccionado = it },
                mesSeleccionado = mesSeleccionado,
                onMesChange = { mesSeleccionado = it },
                areaSeleccionada = areaSeleccionada,
                onAreaChange = { areaSeleccionada = it },
                aniosDisponibles = aniosDisponibles,
                mesesDisponibles = mesesDisponibles,
                areasDisponibles = areasDisponibles,
                tiposSlaDisponibles = tiposSlaDisponibles,
                habilitado = !cargando
            )

            // 3. Contenido principal
            if (cargando) {
                CargandoIndicador()
            } else if (error != null) {
                ErrorMensaje(error!!)
            } else if (historico.isNotEmpty()) {
                // KPIs principales
                val kpis = vm.calcularKPIs()
                if (kpis != null) {
                    TarjetasKPIs(
                        mejorMes = "${kpis.valorMejorMes.toInt()}%",
                        peorMes = "${kpis.valorPeorMes.toInt()}%",
                        promedio = "${kpis.promedioHistorico.toInt()}%",
                        estadoTendencia = estadoTendencia ?: "estable"
                    )
                }

                // Gráfico de líneas
                GraficoTendencia(
                    historico = historico,
                    tendencia = tendencia,
                    proyeccion = proyeccion
                )

                // Tabla de detalle
                TablaDetalle(historico = historico)

                // Botones de acción
                BotonesAccion(
                    onExportarPDF = { vm.exportarPDF() },
                    onCompartir = { vm.compartirReporte() },
                    habilitado = !cargando
                )

                // Pie de página
                if (ultimaActualizacion != null) {
                    PiePaginaTendencia(ultimaActualizacion!!)
                }
            }
        }
    }
}

@Composable
private fun EncabezadoTendencia() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Tendencia y Proyección del Cumplimiento SLA",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF202124),
            lineHeight = 24.sp
        )
        Text(
            text = "Análisis del comportamiento histórico y estimación futura del nivel de servicio",
            fontSize = 12.sp,
            color = GrisTexto,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun FiltrosAnalisis(
    tipoSlaSeleccionado: String,
    onTipoSlaChange: (String) -> Unit,
    anioSeleccionado: String,
    onAnioChange: (String) -> Unit,
    mesSeleccionado: String,
    onMesChange: (String) -> Unit,
    areaSeleccionada: String,
    onAreaChange: (String) -> Unit,
    aniosDisponibles: List<Int>,
    mesesDisponibles: List<Int>,
    areasDisponibles: List<com.example.proyecto1.data.remote.dto.AreaFiltroDto>,
    tiposSlaDisponibles: List<com.example.proyecto1.data.remote.dto.TipoSlaDto>,
    habilitado: Boolean
) {
    var expandedTipoSla by remember { mutableStateOf(false) }
    var expandedAnio by remember { mutableStateOf(false) }
    var expandedMes by remember { mutableStateOf(false) }
    var expandedArea by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Filtros",
                    tint = AzulCorporativo,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Filtros de Análisis",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF202124)
                )
            }

            Text(
                text = "Personaliza el análisis de tendencia con datos desde la base de datos",
                fontSize = 11.sp,
                color = GrisTexto
            )

            // Primera fila: Tipo SLA (ocupa todo el ancho)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Tipo SLA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = GrisTexto
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    OutlinedButton(
                        onClick = { expandedTipoSla = true },
                        enabled = habilitado && tiposSlaDisponibles.isNotEmpty(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GrisTexto),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val textoMostrar = if (tipoSlaSeleccionado.isEmpty()) {
                                if (tiposSlaDisponibles.isEmpty()) "Cargando..." else "Seleccionar"
                            } else {
                                tiposSlaDisponibles.find { it.codigo == tipoSlaSeleccionado }?.let {
                                    "${it.codigo} (${it.diasUmbral} días)"
                                } ?: tipoSlaSeleccionado
                            }
                            Text(textoMostrar, fontSize = 13.sp)
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = expandedTipoSla,
                        onDismissRequest = { expandedTipoSla = false }
                    ) {
                        tiposSlaDisponibles.forEach { tipo ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(tipo.codigo, fontWeight = FontWeight.Bold)
                                        Text(tipo.descripcion, fontSize = 11.sp, color = GrisTexto)
                                    }
                                },
                                onClick = {
                                    onTipoSlaChange(tipo.codigo)
                                    expandedTipoSla = false
                                }
                            )
                        }
                    }
                }
            }

            // Segunda fila: Año y Mes (responsivos)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Año (desde BD)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Año",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = GrisTexto
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box {
                        OutlinedButton(
                            onClick = { expandedAnio = true },
                            enabled = habilitado && aniosDisponibles.isNotEmpty(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GrisTexto),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val textoMostrar = if (anioSeleccionado.isEmpty()) {
                                    if (aniosDisponibles.isEmpty()) "Cargando..." else "Seleccionar"
                                } else {
                                    anioSeleccionado
                                }
                                Text(textoMostrar, fontSize = 13.sp)
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = expandedAnio,
                            onDismissRequest = { expandedAnio = false }
                        ) {
                            aniosDisponibles.forEach { anio ->
                                DropdownMenuItem(
                                    text = { Text(anio.toString()) },
                                    onClick = {
                                        onAnioChange(anio.toString())
                                        expandedAnio = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Mes (desde BD - se carga al seleccionar año)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mes (opcional)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = GrisTexto
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box {
                        OutlinedButton(
                            onClick = { expandedMes = true },
                            enabled = habilitado && anioSeleccionado.isNotEmpty(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GrisTexto),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val mesesNombres = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
                                val textoMostrar = when {
                                    mesSeleccionado.isEmpty() -> "Todos los meses"
                                    mesesDisponibles.isEmpty() -> "Cargando..."
                                    else -> {
                                        val mesInt = mesSeleccionado.toIntOrNull()
                                        if (mesInt != null && mesInt in 1..12) {
                                            mesesNombres[mesInt - 1]
                                        } else {
                                            mesSeleccionado
                                        }
                                    }
                                }
                                Text(textoMostrar, fontSize = 13.sp)
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = expandedMes,
                            onDismissRequest = { expandedMes = false }
                        ) {
                            // Opción "Todos"
                            DropdownMenuItem(
                                text = { Text("Todos los meses", fontWeight = FontWeight.Bold) },
                                onClick = {
                                    onMesChange("")
                                    expandedMes = false
                                }
                            )
                            if (mesesDisponibles.isNotEmpty()) {
                                HorizontalDivider()
                                val mesesNombres = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                                                         "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
                                mesesDisponibles.forEach { mes ->
                                    DropdownMenuItem(
                                        text = { Text("${mesesNombres[mes - 1]} ($mes)") },
                                        onClick = {
                                            onMesChange(mes.toString())
                                            expandedMes = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Área / Rol (desde BD)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Rol / Área",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = GrisTexto
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    OutlinedButton(
                        onClick = { expandedArea = true },
                        enabled = habilitado,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GrisTexto),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val textoMostrar = when {
                                areaSeleccionada.isEmpty() -> "Todas las áreas"
                                areasDisponibles.isEmpty() -> "Cargando..."
                                else -> areasDisponibles.find { it.id.toString() == areaSeleccionada }?.let {
                                    "Área ${it.nombre}"
                                } ?: "Área $areaSeleccionada"
                            }
                            Text(textoMostrar, fontSize = 13.sp)
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = expandedArea,
                        onDismissRequest = { expandedArea = false }
                    ) {
                        // Opción "Todas"
                        DropdownMenuItem(
                            text = { Text("Todas las áreas", fontWeight = FontWeight.Bold) },
                            onClick = {
                                onAreaChange("")
                                expandedArea = false
                            }
                        )
                        HorizontalDivider()
                        // Áreas desde BD
                        areasDisponibles.forEach { area ->
                            DropdownMenuItem(
                                text = { Text("Área ${area.nombre} (ID: ${area.id})") },
                                onClick = {
                                    onAreaChange(area.id.toString())
                                    expandedArea = false
                                }
                            )
                        }
                    }
                }
            }

            // Información de periodo (siempre muestra "Últimos 12 meses")
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Periodo de análisis",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = GrisTexto
                )
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AzulCorporativo.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = AzulCorporativo,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Últimos 12 meses del año seleccionado",
                            fontSize = 13.sp,
                            color = Color(0xFF202124)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetasKPIs(
    mejorMes: String,
    peorMes: String,
    promedio: String,
    estadoTendencia: String
) {
    // Responsive: 2 columnas en pantallas pequeñas, 4 en grandes
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Primera fila
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Mejor mes
            TarjetaKPI(
                titulo = "Mejor mes",
                valor = mejorMes,
                subtitulo = "Mes positivo",
                color = Verde,
                icono = Icons.Default.Check,
                modifier = Modifier.weight(1f)
            )

            // Peor mes
            TarjetaKPI(
                titulo = "Peor mes",
                valor = peorMes,
                subtitulo = "Mes crítico",
                color = Rojo,
                icono = Icons.Default.Warning,
                modifier = Modifier.weight(1f)
            )
        }

        // Segunda fila
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Promedio del periodo
            TarjetaKPI(
                titulo = "Promedio",
                valor = promedio,
                subtitulo = "Media",
                color = AzulCorporativo,
                icono = Icons.Default.Info,
                modifier = Modifier.weight(1f)
            )

            // Estado de tendencia
            val (iconoTendencia, colorTendencia, textoTendencia) = when (estadoTendencia.lowercase()) {
                "mejorando" -> Triple(Icons.Default.KeyboardArrowUp, Verde, "↑ Mejorando")
                "empeorando" -> Triple(Icons.Default.KeyboardArrowDown, Rojo, "↓ Empeorando")
                else -> Triple(Icons.Default.Refresh, Amarillo, "→ Estable")
            }

            TarjetaKPI(
                titulo = "Tendencia",
                valor = textoTendencia,
                subtitulo = "",
                color = colorTendencia,
                icono = iconoTendencia,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TarjetaKPI(
    titulo: String,
    valor: String,
    subtitulo: String,
    color: Color,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = titulo,
                    fontSize = 12.sp,
                    color = GrisTexto,
                    lineHeight = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                Card(
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.padding(6.dp).size(16.dp)
                    )
                }
            }

            Text(
                text = valor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF202124)
            )

            if (subtitulo.isNotEmpty()) {
                Text(
                    text = subtitulo,
                    fontSize = 11.sp,
                    color = GrisTexto
                )
            }
        }
    }
}

@Composable
private fun GraficoTendencia(
    historico: List<com.example.proyecto1.data.remote.dto.PuntoHistoricoDto>,
    tendencia: List<com.example.proyecto1.data.remote.dto.PuntoTendenciaDto>,
    proyeccion: Double?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Gráfico",
                    tint = AzulCorporativo,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Evolución Histórica y Predicción",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF202124)
                )
            }

            Text(
                text = "Tendencia del cumplimiento SLA a lo largo del tiempo",
                fontSize = 11.sp,
                color = GrisTexto
            )

            // Leyenda
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LeyendaItem("Histórico", AzulCorporativo)
                LeyendaItem("Tendencia", Naranja)
                if (proyeccion != null) {
                    LeyendaItem("Proyección", Verde)
                }
            }

            // Gráfico (usar librería de gráficos)
            if (historico.isNotEmpty()) {
                GraficoLineasChart(historico, tendencia, proyeccion)
            }
        }
    }
}

@Composable
private fun LeyendaItem(texto: String, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = RoundedCornerShape(2.dp))
        )
        Text(
            text = texto,
            fontSize = 11.sp,
            color = GrisTexto
        )
    }
}

@Composable
private fun GraficoLineasChart(
    historico: List<com.example.proyecto1.data.remote.dto.PuntoHistoricoDto>,
    tendencia: List<com.example.proyecto1.data.remote.dto.PuntoTendenciaDto>,
    proyeccion: Double?
) {
    // Gráfico simple con Canvas (hasta que instales YCharts)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(Color.White)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val width = size.width
            val height = size.height
            val padding = 40f

            // Calcular escala
            val maxValor = historico.maxOfOrNull { it.valor }?.toFloat() ?: 100f
            val minValor = historico.minOfOrNull { it.valor }?.toFloat() ?: 0f
            val rangoValor = maxValor - minValor

            val stepX = (width - padding * 2) / (historico.size - 1).coerceAtLeast(1)

            // Dibujar ejes
            drawLine(
                color = Color.Gray.copy(alpha = 0.3f),
                start = Offset(padding, padding),
                end = Offset(padding, height - padding),
                strokeWidth = 2f
            )
            drawLine(
                color = Color.Gray.copy(alpha = 0.3f),
                start = Offset(padding, height - padding),
                end = Offset(width - padding, height - padding),
                strokeWidth = 2f
            )

            // Dibujar línea histórica (azul)
            if (historico.size > 1) {
                for (i in 0 until historico.size - 1) {
                    val x1 = padding + stepX * i
                    val y1 = height - padding - ((historico[i].valor.toFloat() - minValor) / rangoValor * (height - padding * 2))
                    val x2 = padding + stepX * (i + 1)
                    val y2 = height - padding - ((historico[i + 1].valor.toFloat() - minValor) / rangoValor * (height - padding * 2))

                    drawLine(
                        color = Color(0xFF2196F3),
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                }

                // Dibujar puntos
                historico.forEachIndexed { index, punto ->
                    val x = padding + stepX * index
                    val y = height - padding - ((punto.valor.toFloat() - minValor) / rangoValor * (height - padding * 2))

                    drawCircle(
                        color = Color(0xFF2196F3),
                        radius = 6f,
                        center = Offset(x, y)
                    )
                }
            }

            // Dibujar línea de tendencia (naranja) si existe
            if (tendencia.size > 1) {
                for (i in 0 until tendencia.size - 1) {
                    val x1 = padding + stepX * i
                    val y1 = height - padding - ((tendencia[i].valor.toFloat() - minValor) / rangoValor * (height - padding * 2))
                    val x2 = padding + stepX * (i + 1)
                    val y2 = height - padding - ((tendencia[i + 1].valor.toFloat() - minValor) / rangoValor * (height - padding * 2))

                    drawLine(
                        color = Color(0xFFFF9800),
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 2f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // Dibujar punto de proyección (verde) si existe
            if (proyeccion != null && historico.isNotEmpty()) {
                val x = padding + stepX * historico.size
                val y = height - padding - ((proyeccion.toFloat() - minValor) / rangoValor * (height - padding * 2))

                drawCircle(
                    color = Color(0xFF4CAF50),
                    radius = 8f,
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = Offset(x, y)
                )
            }
        }

        // Mensaje informativo
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (historico.isEmpty()) {
                Text(
                    "No hay datos para mostrar",
                    color = GrisTexto,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun TablaDetalle(historico: List<com.example.proyecto1.data.remote.dto.PuntoHistoricoDto>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Tabla",
                    tint = AzulCorporativo,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Detalle del desempeño por mes con datos históricos",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF202124)
                )
            }

            // Encabezados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Mes/Año", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrisTexto, modifier = Modifier.weight(1f))
                Text("Total Casos", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrisTexto, modifier = Modifier.weight(1f))
                Text("Cumplidos", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrisTexto, modifier = Modifier.weight(1f))
                Text("No Cumplidos", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrisTexto, modifier = Modifier.weight(1f))
                Text("% Cumpli", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrisTexto, modifier = Modifier.weight(0.8f))
            }

            HorizontalDivider(color = Color(0xFFE0E0E0))

            // Filas de datos
            historico.forEach { punto ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(punto.mes, fontSize = 12.sp, color = Color(0xFF202124), modifier = Modifier.weight(1f))
                    Text(punto.totalCasos.toString(), fontSize = 12.sp, color = Color(0xFF202124), modifier = Modifier.weight(1f))
                    Text(punto.cumplidos.toString(), fontSize = 12.sp, color = Verde, modifier = Modifier.weight(1f))
                    Text(punto.noCumplidos.toString(), fontSize = 12.sp, color = Rojo, modifier = Modifier.weight(1f))

                    val porcentaje = punto.valor.toInt()
                    val colorPorcentaje = when {
                        porcentaje >= 90 -> Verde
                        porcentaje >= 70 -> Amarillo
                        else -> Rojo
                    }
                    Text(
                        "$porcentaje%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorPorcentaje,
                        modifier = Modifier.weight(0.8f)
                    )
                }
                HorizontalDivider(color = Color(0xFFF0F0F0))
            }
        }
    }
}

@Composable
private fun BotonesAccion(
    onExportarPDF: () -> Unit,
    onCompartir: () -> Unit,
    habilitado: Boolean
) {
    // Responsive: Columna en pantallas pequeñas, fila en grandes
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Botón Exportar PDF
        Button(
            onClick = onExportarPDF,
            enabled = habilitado,
            colors = ButtonDefaults.buttonColors(containerColor = AzulCorporativo),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Exportar PDF", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }

        // Botón Compartir
        OutlinedButton(
            onClick = onCompartir,
            enabled = habilitado,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AzulCorporativo),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, AzulCorporativo)
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Compartir Reporte", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun CargandoIndicador() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = AzulCorporativo)
            Text("Cargando datos...", fontSize = 13.sp, color = GrisTexto)
        }
    }
}

@Composable
private fun ErrorMensaje(mensaje: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Rojo.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Rojo, modifier = Modifier.size(24.dp))
            Text(mensaje, fontSize = 13.sp, color = Rojo)
        }
    }
}

@Composable
private fun PiePaginaTendencia(ultimaActualizacion: String) {
    Text(
        text = "Última actualización: $ultimaActualizacion",
        fontSize = 11.sp,
        color = GrisTexto,
        modifier = Modifier.fillMaxWidth()
    )
}

