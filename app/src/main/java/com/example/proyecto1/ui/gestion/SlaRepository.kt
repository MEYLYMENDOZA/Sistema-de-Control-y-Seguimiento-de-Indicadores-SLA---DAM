package com.example.proyecto1.ui.gestion

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

// Repositorio para manejar todas las operaciones de datos SLA con Firebase.
class SlaRepository {

    private val db = Firebase.firestore
    private val slaCollection = db.collection("sla_records")

    // Obtiene los registros en tiempo real usando callbackFlow.
    fun getSlaRecords(): Flow<List<SlaRecord>> = callbackFlow {
        val listenerRegistration: ListenerRegistration = slaCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("Error al escuchar cambios: $error")
                close(error) // Cierra el flow con el error
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val records = snapshot.documents.mapNotNull {
                    try {
                        it.toObject<SlaRecord>()?.copy(id = it.id)
                    } catch (e: Exception) {
                        println("Error al parsear documento ${it.id}: $e")
                        null
                    }
                }
                // --- CORRECCIÓN DEFINITIVA ---
                // Se usa `channel.trySend` para ser explícitos y evitar el error de resolución de contexto.
                channel.trySend(records)
            }
        }
        // Se asegura de que el listener de Firebase se elimine cuando el Flow ya no se necesite.
        awaitClose { listenerRegistration.remove() }
    }

    // Sube una lista de registros a Firebase en un batch.
    suspend fun uploadSlaRecords(records: List<SlaRecord>) {
        val batch = db.batch()
        records.forEach { record ->
            val docRef = slaCollection.document()
            batch.set(docRef, record)
        }
        batch.commit().await()
    }

    // Actualiza un único registro.
    suspend fun updateSlaRecord(record: SlaRecord) {
        slaCollection.document(record.id).set(record).await()
    }

    // Borra una lista de registros por sus IDs.
    suspend fun deleteSlaRecords(recordIds: List<String>) {
        val batch = db.batch()
        recordIds.forEach { id ->
            batch.delete(slaCollection.document(id))
        }
        batch.commit().await()
    }
    
    // Borra todos los registros de la colección.
    suspend fun deleteAllRecords() {
        val allRecords = slaCollection.get().await()
        val batch = db.batch()
        allRecords.documents.forEach { 
            batch.delete(it.reference)
        }
        batch.commit().await()
    }
}
