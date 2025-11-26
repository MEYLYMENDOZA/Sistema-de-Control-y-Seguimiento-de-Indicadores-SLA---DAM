package com.example.proyecto1.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppScreens(val route: String, val title: String, val icon: ImageVector) {
    object Inicio : AppScreens("inicio", "Inicio", Icons.Default.Dashboard)
    object CargaDatos : AppScreens("carga_datos", "Carga de Datos", Icons.Default.UploadFile)
    object GestionDatos : AppScreens("gestion_datos", "Gestión de Datos", Icons.Default.Analytics)
}

val navigationItems = listOf(
    AppScreens.Inicio,
    AppScreens.CargaDatos,
    AppScreens.GestionDatos
)
