package com.example.proyecto1.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proyecto1.data.model.Role
import com.example.proyecto1.data.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(onDismiss: () -> Unit, onUserAdded: (User) -> Unit) {
    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("changeme123") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val roles = Role.values()
    var selectedRole by remember { mutableStateOf(roles[0]) }
    var roleExpanded by remember { mutableStateOf(false) }

    val statuses = mapOf("Pending" to "Pendiente", "Verified" to "Verificado")
    var selectedStatus by remember { mutableStateOf("Pending") }
    var statusExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Usuario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Usuario *") })
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Nombre Completo *") })
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña *") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email (opcional)") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono (opcional)") })

                ExposedDropdownMenuBox(expanded = roleExpanded, onExpandedChange = { roleExpanded = !roleExpanded }) {
                    OutlinedTextField(
                        value = selectedRole.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                        roles.forEach { role ->
                            DropdownMenuItem(text = { Text(role.name) }, onClick = { 
                                selectedRole = role 
                                roleExpanded = false
                            })
                        }
                    }
                }

                ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = !statusExpanded }) {
                    OutlinedTextField(
                        value = statuses[selectedStatus] ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                        statuses.forEach { (value, displayText) ->
                            DropdownMenuItem(text = { Text(displayText) }, onClick = { 
                                selectedStatus = value
                                statusExpanded = false 
                            })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val newUser = User(
                    id = 0, // Will be replaced by the repository
                    username = username,
                    fullName = fullName,
                    email = email,
                    role = selectedRole,
                    status = selectedStatus
                )
                onUserAdded(newUser)
            }) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
