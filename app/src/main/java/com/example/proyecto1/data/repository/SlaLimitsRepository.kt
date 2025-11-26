package com.example.proyecto1.data.repository

import com.example.proyecto1.data.model.SlaLimits
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para gestionar los límites de SLA en Firestore.
 */
class SlaLimitsRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collectionRef = db.collection("configuracion_sistema")

    /**
     * Lee el primer (y único) documento de la colección de configuración.
     */
    suspend fun fetchSlaLimits(): SlaLimits {
        // Obtenemos el primer documento de la colección, sea cual sea su ID.
        val snapshot = collectionRef.limit(1).get().await()
        if (snapshot.isEmpty) {
            return SlaLimits() // Devuelve default si la colección está vacía
        }
        return snapshot.documents.first().toObject(SlaLimits::class.java) ?: SlaLimits()
    }

    /**
     * Actualiza el primer (y único) documento de la colección.
     */
    suspend fun updateSlaLimits(newLimits: SlaLimits) {
        val snapshot = collectionRef.limit(1).get().await()
        if (!snapshot.isEmpty) {
            val docId = snapshot.documents.first().id
            collectionRef.document(docId).set(newLimits).await()
        } else {
            // Opcional: si no existe, crea uno nuevo con ID autogenerado
            collectionRef.add(newLimits).await()
        }
    }
}