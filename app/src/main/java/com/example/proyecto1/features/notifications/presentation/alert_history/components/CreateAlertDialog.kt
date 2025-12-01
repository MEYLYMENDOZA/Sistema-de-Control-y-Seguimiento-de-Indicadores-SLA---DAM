package com.example.proyecto1.features.notifications.presentation.alert_history.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proyecto1.features.notifications.data.model.PersonalDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlertDialog(
    personalList: List<PersonalDto>, // <--- AHORA RECIBIMOS LA LISTA
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit // <--- AHORA DEVUELVE TRES PARÁMETROS
) {
    var mensaje by remember { mutableStateOf("") }

    // Variables para el Dropdown de Personas
    var expandedPersonal by remember { mutableStateOf(false) }
    var selectedPerson by remember { mutableStateOf<PersonalDto?>(null) }

    // Variables para el Dropdown de Nivel
    var expandedNivel by remember { mutableStateOf(false) }
    var selectedNivel by remember { mutableStateOf<String?>(null) }
    val nivelOptions = listOf("Alto", "Medio", "Bajo")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Reportar Incumplimiento") },
        text = {
            Column {
                Text("Selecciona al responsable, nivel y detalla el problema:")
                Spacer(modifier = Modifier.height(10.dp))

                // --- DROPDOWN 1: PERSONAS ---
                ExposedDropdownMenuBox(
                    expanded = expandedPersonal,
                    onExpandedChange = { expandedPersonal = !expandedPersonal }
                ) {
                    OutlinedTextField(
                        value = selectedPerson?.nombreCompleto ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Responsable") },
                        placeholder = { Text("Seleccione...") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPersonal) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedPersonal,
                        onDismissRequest = { expandedPersonal = false }
                    ) {
                        if (personalList.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Cargando o sin datos...") },
                                onClick = { expandedPersonal = false }
                            )
                        } else {
                            personalList.forEach { persona ->
                                DropdownMenuItem(
                                    text = { Text(persona.nombreCompleto) },
                                    onClick = {
                                        selectedPerson = persona
                                        expandedPersonal = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- DROPDOWN 2: NIVEL ---
                ExposedDropdownMenuBox(
                    expanded = expandedNivel,
                    onExpandedChange = { expandedNivel = !expandedNivel }
                ) {
                    OutlinedTextField(
                        value = selectedNivel ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Nivel de Alerta") },
                        placeholder = { Text("Seleccione...") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedNivel) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedNivel,
                        onDismissRequest = { expandedNivel = false }
                    ) {
                        nivelOptions.forEach { nivel ->
                            DropdownMenuItem(
                                text = { Text(nivel) },
                                onClick = {
                                    selectedNivel = nivel
                                    expandedNivel = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- CAMPO DETALLE ---
                OutlinedTextField(
                    value = mensaje,
                    onValueChange = { mensaje = it },
                    label = { Text("Detalle del problema") },
                    placeholder = { Text("Ej: No entregó a tiempo") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                // Solo activamos si seleccionó persona, nivel y escribió mensaje
                enabled = mensaje.isNotEmpty() && selectedPerson != null && selectedNivel != null,
                onClick = {
                    // Enviamos los tres datos
                    onConfirm(
                        mensaje,
                        selectedPerson?.nombreCompleto ?: "Desconocido",
                        selectedNivel ?: "Medio"
                    )
                }
            ) {
                Text("Registrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}