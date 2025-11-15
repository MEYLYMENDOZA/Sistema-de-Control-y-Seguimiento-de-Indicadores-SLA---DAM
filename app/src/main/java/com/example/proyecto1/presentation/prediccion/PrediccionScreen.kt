package com.example.proyecto1.presentation.prediccion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrediccionScreen(
    vm: PrediccionViewModel
) {
    val prediccion by vm.prediccion.collectAsState()
    val slope by vm.slope.collectAsState()
    val intercept by vm.intercept.collectAsState()
    val error by vm.error.collectAsState()

    LaunchedEffect(Unit) {
        vm.cargarYPredecir()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Predicción SLA", fontSize = 26.sp)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Próximo mes:", fontSize = 16.sp)
                Text(
                    text = "%.2f%%".format(prediccion ?: 0.0),
                    fontSize = 40.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Pendiente (m): ${slope ?: 0.0}")
                Text("Intercepto (b): ${intercept ?: 0.0}")
            }
        }

        if (error != null) {
            Text(error!!, color = Color.Red)
        }

        Spacer(Modifier.height(20.dp))

        Button(onClick = { vm.cargarYPredecir() }) {
            Text("Recalcular")
        }
    }
}
