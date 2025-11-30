package com.example.proyecto1.presentation.gestion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.proyecto1.presentation.carga.CargaItemData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionScreen(vm: GestionViewModel = hiltViewModel()) {
    val uiState by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // -- Snackbar para mensajes de éxito y error --
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Long)
            vm.dismissError() // Limpia el mensaje
        }
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar("Error: $it")
            vm.dismissError() // Limpia el mensaje
        }
    }
    
    // -- DIÁLOGO DE EDICIÓN --
    if (uiState.itemToEdit != null) {
        EditRecordDialog(uiState = uiState, vm = vm)
    }

    // -- CONTENIDO PRINCIPAL CON SCAFFOLD Y BOTÓN FLOTANTE --
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (uiState.allItems.isNotEmpty()) { // El botón solo existe si hay datos
                ExtendedFloatingActionButton(
                    onClick = { vm.subirDatosAGuardar() },
                    expanded = !uiState.isLoading,
                    icon = { Icon(Icons.Filled.CloudUpload, "Subir Datos") },
                    text = { Text(text = "Subir datos") },
                )
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F2F7))
                .padding(it)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item { Header() }
            item { SearchAndFilter(uiState, vm) }
            
            item {
                Row(modifier=Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){
                    Text("Registros SLA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                     TextButton(onClick = { vm.onToggleAllSelected(true) }) {
                        Text("Seleccionar todos")
                    }
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical=32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            if (!uiState.isLoading && uiState.allItems.isEmpty()) {
                 item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                             Text("No hay datos para gestionar.", color = Color.Gray, style = MaterialTheme.typography.titleMedium)
                             Text("Vaya a la pantalla de Carga para procesar un nuevo archivo.", color = Color.Gray)
                        }
                    }
                }
            }

            items(uiState.displayedItems, key = { it.codigo }) { item ->
                SlaRecordCard(item = item, isSelected = item.codigo in uiState.selectedItemCodes, onToggleSelect = { vm.onToggleItemSelected(item.codigo, it) }, onEdit = { vm.onEditItem(item) })
            }
        }
    }
}

@Composable
fun Header() {
    Column {
        Text("Gestión de Datos", style = MaterialTheme.typography.headlineSmall)
        Text("Visualiza, edita y sube los registros a la base de datos.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAndFilter(uiState: GestionUiState, vm: GestionViewModel) {
     Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(0.dp), shape = RoundedCornerShape(12.dp)) {
         Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)){
             OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { vm.onSearchQueryChanged(it) },
                label = { Text("Buscar por código, rol o tipo SLA...") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically){
                SummaryChip("Total: ${uiState.allItems.size}", Icons.Default.Info)
                SummaryChip("Cumplen: ${uiState.allItems.count { it.estado == "Cumple" }}", Icons.Default.CheckCircle, containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF388E3C))
                SummaryChip("No Cumplen: ${uiState.allItems.count { it.estado != "Cumple" }}", Icons.Default.Error, containerColor = Color(0xFFFFEBEE), contentColor = Color(0xFFD32F2F))
            }
            // --- BOTÓN DE ELIMINAR ---
             Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End){
                 Button(
                    onClick = { vm.deleteSelectedItems() }, 
                    enabled = uiState.selectedItemCodes.isNotEmpty() && !uiState.isLoading, 
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                 ) {
                    Icon(Icons.Default.Delete, contentDescription=null)
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Text("Eliminar (${uiState.selectedItemCodes.size})")
                }
            }
         }
     }
}

@Composable
fun SummaryChip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, containerColor: Color = MaterialTheme.colorScheme.surfaceVariant, contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Row(modifier = Modifier.clip(CircleShape).background(containerColor).padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)){
        Icon(icon, contentDescription=null, tint = contentColor, modifier=Modifier.size(16.dp))
        Text(text, color = contentColor, fontSize = 12.sp)
    }
}

@Composable
fun SlaRecordCard(item: CargaItemData, isSelected: Boolean, onToggleSelect: (Boolean) -> Unit, onEdit: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(0.dp), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Checkbox(checked = isSelected, onCheckedChange = onToggleSelect)
                    Text(item.codigo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                InfoColumn("Rol", item.rol)
                InfoColumn("F. Solicitud", item.fechaSolicitud)
                InfoColumn("F. Ingreso", item.fechaIngreso)
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                InfoColumn("Tipo SLA", item.tipoSla)
                Pill(text = "${item.diasTranscurridos} días", color = Color(0xFFE3F2FD), textColor = Color(0xFF1565C0))
                Pill(text = item.estado, color = if (item.estado == "Cumple") Color(0xFFE8F5E9) else Color(0xFFFFEBEE), textColor = if (item.estado == "Cumple") Color(0xFF2E7D32) else Color(0xFFC62828))
            }
        }
    }
}

@Composable
fun InfoColumn(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun Pill(text: String, color: Color, textColor: Color) {
    Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(color).padding(horizontal = 12.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
        Text(text, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecordDialog(uiState: GestionUiState, vm: GestionViewModel) {
    Dialog(onDismissRequest = { vm.onDismissEditDialog() }) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Editar Registro", style = MaterialTheme.typography.titleLarge)
                Text("Los campos NUM DIAS SLA y CUMPLE se recalcularán automáticamente", style = MaterialTheme.typography.bodySmall)
                
                OutlinedTextField(
                    value = uiState.itemToEdit?.codigo ?: "",
                    onValueChange = {}, 
                    label = { Text("Código (No editable)") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.editedRol,
                    onValueChange = { vm.onEditRolChanged(it) },
                    label = { Text("Rol *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.editedFechaSolicitud,
                    onValueChange = { vm.onEditFechaSolicitudChanged(it) },
                    label = { Text("Fecha Solicitud * (dd/MM/yyyy)") },
                    modifier = Modifier.fillMaxWidth()
                )

                 OutlinedTextField(
                    value = uiState.editedFechaIngreso,
                    onValueChange = { vm.onEditFechaIngresoChanged(it) },
                    label = { Text("Fecha Ingreso * (dd/MM/yyyy)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // TODO: Reemplazar con un Dropdown
                OutlinedTextField(
                    value = uiState.editedTipoSla,
                    onValueChange = { vm.onEditTipoSlaChanged(it) },
                    label = { Text("Tipo SLA * (SLA1 o SLA2)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // TODO: Añadir vista previa del recálculo

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { vm.onDismissEditDialog() }) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { vm.onSaveChanges() }) {
                        Text("Guardar Cambios")
                    }
                }
            }
        }
    }
}
