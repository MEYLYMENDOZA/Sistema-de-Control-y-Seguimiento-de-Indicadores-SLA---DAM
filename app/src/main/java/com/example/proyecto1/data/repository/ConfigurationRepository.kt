package com.example.proyecto1.data.repository

import com.example.proyecto1.data.model.SlaConfig
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para gestionar la configuración de SLA en Firestore.
 */
class ConfigurationRepository {
    private val db = FirebaseFirestore.getInstance()
    private val configDocument = db.collection("config").document("sla")

    /**
     * Guarda la configuración de SLA en Firestore.
     */
    suspend fun saveConfiguration(config: SlaConfig) {
        configDocument.set(config).await()
    }

    /**
     * Obtiene la configuración de SLA de Firestore.
     * Si no hay ninguna configuración guardada, devuelve una configuración por defecto.
     */
    suspend fun getConfiguration(): SlaConfig {
        return try {
            val snapshot = configDocument.get().await()
            snapshot.toObject(SlaConfig::class.java) ?: SlaConfig()
        } catch (e: Exception) {
            // En un caso real, aquí se debería registrar el error
            SlaConfig() // Devuelve una configuración por defecto en caso de error
        }
    }
}