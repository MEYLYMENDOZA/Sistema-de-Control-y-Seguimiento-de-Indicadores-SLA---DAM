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

    // Función auxiliar para convertir distintos tipos numéricos a Double de forma segura
    private fun Any?.toDoubleSafe(): Double = when (this) {
        is Double -> this
        is Float -> this.toDouble()
        is Long -> this.toDouble()
        is Int -> this.toDouble()
        is Number -> this.toDouble()
        is String -> this.toDoubleOrNull() ?: 0.0
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

            // Ordenamos por un campo 'orden' si existe, si no por 'mes' (esperando formato numérico o yyyy-MM),
            // si no existe ninguno, mantenemos orden por id o por el orden devuelto.
            val docs = snapshot.documents.sortedBy { doc ->
                val orden = doc.get("orden")
                if (orden != null) {
                    orden.toDoubleSafe()
                } else {
                    val mes = doc.get("mes")
                    // Si mes es yyyy-MM, extraemos el año y mes como número: yyyy*12 + mm
                    when (mes) {
                        is String -> {
                            val parts = mes.split("-")
                            if (parts.size >= 2) {
                                val y = parts[0].toIntOrNull() ?: 0
                                val m = parts[1].toIntOrNull() ?: 0
                                (y * 12 + m).toDouble()
                            } else {
                                mes.toDoubleOrNull() ?: 0.0
                            }
                        }
                        else -> mes?.toString()?.toDoubleOrNull() ?: 0.0
                    }
                }
            }

            val history = docs.mapIndexed { index, doc ->
                val raw = doc.get("porcentajeSla") ?: doc.get("porcentaje_sla") ?: doc.get("sla")
                val slaVal = raw.toDoubleSafe()

                Log.d(TAG, "Mes ${index + 1}: SLA = $slaVal% (docId=${doc.id})")

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
            val next = (x.maxOrNull() ?: 0.0) + 1.0
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
