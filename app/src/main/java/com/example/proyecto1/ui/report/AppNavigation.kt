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
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyecto1.ui.theme.Black
import com.example.proyecto1.ui.theme.Proyecto1Theme
import com.example.proyecto1.ui.theme.White
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val openDrawer: () -> Unit = { scope.launch { drawerState.open() } }
    val closeDrawer: () -> Unit = { scope.launch { drawerState.close() } }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(navController = navController, closeDrawer = closeDrawer)
        }
    ) {
        NavHost(navController = navController, startDestination = "dashboard") {
            composable("dashboard") {
                DashboardScreen(navController, openDrawer)
            }
            composable("report_preview") {
                ReportPreviewScreen(navController)
            }
            composable("configuration") {
                ConfigurationScreen(openDrawer)
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
                    selected = item == "Reportes",
                    onClick = {
                        closeDrawer()
                        if (item == "Configuración") {
                            navController.navigate("configuration")
                        } else {
                            //TODO: navController.navigate(item.lowercase())
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Black.copy(alpha = 0.1f),
                        unselectedContainerColor = White,
                        selectedTextColor = Black,
                        unselectedTextColor = Black
                    )
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