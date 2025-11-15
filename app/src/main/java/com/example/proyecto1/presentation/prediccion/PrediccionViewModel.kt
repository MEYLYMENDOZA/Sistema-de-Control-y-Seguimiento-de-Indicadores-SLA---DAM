package com.example.proyecto1.presentation.prediccion

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto1.domain.repository.PrediccionRepository
import com.example.proyecto1.data.remote.FirestoreSeeder
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class SlaDataPoint(
    val mes: String,
    val valor: Double,
    val orden: Int
)

data class EstadisticasSla(
    val mejorMes: Pair<String, Double>,
    val peorMes: Pair<String, Double>,
    val promedio: Double,
    val tendencia: String // "POSITIVA", "NEGATIVA", "ESTABLE"
)

class PrediccionViewModel : ViewModel() {

    private val repo = PrediccionRepository()
    private val db = FirebaseFirestore.getInstance()

    private val _prediccion = MutableStateFlow<Double?>(null)
    val prediccion: StateFlow<Double?> get() = _prediccion

    private val _slope = MutableStateFlow<Double?>(null)
    val slope: StateFlow<Double?> get() = _slope

    private val _intercept = MutableStateFlow<Double?>(null)
    val intercept: StateFlow<Double?> get() = _intercept

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> get() = _cargando

    private val _datosHistoricos = MutableStateFlow<List<SlaDataPoint>>(emptyList())
    val datosHistoricos: StateFlow<List<SlaDataPoint>> get() = _datosHistoricos

    private val _estadisticas = MutableStateFlow<EstadisticasSla?>(null)
    val estadisticas: StateFlow<EstadisticasSla?> get() = _estadisticas

    private val _mostrarAdvertencia = MutableStateFlow(false)
    val mostrarAdvertencia: StateFlow<Boolean> get() = _mostrarAdvertencia

    private val UMBRAL_MINIMO = 85.0 // SLA mínimo aceptable

    fun cargarYPredecir() {
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null

            try {
                Log.d("PrediccionViewModel", "Iniciando carga de datos y predicción...")
                FirestoreSeeder.seedIfEmpty(db)
                Log.d("PrediccionViewModel", "Seeder ejecutado correctamente")
            } catch (e: Exception) {
                Log.e("PrediccionViewModel", "Error en seeder (ignorado)", e)
            }

            try {
                // Cargar datos históricos
                cargarDatosHistoricos()

                // Calcular predicción
                val (p, m, b) = repo.calcularPrediccion()
                _prediccion.value = p
                _slope.value = m
                _intercept.value = b
                _error.value = null

                // Verificar advertencia
                _mostrarAdvertencia.value = p < UMBRAL_MINIMO

                Log.d("PrediccionViewModel", "Predicción calculada: $p, slope: $m, intercept: $b")
            } catch (e: Exception) {
                _error.value = e.message
                Log.e("PrediccionViewModel", "Error al calcular predicción: ${e.message}", e)
            } finally {
                _cargando.value = false
            }
        }
    }

    private suspend fun cargarDatosHistoricos() {
        try {
            val snapshot = db.collection("sla_historico")
                .get()
                .await()

            val datos = snapshot.documents.mapNotNull { doc ->
                val mes = doc.getString("mes") ?: return@mapNotNull null
                val porcentaje = doc.getDouble("porcentajeSla") ?: return@mapNotNull null
                val orden = doc.getLong("orden")?.toInt() ?: 0
                SlaDataPoint(mes, porcentaje, orden)
            }.sortedBy { it.orden }

            _datosHistoricos.value = datos

            // Calcular estadísticas
            if (datos.isNotEmpty()) {
                val valores = datos.map { it.valor }
                val mejor = datos.maxByOrNull { it.valor }!!
                val peor = datos.minByOrNull { it.valor }!!
                val promedio = valores.average()

                // Determinar tendencia
                val primerasMitad = valores.take(valores.size / 2).average()
                val segundaMitad = valores.takeLast(valores.size / 2).average()
                val tendencia = when {
                    segundaMitad > primerasMitad + 1 -> "POSITIVA"
                    segundaMitad < primerasMitad - 1 -> "NEGATIVA"
                    else -> "ESTABLE"
                }

                _estadisticas.value = EstadisticasSla(
                    mejorMes = Pair(mejor.mes, mejor.valor),
                    peorMes = Pair(peor.mes, peor.valor),
                    promedio = promedio,
                    tendencia = tendencia
                )
            }
        } catch (e: Exception) {
            Log.e("PrediccionViewModel", "Error cargando datos históricos", e)
        }
    }

    fun exportarResultado() {
        // TODO: Implementar exportación a PDF/Excel
        Log.d("PrediccionViewModel", "Exportar resultado solicitado")
    }
}
