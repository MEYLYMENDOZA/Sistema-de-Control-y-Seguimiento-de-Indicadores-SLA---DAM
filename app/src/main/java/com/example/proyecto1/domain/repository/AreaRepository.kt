package com.example.proyecto1.domain.repository

import android.util.Log
import com.example.proyecto1.domain.model.Area
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AreaRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val TAG = "AreaRepository"
    private val collection = db.collection("areas")

    suspend fun getAll(): List<Area> {
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { doc ->
                Area(
                    id = doc.id,
                    nombreArea = doc.getString("nombre_area") ?: "",
                    descripcion = doc.getString("descripcion") ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo áreas", e)
            emptyList()
        }
    }

    suspend fun getById(id: String): Area? {
        return try {
            val doc = collection.document(id).get().await()
            if (doc.exists()) {
                Area(
                    id = doc.id,
                    nombreArea = doc.getString("nombre_area") ?: "",
                    descripcion = doc.getString("descripcion") ?: ""
                )
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo área por ID", e)
            null
        }
    }

    suspend fun create(area: Area): String? {
        return try {
            val data = mapOf(
                "nombre_area" to area.nombreArea,
                "descripcion" to area.descripcion
            )
            val doc = collection.add(data).await()
            doc.id
        } catch (e: Exception) {
            Log.e(TAG, "Error creando área", e)
            null
        }
    }

    suspend fun update(id: String, area: Area): Boolean {
        return try {
            val data = mapOf(
                "nombre_area" to area.nombreArea,
                "descripcion" to area.descripcion
            )
            collection.document(id).update(data).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando área", e)
            false
        }
    }

    suspend fun delete(id: String): Boolean {
        return try {
            collection.document(id).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando área", e)
            false
        }
    }
}

