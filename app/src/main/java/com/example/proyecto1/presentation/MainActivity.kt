package com.example.proyecto1.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proyecto1.presentation.navigation.AppDrawer
import com.example.proyecto1.presentation.navigation.AppNavigation
import com.example.proyecto1.ui.theme.Proyecto1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Proyecto1Theme {

                // 1. Prepara las variables para la navegación
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                // 2. Obtiene la ruta actual para saber qué item resaltar en el menú
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: ""

                // 3. Define la estructura de la app (Menú + Contenido)
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        // Este es el menú que creaste
                        AppDrawer(
                            navController = navController,
                            drawerState = drawerState,
                            scope = scope,
                            currentRoute = currentRoute
                        )
                    }
                ) {
                    // Este es el "mapa" de navegación que creaste
                    AppNavigation(
                        navController = navController,
                        drawerState = drawerState,
                        scope = scope
                    )
                }
            }
        }
    }
}