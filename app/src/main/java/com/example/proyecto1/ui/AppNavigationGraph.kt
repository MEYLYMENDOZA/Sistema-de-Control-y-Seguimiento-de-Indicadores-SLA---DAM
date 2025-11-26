package com.example.proyecto1.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.proyecto1.ui.gestion.CargaDatosScreen
import com.example.proyecto1.ui.gestion.GestionDatosScreen
import com.example.proyecto1.ui.gestion.GestionDatosViewModel
import com.example.proyecto1.ui.home.HomeViewModel
import com.example.proyecto1.ui.prediction.PredictionScreen
import com.example.proyecto1.ui.prediction.PredictionViewModel
import com.example.proyecto1.ui.report.DashboardScreen

/**
 * Defines the navigation graph for the application.
 * This composable contains the NavHost and all screen destinations.
 * It receives a [NavHostController] from the parent (MainScreen) to integrate with the navigation drawer.
 */
@Composable
fun AppNavigationGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    val gestionDatosViewModel: GestionDatosViewModel = viewModel()

    // Factory for HomeViewModel
    val homeViewModelFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(gestionDatosViewModel) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
    val homeViewModel: HomeViewModel = viewModel(factory = homeViewModelFactory)

    // CORRECCIÓN: Factory para PredictionViewModel para que pueda acceder a los datos
    val predictionViewModelFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PredictionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PredictionViewModel(gestionDatosViewModel) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
    val predictionViewModel: PredictionViewModel = viewModel(factory = predictionViewModelFactory)

    NavHost(
        navController = navController,
        startDestination = AppScreens.Inicio.route,
        modifier = modifier
    ) {
        composable(AppScreens.Inicio.route) {
            DashboardScreen(homeViewModel = homeViewModel)
        }

        composable(AppScreens.CargaDatos.route) {
            CargaDatosScreen(viewModel = gestionDatosViewModel)
        }

        composable(AppScreens.GestionDatos.route) {
            GestionDatosScreen(viewModel = gestionDatosViewModel)
        }

        composable(AppScreens.Prediccion.route) {
            // Se pasa solo el ViewModel de predicción, ya que ahora contiene la lógica necesaria
            PredictionScreen(predictionViewModel = predictionViewModel)
        }
    }
}
