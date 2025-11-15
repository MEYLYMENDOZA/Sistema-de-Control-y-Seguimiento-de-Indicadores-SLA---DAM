package com.example.proyecto1.presentation.navigation

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.proyecto1.features.notifications.presentation.alert_history.AlertsHistoryScreen
import com.example.proyecto1.features.notifications.presentation.dashboard.AlertsDashboardScreen
import com.example.proyecto1.features.notifications.presentation.email_notifications.EmailNotificationsScreen
// ¡Importa tus otras pantallas aquí!
// import com.example.proyecto1.features.notifications.presentation.notification_settings.AlertSettingsScreen
// import com.example.proyecto1.features.notifications.presentation.critical_cases.CriticalCasesScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    navController: NavHostController,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    // NavHost es el contenedor que intercambia las pantallas
    NavHost(
        navController = navController,
        startDestination = AppScreens.AlertsDashboard.route // Inicia en el Dashboard
    ) {

        // Ruta para el Dashboard Principal
        composable(AppScreens.AlertsDashboard.route) {
            AlertsDashboardScreen(
                onMenuClick = { scope.launch { drawerState.open() } },
                onNavigateToAlertsHistory = {
                    navController.navigate(AppScreens.AlertsHistory.route)
                },
                onNavigateToCriticalCases = {
                    // navController.navigate(AppScreens.CriticalCasesDetail.route)
                    println("Navegando a Casos Críticos...")
                },
                onNavigateToSettings = {
                    // navController.navigate(AppScreens.AlertSettings.route)
                    println("Navegando a Configuración...")
                }
            )
        }

        // Ruta para el Historial de Alertas (US-13)
        composable(AppScreens.AlertsHistory.route) {
            AlertsHistoryScreen(
                onMenuClick = { scope.launch { drawerState.open() } }
            )
        }

        // Ruta para el Historial de Email (US-14)
        composable(AppScreens.EmailHistory.route) {
            EmailNotificationsScreen(
                onMenuClick = { scope.launch { drawerState.open() } }
            )
        }

        // ... (Aquí puedes añadir las otras rutas)
        // composable(AppScreens.AlertSettings.route) { ... }
        // composable(AppScreens.CriticalCasesDetail.route) { ... }
    }
}