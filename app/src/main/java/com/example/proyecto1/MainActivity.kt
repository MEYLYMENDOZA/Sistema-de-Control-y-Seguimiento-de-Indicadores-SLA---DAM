package com.example.proyecto1

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.proyecto1.presentation.carga.CargaScreen
import com.example.proyecto1.presentation.gestion.GestionScreen
import com.example.proyecto1.presentation.navigation.NavigationViewModel
import com.example.proyecto1.ui.login.LoginScreen
import com.example.proyecto1.ui.report.ConfigurationScreen
import com.example.proyecto1.ui.report.DashboardScreen
import com.example.proyecto1.presentation.prediccion.PrediccionScreen
import com.example.proyecto1.presentation.prediccion.PrediccionViewModel
import com.example.proyecto1.presentation.tendencia.TendenciaScreen
import com.example.proyecto1.presentation.tendencia.TendenciaViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

sealed class Screen(val route: String, val label: String) {
    object Login : Screen("login", "Login")
    object Alertas : Screen("alertas", "Alertas")
    object Dashboard : Screen("dashboard", "Dashboard")
    object Reportes : Screen("reportes", "Reportes")
    object Usuarios : Screen("usuarios", "Usuarios")
    object Carga : Screen("carga", "Carga")
    object Gestion : Screen("gestion", "Gestión de Datos") // <-- NUEVA RUTA
    object Prediccion : Screen("prediccion", "Predicción")
    object Tendencia : Screen("tendencia", "Tendencia")
    object Configuracion : Screen("configuracion", "Configuración")
}

class SessionViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = getApplication<Application>().applicationContext
    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")

    suspend fun isUserLoggedIn(): Boolean = appContext.dataStore.data
        .map { it[IS_LOGGED_IN] ?: false }
        .first()

    fun saveSession() { viewModelScope.launch { appContext.dataStore.edit { it[IS_LOGGED_IN] = true } } }
    fun clearSession() { viewModelScope.launch { appContext.dataStore.edit { it[IS_LOGGED_IN] = false } } }
}

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SessionViewModel(application) as T
        }
         if (modelClass.isAssignableFrom(NavigationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NavigationViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val application = LocalContext.current.applicationContext as Application
                val factory = remember { MainViewModelFactory(application) }
                AppRoot(sessionViewModel = viewModel(factory = factory), navViewModel = viewModel(factory = factory))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(sessionViewModel: SessionViewModel, navViewModel: NavigationViewModel) {
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
        val drawerState = rememberM3DrawerState(M3DrawerValue.Closed)
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
                Screen.Dashboard.route -> "Dashboard"
                Screen.Reportes.route -> "Reportes" 
                Screen.Usuarios.route -> "Usuarios"
                Screen.Carga.route -> "Carga de Datos"
                Screen.Gestion.route -> "Gestión de Datos" // <-- TÍTULO PARA LA NUEVA PANTALLA
                Screen.Prediccion.route -> "Predicción SLA"
                Screen.Tendencia.route -> "Tendencia y Proyección SLA"
                Screen.Configuracion.route -> "Configuración"
                else -> "SLA Tracker"
            }

            Scaffold(
                topBar = {
                    if (currentRoute != Screen.Reportes.route && currentRoute != Screen.Configuracion.route) {
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
                    composable(Screen.Alertas.route) { AlertasPlaceholder() }
                    composable(Screen.Dashboard.route) { DashboardPlaceholder() }
                    composable(Screen.Reportes.route) {
                        DashboardScreen(
                            navController = modulesNavController,
                            openDrawer = { scope.launch { drawerState.open() } }
                        )
                    }
                    composable(Screen.Usuarios.route) { UsuariosPlaceholder() }
                    composable(Screen.Carga.route) { CargaScreen() } 
                    composable(Screen.Gestion.route) { GestionScreen() } // <-- NAVEGACIÓN A LA NUEVA PANTALLA
                    composable(Screen.Prediccion.route) {
                        val prediccionViewModel: PrediccionViewModel = viewModel()
                        PrediccionScreen(vm = prediccionViewModel)
                    }

                    composable(Screen.Tendencia.route) {
                        val tendenciaViewModel: TendenciaViewModel = viewModel()
                        TendenciaScreen(vm = tendenciaViewModel)
                    }
                    composable(Screen.Configuracion.route) { 
                        ConfigurationScreen(openDrawer = { scope.launch { drawerState.open() } })
                     }

                }
            }
        }
    }
}

@Composable
fun DrawerMenu(isGestionEnabled: Boolean, onNavigateTo: (String) -> Unit, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clickable(enabled = isEnabled) { onNavigateTo(screen.route) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

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
fun AlertasPlaceholder() { PlaceholderScreen(title = "Alertas") }

@Composable
fun DashboardPlaceholder() { PlaceholderScreen(title = "Dashboard") }

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
