package com.example.proyecto1.presentation.prediccion

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import java.util.Locale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

// Colores corporativos según especificación
private val AzulCorporativo = Color(0xFF2196F3)
private val GrisClaro = Color(0xFFF4F6F8)
private val GrisTexto = Color(0xFF616161)
private val Verde = Color(0xFF4CAF50)
private val Rojo = Color(0xFFE53935)

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
    val datosHistoricos by vm.datosHistoricos.collectAsState()

    // Filtros dinamicos desde la base de datos
    val tiposSlaDisponibles by vm.tiposSlaDisponibles.collectAsState()
    val aniosDisponibles by vm.aniosDisponibles.collectAsState()
    val mesesDisponibles by vm.mesesDisponibles.collectAsState()

    // Estado local para filtros - INICIAR VACÍO para esperar datos de BD
    var tipoSlaSeleccionado by remember { mutableStateOf("") }
    var mesInicioSeleccionado by remember { mutableStateOf("Enero") }
    var mesFinSeleccionado by remember { mutableStateOf("Diciembre") }
    var anioSeleccionado by remember { mutableStateOf("2025") }

    // Logging de cambios de estado de meses
    LaunchedEffect(mesInicioSeleccionado, mesFinSeleccionado) {
        Log.d("PrediccionScreen", "📅 Estado actual: mesInicio='$mesInicioSeleccionado', mesFin='$mesFinSeleccionado'")
    }

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

    // Cuando se carguen los tipos SLA desde la BD, seleccionar el primero automáticamente
    LaunchedEffect(tiposSlaDisponibles) {
        Log.d("PrediccionScreen", "📋 Tipos SLA disponibles: ${tiposSlaDisponibles.size}")
        tiposSlaDisponibles.forEach { (codigo, descripcion) ->
            Log.d("PrediccionScreen", "   • $codigo: $descripcion")
        }

        if (tiposSlaDisponibles.isNotEmpty() && tipoSlaSeleccionado.isEmpty()) {
            val primerTipo = tiposSlaDisponibles.first().first
            tipoSlaSeleccionado = primerTipo
            Log.d("PrediccionScreen", "✅ Tipo SLA seleccionado automáticamente: $primerTipo")
        }
    }

    // Cargar tipos SLA y años disponibles al inicio
    LaunchedEffect(Unit) {
        Log.d("PrediccionScreen", "🔵 Inicializando PrediccionScreen")
        vm.cargarAniosDisponibles()
        vm.cargarTiposSlaDisponibles()
    }

    // Cargar datos automáticamente cuando haya tipo SLA seleccionado
    LaunchedEffect(tipoSlaSeleccionado, anioSeleccionado) {
        if (tipoSlaSeleccionado.isNotEmpty() && anioSeleccionado.isNotEmpty()) {
            val anioInt = anioSeleccionado.toIntOrNull()
            if (anioInt != null) {
                val mesInicio = mesToIndex(mesInicioSeleccionado)
                val mesFin = mesToIndex(mesFinSeleccionado)

                Log.d("PrediccionScreen", "🔄 Auto-cargando predicción: tipoSla=$tipoSlaSeleccionado, anio=$anioInt")

                vm.cargarYPredecir(
                    mesInicio = mesInicio,
                    mesFin = mesFin,
                    anio = anioInt,
                    meses = 12,
                    tipoSla = tipoSlaSeleccionado
                )
            }
        } else {
            Log.d("PrediccionScreen", "⏸️ Esperando selección de tipo SLA (actual: '$tipoSlaSeleccionado')")
        }
    }

    // Logging del estado de los datos
    LaunchedEffect(prediccion, datosHistoricos, cargando, error) {
        Log.d("PrediccionScreen", "📊 Estado: prediccion=$prediccion, historicos=${datosHistoricos.size} puntos, cargando=$cargando, error=$error")
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
                    vm.cargarYPredecir(
                        mesInicio = mesInicio,
                        mesFin = mesFin,
                        anio = anioInt,
                        meses = 12,
                        tipoSla = tipoSlaSeleccionado.ifEmpty { "SLA001" }
                    )
                },
                habilitado = !cargando,
                usandoDatosDemo = usandoDatosDemo,
                tipoSlaSeleccionado = tipoSlaSeleccionado,
                onTipoSlaSeleccionado = { tipoSlaSeleccionado = it },
                tiposSlaDisponibles = tiposSlaDisponibles,
                mesInicioSeleccionado = mesInicioSeleccionado,
                onMesInicioSeleccionado = { nuevoMes ->
                    Log.d("PrediccionScreen", "🗓️ Mes Inicio: '$mesInicioSeleccionado' → '$nuevoMes'")
                    mesInicioSeleccionado = nuevoMes
                    Log.d("PrediccionScreen", "✅ Mes Inicio actualizado a: '$mesInicioSeleccionado'")
                },
                mesFinSeleccionado = mesFinSeleccionado,
                onMesFinSeleccionado = { nuevoMes ->
                    Log.d("PrediccionScreen", "🗓️ Mes Fin: '$mesFinSeleccionado' → '$nuevoMes'")
                    mesFinSeleccionado = nuevoMes
                    Log.d("PrediccionScreen", "✅ Mes Fin actualizado a: '$mesFinSeleccionado'")
                },
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

                    // Gráfico de histórico y predicción
                    val datosHistoricos by vm.datosHistoricos.collectAsState()
                    if (datosHistoricos.isNotEmpty()) {
                        GraficoHistoricoYPrediccion(
                            datosHistoricos = datosHistoricos,
                            prediccion = prediccion,
                            slope = slope,
                            intercept = intercept,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

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
                    onRecalcular = {
                        val mesInicio = mesToIndex(mesInicioSeleccionado)
                        val mesFin = mesToIndex(mesFinSeleccionado)
                        val anioInt = anioSeleccionado.toIntOrNull()
                        vm.cargarYPredecir(
                            mesInicio = mesInicio,
                            mesFin = mesFin,
                            anio = anioInt,
                            meses = 12,
                            tipoSla = tipoSlaSeleccionado.ifEmpty { "SLA001" }
                        )
                    },
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
    tipoSlaSeleccionado: String,
    onTipoSlaSeleccionado: (String) -> Unit,
    tiposSlaDisponibles: List<Pair<String, String>>,
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
                tipoSlaSeleccionado = tipoSlaSeleccionado,
                onTipoSlaSeleccionado = onTipoSlaSeleccionado,
                tiposSlaDisponibles = tiposSlaDisponibles,
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
    tipoSlaSeleccionado: String,
    onTipoSlaSeleccionado: (String) -> Unit,
    tiposSlaDisponibles: List<Pair<String, String>>,
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

    // Mostrar TODOS los meses siempre disponibles (no depender de BD)
    val mesesParaMostrar = nombresMeses

    Log.d("PrediccionScreen", "📅 Meses para mostrar: ${mesesParaMostrar.size} (${mesesParaMostrar.joinToString()})")

    // Construir lista de años como strings, con fallback si está vacío
    val aniosParaMostrar = if (aniosDisponibles.isNotEmpty()) {
        aniosDisponibles.map { it.toString() }
    } else {
        // Fallback: últimos 3 años
        listOf("2023", "2024", "2025")
    }

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
                text = "Filtros para Predicción SLA",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF202124)
            )

            // Selector de Tipo SLA
            var expandedTipoSla by remember { mutableStateOf(false) }
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Tipo de SLA",
                    fontSize = 12.sp,
                    color = GrisTexto,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box {
                    OutlinedButton(
                        onClick = { expandedTipoSla = true },
                        enabled = habilitado && tiposSlaDisponibles.isNotEmpty(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = GrisTexto
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val textoMostrar = if (tipoSlaSeleccionado.isEmpty()) {
                                if (tiposSlaDisponibles.isEmpty()) "Cargando..." else "Seleccionar"
                            } else {
                                // Mostrar el código del SLA (SLA001, SLA002, etc.)
                                tipoSlaSeleccionado
                            }
                            Text(
                                text = textoMostrar,
                                fontSize = 13.sp
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expandir",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expandedTipoSla,
                        onDismissRequest = { expandedTipoSla = false }
                    ) {
                        tiposSlaDisponibles.forEach { (codigo, descripcion) ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(codigo, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(descripcion, fontSize = 11.sp, color = GrisTexto)
                                    }
                                },
                                onClick = {
                                    onTipoSlaSeleccionado(codigo)
                                    expandedTipoSla = false
                                }
                            )
                        }
                    }
                }
            }

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
private fun GraficoHistoricoYPrediccion(
    datosHistoricos: List<SlaDataPoint>,
    prediccion: Double?,
    @Suppress("UNUSED_PARAMETER") slope: Double?,
    @Suppress("UNUSED_PARAMETER") intercept: Double?,
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Título
            Text(
                text = "Histórico y Predicción",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF202124)
            )

            // Descripción
            Text(
                text = "Cada barra vertical representa un mes. El último segmento (verde) muestra la predicción para el próximo período.",
                fontSize = 11.sp,
                color = GrisTexto.copy(alpha = 0.8f),
                lineHeight = 14.sp
            )

            // Leyenda
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LeyendaItem(color = AzulCorporativo, texto = "Histórico")
                LeyendaItem(color = Verde, texto = "Predicción")
                LeyendaItem(color = Color(0xFFFF9800), texto = "Proyección")
            }

            // Gráfico
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)  // ← Aumentado de 280dp a 350dp
                    .padding(vertical = 8.dp)
            ) {
                val width = size.width
                val height = size.height
                val paddingLeft = 80f  // ← Aumentado de 70f a 80f
                val paddingRight = 60f  // ← Aumentado de 50f a 60f
                val paddingTop = 40f  // ← Aumentado de 30f a 40f
                val paddingBottom = 100f  // ← Aumentado de 70f a 100f

                val graphWidth = width - paddingLeft - paddingRight
                val graphHeight = height - paddingTop - paddingBottom

                // Calcular rango de datos
                val allValues = datosHistoricos.map { it.valor }
                if (allValues.isEmpty()) return@Canvas

                val minValue = kotlin.math.min(0.0, allValues.minOrNull() ?: 0.0)
                val maxValue = kotlin.math.max(100.0, allValues.maxOrNull() ?: 100.0)
                val valueRange = maxValue - minValue

                // Función para convertir valor Y a coordenada
                fun valueToY(value: Double): Float {
                    return paddingTop + graphHeight - ((value - minValue) / valueRange * graphHeight).toFloat()
                }

                // Función para convertir índice X a coordenada (incluyendo predicción)
                fun indexToX(index: Int, total: Int): Float {
                    val spacing = graphWidth / total.toFloat()
                    return paddingLeft + (index * spacing) + (spacing / 2f)
                }

                // Dibujar barras de fondo alternadas por mes (histórico)
                datosHistoricos.forEachIndexed { index, _ ->
                    if (index % 2 == 0) {
                        val x = indexToX(index, datosHistoricos.size + 1)
                        val barWidth = graphWidth / (datosHistoricos.size + 1).toFloat()

                        drawRect(
                            color = Color(0xFFF5F7FA),
                            topLeft = Offset(x - barWidth / 2f, paddingTop),
                            size = androidx.compose.ui.geometry.Size(barWidth, graphHeight)
                        )
                    }
                }

                // Dibujar barra de fondo para la predicción (verde claro)
                val predIndex = datosHistoricos.size
                val xPred = indexToX(predIndex, datosHistoricos.size + 1)
                val barWidth = graphWidth / (datosHistoricos.size + 1).toFloat()

                drawRect(
                    color = Verde.copy(alpha = 0.05f),
                    topLeft = Offset(xPred - barWidth / 2f, paddingTop),
                    size = androidx.compose.ui.geometry.Size(barWidth, graphHeight)
                )

                // Línea divisoria entre histórico y predicción
                drawLine(
                    color = Color(0xFFFF9800).copy(alpha = 0.4f),
                    start = Offset(xPred - barWidth / 2f, paddingTop - 10f),
                    end = Offset(xPred - barWidth / 2f, paddingTop + graphHeight),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))
                )

                // Dibujar ejes
                // Eje Y
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(paddingLeft, paddingTop),
                    end = Offset(paddingLeft, paddingTop + graphHeight),
                    strokeWidth = 2f
                )

                // Eje X
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(paddingLeft, paddingTop + graphHeight),
                    end = Offset(paddingLeft + graphWidth, paddingTop + graphHeight),
                    strokeWidth = 2f
                )

                // Dibujar líneas de guía horizontales y etiquetas Y
                val numGuias = 5
                for (i in 0..numGuias) {
                    val valor = minValue + (valueRange / numGuias * i)
                    val y = valueToY(valor)

                    // Línea de guía
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.1f),
                        start = Offset(paddingLeft, y),
                        end = Offset(paddingLeft + graphWidth, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
                    )

                    // Etiqueta Y
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            String.format(Locale.US, "%.0f%%", valor),
                            paddingLeft - 15f,  // ← Ajustado de -10f a -15f
                            y + 8f,  // ← Ajustado de +5f a +8f
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.GRAY
                                textSize = 32f  // ← Aumentado de 28f a 32f
                                textAlign = android.graphics.Paint.Align.RIGHT
                                isFakeBoldText = true  // ← Agregado para mejor lectura
                            }
                        )
                    }
                }

                // Dibujar línea histórica (azul)
                for (i in 0 until datosHistoricos.size - 1) {
                    val x1 = indexToX(i, datosHistoricos.size + 1) // +1 para incluir predicción
                    val y1 = valueToY(datosHistoricos[i].valor)
                    val x2 = indexToX(i + 1, datosHistoricos.size + 1)
                    val y2 = valueToY(datosHistoricos[i + 1].valor)

                    drawLine(
                        color = AzulCorporativo,
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                }

                // Dibujar puntos del histórico
                datosHistoricos.forEachIndexed { index, punto ->
                    val x = indexToX(index, datosHistoricos.size + 1)
                    val y = valueToY(punto.valor)

                    // Círculo externo
                    drawCircle(
                        color = Color.White,
                        radius = 8f,
                        center = Offset(x, y)
                    )
                    // Círculo interno
                    drawCircle(
                        color = AzulCorporativo,
                        radius = 6f,
                        center = Offset(x, y)
                    )
                }

                // Dibujar línea de predicción (verde punteada)
                if (prediccion != null && datosHistoricos.isNotEmpty()) {
                    val lastIndex = datosHistoricos.size - 1
                    val x1 = indexToX(lastIndex, datosHistoricos.size + 1)
                    val y1 = valueToY(datosHistoricos.last().valor)
                    val x2 = indexToX(datosHistoricos.size, datosHistoricos.size + 1)
                    val y2 = valueToY(prediccion)

                    drawLine(
                        color = Verde,
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )

                    // Punto de predicción (más grande)
                    drawCircle(
                        color = Color.White,
                        radius = 12f,
                        center = Offset(x2, y2)
                    )
                    drawCircle(
                        color = Verde,
                        radius = 9f,
                        center = Offset(x2, y2)
                    )

                    // Fondo para la etiqueta de predicción
                    val labelText = String.format(Locale.US, "%.1f%%", prediccion)
                    val labelY = y2 - 30f  // ← Aumentado de -25f a -30f

                    // Fondo blanco con borde verde (más grande)
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(x2 - 45f, labelY - 30f),  // ← Aumentado de -35f/-25f a -45f/-30f
                        size = androidx.compose.ui.geometry.Size(90f, 38f),  // ← Aumentado de 70f/30f a 90f/38f
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)  // ← Aumentado de 6f a 8f
                    )

                    drawRoundRect(
                        color = Verde,
                        topLeft = Offset(x2 - 45f, labelY - 30f),  // ← Aumentado
                        size = androidx.compose.ui.geometry.Size(90f, 38f),  // ← Aumentado
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),  // ← Aumentado
                        style = Stroke(width = 3f)  // ← Aumentado de 2f a 3f
                    )

                    // Etiqueta de predicción
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            labelText,
                            x2,
                            labelY,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.rgb(56, 142, 60)
                                textSize = 32f  // ← Aumentado de 28f a 32f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                            }
                        )
                    }
                }

                // Etiquetas del eje X - Mostrar etiquetas optimizadas
                datosHistoricos.forEachIndexed { index, punto ->
                    val x = indexToX(index, datosHistoricos.size + 1)

                    // Si hay más de 6 meses, mostrar etiquetas alternadas
                    val mostrarEtiqueta = if (datosHistoricos.size > 6) {
                        index % 2 == 0 // Mostrar solo índices pares
                    } else {
                        true // Mostrar todos
                    }

                    if (mostrarEtiqueta) {
                        val y = paddingTop + graphHeight + 30f

                        // Nombre del mes (primera línea) - más pequeño
                        drawContext.canvas.nativeCanvas.apply {
                            drawText(
                                punto.mes,
                                x,
                                y,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.GRAY
                                    textSize = 20f  // Reducido de 26f a 20f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isFakeBoldText = true
                                }
                            )
                        }

                        // Valor del mes (segunda línea)
                        drawContext.canvas.nativeCanvas.apply {
                            drawText(
                                String.format(Locale.US, "%.0f%%", punto.valor),
                                x,
                                y + 22f,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.rgb(33, 150, 243)
                                    textSize = 19f  // Reducido de 24f a 19f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isFakeBoldText = true
                                }
                            )
                        }
                    } else {
                        // Para meses no etiquetados, solo mostrar el valor como punto pequeño
                        val y = paddingTop + graphHeight + 35f
                        drawContext.canvas.nativeCanvas.apply {
                            drawText(
                                "•",
                                x,
                                y,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.GRAY
                                    textSize = 16f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                            )
                        }
                    }
                }

                // Etiqueta "PRÓXIMO MES" en el eje X para predicción
                if (prediccion != null) {
                    val x = indexToX(datosHistoricos.size, datosHistoricos.size + 1)
                    val y = paddingTop + graphHeight + 30f  // ← Aumentado de 25f a 30f

                    // Texto "PRÓXIMO" (primera línea)
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            "PRÓXIMO",
                            x,
                            y,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.rgb(76, 175, 80)
                                textSize = 24f  // ← Aumentado de 20f a 24f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                            }
                        )
                    }

                    // Texto "MES" (segunda línea)
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            "MES",
                            x,
                            y + 22f,  // ← Aumentado de 18f a 22f
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.rgb(76, 175, 80)
                                textSize = 24f  // ← Aumentado de 20f a 24f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                            }
                        )
                    }

                    // Valor predicho (tercera línea - destacado)
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            String.format(Locale.US, "%.1f%%", prediccion),
                            x,
                            y + 47f,  // ← Aumentado de 38f a 47f
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.rgb(56, 142, 60)
                                textSize = 26f  // ← Aumentado de 22f a 26f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LeyendaItem(color: Color, texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            modifier = Modifier.size(12.dp),
            color = color,
            shape = RoundedCornerShape(2.dp)
        ) {}
        Text(
            text = texto,
            fontSize = 12.sp,
            color = GrisTexto
        )
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

            HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)

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
                        fontSize = 12.sp,
                        color = GrisTexto,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "%.1f%%".format(prediccion),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulCorporativo
                    )
                }

                // Real
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Real",
                        fontSize = 12.sp,
                        color = GrisTexto,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "%.1f%%".format(valorReal),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF202124)
                    )
                }

                // Diferencia
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Diferencia",
                        fontSize = 12.sp,
                        color = GrisTexto,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${if (esPositivo) "+" else ""}%.1f%%".format(diferencia),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorDiferencia
                    )
                }
            }
        }
    }
}
