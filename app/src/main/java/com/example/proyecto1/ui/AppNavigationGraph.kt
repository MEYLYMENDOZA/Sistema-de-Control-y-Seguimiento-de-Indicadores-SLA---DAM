package com.example.proyecto1.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.proyecto1.ui.gestion.CargaDatosScreen
import com.example.proyecto1.ui.gestion.GestionDatosScreen
import com.example.proyecto1.ui.gestion.GestionDatosViewModel
import com.example.proyecto1.ui.report.DashboardScreen

@Composable
fun AppNavigationGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    val gestionDatosViewModel: GestionDatosViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = AppScreens.Inicio.route,
        modifier = modifier
    ) {
        composable(AppScreens.Inicio.route) {
            DashboardScreen(gestionDatosViewModel) // Corregido: Eliminado el error de tipeo.
        }

        composable(AppScreens.CargaDatos.route) {
            CargaDatosScreen(gestionDatosViewModel)
        }

        composable(AppScreens.GestionDatos.route) {
            GestionDatosScreen(gestionDatosViewModel)
        }
    }
}
