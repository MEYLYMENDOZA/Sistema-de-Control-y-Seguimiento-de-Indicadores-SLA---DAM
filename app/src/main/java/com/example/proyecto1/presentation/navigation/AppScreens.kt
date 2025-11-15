package com.example.proyecto1.presentation.navigation

// Esta clase define todas las pantallas de tu app
sealed class AppScreens(val route: String) {
    // Pantalla Principal (el dashboard scrolleable)
    object AlertsDashboard : AppScreens("dashboard_alertas")

    // Tu US-13 (la lista de historial de alertas)
    object AlertsHistory : AppScreens("historial_alertas")

    // Tu US-14 (el historial de emails)
    object EmailHistory : AppScreens("historial_email")

    // Tus otras pantallas del Figma
    object CriticalCasesDetail : AppScreens("detalle_casos_criticos")
    object AlertSettings : AppScreens("configuracion_alertas")
}