package com.example.proyecto1 // <--- ESTE ES EL PAQUETE CORRECTO DE TUS COMPAÑEROS

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// --- IMPORTS DE TUS PANTALLAS (BRAYAN) ---
import com.example.proyecto1.features.notifications.presentation.dashboard.AlertsDashboardScreen
import com.example.proyecto1.features.notifications.presentation.dashboard.AlertsDashboardViewModel
import com.example.proyecto1.features.notifications.presentation.alert_history.AlertsHistoryScreen
import com.example.proyecto1.features.notifications.presentation.alert_history.AlertsHistoryViewModel
import com.example.proyecto1.features.notifications.presentation.email_notifications.EmailNotificationsScreen
import com.example.proyecto1.features.notifications.presentation.email_notifications.EmailNotificationsViewModel

// --- IMPORTS DEL EQUIPO ---
import com.example.proyecto1.ui.login.LoginScreen
import com.example.proyecto1.ui.report.ConfigurationScreen
import com.example.proyecto1.ui.report.DashboardScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


// DataStore delegate
private val Context.dataStore by preferencesDataStore(name = "user_prefs")

// -------------------------------------------------
// 1. RUTAS DE PANTALLAS (Screen routes)
sealed class Screen(val route: String, val label: String) {
    object Login : Screen("login", "Login")

    // Tu Dashboard Principal de Alertas (Ahora apunta a tu pantalla)
    object Alertas : Screen("alertas", "Alertas")

    // --- TUS NUEVAS RUTAS (BRAYAN) ---
    object AlertasHistorial : Screen("alertas_historial", "Historial")
    object NotificacionesEmail : Screen("notificaciones_email", "Notificaciones")
    // ---------------------------------

    object Dashboard : Screen("dashboard", "Dashboard")
    object Reportes : Screen("reportes", "Reportes")
    object Usuarios : Screen("usuarios", "Usuarios")
    object Carga : Screen("carga", "Carga")
    object Gestion : Screen("gestion", "Gestión de Datos")
    object Prediccion : Screen("prediccion", "Predicción")
    object Tendencia : Screen("tendencia", "Tendencia")
    object Configuracion : Screen("configuracion", "Configuración")
}

@AndroidEntryPoint // <-- ESTA ANOTACIÓN ES LA CLAVE DE TODO
// -------------------------------------------------
// 2. VIEWMODEL DE SESIÓN (LOGIN)
class SessionViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = getApplication<Application>().applicationContext
    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")

    suspend fun isUserLoggedIn(): Boolean = appContext.dataStore.data
        .map { it[IS_LOGGED_IN] ?: false }
        .first()

    fun saveSession() { viewModelScope.launch { appContext.dataStore.edit { it[IS_LOGGED_IN] = true } } }
    fun clearSession() { viewModelScope.launch { appContext.dataStore.edit { it[IS_LOGGED_IN] = false } } }
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

// -------------------------------------------------
// 3. MAIN ACTIVITY
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppRoot()
            }
        }
    }
}

// -------------------------------------------------
// 4. APP ROOT (Lógica de Navegación Principal)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(sessionViewModel: SessionViewModel = hiltViewModel(), navViewModel: NavigationViewModel = hiltViewModel()) {
    val isLoggedIn = remember { mutableStateOf<Boolean?>(null) }

    if (isLoggedIn.value == null) {
        LaunchedEffect(Unit) { isLoggedIn.value = sessionViewModel.isUserLoggedIn() }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    if (isLoggedIn.value == false) {
        val loginNavController = rememberNavController()
        NavHost(navController = loginNavController, startDestination = Screen.Login.route) {
            composable(Screen.Login.route) {
                LoginScreen(onLoginSuccess = {
                    sessionViewModel.saveSession()
                    isLoggedIn.value = true
                })
            }
        }
    } else {
        val modulesNavController = rememberNavController()
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val isGestionEnabled by navViewModel.isGestionDeDatosEnabled.collectAsState()

        ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
            ModalDrawerSheet {
                DrawerMenu(
                    isGestionEnabled = isGestionEnabled,
                    onNavigateTo = { route ->
                        scope.launch { drawerState.close() }
                        modulesNavController.navigate(route) { launchSingleTop = true }
                    },
                    onLogout = {
                        sessionViewModel.clearSession(); isLoggedIn.value = false
                    }
                )
            }
        }) {
            val currentDestination by modulesNavController.currentBackStackEntryAsState()
            val currentRoute = currentDestination?.destination?.route

            val titulo = when (currentRoute) {
                Screen.Alertas.route -> "Alertas"
                Screen.AlertasHistorial.route -> "Historial de Alertas"
                Screen.NotificacionesEmail.route -> "Notificaciones Email"
                Screen.Dashboard.route -> "Dashboard"
                Screen.Reportes.route -> "Reportes"
                Screen.Usuarios.route -> "Usuarios"
                Screen.Carga.route -> "Carga de Datos"
                Screen.Gestion.route -> "Gestión de Datos"
                Screen.Prediccion.route -> "Predicción SLA"
                Screen.Tendencia.route -> "Tendencia y Proyección SLA"
                Screen.Configuracion.route -> "Configuración"
                else -> "SLA Tracker"
            }

            Scaffold(
                topBar = {
                    // Ocultamos el TopBar global en TUS pantallas porque ya tienen uno propio
                    if (currentRoute != Screen.Alertas.route &&
                        currentRoute != Screen.AlertasHistorial.route &&
                        currentRoute != Screen.NotificacionesEmail.route) {
                        TopAppBar(
                            title = { Text(titulo) },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Filled.Menu, contentDescription = "Abrir menú")
                                }
                            }
                        )
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = modulesNavController,
                    startDestination = Screen.Alertas.route,
                    modifier = Modifier.padding(innerPadding)
                ) {

                    // --- AQUÍ CONECTAMOS TUS PANTALLAS ---

                    // 1. DASHBOARD DE ALERTAS (Reemplaza al Placeholder)
                    composable(Screen.Alertas.route) {
                        val dashboardViewModel: AlertsDashboardViewModel = viewModel()
                        AlertsDashboardScreen(
                            viewModel = dashboardViewModel,
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onNavigateToAlertsHistory = {
                                modulesNavController.navigate(Screen.AlertasHistorial.route)
                            },
                            onNavigateToCriticalCases = { /* TODO */ },
                            onNavigateToSettings = {
                                modulesNavController.navigate(Screen.Configuracion.route)
                            }
                        )
                    }

                    // 2. HISTORIAL DE ALERTAS (US-13)
                    composable(Screen.AlertasHistorial.route) {
                        val historyViewModel: AlertsHistoryViewModel = viewModel()
                        AlertsHistoryScreen(
                            viewModel = historyViewModel,
                            onMenuClick = { scope.launch { drawerState.open() } }
                        )
                    }

                    // 3. NOTIFICACIONES EMAIL (US-14)
                    composable(Screen.NotificacionesEmail.route) {
                        val emailViewModel: EmailNotificationsViewModel = viewModel()
                        EmailNotificationsScreen(
                            viewModel = emailViewModel,
                            onMenuClick = { scope.launch { drawerState.open() } }
                        )
                    }

                    // --- PANTALLAS DE TUS COMPAÑEROS ---
                    composable(Screen.Dashboard.route) { DashboardPlaceholder() }
                    composable(Screen.Reportes.route) {
                        DashboardScreen(
                            navController = modulesNavController,
                            openDrawer = { scope.launch { drawerState.open() } }
                        )
                    }
                    composable(Screen.Usuarios.route) { UsuariosPlaceholder() }
                    composable(Screen.Carga.route) { CargaScreen() }
                    composable(Screen.Gestion.route) { GestionScreen(vm = hiltViewModel()) } 
                    composable(Screen.Prediccion.route) { PrediccionScreen(vm = hiltViewModel()) }
                    composable(Screen.Tendencia.route) { TendenciaScreen(vm = hiltViewModel()) }
                    composable(Screen.Configuracion.route) { 
                        ConfigurationScreen(openDrawer = { scope.launch { drawerState.open() } })
                     }
                }
            }
        }
    }
}

// -------------------------------------------------
// 5. DRAWER MENU (Menú Lateral)
@Composable
fun DrawerMenu(isGestionEnabled: Boolean, onNavigateTo: (String) -> Unit, onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Menú", style = MaterialTheme.typography.titleLarge)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        val menuItems = listOf(
            Screen.Alertas, Screen.Dashboard, Screen.Reportes, Screen.Usuarios, Screen.Carga, 
            Screen.Gestion, Screen.Prediccion, Screen.Tendencia, Screen.Configuracion
        )

        menuItems.forEach { screen ->
            val isEnabled = if (screen == Screen.Gestion) isGestionEnabled else true
             Text(
                text = screen.label,
                color = if(isEnabled) Color.Black else Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp).clickable(enabled = isEnabled) { onNavigateTo(screen.route) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth().clickable { onLogout() }.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Cerrar sesión")
        }
    }
}

// --- Placeholders del Equipo ---

@Composable
fun DashboardPlaceholder() { PlaceholderScreen(title = "Dashboard") }
fun AlertasPlaceholder() { PlaceholderScreen(title = "Alertas") }

@Composable
fun ReportesPlaceholder() { PlaceholderScreen(title = "Reportes") }
@Composable
fun UsuariosPlaceholder() { PlaceholderScreen(title = "Usuarios") }
@Composable
fun CargaPlaceholder() { PlaceholderScreen(title = "Carga") }
@Composable
fun ConfiguracionPlaceholder() { PlaceholderScreen(title = "Configuración") }

@Composable
fun PlaceholderScreen(title: String) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Contenido de $title...")
        }
    }
}
@Composable
fun PlaceholderScreen(title: String) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Contenido de $title...")
        }
    }
}
