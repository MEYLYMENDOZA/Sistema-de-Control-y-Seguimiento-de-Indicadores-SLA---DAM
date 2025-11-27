package com.example.proyecto1.ui.gestion

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*

@Composable
fun GestionMainScreen(viewModel: GestionDatosViewModel) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Cargar Archivo", "Ver Datos")

    Column {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> CargaDatosScreen(viewModel = viewModel)
            1 -> GestionDatosScreen(viewModel = viewModel)
        }
    }
}
