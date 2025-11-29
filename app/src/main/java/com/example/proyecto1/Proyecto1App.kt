package com.example.proyecto1

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp // <-- ESTA ANOTACIÓN ES LA CLAVE DE TODO
class Proyecto1App : Application() {
    // El cuerpo puede estar vacío. La anotación es lo que importa.
}
