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
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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
    object Configuracion : Screen("configuracion", "Configuración")
}

// -------------------------------------------------
// LoginViewModel real que usa Preferences DataStore para persistir la sesión
class LoginViewModel(application: Application) : AndroidViewModel(application) {
    // Usamos appContext para DataStore
    private val appContext = getApplication<Application>().applicationContext
    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")

    // Lee de DataStore si existe sesión guardada
    suspend fun isUserLoggedIn(): Boolean {
        return appContext.dataStore.data
            .map { preferences -> preferences[IS_LOGGED_IN] ?: false }
            .first()
    }

    // Guarda sesión en DataStore
    fun saveSession() {
        viewModelScope.launch {
            appContext.dataStore.edit { prefs ->
                prefs[IS_LOGGED_IN] = true
            }
        }
    }

    // Borra sesión en DataStore
    fun clearSession() {
        viewModelScope.launch {
            appContext.dataStore.edit { prefs ->
                prefs[IS_LOGGED_IN] = false
            }
        }
    }
}

// Factory para crear LoginViewModel desde Compose pasando Application
class LoginViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(application) as T
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
                // Obtenemos la Application para pasarla a la Factory
                val application = LocalContext.current.applicationContext as Application
                val factory = remember { LoginViewModelFactory(application) }

                // Pasamos el ViewModel instanciado correctamente con la Factory
                AppRoot(loginViewModel = viewModel(factory = factory))
            }
        }
    }
}

// -------------------------------------------------
// AppRoot: controla la separación estricta de NavHosts
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(loginViewModel: LoginViewModel) {
    // Estado central que determina qué NavHost se renderiza
    val isLoggedIn = remember { mutableStateOf<Boolean?>(null) } // Inicializado como null

    // Ruta pendiente para navegar en el NavHost de módulos una vez creado
    val pendingRoute = remember { mutableStateOf<String?>(null) }

    // Mostramos un Splash/Loading si aún no hemos comprobado la sesión
    if (isLoggedIn.value == null) {
        // CARGA INICIAL: consultamos al ViewModel si existe sesión guardada
        LaunchedEffect(Unit) {
            val saved = loginViewModel.isUserLoggedIn()
            if (saved) {
                // Si existe sesión persistida, marcamos logged in
                isLoggedIn.value = true
                pendingRoute.value = Screen.Alertas.route
            } else {
                // Garantizamos que el inicio de la app muestre Login
                isLoggedIn.value = false
            }
        }
        // Placeholder de Carga
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return // Salimos de la composición hasta que isLoggedIn no sea null
    }

    // Si NO está logueado -> sólo NavHost de Login
    if (isLoggedIn.value == false) {
        // NavController exclusivo para el flujo de login
        val loginNavController = rememberNavController()

        NavHost(
            navController = loginNavController,
            startDestination = Screen.Login.route
        ) {
            composable(Screen.Login.route) {
                LoginScreen(onLoginSuccess = {
                    // Guardamos sesión en ViewModel (persistencia)
                    loginViewModel.saveSession()

                    // Establecemos la ruta a la que el NavHost de módulos deberá navegar
                    pendingRoute.value = Screen.Alertas.route

                    // Cambiamos el estado central -> esto desmontará este NavHost (login)
                    isLoggedIn.value = true
                })
            }
        }
    } else if (isLoggedIn.value == true) { // Solo si isLoggedIn es true
        // Usuario logueado -> NavHost de Módulos con Drawer y BottomBar
        val modulesNavController = rememberNavController()
        val drawerState = rememberM3DrawerState(M3DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        // Cuando se monte el NavController de módulos, navegamos a la ruta pendiente si existe
        LaunchedEffect(key1 = pendingRoute.value) {
            pendingRoute.value?.let { route ->
                modulesNavController.navigate(route) {
                    // Limpia la pila si es necesario, aunque con esta estructura no es estrictamente necesario
                    // ya que el NavHost de Login se desmontó completamente.
                    launchSingleTop = true
                }
                pendingRoute.value = null
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    DrawerMenu(
                        onNavigateTo = { route ->
                            scope.launch { drawerState.close() }
                            modulesNavController.navigate(route) { launchSingleTop = true }
                        },
                        onLogout = {
                            // Borramos sesión en ViewModel y cambiamos el estado central
                            loginViewModel.clearSession()
                            isLoggedIn.value = false
                        }
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Mi App - Módulos") },
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
                    // Aseguramos que inicie en la ruta de Alertas por defecto
                    startDestination = Screen.Alertas.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(Screen.Alertas.route) { AlertasPlaceholder() }
                    composable(Screen.Dashboard.route) { DashboardPlaceholder() }
                    composable(Screen.Reportes.route) { ReportesPlaceholder() }
                    composable(Screen.Usuarios.route) { UsuariosPlaceholder() }
                    composable(Screen.Carga.route) { CargaPlaceholder() }
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
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Inicio de Sesión", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Usuario") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = {
                // Simulación de éxito. La persistencia ocurre en AppRoot después de este callback.
                onLoginSuccess()
            }) {
                Text("Entrar")
            }
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