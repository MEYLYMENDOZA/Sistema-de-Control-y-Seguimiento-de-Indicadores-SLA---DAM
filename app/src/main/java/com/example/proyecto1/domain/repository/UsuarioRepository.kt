package com.example.proyecto1.domain.repository

import android.util.Log
import com.example.proyecto1.domain.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UsuarioRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val TAG = "UsuarioRepository"
    private val collection = db.collection("usuarios")

    suspend fun getAll(): List<Usuario> {
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { doc ->
                documentToUsuario(doc.id, doc.data ?: emptyMap())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo usuarios", e)
            emptyList()
        }
    }

    suspend fun getById(id: String): Usuario? {
        return try {
            val doc = collection.document(id).get().await()
            if (doc.exists()) {
                documentToUsuario(doc.id, doc.data ?: emptyMap())
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo usuario por ID", e)
            null
        }
    }

    suspend fun getByFirebaseUid(firebaseUid: String): Usuario? {
        return try {
            val snapshot = collection
                .whereEqualTo("firebaseUid", firebaseUid)
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val doc = snapshot.documents.first()
                documentToUsuario(doc.id, doc.data ?: emptyMap())
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo usuario por Firebase UID", e)
            null
        }
    }

    suspend fun getByUsername(username: String): Usuario? {
        return try {
            val snapshot = collection
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val doc = snapshot.documents.first()
                documentToUsuario(doc.id, doc.data ?: emptyMap())
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo usuario por username", e)
            null
        }
    }

    suspend fun create(usuario: Usuario): String? {
        return try {
            val data = usuarioToMap(usuario)
            val doc = collection.add(data).await()
            doc.id
        } catch (e: Exception) {
            Log.e(TAG, "Error creando usuario", e)
            null
        }
    }

    suspend fun update(id: String, usuario: Usuario): Boolean {
        return try {
            val data = usuarioToMap(usuario)
            collection.document(id).update(data).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando usuario", e)
            false
        }
    }

    suspend fun delete(id: String): Boolean {
        return try {
            collection.document(id).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando usuario", e)
            false
        }
    }

    private fun documentToUsuario(id: String, data: Map<String, Any>): Usuario {
        return Usuario(
            id = id,
            username = data["username"] as? String ?: "",
            correo = data["correo"] as? String ?: "",
            idRolSistema = data["idRolSistema"] as? String ?: "",
            idEstadoUsuario = data["idEstadoUsuario"] as? String ?: "",
            firebaseUid = data["firebaseUid"] as? String ?: ""
        )
    }

    private fun usuarioToMap(usuario: Usuario): Map<String, Any> {
        return mapOf(
            "username" to usuario.username,
            "correo" to usuario.correo,
            "idRolSistema" to usuario.idRolSistema,
            "idEstadoUsuario" to usuario.idEstadoUsuario,
            "firebaseUid" to usuario.firebaseUid
        )
    }
}

