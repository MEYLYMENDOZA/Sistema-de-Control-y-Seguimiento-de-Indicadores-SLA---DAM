package com.example.proyecto1.domain.repository

import android.util.Log
import com.example.proyecto1.domain.math.LinearRegression
import com.example.proyecto1.domain.model.SlaHistory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PrediccionRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val TAG = "PrediccionRepository"

    private fun Number?.toDoubleSafe(): Double = when (this) {
        is Double -> this
        is Float -> this.toDouble()
        is Long -> this.toDouble()
        is Int -> this.toDouble()
        else -> 0.0
    }

    suspend fun calcularPrediccion(): Triple<Double, Double, Double> {
        try {
            Log.d(TAG, "Obteniendo datos de sla_historico...")

            val snapshot = db.collection("sla_historico")
                .get()
                .await()

            if (snapshot.isEmpty) {
                Log.e(TAG, "No hay datos en sla_historico")
                throw Exception("No hay datos suficientes en la colección sla_historico.")
            }

            Log.d(TAG, "Documentos encontrados: ${snapshot.size()}")

            val docs = snapshot.documents.sortedBy {
                // Orden por un campo "orden" o por mes si existe
                (it.getLong("orden") ?: it.getString("mes")?.toLongOrNull() ?: 0L)
            }

            val history = docs.mapIndexed { index, doc ->
                val raw = doc.get("porcentajeSla")
                val slaVal = if (raw is Number) raw.toDouble() else (raw as? String)?.toDoubleOrNull() ?: 0.0

                Log.d(TAG, "Mes ${index + 1}: SLA = $slaVal%")

                SlaHistory(
                    monthIndex = index + 1,
                    slaPercentage = slaVal
                )
            }

            if (history.size < 3) {
                Log.e(TAG, "Insuficientes datos: ${history.size} meses (mínimo 3)")
                throw Exception("Se requieren al menos 3 meses de datos históricos. Actualmente hay ${history.size}.")
            }

            val x = history.map { it.monthIndex.toDouble() }.toDoubleArray()
            val y = history.map { it.slaPercentage }.toDoubleArray()

            Log.d(TAG, "Calculando regresión lineal...")
            val model = LinearRegression(x, y)
            val next = x.max() + 1
            val pred = model.predict(next)

            Log.d(TAG, "Predicción para mes ${next.toInt()}: $pred%")
            Log.d(TAG, "Pendiente: ${model.slope}, Intercepto: ${model.intercept}")

            return Triple(pred, model.slope, model.intercept)
        } catch (e: Exception) {
            Log.e(TAG, "Error en calcularPrediccion", e)
            throw e
        }
    }
}
