package com.example.proyecto1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.proyecto1.presentation.prediccion.PrediccionScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto1.presentation.prediccion.PrediccionViewModel
import com.example.proyecto1.ui.theme.Proyecto1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Proyecto1Theme {
                val vm: PrediccionViewModel = viewModel()
                PrediccionScreen(vm)   // Pantalla principal
            }
        }
    }
}
