package com.example.proyecto1.presentation.prediccion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colores corporativos según especificación
private val AzulCorporativo = Color(0xFF2196F3)
private val GrisClaro = Color(0xFFF4F6F8)
private val GrisTexto = Color(0xFF616161)
private val Verde = Color(0xFF4CAF50)
private val Rojo = Color(0xFFE53935)
private val Amarillo = Color(0xFFFFA726)
private val FondoGris = Color(0xFFF5F7FA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrediccionScreen(
    vm: PrediccionViewModel
) {
    val prediccion by vm.prediccion.collectAsState()
    val valorReal by vm.valorReal.collectAsState()
    val slope by vm.slope.collectAsState()
    val intercept by vm.intercept.collectAsState()
    val error by vm.error.collectAsState()
    val cargando by vm.cargando.collectAsState()
    val mostrarAdvertencia by vm.mostrarAdvertencia.collectAsState()
    val ultimaActualizacion by vm.ultimaActualizacion.collectAsState()
    val usandoDatosDemo by vm.usandoDatosDemo.collectAsState()

    // Filtros dinamicos desde la base de datos
    val aniosDisponibles by vm.aniosDisponibles.collectAsState()
    val mesesDisponibles by vm.mesesDisponibles.collectAsState()

    // Estado local para filtros - usa el primer anio disponible o vacio
    var mesInicioSeleccionado by remember { mutableStateOf("Enero") }
    var mesFinSeleccionado by remember { mutableStateOf("Diciembre") }
    var anioSeleccionado by remember { mutableStateOf("") }

    // Funcion helper para convertir nombre de mes a indice
    fun mesToIndex(nombre: String): Int? {
        val meses = listOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
        val idx = meses.indexOf(nombre)
        return if (idx >= 0) idx + 1 else null
    }

    fun validarRango(): String? {
        val mesInicio = mesToIndex(mesInicioSeleccionado)
        val mesFin = mesToIndex(mesFinSeleccionado)

        if (mesInicio != null && mesFin != null && mesFin < mesInicio) {
            return "El mes de fin debe ser mayor o igual al mes de inicio"
        }
        return null
    }

    // Cuando se carguen los anios disponibles, seleccionar el mas reciente
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
            // Cargar datos automáticamente cuando hay año seleccionado
            val mesInicio = mesToIndex(mesInicioSeleccionado)
            val mesFin = mesToIndex(mesFinSeleccionado)
            vm.cargarYPredecir(mesInicio = mesInicio, mesFin = mesFin, anio = anioInt, meses = 12)
        }
    }

    LaunchedEffect(Unit) {
        // Solo cargar años disponibles, no datos todavía
        vm.cargarAniosDisponibles()
    }

    Scaffold {
        padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GrisClaro)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Encabezado principal
            EncabezadoPrincipal()

            // 2. Barra de filtros
            BarraFiltros(
                onActualizar = {
                    val errorRango = validarRango()
                    if (errorRango != null) {
                        // Mostrar error de validación
                        return@BarraFiltros
                    }
                    val mesInicio = mesToIndex(mesInicioSeleccionado)
                    val mesFin = mesToIndex(mesFinSeleccionado)
                    val anioInt = anioSeleccionado.toIntOrNull()
                    vm.cargarYPredecir(mesInicio = mesInicio, mesFin = mesFin, anio = anioInt, meses = 12)
                },
                habilitado = !cargando,
                usandoDatosDemo = usandoDatosDemo,
                mesInicioSeleccionado = mesInicioSeleccionado,
                onMesInicioSeleccionado = { mesInicioSeleccionado = it },
                mesFinSeleccionado = mesFinSeleccionado,
                onMesFinSeleccionado = { mesFinSeleccionado = it },
                anioSeleccionado = anioSeleccionado,
                onAnioSeleccionado = { anioSeleccionado = it },
                aniosDisponibles = aniosDisponibles,
                mesesDisponibles = mesesDisponibles,
                errorValidacion = validarRango()
            )


            // 3. Contenido principal
            if (cargando) {
                CargandoIndicador()
            } else if (error != null) {
                ErrorMensaje(error!!)
            } else {
                // Sección de resultado principal
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // KPI principal
                    TarjetaKPIPrincipal(
                        prediccion = prediccion,
                        slope = slope,
                        ultimaActualizacion = ultimaActualizacion,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Comparación predicción vs realidad (si existe)
                    if (valorReal != null) {
                        TarjetaComparacion(
                            prediccion = prediccion,
                            valorReal = valorReal,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Coeficientes del modelo
                    TarjetaCoeficientes(
                        slope = slope,
                        intercept = intercept,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Notificación de advertencia
                if (mostrarAdvertencia) {
                    TarjetaAdvertencia()
                }

                // Acciones del usuario
                AccionesUsuario(
                    onRecalcular = { vm.cargarYPredecir() },
                    onExportar = { vm.exportarResultado() },
                    habilitado = !cargando
                )

                // Pie de pantalla
                PiePantalla()
            }
        }
    }
}

@Composable
private fun EncabezadoPrincipal() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Predicción de Cumplimiento SLA",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF202124),
            lineHeight = 24.sp
        )
        Text(
            text = "Estimación basada en datos históricos y regresión lineal simple (y = mx + b)",
            fontSize = 12.sp,
            color = GrisTexto,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun BarraFiltros(
    onActualizar: () -> Unit,
    habilitado: Boolean,
    usandoDatosDemo: Boolean,
    mesInicioSeleccionado: String,
    onMesInicioSeleccionado: (String) -> Unit,
    mesFinSeleccionado: String,
    onMesFinSeleccionado: (String) -> Unit,
    anioSeleccionado: String,
    onAnioSeleccionado: (String) -> Unit,
    aniosDisponibles: List<Int>,
    mesesDisponibles: List<Int>,
    errorValidacion: String?
) {
    val colorBanner = if (usandoDatosDemo) Color(0xFFE3F2FD) else Color(0xFFE8F5E9)
    val colorTexto = if (usandoDatosDemo) Color(0xFF0D47A1) else Color(0xFF1B5E20)
    val colorIcono = if (usandoDatosDemo) AzulCorporativo else Verde
    val titulo = if (usandoDatosDemo) "Predicción con Datos Demo" else "✓ Conectado a API Real"
    val mensaje = if (usandoDatosDemo) {
        "Las predicciones mostradas se basan en datos de demostración. Verifica que tu API esté corriendo en http://localhost:5120"
    } else {
        "Conectado exitosamente a tu API. Los datos se obtienen de SQL Server en tiempo real."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorBanner),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Info",
                        tint = colorIcono,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = titulo,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorIcono
                    )
                }

                if (usandoDatosDemo) {
                    Button(
                        onClick = onActualizar,
                        enabled = habilitado,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = AzulCorporativo
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text("Reintentar", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Text(
                text = mensaje,
                fontSize = 11.sp,
                color = colorTexto,
                lineHeight = 15.sp
            )

            // Selectores de filtros
            SelectorMesAnio(
                mesInicioSeleccionado = mesInicioSeleccionado,
                onMesInicioSeleccionado = onMesInicioSeleccionado,
                mesFinSeleccionado = mesFinSeleccionado,
                onMesFinSeleccionado = onMesFinSeleccionado,
                anioSeleccionado = anioSeleccionado,
                onAnioSeleccionado = onAnioSeleccionado,
                onActualizar = onActualizar,
                habilitado = habilitado,
                aniosDisponibles = aniosDisponibles,
                mesesDisponibles = mesesDisponibles,
                errorValidacion = errorValidacion
            )
        }
    }
}

@Composable
private fun SelectorMesAnio(
    mesInicioSeleccionado: String,
    onMesInicioSeleccionado: (String) -> Unit,
    mesFinSeleccionado: String,
    onMesFinSeleccionado: (String) -> Unit,
    anioSeleccionado: String,
    onAnioSeleccionado: (String) -> Unit,
    onActualizar: () -> Unit,
    habilitado: Boolean,
    aniosDisponibles: List<Int>,
    mesesDisponibles: List<Int>,
    errorValidacion: String?
) {
    var expandedMesInicio by remember { mutableStateOf(false) }
    var expandedMesFin by remember { mutableStateOf(false) }
    var expandedAnio by remember { mutableStateOf(false) }

    val nombresMeses = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    // Construir lista de meses basada en los disponibles en la BD
    val mesesParaMostrar = buildList {
        mesesDisponibles.forEach { mes ->
            if (mes in 1..12) {
                add(nombresMeses[mes - 1])
            }
        }
    }

    // Construir lista de anios como strings
    val aniosParaMostrar = aniosDisponibles.map { it.toString() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Rango de Período para Predicción",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF202124)
            )

            // Selector de Año
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Año",
                    fontSize = 12.sp,
                    color = GrisTexto,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box {
                    OutlinedButton(
                        onClick = { expandedAnio = true },
                        enabled = habilitado,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = GrisTexto
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(
                            text = anioSeleccionado,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expandir",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expandedAnio,
                        onDismissRequest = { expandedAnio = false }
                    ) {
                        aniosParaMostrar.forEach { anio ->
                            DropdownMenuItem(
                                text = { Text(anio) },
                                onClick = {
                                    onAnioSeleccionado(anio)
                                    expandedAnio = false
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Selector de Mes Inicio
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mes Inicio",
                        fontSize = 12.sp,
                        color = GrisTexto,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Box {
                        OutlinedButton(
                            onClick = { expandedMesInicio = true },
                            enabled = habilitado,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = GrisTexto
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (errorValidacion != null) Rojo else Color(0xFFE0E0E0)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text(
                                text = mesInicioSeleccionado,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expandir",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = expandedMesInicio,
                            onDismissRequest = { expandedMesInicio = false }
                        ) {
                            mesesParaMostrar.forEach { mes ->
                                DropdownMenuItem(
                                    text = { Text(mes) },
                                    onClick = {
                                        onMesInicioSeleccionado(mes)
                                        expandedMesInicio = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Selector de Mes Fin
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mes Fin",
                        fontSize = 12.sp,
                        color = GrisTexto,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Box {
                        OutlinedButton(
                            onClick = { expandedMesFin = true },
                            enabled = habilitado,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = GrisTexto
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (errorValidacion != null) Rojo else Color(0xFFE0E0E0)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text(
                                text = mesFinSeleccionado,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expandir",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = expandedMesFin,
                            onDismissRequest = { expandedMesFin = false }
                        ) {
                            mesesParaMostrar.forEach { mes ->
                                DropdownMenuItem(
                                    text = { Text(mes) },
                                    onClick = {
                                        onMesFinSeleccionado(mes)
                                        expandedMesFin = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Mensaje de error de validación
            if (errorValidacion != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = Rojo,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = errorValidacion,
                        fontSize = 11.sp,
                        color = Rojo,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Botón Actualizar Datos
            Button(
                onClick = onActualizar,
                enabled = habilitado && errorValidacion == null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulCorporativo,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth().height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Actualizar",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Calcular Predicción", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun TarjetaKPIPrincipal(
    prediccion: Double?,
    slope: Double?,
    ultimaActualizacion: String?,
    modifier: Modifier = Modifier
) {
    val tendenciaPositiva = (slope ?: 0.0) > 0

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título de la sección
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Info",
                    tint = GrisTexto,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "SLA Proyectado para el próximo mes",
                    fontSize = 13.sp,
                    color = GrisTexto,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp
                )
            }

            // Valor principal de la predicción
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "%.1f%%".format(prediccion ?: 0.0),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF202124),
                    lineHeight = 48.sp
                )

                // Indicador de tendencia
                Surface(
                    color = if (tendenciaPositiva)
                        Color(0xFFE8F5E9)
                    else
                        Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Tendencia",
                            tint = if (tendenciaPositiva) Verde else Rojo,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (tendenciaPositiva) "tendencia\npositiva" else "tendencia\nnegativa",
                            fontSize = 10.sp,
                            color = if (tendenciaPositiva) Verde else Rojo,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)

            // Texto de fecha
            Text(
                text = "Última actualización: ${ultimaActualizacion ?: "Cargando..."}",
                fontSize = 11.sp,
                color = GrisTexto.copy(alpha = 0.7f),
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun TarjetaCoeficientes(
    slope: Double?,
    intercept: Double?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Coeficientes del Modelo",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF202124)
            )

            Text(
                text = "Parámetros de regresión lineal",
                fontSize = 11.sp,
                color = GrisTexto.copy(alpha = 0.7f)
            )

            HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)

            // Pendiente
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Pendiente",
                    fontSize = 12.sp,
                    color = GrisTexto,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "%.4f".format(slope ?: 0.0),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF202124)
                )
            }

            HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)

            // Intercepto
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Intercepto",
                    fontSize = 12.sp,
                    color = GrisTexto,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "%.4f".format(intercept ?: 0.0),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF202124)
                )
            }

            Text(
                text = "Modelo generado automáticamente",
                fontSize = 10.sp,
                color = GrisTexto.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun TarjetaAdvertencia() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF8E1)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Advertencia",
                tint = Color(0xFFF57C00),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Advertencia: Predicción inferior al umbral mínimo de cumplimiento.",
                fontSize = 12.sp,
                color = Color(0xFF6D4C41),
                fontWeight = FontWeight.Medium,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun AccionesUsuario(
    onRecalcular: () -> Unit,
    onExportar: () -> Unit,
    habilitado: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onRecalcular,
            enabled = habilitado,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AzulCorporativo
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Recalcular",
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Recalcular Predicción",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        OutlinedButton(
            onClick = onExportar,
            enabled = habilitado,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = GrisTexto
            ),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Text(
                text = "Exportar Resultado",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PiePantalla() {
    Text(
        text = "Fuente de datos: Historial SLA mensual",
        fontSize = 12.sp,
        color = GrisTexto.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CargandoIndicador() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = AzulCorporativo)
            Text(
                text = "Calculando predicción...",
                fontSize = 14.sp,
                color = GrisTexto
            )
        }
    }
}

@Composable
private fun ErrorMensaje(mensaje: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Rojo.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = Rojo,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = mensaje,
                fontSize = 12.sp,
                color = Rojo,
                fontWeight = FontWeight.Medium,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun TarjetaComparacion(
    prediccion: Double?,
    valorReal: Double?,
    modifier: Modifier = Modifier
) {
    if (prediccion == null || valorReal == null) return

    val diferencia = valorReal - prediccion
    val porcentajeDiferencia = (diferencia / prediccion) * 100
    val esPositivo = diferencia >= 0

    val colorDiferencia = when {
        kotlin.math.abs(diferencia) < 1.0 -> Color(0xFF9E9E9E) // Gris - casi igual
        esPositivo -> Verde // Verde - mejor que lo predicho
        else -> Color(0xFFF57C00) // Naranja - peor que lo predicho
    }

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 Comparación: Predicho vs Real",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF202124)
                )
                Icon(
                    imageVector = if (esPositivo) Icons.Default.Check else Icons.Default.Warning,
                    contentDescription = null,
                    tint = colorDiferencia,
                    modifier = Modifier.size(20.dp)
                )
            }

            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

            // Fila de comparación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Predicción
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Predicho",
                        fontSize = 11.sp,
                        color = GrisTexto
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${String.format("%.1f", prediccion)}%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulCorporativo
                    )
                }

                // Separador visual
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(60.dp)
                        .background(Color(0xFFE0E0E0))
                )

                // Real
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Real",
                        fontSize = 11.sp,
                        color = GrisTexto
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${String.format("%.1f", valorReal)}%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Verde
                    )
                }
            }

            // Diferencia
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = colorDiferencia.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (esPositivo) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = colorDiferencia,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Diferencia: ${if (esPositivo) "+" else ""}${String.format("%.2f", diferencia)}% " +
                                "(${if (esPositivo) "+" else ""}${String.format("%.1f", porcentajeDiferencia)}%)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorDiferencia
                    )
                }
            }

            // Interpretación
            Text(
                text = when {
                    kotlin.math.abs(diferencia) < 1.0 -> "✓ La predicción fue muy precisa"
                    esPositivo -> "✓ El cumplimiento fue mejor de lo esperado"
                    else -> "⚠ El cumplimiento fue menor a lo predicho"
                },
                fontSize = 11.sp,
                color = GrisTexto,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}
