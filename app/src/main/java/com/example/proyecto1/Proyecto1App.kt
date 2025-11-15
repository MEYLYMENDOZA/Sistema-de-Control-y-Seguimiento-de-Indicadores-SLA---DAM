package com.example.proyecto1

import android.app.Application
import com.google.firebase.FirebaseApp

class Proyecto1App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializa Firebase con la configuración provista por google-services.json
        FirebaseApp.initializeApp(this)
    }
}

