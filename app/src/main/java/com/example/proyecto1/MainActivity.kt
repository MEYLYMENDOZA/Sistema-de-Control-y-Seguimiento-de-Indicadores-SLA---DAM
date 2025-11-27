package com.example.proyecto1 // ASUMO QUE TU CLASE MAINACTIVITY ESTÁ EN ESTE PACKAGE

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Report
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // NECESARIO PARA OBTENER EL CONTEXTO
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue as M3DrawerValue
import androidx.compose.material3.rememberDrawerState as rememberM3DrawerState
import com.example.proyecto1.ui.gestion.GestionDatosViewModel
import com.example.proyecto1.ui.gestion.GestionMainScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.example.proyecto1.ui.login.LoginScreen
import com.example.proyecto1.presentation.prediccion.PrediccionScreen
import com.example.proyecto1.presentation.prediccion.PrediccionViewModel

// DataStore delegate (Preferences) - disponible a nivel de archivo
private val Context.dataStore by preferencesDataStore(name = "user_prefs")

// -------------------------------------------------
// Screen routes (simple sealed)
sealed class Screen(val route: String, val label: String) {
    object Login : Screen("login", "Login")
    object Alertas : Screen("alertas", "Alertas")
    object Dashboard : Screen("dashboard", "Dashboard")
    object Reportes : Screen("reportes", "Reportes")
    object Usuarios : Screen("usuarios", "Usuarios")
    object Carga : Screen("carga", "Carga")
    object Prediccion : Screen("prediccion", "Predicción")
    object Configuracion : Screen("configuracion", "Configuración")
}

// -------------------------------------------------
// SessionViewModel (antes LoginViewModel) que usa Preferences DataStore para persistir la sesión
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
// MainActivity completo y autocontenido
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val application = LocalContext.current.applicationContext as Application
                val factory = remember { SessionViewModelFactory(application) }
                AppRoot(sessionViewModel = viewModel(factory = factory))
            }
        }
    }
}

// -------------------------------------------------
// AppRoot: controla la separación estricta de NavHosts
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(sessionViewModel: SessionViewModel) {
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
                // Usa LoginScreen avanzado desde ui.login
                LoginScreen(onLoginSuccess = {
                    sessionViewModel.saveSession()
                    isLoggedIn.value = true
                })
            }
        }
    } else {
        val modulesNavController = rememberNavController()
        val drawerState = rememberM3DrawerState(M3DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
            ModalDrawerSheet {
                DrawerMenu(
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

            // Determinar el título según la pantalla actual
            val titulo = when (currentRoute) {
                Screen.Alertas.route -> "Alertas"
                Screen.Dashboard.route -> "Dashboard"
                Screen.Reportes.route -> "Reportes"
                Screen.Usuarios.route -> "Usuarios"
                Screen.Carga.route -> "Mi App - Módulos"
                Screen.Prediccion.route -> "Predicción SLA"
                Screen.Configuracion.route -> "Configuración"
                else -> "SLA Tracker"
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(titulo) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Abrir menú")
                            }
                        }
                    )
                },
                bottomBar = {
                    NavigationBar {
                        val items = listOf(Screen.Alertas, Screen.Dashboard, Screen.Reportes)
                        val currentDestination by modulesNavController.currentBackStackEntryAsState()
                        val currentRoute = currentDestination?.destination?.route
                        items.forEach { screen ->
                            NavigationBarItem(
                                selected = currentRoute == screen.route,
                                onClick = { modulesNavController.navigate(screen.route) { launchSingleTop = true } },
                                icon = { Icon(Icons.Filled.Report, contentDescription = null) },
                                label = { Text(screen.label) }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = modulesNavController,
                    startDestination = Screen.Alertas.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(Screen.Alertas.route) { AlertasPlaceholder() }
                    composable(Screen.Dashboard.route) { DashboardPlaceholder() }
                    composable(Screen.Reportes.route) { ReportesPlaceholder() }
                    composable(Screen.Usuarios.route) { UsuariosPlaceholder() }
                    composable(Screen.Carga.route) { 
                        val gestionDatosViewModel: GestionDatosViewModel = viewModel()
                        GestionMainScreen(viewModel = gestionDatosViewModel) 
                    }
                    composable(Screen.Prediccion.route) {
                        val prediccionViewModel: PrediccionViewModel = viewModel()
                        PrediccionScreen(vm = prediccionViewModel)
                    }
                    composable(Screen.Configuracion.route) { ConfiguracionPlaceholder() }
                }
            }
        }
    }
}

// -------------------------------------------------
// DrawerMenu, BottomBar, LoginScreen y Placeholders

@Composable
fun DrawerMenu(onNavigateTo: (String) -> Unit, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Menú", style = MaterialTheme.typography.titleLarge)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Enlaces a módulos
        Text(
            text = "Alertas",
            modifier = Modifier
                .padding(vertical = 8.dp)
                .clickable { onNavigateTo(Screen.Alertas.route) }
        )

        Text(
            text = "Dashboard",
            modifier = Modifier
                .padding(vertical = 8.dp)
                .clickable { onNavigateTo(Screen.Dashboard.route) }
        )

        Text(
            text = "Reportes",
            modifier = Modifier
                .padding(vertical = 8.dp)
                .clickable { onNavigateTo(Screen.Reportes.route) }
        )

        Text(
            text = "Usuarios",
            modifier = Modifier
                .padding(vertical = 8.dp)
                .clickable { onNavigateTo(Screen.Usuarios.route) }
        )

        Text(
            text = "Carga",
            modifier = Modifier
                .padding(vertical = 8.dp)
                .clickable { onNavigateTo(Screen.Carga.route) }
        )

        Text(
            text = "Predicción",
            modifier = Modifier
                .padding(vertical = 8.dp)
                .clickable { onNavigateTo(Screen.Prediccion.route) }
        )

        Text(
            text = "Configuración",
            modifier = Modifier
                .padding(vertical = 8.dp)
                .clickable { onNavigateTo(Screen.Configuracion.route) }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Logout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLogout() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Cerrar sesión")
        }
    }
}


@Composable
fun AlertasPlaceholder() {
    PlaceholderScreen(title = "Alertas")
}

@Composable
fun DashboardPlaceholder() {
    PlaceholderScreen(title = "Dashboard")
}

@Composable
fun ReportesPlaceholder() {
    PlaceholderScreen(title = "Reportes")
}

@Composable
fun UsuariosPlaceholder() {
    PlaceholderScreen(title = "Usuarios")
}

@Composable
fun CargaPlaceholder() {
    PlaceholderScreen(title = "Carga")
}

@Composable
fun ConfiguracionPlaceholder() {
    PlaceholderScreen(title = "Configuración")
}

@Composable
fun PlaceholderScreen(title: String) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Contenido de $title...")
        }
    }
}