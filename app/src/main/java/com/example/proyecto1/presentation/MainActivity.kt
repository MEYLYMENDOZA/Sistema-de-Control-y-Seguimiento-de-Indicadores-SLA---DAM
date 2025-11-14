package com.example.proyecto1.presentation // Asegúrate que el package sea el correcto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
// (importation de AlertsHistoryScreen)
import com.example.proyecto1.features.notifications.presentation.alert_history.AlertsHistoryScreen
// (importación de EmailNotificationsScreen)
// import com.example.proyecto1.features.notifications.presentation.email_notifications.EmailNotificationsScreen
import com.example.proyecto1.ui.theme.Proyecto1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Proyecto1Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // pantalla de alertas
                    AlertsHistoryScreen()

                    // pantalla de notificaciones
                    // EmailNotificationsScreen()
                }
            }
        }
    }
}