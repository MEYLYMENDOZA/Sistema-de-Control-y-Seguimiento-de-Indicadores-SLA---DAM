package com.example.proyecto1.presentation.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyecto1.R // ¡Asegúrate de tener un logo en res/drawable!
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppDrawer(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    currentRoute: String
) {
    ModalDrawerSheet {
        // --- Cabecera del Menú ---
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Asumiendo que tienes un logo en res/drawable/ic_logo_tracker.xml o similar
            /*
            Image(
                painter = painterResource(id = R.drawable.ic_logo_tracker),
                contentDescription = "Logo SLA Tracker"
            )
            */
            // Texto de placeholder si no tienes logo
            Text(
                "SLA Tracker",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Control y Seguimiento",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Info de Usuario ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = "Admin",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Admin",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        "Administrador",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // --- Items de Navegación ---
        Column(modifier = Modifier.padding(12.dp)) {
            DrawerItem(
                label = "Inicio",
                icon = Icons.Default.Home,
                description = "Dashboard y KPIs",
                isSelected = currentRoute == AppScreens.AlertsDashboard.route,
                onClick = {
                    navController.navigate(AppScreens.AlertsDashboard.route) { popUpTo(0) }
                    scope.launch { drawerState.close() }
                }
            )

            // ... (Aquí puedes añadir "Carga de Datos", "Gestión de Datos", etc.) ...

            DrawerItem(
                label = "Alertas",
                icon = Icons.Default.Notifications,
                description = "Notificaciones y alertas",
                isSelected = currentRoute == AppScreens.AlertsDashboard.route, // Se resalta "Inicio" y "Alertas"
                onClick = {
                    navController.navigate(AppScreens.AlertsDashboard.route) { popUpTo(0) }
                    scope.launch { drawerState.close() }
                }
            )

            DrawerItem(
                label = "Notificaciones",
                icon = Icons.Default.Email,
                description = "Historial de reportes enviados",
                isSelected = currentRoute == AppScreens.EmailHistory.route,
                onClick = {
                    navController.navigate(AppScreens.EmailHistory.route)
                    scope.launch { drawerState.close() }
                }
            )
            // ... (Aquí puedes añadir "Reportes", "Usuarios", etc.) ...
        }
    }
}

// Composable de ayuda para cada fila del menú
@Composable
private fun DrawerItem(
    label: String,
    icon: ImageVector,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Column {
                Text(label, fontWeight = FontWeight.SemiBold)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        icon = { Icon(icon, contentDescription = label) },
        selected = isSelected,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}