package com.example.proyecto1.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto1.data.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen() {
    val viewModel: UserAdministrationViewModel = viewModel(factory = UserAdministrationViewModelFactory())
    val users by viewModel.users.collectAsState()
    var showAddUserScreen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val verifiedCount = users.count { it.status == "Verified" }
    val pendingCount = users.count { it.status == "Pending" }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddUserScreen = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add User")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Administración de Usuarios", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Gestiona los usuarios registrados en el sistema", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryCard("Verificados", verifiedCount.toString(), Modifier.weight(1f))
                SummaryCard("Pendientes", pendingCount.toString(), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar por usuario, nombre, email...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (showAddUserScreen) {
                AddUserScreen(
                    onDismiss = { showAddUserScreen = false },
                    onUserAdded = {
                        viewModel.addUser(it)
                        showAddUserScreen = false
                    }
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(users.filter { it.username.contains(searchQuery, true) || it.fullName.contains(searchQuery, true) || it.email.contains(searchQuery, true) }) { user ->
                    UserListItem(user = user)
                }
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, count: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = count, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
fun UserListItem(user: User) {
    val statuses = mapOf("Pending" to "Pendiente", "Verified" to "Verificado")
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.fullName, style = MaterialTheme.typography.titleMedium)
                Text(text = user.email, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = statuses[user.status] ?: user.status,
                color = if (user.status == "Verified") Color.Green else Color.Red
            )
        }
    }
}
