package com.example.proyecto1.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyecto1.ui.gestion.CargaDatosScreen
import com.example.proyecto1.ui.gestion.GestionDatosScreen
import com.example.proyecto1.ui.gestion.GestionDatosViewModel
import com.example.proyecto1.ui.theme.Black
import com.example.proyecto1.ui.theme.Proyecto1Theme
import com.example.proyecto1.ui.theme.White
import com.example.proyecto1.ui.viewmodel.SlaDashboardViewModel
import com.example.proyecto1.ui.viewmodel.SlaLimitsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val closeDrawer: () -> Unit = { scope.launch { drawerState.close() } }

    // Instancias de ViewModel compartidas
    val slaDashboardViewModel: SlaDashboardViewModel = viewModel()
    val slaLimitsViewModel: SlaLimitsViewModel = viewModel()
    // CORRECCIÓN: Se crea una instancia compartida de GestionDatosViewModel para todas las pantallas relacionadas.
    val gestionDatosViewModel: GestionDatosViewModel = viewModel()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(navController = navController, closeDrawer = closeDrawer)
        }
    ) {
        NavHost(navController = navController, startDestination = "dashboard") {
            composable("dashboard") {
                // CORRECCIÓN: La llamada a DashboardScreen ahora solo necesita el ViewModel correcto.
                DashboardScreen(viewModel = gestionDatosViewModel)
            }
            composable("report_preview") {
                ReportScreen(navController = navController, viewModel = slaDashboardViewModel)
            }
            composable("configuration") {
                ConfigurationScreen(viewModel = slaLimitsViewModel)
            }
            // CORRECCIÓN: Se añaden las nuevas pantallas al NavHost, usando el ViewModel compartido.
            composable("carga_datos") {
                CargaDatosScreen(viewModel = gestionDatosViewModel)
            }
            composable("gestion_datos") {
                GestionDatosScreen(viewModel = gestionDatosViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerContent(navController: NavController, closeDrawer: () -> Unit) {
    ModalDrawerSheet {
        Column {
            Column(
                modifier = Modifier
                    .background(Black)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text("SLA Tracker", style = MaterialTheme.typography.titleLarge, color = White)
                Text("Control y Seguimiento", style = MaterialTheme.typography.bodySmall, color = White)
                Spacer(modifier = Modifier.height(16.dp))
                Text("admin", style = MaterialTheme.typography.titleMedium, color = White)
                Text("Administrador", style = MaterialTheme.typography.bodyMedium, color = White)
            }
            HorizontalDivider(thickness = 0.5.dp)
            val menuItems = listOf(
                "Inicio", "Carga de Datos", "Gestión de Datos", "Métricas SLA", "Reportes", "Notificaciones", "Usuarios", "Configuración"
            )
            menuItems.forEach { item ->
                NavigationDrawerItem(
                    label = { Text(item) },
                    selected = false, // Debería ser dinámico según la ruta actual
                    onClick = {
                        closeDrawer()
                        // CORRECCIÓN: Se añaden las nuevas rutas de navegación.
                        when (item) {
                            "Inicio" -> navController.navigate("dashboard")
                            "Configuración" -> navController.navigate("configuration")
                            "Reportes" -> navController.navigate("report_preview")
                            "Carga de Datos" -> navController.navigate("carga_datos")
                            "Gestión de Datos" -> navController.navigate("gestion_datos")
                            else -> {
                                // TODO: Implementar otras rutas
                            }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavigationPreview() {
    Proyecto1Theme {
        AppNavigation()
    }
}
