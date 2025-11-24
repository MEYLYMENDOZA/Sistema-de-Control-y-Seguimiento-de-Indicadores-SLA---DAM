package com.example.proyecto1

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proyecto1.ui.alertas.AlertasScreen
import com.example.proyecto1.ui.configuracion.ConfigurationScreen
import com.example.proyecto1.ui.gestion.CargaDatosScreen
import com.example.proyecto1.ui.gestion.GestionDatosScreen
import com.example.proyecto1.ui.gestion.GestionDatosViewModel
import com.example.proyecto1.ui.inicio.InicioScreen
import com.example.proyecto1.ui.login.LoginScreen
import com.example.proyecto1.ui.notificaciones.NotificacionesScreen
import com.example.proyecto1.ui.prediccion.PrediccionSLAScreen
import com.example.proyecto1.ui.report.AppNavigation
import com.example.proyecto1.ui.tendencia.TendenciaSLAScreen
import com.example.proyecto1.ui.theme.Proyecto1Theme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// Definición de DataStore a nivel de archivo
private val Context.dataStore by preferencesDataStore(name = "user_prefs")

sealed class Screen(
    val route: String,
    val label: String,
    val subtitle: String,
    val icon: ImageVector
) {
    object Inicio : Screen("inicio", "Inicio", "Dashboard y KPIs", Icons.Outlined.Home)
    object CargaDatos : Screen("carga_datos", "Carga de Datos", "Subir archivo Excel", Icons.Outlined.Upload)
    object GestionDatos : Screen("gestion_datos", "Gestión de Datos", "Editar y eliminar registros", Icons.Outlined.Storage)
    object PrediccionSLA : Screen("prediccion_sla", "Predicción SLA", "Regresión lineal", Icons.Outlined.TrackChanges)
    object TendenciaSLA : Screen("tendencia_sla", "Tendencia SLA", "Evolución histórica", Icons.Outlined.Timeline)
    object Alertas : Screen("alertas", "Alertas", "Notificaciones y alertas", Icons.Outlined.NotificationsActive)
    object Reportes : Screen("reportes", "Reportes", "Generar reportes PDF", Icons.Outlined.PictureAsPdf)
    object Notificaciones : Screen("notificaciones", "Notificaciones", "Historial de reportes enviados", Icons.Outlined.Email)
    object Configuracion : Screen("configuracion", "Configuración", "Ajustes de SLA", Icons.Outlined.Settings)
    object Login : Screen("login", "Login", "", Icons.Outlined.Lock) // No tiene subtítulo
}

val moduleScreens = listOf(
    Screen.Inicio,
    Screen.CargaDatos,
    Screen.GestionDatos,
    Screen.PrediccionSLA,
    Screen.TendenciaSLA,
    Screen.Alertas,
    Screen.Reportes,
    Screen.Notificaciones,
    Screen.Configuracion
)

class SessionViewModel(application: Application) : AndroidViewModel(application) {
    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    suspend fun isUserLoggedIn(): Boolean = getApplication<Application>().dataStore.data.map { it[IS_LOGGED_IN] ?: false }.first()
    fun saveSession() = viewModelScope.launch { getApplication<Application>().dataStore.edit { it[IS_LOGGED_IN] = true } }
    fun clearSession() = viewModelScope.launch { getApplication<Application>().dataStore.edit { it[IS_LOGGED_IN] = false } }
}

class SessionViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SessionViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Proyecto1Theme {
                val application = LocalContext.current.applicationContext as Application
                val sessionViewModel: SessionViewModel = viewModel(factory = SessionViewModelFactory(application))
                AppRoot(sessionViewModel = sessionViewModel)
            }
        }
    }
}

@Composable
fun AppRoot(sessionViewModel: SessionViewModel) {
    val isLoggedIn = remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) { isLoggedIn.value = sessionViewModel.isUserLoggedIn() }

    when (isLoggedIn.value) {
        null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        false -> {
            NavHost(rememberNavController(), startDestination = Screen.Login.route) {
                composable(Screen.Login.route) {
                    LoginScreen(onLoginSuccess = { sessionViewModel.saveSession(); isLoggedIn.value = true })
                }
            }
        }
        true -> MainAppScreen(onLogout = { sessionViewModel.clearSession(); isLoggedIn.value = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val application = LocalContext.current.applicationContext as Application
    val gestionDatosViewModel: GestionDatosViewModel = viewModel(factory = GestionDatosViewModelFactory(application))

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Inicio.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { AppDrawer(currentRoute, { route -> navController.navigate(route) { launchSingleTop = true }; scope.launch { drawerState.close() } }, onLogout) }
    ) {
        Scaffold(
            topBar = {
                val currentScreen = moduleScreens.find { it.route == currentRoute }
                if (currentRoute != Screen.Reportes.route) {
                    AppTopBar(currentScreen?.label ?: "") { scope.launch { drawerState.open() } }
                }
            }
        ) { paddingValues ->
            NavHost(navController, startDestination = Screen.Inicio.route, Modifier.padding(paddingValues)) {
                composable(Screen.Inicio.route) { InicioScreen() }
                composable(Screen.CargaDatos.route) { CargaDatosScreen(viewModel = gestionDatosViewModel) }
                composable(Screen.GestionDatos.route) { GestionDatosScreen(viewModel = gestionDatosViewModel) }
                composable(Screen.PrediccionSLA.route) { PrediccionSLAScreen() }
                composable(Screen.TendenciaSLA.route) { TendenciaSLAScreen() }
                composable(Screen.Alertas.route) { AlertasScreen() }
                composable(Screen.Reportes.route) { AppNavigation() }
                composable(Screen.Notificaciones.route) { NotificacionesScreen() }
                composable(Screen.Configuracion.route) { ConfigurationScreen(openDrawer = { scope.launch { drawerState.open() } }) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(title: String, onMenuClick: () -> Unit) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Medium) },
        navigationIcon = { IconButton(onClick = onMenuClick) { Icon(Icons.Filled.Menu, "Abrir menú") } },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(currentRoute: String, onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    ModalDrawerSheet {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.AccountCircle,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("analista", style = MaterialTheme.typography.titleMedium)
                    Text("Analista", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Items
            Spacer(Modifier.height(12.dp))
            for (screen in moduleScreens) {
                NavigationDrawerItem(
                    label = {
                        Column(verticalArrangement = Arrangement.Center) {
                            Text(screen.label, style = MaterialTheme.typography.labelLarge)
                            if (screen.subtitle.isNotEmpty()) {
                                Text(
                                    screen.subtitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    selected = currentRoute == screen.route,
                    onClick = { onNavigate(screen.route) },
                    icon = { Icon(screen.icon, contentDescription = screen.label) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.weight(1f))

            // Footer
            HorizontalDivider(Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
            NavigationDrawerItem(
                label = {
                    Column(verticalArrangement = Arrangement.Center) {
                        Text("Cerrar Sesión", style = MaterialTheme.typography.labelLarge)
                        Text(
                            "Salir del sistema",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                selected = false,
                onClick = onLogout,
                icon = { Icon(Icons.Outlined.Logout, contentDescription = "Cerrar Sesión") },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

// Factory para crear el GestionDatosViewModel
class GestionDatosViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GestionDatosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GestionDatosViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
