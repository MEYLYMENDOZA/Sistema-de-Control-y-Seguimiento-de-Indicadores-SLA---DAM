package com.example.proyecto1

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class Proyecto1App : Application() {

    // Scope para operaciones asíncronas de la aplicación
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        android.util.Log.d("Proyecto1App", "Application onCreate - iniciando")
        try {
            // Inicializa Firebase con la configuración provista por google-services.json
            FirebaseApp.initializeApp(this)
            android.util.Log.d("Proyecto1App", "FirebaseApp.initializeApp OK")

            // Configurar Firestore con persistencia local habilitada
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

            FirebaseFirestore.getInstance().firestoreSettings = settings
            android.util.Log.d("Proyecto1App", "Firestore settings aplicados")

            // ✅ Inicializar Retrofit de forma ASÍNCRONA para no bloquear el hilo principal
            android.util.Log.d("Proyecto1App", "🔍 Iniciando detección automática de API (asíncrono)...")
            applicationScope.launch {
                try {
                    com.example.proyecto1.data.remote.api.RetrofitClient.initialize(this@Proyecto1App)
                    val baseUrl = com.example.proyecto1.data.remote.api.RetrofitClient.getCurrentBaseUrl()
                    android.util.Log.d("Proyecto1App", "✅ API configurada: $baseUrl")
                } catch (e: Exception) {
                    android.util.Log.e("Proyecto1App", "❌ Error al configurar API", e)
                }
            }

        } catch (e: Exception) {
            // Log del error en caso de fallo de inicialización
            android.util.Log.e("Proyecto1App", "Error al inicializar Firebase", e)
        }
    }
}
