package com.example.proyecto1.ui.report

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proyecto1.AppDrawer
import com.example.proyecto1.ui.configuracion.ConfigurationScreen
import com.example.proyecto1.ui.theme.Proyecto1Theme
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                    scope.launch { drawerState.close() }
                },
                onLogout = { /* TODO: Implementar cierre de sesión */ }
            )
        }
    ) {
        NavHost(navController = navController, startDestination = "dashboard") {
            composable("dashboard") {
                DashboardScreen(navController) { scope.launch { drawerState.open() } }
            }
            composable("report_preview") {
                ReportPreviewScreen(navController)
            }
            composable("configuracion") {
                ConfigurationScreen { scope.launch { drawerState.open() } }
            }
        }
    }
}
