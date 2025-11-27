package com.example.proyecto1

import android.app.Application
import com.example.proyecto1.data.remote.RetrofitClient
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class Proyecto1App : Application() {
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

            // ✅ Inicializar Retrofit con detección automática de IP
            android.util.Log.d("Proyecto1App", "🔍 Iniciando detección automática de API...")
            RetrofitClient.initialize(this)
            android.util.Log.d("Proyecto1App", "✅ API configurada: ${RetrofitClient.getCurrentBaseUrl()}")

            // ✅ Inicializar también el RetrofitClient de la carpeta api
            com.example.proyecto1.data.remote.api.RetrofitClient.initialize(this)
            android.util.Log.d("Proyecto1App", "✅ API (api package) configurada: ${com.example.proyecto1.data.remote.api.RetrofitClient.getCurrentBaseUrl()}")

        } catch (e: Exception) {
            // Log del error en caso de fallo de inicialización
            android.util.Log.e("Proyecto1App", "Error al inicializar Firebase", e)
        }
    }
}
