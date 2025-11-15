package com.example.proyecto1.presentation.prediccion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
    val slope by vm.slope.collectAsState()
    val intercept by vm.intercept.collectAsState()
    val error by vm.error.collectAsState()
    val cargando by vm.cargando.collectAsState()
    val mostrarAdvertencia by vm.mostrarAdvertencia.collectAsState()

    LaunchedEffect(Unit) {
        vm.cargarYPredecir()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GrisClaro)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Encabezado principal
            EncabezadoPrincipal()

            // 2. Barra de filtros
            BarraFiltros(
                onActualizar = { vm.cargarYPredecir() },
                habilitado = !cargando
            )

            // 3. Contenido principal
            if (cargando) {
                CargandoIndicador()
            } else if (error != null) {
                ErrorMensaje(error!!)
            } else {
                // Sección de resultado principal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // KPI principal
                    TarjetaKPIPrincipal(
                        prediccion = prediccion,
                        slope = slope,
                        modifier = Modifier.weight(1.5f)
                    )

                    // Coeficientes del modelo
                    TarjetaCoeficientes(
                        slope = slope,
                        intercept = intercept,
                        modifier = Modifier.weight(1f)
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
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF202124)
        )
        Text(
            text = "Estimación basada en datos históricos y regresión lineal simple (y = mx + b)",
            fontSize = 14.sp,
            color = GrisTexto
        )
    }
}

@Composable
private fun BarraFiltros(
    onActualizar: () -> Unit,
    habilitado: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filtros de Análisis",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF202124)
            )

            OutlinedButton(
                onClick = onActualizar,
                enabled = habilitado,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AzulCorporativo
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Actualizar",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Actualizar Datos")
            }
        }
    }
}

@Composable
private fun TarjetaKPIPrincipal(
    prediccion: Double?,
    slope: Double?,
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
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "SLA Proyectado para el próximo mes",
                fontSize = 16.sp,
                color = GrisTexto,
                fontWeight = FontWeight.Medium
            )

            // Valor principal
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "%.1f%%".format(prediccion ?: 0.0),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (tendenciaPositiva) Verde else Rojo
                )

                // Indicador de tendencia
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (tendenciaPositiva)
                            Verde.copy(alpha = 0.1f)
                        else
                            Rojo.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        imageVector = if (tendenciaPositiva)
                            Icons.Default.KeyboardArrowUp
                        else
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = "Tendencia",
                        tint = if (tendenciaPositiva) Verde else Rojo,
                        modifier = Modifier.padding(12.dp).size(32.dp)
                    )
                }
            }

            Divider(color = GrisClaro)

            Text(
                text = if (tendenciaPositiva)
                    "Tendencia positiva detectada"
                else
                    "Tendencia negativa detectada",
                fontSize = 14.sp,
                color = GrisTexto
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Coeficientes del Modelo",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF202124)
            )

            Divider(color = GrisClaro)

            // Pendiente
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pendiente (m):",
                    fontSize = 14.sp,
                    color = GrisTexto
                )
                Text(
                    text = "%.4f".format(slope ?: 0.0),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AzulCorporativo
                )
            }

            // Intercepto
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Intercepto (b):",
                    fontSize = 14.sp,
                    color = GrisTexto
                )
                Text(
                    text = "%.4f".format(intercept ?: 0.0),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AzulCorporativo
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Modelo generado automáticamente",
                fontSize = 12.sp,
                color = GrisTexto.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun TarjetaAdvertencia() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Amarillo.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Advertencia",
                tint = Amarillo,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Advertencia: Predicción inferior al umbral mínimo de cumplimiento.",
                fontSize = 14.sp,
                color = Color(0xFF202124),
                fontWeight = FontWeight.Medium
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onRecalcular,
            enabled = habilitado,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = AzulCorporativo
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Recalcular Predicción",
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        OutlinedButton(
            onClick = onExportar,
            enabled = habilitado,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = GrisTexto
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Exportar Resultado",
                modifier = Modifier.padding(vertical = 8.dp)
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = Rojo,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = mensaje,
                fontSize = 14.sp,
                color = Rojo,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
