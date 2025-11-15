package com.example.proyecto1.data.remote

import com.example.proyecto1.data.remote.dto.SlaHistoricoDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirestoreSeeder {

    suspend fun seedIfEmpty(db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
        val col = db.collection("sla_historico")
        val snapshot = col.get().await()
        if (!snapshot.isEmpty) return

        val sample = listOf(
            SlaHistoricoDto("2024-01", 100, 95, 5, 95.0),
            SlaHistoricoDto("2024-02", 120, 114, 6, 95.0),
            SlaHistoricoDto("2024-03", 110, 104, 6, 94.54),
            SlaHistoricoDto("2024-04", 130, 125, 5, 96.15)
        )

        sample.forEachIndexed { index, dto ->
            val map = mapOf(
                "mes" to dto.mes,
                "totalSolicitudes" to dto.totalSolicitudes,
                "cumplidas" to dto.cumplidas,
                "noCumplidas" to dto.noCumplidas,
                "porcentajeSla" to dto.porcentajeSla,
                "orden" to index + 1
            )
            col.add(map).await()
        }
    }
}

