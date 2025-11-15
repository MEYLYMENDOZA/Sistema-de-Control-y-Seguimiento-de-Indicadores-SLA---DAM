package com.example.proyecto1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.proyecto1.ui.gestion.CargaDatosScreen
import com.example.proyecto1.ui.gestion.GestionDatosScreen
import com.example.proyecto1.ui.gestion.GestionDatosViewModel
import com.example.proyecto1.ui.login.LoginScreen
import com.example.proyecto1.ui.theme.Proyecto1Theme
import com.example.proyecto1.ui.user.UserListScreen

// --- Definición de pantallas ---
sealed class Screen(val route: String, val label: String? = null, val icon: ImageVector? = null) {
    object Login : Screen("login")
    object Carga : Screen("carga", "Carga de Datos", Icons.Default.Upload)
    object Gestion : Screen("gestion", "Gestión de Datos", Icons.Default.Edit)
    object UserAdmin : Screen("user_admin", "Usuarios", Icons.Default.Person)
}

val bottomNavItems = listOf(
    Screen.Carga,
    Screen.Gestion,
    Screen.UserAdmin
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Proyecto1Theme {
                AppRoot()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot() {

    val navController = rememberNavController()
    val isLoggedIn = remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (isLoggedIn.value) {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = null) },
                            label = { Text(screen.label!!) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        val gestionViewModel: GestionDatosViewModel = viewModel()

        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        isLoggedIn.value = true
                        navController.navigate(Screen.Carga.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Carga.route) {
                CargaDatosScreen(gestionViewModel)
            }

            composable(Screen.Gestion.route) {
                GestionDatosScreen(gestionViewModel)
            }

            composable(Screen.UserAdmin.route) {
                UserListScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMain() {
    Proyecto1Theme {
        AppRoot()
    }
}
