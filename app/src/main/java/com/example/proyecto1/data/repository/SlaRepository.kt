package com.example.proyecto1.data.repository

import com.example.proyecto1.data.model.SlaTicket
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para gestionar los tickets de SLA en Firestore.
 */
class SlaRepository {
    private val db = FirebaseFirestore.getInstance()
    private val ticketsCollection = db.collection("sla_tickets")

    /**
     * Obtiene todos los tickets de SLA de Firestore.
     */
    suspend fun getAllTickets(): List<SlaTicket> {
        return try {
            ticketsCollection.get().await().map { document ->
                document.toObject(SlaTicket::class.java)
            }
        } catch (e: Exception) {
            // En un caso real, aquí se debería registrar el error
            emptyList()
        }
    }
}