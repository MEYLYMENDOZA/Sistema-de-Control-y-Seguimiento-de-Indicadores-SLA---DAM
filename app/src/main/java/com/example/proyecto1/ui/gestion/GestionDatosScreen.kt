package com.example.proyecto1.ui.gestion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.proyecto1.ui.theme.Proyecto1Theme
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// --- Pantalla Principal de Gestión de Datos ---
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
                it.rol.lowercase(Locale.getDefault()).contains(query)
            }
        }
    }

    if (!uiState.dataLoaded) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aún no se han cargado datos.", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Tarjeta de control (Búsqueda y borrado)
            item {
                ControlHeaderCard(
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                    selectedCount = uiState.selectedRecordIds.size,
                    onDeleteClick = { viewModel.onDeleteSelectedClicked() }
                )
            }

            // 2. Cabecera de la lista de registros
            item {
                ListHeader(filteredCount = filteredRecords.size, totalCount = uiState.records.size) {
                    val ids = filteredRecords.map { it.id }
                    viewModel.onSelectAllFiltered(ids, it)
                }
            }
            
            // 3. Lista de registros en tarjetas
            itemsIndexed(filteredRecords, key = { _, item -> item.id }) { index, record ->
                val isSelected = record.id in uiState.selectedRecordIds
                SlaRecordCard(
                    record = record,
                    isSelected = isSelected,
                    onSelectedChange = { viewModel.onRecordSelectionChanged(record.id, it) },
                    onEditClick = { editingRecord = record }
                )
            }
        }
    }

    editingRecord?.let {
        EditRecordDialog(
            record = it,
            onDismiss = { editingRecord = null },
            onSave = {
                viewModel.onSaveRecord(it)
                editingRecord = null
            }
        )
    }
}

// --- Componentes Rediseñados ---

@Composable
fun ControlHeaderCard(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCount: Int,
    onDeleteClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Gestión de Datos", style = MaterialTheme.typography.titleLarge)
            Text(
                "Visualiza, edita y elimina registros SLA. Total: ${selectedCount} seleccionados",
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por código, rol o tipo SLA...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            )
            Button(
                onClick = onDeleteClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedCount > 0,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Eliminar ($selectedCount)")
            }
        }
    }
}

@Composable
fun ListHeader(filteredCount: Int, totalCount: Int, onSelectAll: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Registros SLA ($filteredCount de $totalCount)", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Seleccionar todos", style = MaterialTheme.typography.bodySmall)
            Checkbox(checked = false, onCheckedChange = onSelectAll) // Lógica de "checked" se debe mejorar
        }
    }
}

@Composable
fun SlaRecordCard(record: SlaRecord, isSelected: Boolean, onSelectedChange: (Boolean) -> Unit, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Fila superior: Checkbox, Código y botón de editar
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Checkbox(checked = isSelected, onCheckedChange = onSelectedChange)
                Text(record.codigo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
            }
            
            // Fila de información principal
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoChip(label = "Rol", value = record.rol, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                InfoChip(label = "Tipo SLA", value = record.tipoSla, modifier = Modifier.weight(1f))
            }
            
            // Fila con las fechas
            Row(modifier = Modifier.fillMaxWidth()) {
                 InfoDate(label = "F. Solicitud", date = record.fechaSolicitud, modifier = Modifier.weight(1f))
                 Spacer(modifier = Modifier.width(8.dp))
                 InfoDate(label = "F. Ingreso", date = record.fechaIngreso, modifier = Modifier.weight(1f))
            }

            // Fila inferior: Estado de cumplimiento
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                StatusChip(label = if(record.cumple) "Cumple" else "No Cumple", isSuccess = record.cumple)
                DaysChip(days = record.diasSla, isSuccess = record.cumple)
            }
        }
    }
}

@Composable
fun InfoChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun InfoDate(label: String, date: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(4.dp))
            Text(date, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun StatusChip(label: String, isSuccess: Boolean) {
    val color = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val icon = if(isSuccess) Icons.Default.CheckCircle else Icons.Default.Cancel
    
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun DaysChip(days: Int, isSuccess: Boolean) {
    val color = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val backgroundColor = if(isSuccess) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer

    Surface(shape = RoundedCornerShape(8.dp), color = backgroundColor) {
        Text(
            text = "$days días",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = color,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

// --- Diálogo de Edición (Ajustado a nuevo Theme) ---

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
                    "SLA2" -> dias < 20 // Corregido de 15 a 20 según las imágenes
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
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Editar Registro", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Cerrar") }
                }
                Text("Los campos NUM DIAS SLA y CUMPLE se recalcularán automáticamente.", style = MaterialTheme.typography.bodySmall)
                
                OutlinedTextField(value = record.codigo, onValueChange = {}, label = { Text("Código (No editable)") }, readOnly = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = rol, onValueChange = { rol = it }, label = { Text("Rol *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = fechaSolicitud, onValueChange = { fechaSolicitud = it }, label = { Text("Fecha Solicitud (dd/MM/yyyy) *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = fechaIngreso, onValueChange = { fechaIngreso = it }, label = { Text("Fecha Ingreso (dd/MM/yyyy) *") }, modifier = Modifier.fillMaxWidth())
                
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(value = tipoSla, onValueChange = {}, readOnly = true, label = { Text("Tipo SLA *") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("SLA1 (< 35 días)") }, onClick = { tipoSla = "SLA1"; expanded = false })
                        DropdownMenuItem(text = { Text("SLA2 (< 20 días)") }, onClick = { tipoSla = "SLA2"; expanded = false })
                    }
                }

                Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Vista Previa del Recálculo:", fontWeight = FontWeight.Bold)
                        Text("• NUM DIAS SLA: $recalculatedDias días")
                        Text("• CUMPLE: ${if (recalculatedCumple) "✅ Sí" else "❌ No"}")
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val updatedRecord = record.copy(
                            rol = rol, fechaSolicitud = fechaSolicitud, fechaIngreso = fechaIngreso, tipoSla = tipoSla, diasSla = recalculatedDias, cumple = recalculatedCumple
                        )
                        onSave(updatedRecord)
                    }) { Text("Guardar Cambios") }
                }
            }
        }
    }
}
