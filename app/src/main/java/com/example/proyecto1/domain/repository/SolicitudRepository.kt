package com.example.proyecto1.domain.repository

import android.util.Log
import com.example.proyecto1.domain.model.Solicitud
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class SolicitudRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val TAG = "SolicitudRepository"
    private val collection = db.collection("solicitud")

    suspend fun getAll(limit: Int = 100): List<Solicitud> {
        return try {
            val snapshot = collection
                .orderBy("creadoEn", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                documentToSolicitud(doc.id, doc.data ?: emptyMap())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo solicitudes", e)
            emptyList()
        }
    }

    suspend fun getById(id: String): Solicitud? {
        return try {
            val doc = collection.document(id).get().await()
            if (doc.exists()) {
                documentToSolicitud(doc.id, doc.data ?: emptyMap())
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo solicitud por ID", e)
            null
        }
    }

    suspend fun getByArea(idArea: String): List<Solicitud> {
        return try {
            val snapshot = collection
                .whereEqualTo("idArea", idArea)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                documentToSolicitud(doc.id, doc.data ?: emptyMap())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo solicitudes por área", e)
            emptyList()
        }
    }

    suspend fun getByEstado(idEstado: String): List<Solicitud> {
        return try {
            val snapshot = collection
                .whereEqualTo("idEstadoSolicitud", idEstado)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                documentToSolicitud(doc.id, doc.data ?: emptyMap())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo solicitudes por estado", e)
            emptyList()
        }
    }

    suspend fun create(solicitud: Solicitud): String? {
        return try {
            val data = solicitudToMap(solicitud)
            val doc = collection.add(data).await()
            doc.id
        } catch (e: Exception) {
            Log.e(TAG, "Error creando solicitud", e)
            null
        }
    }

    suspend fun update(id: String, solicitud: Solicitud): Boolean {
        return try {
            val data = solicitudToMap(solicitud)
            collection.document(id).update(data).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando solicitud", e)
            false
        }
    }

    suspend fun delete(id: String): Boolean {
        return try {
            collection.document(id).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando solicitud", e)
            false
        }
    }

    private fun documentToSolicitud(id: String, data: Map<String, Any>): Solicitud {
        return Solicitud(
            id = id,
            idPersonal = data["idPersonal"] as? String ?: "",
            idRolRegistro = data["idRolRegistro"] as? String ?: "",
            idSla = data["idSla"] as? String ?: "",
            idArea = data["idArea"] as? String ?: "",
            idEstadoSolicitud = data["idEstadoSolicitud"] as? String ?: "",
            fechaSolicitud = data["fechaSolicitud"] as? Timestamp,
            fechaIngreso = data["fechaIngreso"] as? Timestamp,
            numDiasSla = (data["numDiasSla"] as? Long)?.toInt() ?: 0,
            resumenSla = data["resumenSla"] as? String ?: "",
            origenDato = data["origenDato"] as? String ?: "",
            creadoPor = data["creadoPor"] as? String ?: "",
            creadoEn = data["creadoEn"] as? Timestamp,
            actualizadoEn = data["actualizadoEn"] as? Timestamp,
            actualizadoPor = data["actualizadoPor"] as? String ?: ""
        )
    }

    private fun solicitudToMap(solicitud: Solicitud): Map<String, Any?> {
        return mapOf(
            "idPersonal" to solicitud.idPersonal,
            "idRolRegistro" to solicitud.idRolRegistro,
            "idSla" to solicitud.idSla,
            "idArea" to solicitud.idArea,
            "idEstadoSolicitud" to solicitud.idEstadoSolicitud,
            "fechaSolicitud" to solicitud.fechaSolicitud,
            "fechaIngreso" to solicitud.fechaIngreso,
            "numDiasSla" to solicitud.numDiasSla,
            "resumenSla" to solicitud.resumenSla,
            "origenDato" to solicitud.origenDato,
            "creadoPor" to solicitud.creadoPor,
            "creadoEn" to (solicitud.creadoEn ?: Timestamp.now()),
            "actualizadoEn" to Timestamp.now(),
            "actualizadoPor" to solicitud.actualizadoPor
        )
    }
}

