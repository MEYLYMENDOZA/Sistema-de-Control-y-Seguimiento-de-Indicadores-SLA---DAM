package com.example.proyecto1.domain.repository

import com.example.proyecto1.domain.math.LinearRegression
import com.example.proyecto1.domain.model.SlaHistory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PrediccionRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun Number?.toDoubleSafe(): Double = when (this) {
        is Double -> this
        is Float -> this.toDouble()
        is Long -> this.toDouble()
        is Int -> this.toDouble()
        else -> 0.0
    }

    suspend fun calcularPrediccion(): Triple<Double, Double, Double> {

        val snapshot = db.collection("sla_historico")
            .get()
            .await()

        if (snapshot.isEmpty) {
            throw Exception("No hay datos suficientes.")
        }

        val docs = snapshot.documents.sortedBy {
            // Orden por un campo "orden" o por mes si existe
            (it.getLong("orden") ?: it.getString("mes")?.toLongOrNull() ?: 0L)
        }

        val history = docs.mapIndexed { index, doc ->
            val raw = doc.get("porcentajeSla")
            val slaVal = if (raw is Number) raw.toDouble() else (raw as? String)?.toDoubleOrNull() ?: 0.0
            SlaHistory(
                monthIndex = index + 1,
                slaPercentage = slaVal
            )
        }

        if (history.size < 3)
            throw Exception("Mínimo 3 meses requeridos.")

        val x = history.map { it.monthIndex.toDouble() }.toDoubleArray()
        val y = history.map { it.slaPercentage }.toDoubleArray()

        val model = LinearRegression(x, y)
        val next = x.max() + 1
        val pred = model.predict(next)

        return Triple(pred, model.slope, model.intercept)
    }
}
