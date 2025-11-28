package com.example.proyecto1

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
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

            // La inicialización de Retrofit ahora es gestionada por Hilt y lazy initialization.
            // No se requiere inicialización manual aquí.
            android.util.Log.d("Proyecto1App", "Inicialización de red gestionada por Hilt/Lazy.")

        } catch (e: Exception) {
            // Log del error en caso de fallo de inicialización
            android.util.Log.e("Proyecto1App", "Error en la inicialización", e)
        }
    }
}
