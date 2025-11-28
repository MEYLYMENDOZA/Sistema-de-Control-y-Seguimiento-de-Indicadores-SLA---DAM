package com.example.proyecto1.presentation.gestion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto1.presentation.carga.CargaItemData

@Composable
fun GestionScreen(gestionViewModel: GestionViewModel = viewModel()) {
    val uiState by gestionViewModel.uiState.collectAsState()

    if (uiState.itemToEdit != null) {
        EditRecordDialog(
            item = uiState.itemToEdit!!,
            onDismiss = { gestionViewModel.onDismissEditDialog() },
            onSave = { updatedItem -> gestionViewModel.onSaveChanges(updatedItem) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F2F5))
            .padding(16.dp)
    ) {
        Text("Gestión de Datos", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Visualiza, edita y elimina registros SLA. Total: ${uiState.allItems.size} registros",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(Modifier.height(16.dp))

        SearchBarSection(gestionViewModel)
        
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Registros SLA", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            Checkbox(checked = uiState.selectedItemCodes.size == uiState.displayedItems.size && uiState.displayedItems.isNotEmpty(), onCheckedChange = { gestionViewModel.onToggleAllSelected(it) })
            Text("Seleccionar todos")
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(uiState.displayedItems, key = { it.codigo }) {
                SlaRecordItem(
                    item = it,
                    isSelected = it.codigo in uiState.selectedItemCodes,
                    onToggleSelection = { isSelected -> gestionViewModel.onToggleItemSelected(it.codigo, isSelected) },
                    onEdit = { gestionViewModel.onEditItem(it) }
                )
            }
        }
    }
}

@Composable
fun SearchBarSection(viewModel: GestionViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    OutlinedTextField(
        value = uiState.searchQuery,
        onValueChange = { viewModel.onSearchQueryChanged(it) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Buscar por código, rol o tipo SLA...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            Button(
                onClick = { viewModel.deleteSelectedItems() },
                enabled = uiState.selectedItemCodes.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                Spacer(Modifier.width(4.dp))
                Text("Eliminar (${uiState.selectedItemCodes.size})")
            }
        }
    )
}

@Composable
fun SlaRecordItem(item: CargaItemData, isSelected: Boolean, onToggleSelection: (Boolean) -> Unit, onEdit: () -> Unit) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isSelected, onCheckedChange = onToggleSelection)
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.codigo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                InfoRow("Rol", item.rol)
                InfoRow("F. Solicitud", "N/A") // Assuming date is not in CargaItemData
                InfoRow("Tipo SLA", item.tipoSla)
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.clickable(onClick = onEdit).padding(4.dp))
                Spacer(Modifier.height(8.dp))
                 InfoRow("F. Ingreso", "N/A")
                 Pill(text = "${item.diasTranscurridos} días", isSuccess = item.estado == "Cumple")
            }
        }
         Row(Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp)){
             Pill(text = item.estado, isSuccess = item.estado == "Cumple")
         }
    }
}

@Composable
fun EditRecordDialog(item: CargaItemData, onDismiss: () -> Unit, onSave: (CargaItemData) -> Unit) {
    var rol by remember { mutableStateOf(item.rol) }
    var tipoSla by remember { mutableStateOf(item.tipoSla) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Editar Registro", style = MaterialTheme.typography.headlineSmall)
                Text("Los campos NUM DIAS SLA y CUMPLE se recalcularán automáticamente", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(value = item.codigo, onValueChange = {}, readOnly = true, label = { Text("Código (No editable)") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = rol, onValueChange = { rol = it }, label = { Text("Rol *") })
                Spacer(Modifier.height(8.dp))
                // Date pickers would go here
                OutlinedTextField(value = "25/11/2025", onValueChange = {}, label = { Text("Fecha Solicitud *") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = "11/01/2026", onValueChange = {}, label = { Text("Fecha Ingreso *") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = tipoSla, onValueChange = { tipoSla = it }, label = { Text("Tipo SLA *") })
                
                Spacer(Modifier.height(16.dp))
                
                // Vista Previa Recálculo Placeholder
                Column(modifier = Modifier.background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp)).padding(12.dp)){
                     Text("Vista Previa del Recálculo", fontWeight = FontWeight.Bold)
                     Text("• NUM DIAS SLA: 47 días")
                     Text("• CUMPLE: No")
                }

                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { 
                        val updatedItem = item.copy(rol = rol, tipoSla = tipoSla)
                        onSave(updatedItem)
                     }) { Text("Guardar Cambios") }
                }
            }
        }
    }
}


@Composable
fun InfoRow(label: String, value: String) {
    Row {
        Text("$label: ", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp)
    }
}

@Composable
fun Pill(text: String, isSuccess: Boolean) {
    val backgroundColor = if (isSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val textColor = if (isSuccess) Color(0xFF2E7D32) else Color(0xFFC62828)
    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

