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
import com.example.proyecto1.ui.report.DashboardScreen

@Composable
fun AppNavigationGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    val gestionDatosViewModel: GestionDatosViewModel = viewModel()

    // Factory para crear HomeViewModel, ya que depende de otro ViewModel.
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

    NavHost(
        navController = navController,
        startDestination = AppScreens.Inicio.route,
        modifier = modifier
    ) {
        composable(AppScreens.Inicio.route) {
            // CORRECCIÓN: Se pasa la instancia correcta de HomeViewModel.
            DashboardScreen(homeViewModel)
        }

        composable(AppScreens.CargaDatos.route) {
            CargaDatosScreen(gestionDatosViewModel)
        }

        composable(AppScreens.GestionDatos.route) {
            GestionDatosScreen(gestionDatosViewModel)
        }
    }
}
