package com.example.proyecto1.ui.gestion

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Componente contenedor para el módulo de Gestión de Datos.
 * Crea el ViewModel y organiza las dos pantallas (Carga y Gestión) en pestañas.
 */
@Composable
fun GestionModuleScreen() {
    // Se crea una única instancia del ViewModel que será compartida por ambas pantallas.
    val viewModel: GestionDatosViewModel = viewModel()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Cargar Archivo", "Ver Datos")

    Column {
        // Pestañas para navegar entre las dos vistas del módulo
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        // Muestra la pantalla correspondiente a la pestaña seleccionada
        when (selectedTab) {
            0 -> CargaDatosScreen(viewModel = viewModel)
            1 -> GestionDatosScreen(viewModel = viewModel)
        }
    }
}
