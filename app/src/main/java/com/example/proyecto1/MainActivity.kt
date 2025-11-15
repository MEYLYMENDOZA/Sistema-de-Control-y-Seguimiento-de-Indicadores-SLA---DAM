package com.example.proyecto1

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto1.presentation.prediccion.PrediccionScreen
import com.example.proyecto1.presentation.prediccion.PrediccionViewModel
import com.example.proyecto1.presentation.prediccion.TendenciaScreen
import com.example.proyecto1.ui.theme.Proyecto1Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            enableEdgeToEdge()
            setContent {
                Proyecto1Theme {
                    SistemaControlApp()
                }
            }
            Log.d("MainActivity", "Activity creada exitosamente")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error en onCreate", e)
            throw e
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SistemaControlApp() {
    val vm: PrediccionViewModel = viewModel()
    var pantallaActual by remember { mutableStateOf<Pantalla>(Pantalla.Prediccion) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerMenu(
                pantallaActual = pantallaActual,
                onPantallaClick = { pantalla ->
                    pantallaActual = pantalla
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            when (pantallaActual) {
                                Pantalla.Dashboard -> "Dashboard y KPIs"
                                Pantalla.CargaDatos -> "Carga de Datos"
                                Pantalla.Prediccion -> "Predicción de Cumplimiento SLA"
                                Pantalla.Alertas -> "Alertas"
                                Pantalla.Reportes -> "Reportes"
                                Pantalla.Usuarios -> "Usuarios"
                                Pantalla.Configuracion -> "Configuración de SLA"
                                Pantalla.Tendencia -> "Tendencia y Proyección"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.White
                    )
                )
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (pantallaActual) {
                    Pantalla.Dashboard -> DashboardPlaceholder()
                    Pantalla.CargaDatos -> CargaDatosPlaceholder()
                    Pantalla.Prediccion -> PrediccionScreen(vm)
                    Pantalla.Alertas -> AlertasPlaceholder()
                    Pantalla.Reportes -> ReportesPlaceholder()
                    Pantalla.Usuarios -> UsuariosPlaceholder()
                    Pantalla.Configuracion -> ConfiguracionPlaceholder()
                    Pantalla.Tendencia -> TendenciaScreen(vm)
                }
            }
        }
    }
}

@Composable
fun DrawerMenu(
    pantallaActual: Pantalla,
    onPantallaClick: (Pantalla) -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(280.dp),
        drawerContainerColor = androidx.compose.ui.graphics.Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
        ) {
            // Header del drawer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Usuario",
                    modifier = Modifier.size(48.dp),
                    tint = androidx.compose.ui.graphics.Color(0xFF424242)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "admin",
                    style = MaterialTheme.typography.titleMedium,
                    color = androidx.compose.ui.graphics.Color(0xFF212121)
                )
                Text(
                    text = "Administrador",
                    style = MaterialTheme.typography.bodySmall,
                    color = androidx.compose.ui.graphics.Color(0xFF757575)
                )
            }

            HorizontalDivider()

            // Opciones del menú según imagen (iconos básicos garantizados)
            DrawerMenuItem(
                icon = Icons.Default.Home,
                title = "Inicio",
                subtitle = "Dashboard y KPIs",
                selected = pantallaActual == Pantalla.Dashboard,
                onClick = { onPantallaClick(Pantalla.Dashboard) }
            )

            DrawerMenuItem(
                icon = Icons.Default.Save,
                title = "Carga de Datos",
                subtitle = "Subir archivo Excel",
                selected = pantallaActual == Pantalla.CargaDatos,
                onClick = { onPantallaClick(Pantalla.CargaDatos) }
            )

            DrawerMenuItem(
                icon = Icons.Default.Star,
                title = "Predicción",
                subtitle = "Análisis predictivo",
                selected = pantallaActual == Pantalla.Prediccion,
                onClick = { onPantallaClick(Pantalla.Prediccion) }
            )

            DrawerMenuItem(
                icon = Icons.Default.Notifications,
                title = "Alertas",
                subtitle = "Notificaciones y alertas",
                selected = pantallaActual == Pantalla.Alertas,
                onClick = { onPantallaClick(Pantalla.Alertas) }
            )

            DrawerMenuItem(
                icon = Icons.Default.List,
                title = "Reportes",
                subtitle = "Generar reportes PDF",
                selected = pantallaActual == Pantalla.Reportes,
                onClick = { onPantallaClick(Pantalla.Reportes) }
            )

            DrawerMenuItem(
                icon = Icons.Default.Person,
                title = "Usuarios",
                subtitle = "Gestión de usuarios",
                selected = pantallaActual == Pantalla.Usuarios,
                onClick = { onPantallaClick(Pantalla.Usuarios) }
            )

            DrawerMenuItem(
                icon = Icons.Default.Settings,
                title = "Configuración",
                subtitle = "Ajustes de la SLA",
                selected = pantallaActual == Pantalla.Configuracion,
                onClick = { onPantallaClick(Pantalla.Configuracion) }
            )

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider()

            // Cerrar sesión
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                label = {
                    Column {
                        Text("Cerrar Sesión", style = MaterialTheme.typography.bodyMedium)
                        Text("Salir del sistema", style = MaterialTheme.typography.bodySmall)
                    }
                },
                selected = false,
                onClick = { /* TODO: Implementar logout */ },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null) },
        label = {
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = androidx.compose.ui.graphics.Color.Gray
                )
            }
        },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
    )
}

// Pantallas placeholder
@Composable
fun DashboardPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = androidx.compose.ui.graphics.Color.Gray
            )
            Spacer(Modifier.height(16.dp))
            Text("Dashboard", style = MaterialTheme.typography.headlineSmall)
            Text("Por implementar", color = androidx.compose.ui.graphics.Color.Gray)
        }
    }
}

@Composable
fun CargaDatosPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Save,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = androidx.compose.ui.graphics.Color.Gray
            )
            Spacer(Modifier.height(16.dp))
            Text("Carga de Datos", style = MaterialTheme.typography.headlineSmall)
            Text("Por implementar", color = androidx.compose.ui.graphics.Color.Gray)
        }
    }
}

@Composable
fun AlertasPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = androidx.compose.ui.graphics.Color.Gray
            )
            Spacer(Modifier.height(16.dp))
            Text("Alertas", style = MaterialTheme.typography.headlineSmall)
            Text("Por implementar", color = androidx.compose.ui.graphics.Color.Gray)
        }
    }
}

@Composable
fun ReportesPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.List,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = androidx.compose.ui.graphics.Color.Gray
            )
            Spacer(Modifier.height(16.dp))
            Text("Reportes", style = MaterialTheme.typography.headlineSmall)
            Text("Por implementar", color = androidx.compose.ui.graphics.Color.Gray)
        }
    }
}

@Composable
fun UsuariosPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = androidx.compose.ui.graphics.Color.Gray
            )
            Spacer(Modifier.height(16.dp))
            Text("Usuarios", style = MaterialTheme.typography.headlineSmall)
            Text("Por implementar", color = androidx.compose.ui.graphics.Color.Gray)
        }
    }
}

@Composable
fun ConfiguracionPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = androidx.compose.ui.graphics.Color.Gray
            )
            Spacer(Modifier.height(16.dp))
            Text("Configuración de SLA", style = MaterialTheme.typography.headlineSmall)
            Text("Por implementar", color = androidx.compose.ui.graphics.Color.Gray)
        }
    }
}

sealed class Pantalla {
    object Dashboard : Pantalla()
    object CargaDatos : Pantalla()
    object Prediccion : Pantalla()
    object Alertas : Pantalla()
    object Reportes : Pantalla()
    object Usuarios : Pantalla()
    object Configuracion : Pantalla()
    object Tendencia : Pantalla()
}
