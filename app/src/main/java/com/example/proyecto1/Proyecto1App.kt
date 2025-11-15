package com.example.proyecto1

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class Proyecto1App : Application() {
    override fun onCreate() {
        super.onCreate()

        try {
            // Inicializa Firebase con la configuración provista por google-services.json
            FirebaseApp.initializeApp(this)

            // Configurar Firestore con persistencia local habilitada
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

            FirebaseFirestore.getInstance().firestoreSettings = settings
        } catch (e: Exception) {
            // Log del error en caso de fallo de inicialización
            android.util.Log.e("Proyecto1App", "Error al inicializar Firebase", e)
        }
    }
}

