package com.example.proyecto1.ui.gestion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto1.ui.theme.Proyecto1Theme
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// La definición de SlaRecord se queda aquí porque ambas pantallas la usan, 
// aunque lo ideal sería moverla a su propio archivo o a un modelo común.
data class SlaRecord(
    val id: String,
    val codigo: String,
    val rol: String,
    val fechaSolicitud: String,
    val fechaIngreso: String,
    val tipoSla: String,
    val diasSla: Int,
    val cumple: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionDatosScreen(viewModel: GestionDatosViewModel) {
    val uiState = viewModel.uiState
    var editingRecord by remember { mutableStateOf<SlaRecord?>(null) }

    val filteredRecords = remember(uiState.searchQuery, uiState.records) {
        if (uiState.searchQuery.isBlank()) {
            uiState.records
        } else {
            val query = uiState.searchQuery.lowercase(Locale.getDefault())
            uiState.records.filter {
                it.codigo.lowercase(Locale.getDefault()).contains(query) ||
                it.rol.lowercase(Locale.getDefault()).contains(query) ||
                it.tipoSla.lowercase(Locale.getDefault()).contains(query)
            }
        }
    }

    if (!uiState.dataLoaded) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aún no se han cargado datos.", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(0.dp)) } // Padding superior

            item {
                SearchAndSelectAll(
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                    selectedCount = uiState.selectedRecordIds.size,
                    onDeleteClick = { viewModel.onDeleteSelectedClicked() },
                    onSelectAll = { shouldSelect ->
                        val ids = filteredRecords.map { it.id }
                        viewModel.onSelectAllFiltered(ids, shouldSelect)
                    },
                    isAllSelected = filteredRecords.isNotEmpty() && filteredRecords.all { it.id in uiState.selectedRecordIds }
                )
            }

            items(filteredRecords, key = { it.id }) { record ->
                val isSelected = record.id in uiState.selectedRecordIds
                SlaRecordItem(
                    record = record,
                    isSelected = isSelected,
                    onSelectedChange = { isChecked ->
                        viewModel.onRecordSelectionChanged(record.id, isChecked)
                    },
                    onEditClick = { editingRecord = record }
                )
            }

            item { Spacer(Modifier.height(16.dp)) } // Padding inferior
        }
    }

    editingRecord?.let { record ->
        EditRecordDialog(
            record = record,
            onDismiss = { editingRecord = null },
            onSave = { updatedRecord ->
                viewModel.onSaveRecord(updatedRecord)
                editingRecord = null
            }
        )
    }
}

// --- Componentes específicos de la pantalla de Gestión ---

@Composable
fun SearchAndSelectAll(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCount: Int,
    onDeleteClick: () -> Unit,
    onSelectAll: (Boolean) -> Unit,
    isAllSelected: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)){
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por código, rol o tipo SLA...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )
            Button(
                onClick = onDeleteClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedCount > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF5350),
                    disabledContainerColor = Color.LightGray
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Eliminar ($selectedCount)")
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onSelectAll(!isAllSelected) }) {
                Checkbox(checked = isAllSelected, onCheckedChange = onSelectAll)
                Text("Seleccionar todos los visibles")
            }
        }
    }
}

@Composable
fun SlaRecordItem(record: SlaRecord, isSelected: Boolean, onSelectedChange: (Boolean) -> Unit, onEditClick: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.White),
        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isSelected, onCheckedChange = onSelectedChange)
                    Text(record.codigo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SlaInfoColumn("Rol", record.rol)
                    SlaInfoColumn("F. Solicitud", record.fechaSolicitud, alignment = Alignment.End)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SlaInfoColumn("Tipo SLA", record.tipoSla)
                    SlaInfoColumn("F. Ingreso", record.fechaIngreso, alignment = Alignment.End)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    CumpleChip(record.cumple)
                    DiasSlaChip(record.diasSla, record.cumple)
                }
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
        }
    }
}

@Composable
fun SlaInfoColumn(label: String, value: String, alignment: Alignment.Horizontal = Alignment.Start) {
    Column(horizontalAlignment = alignment) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}


@Composable
fun DiasSlaChip(days: Int, cumple: Boolean) {
    val color = if (cumple) Color(0xFF388E3C) else Color(0xFFD32F2F)
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.1f)) {
        Text(
            text = "$days días",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = color,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun CumpleChip(cumple: Boolean) {
    val text = if (cumple) "✓ Cumple" else "✗ No Cumple"
    val color = if (cumple) Color(0xFF388E3C) else Color(0xFFD32F2F)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(if (cumple) Icons.Default.CheckCircle else Icons.Default.Cancel, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecordDialog(record: SlaRecord, onDismiss: () -> Unit, onSave: (SlaRecord) -> Unit) {
    var rol by remember { mutableStateOf(record.rol) }
    var fechaSolicitud by remember { mutableStateOf(record.fechaSolicitud) }
    var fechaIngreso by remember { mutableStateOf(record.fechaIngreso) }
    var tipoSla by remember { mutableStateOf(record.tipoSla) }

    val (recalculatedDias, recalculatedCumple) = remember(fechaSolicitud, fechaIngreso, tipoSla) {
        try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date1 = dateFormat.parse(fechaSolicitud)
            val date2 = dateFormat.parse(fechaIngreso)
            if (date1 != null && date2 != null) {
                val diff = date2.time - date1.time
                val dias = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
                val cumple = when (tipoSla) {
                    "SLA1" -> dias < 35
                    "SLA2" -> dias < 15
                    else -> false
                }
                dias to cumple
            } else {
                record.diasSla to record.cumple
            }
        } catch (e: Exception) {
            record.diasSla to record.cumple
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Editar Registro", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
                Text(
                    "Los campos NUM DIAS SLA y CUMPLE se recalcularán automáticamente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = record.codigo,
                    onValueChange = {},
                    label = { Text("Código (No editable)") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = rol, onValueChange = { rol = it }, label = { Text("Rol *") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = fechaSolicitud, onValueChange = { fechaSolicitud = it }, label = { Text("Fecha Solicitud (dd/MM/yyyy) *") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = fechaIngreso, onValueChange = { fechaIngreso = it }, label = { Text("Fecha Ingreso (dd/MM/yyyy) *") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = tipoSla,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo SLA *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("SLA1") }, onClick = { tipoSla = "SLA1"; expanded = false })
                        DropdownMenuItem(text = { Text("SLA2") }, onClick = { tipoSla = "SLA2"; expanded = false })
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Vista Previa del Recálculo:", fontWeight = FontWeight.Bold)
                        Text("• NUM DIAS SLA: $recalculatedDias días")
                        Text("• CUMPLE: ${if (recalculatedCumple) "✅ Sí" else "❌ No"}")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val updatedRecord = record.copy(
                            rol = rol,
                            fechaSolicitud = fechaSolicitud,
                            fechaIngreso = fechaIngreso,
                            tipoSla = tipoSla,
                            diasSla = recalculatedDias,
                            cumple = recalculatedCumple
                        )
                        onSave(updatedRecord)
                    }) {
                        Text("Guardar Cambios")
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GestionDatosScreenPreview() {
    Proyecto1Theme {
        // Para la preview, podríamos necesitar un ViewModel de mentira, pero por ahora lo dejamos así.
        // GestionDatosScreen(viewModel = viewModel())
    }
}
