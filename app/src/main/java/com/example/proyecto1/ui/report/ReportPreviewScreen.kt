package com.example.proyecto1.ui.report

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun ReportPreviewScreen(navController: NavController) {
    var showEmailDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(modifier = Modifier.weight(1f)) {
                // TODO: Display PDF preview here
                Text(
                    text = "PDF Preview",
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { /* TODO: Download PDF */ }) {
                    Text("Descargar PDF")
                }
                Button(onClick = { showEmailDialog = true }) {
                    Text("Compartir PDF")
                }
            }
        }
    }

    if (showEmailDialog) {
        AlertDialog(
            onDismissRequest = { showEmailDialog = false },
            title = { Text("Enviar por Correo") },
            text = { Text("¿Deseas recibir este reporte automáticamente en tu correo electrónico registrado?") },
            confirmButton = {
                Button(
                    onClick = {
                        showEmailDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Reporte enviado con éxito a: usuario@empresa.com")
                        }
                    }
                ) {
                    Text("Sí, enviar")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showEmailDialog = false }
                ) {
                    Text("No, gracias")
                }
            }
        )
    }
}