package com.example.proyecto1.data.repository

import com.example.proyecto1.data.model.SlaHistorico
import com.example.proyecto1.data.model.Solicitud
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

// --- Clases para empaquetar los resultados de los KPIs ---
data class KpiResult(
    val totalCasosCerrados: Int,
    val casosCumplen: Int,
    val casosNoCumplen: Int,
    val promedioDiasResolucion: Double,
    val cumplimientoPorRol: Map<String, Double>,
    val cumplimientoPorTipoSla: Map<String, Double>
)

data class SlaHistoricoResult(
    val historico: List<SlaHistorico>
)

/**
 * Repositorio para obtener y procesar datos del dashboard de SLA desde Firestore.
 */
class SlaDashboardRepository {

    private val db = FirebaseFirestore.getInstance()
    private val solicitudCollection = db.collection("solicitud")
    private val slaHistoricoCollection = db.collection("sla_historico")

    /**
     * Obtiene y calcula los principales KPIs de SLA a partir de la colección 'solicitud'.
     *
     * @return Un objeto [KpiResult] con todas las métricas calculadas.
     * @throws Exception Si ocurre un error durante la consulta a Firestore.
     */
    suspend fun fetchSlaKpis(): KpiResult {
        // 1. Consultar todos los documentos de la colección 'solicitud'
        val snapshot = solicitudCollection.get().await()
        val solicitudes = snapshot.toObjects(Solicitud::class.java)

        // 2. Realizar agregaciones en memoria
        val casosCerrados = solicitudes.filter { it.fecha_cierre != null } // Asumimos que un caso cerrado tiene fecha de cierre
        val totalCasosCerrados = casosCerrados.size
        val casosCumplen = casosCerrados.count { it.cumple_sla }
        val casosNoCumplen = totalCasosCerrados - casosCumplen

        val promedioDiasResolucion = if (casosCerrados.isNotEmpty()) {
            casosCerrados.map { it.dias_resolucion }.average()
        } else {
            0.0
        }

        // 3. Agrupar por rol_asignado para calcular el % de cumplimiento
        val cumplimientoPorRol = casosCerrados
            .groupBy { it.rol_asignado }
            .mapValues { (_, group) ->
                val total = group.size
                val cumplen = group.count { it.cumple_sla }
                if (total > 0) (cumplen.toDouble() / total) * 100 else 0.0
            }

        // 4. Agrupar por tipo_sla para calcular el % de cumplimiento
        val cumplimientoPorTipoSla = casosCerrados
            .groupBy { it.tipo_sla }
            .mapValues { (_, group) ->
                val total = group.size
                val cumplen = group.count { it.cumple_sla }
                if (total > 0) (cumplen.toDouble() / total) * 100 else 0.0
            }

        return KpiResult(
            totalCasosCerrados = totalCasosCerrados,
            casosCumplen = casosCumplen,
            casosNoCumplen = casosNoCumplen,
            promedioDiasResolucion = promedioDiasResolucion,
            cumplimientoPorRol = cumplimientoPorRol,
            cumplimientoPorTipoSla = cumplimientoPorTipoSla
        )
    }

    /**
     * Obtiene el historial de SLA ordenado por mes.
     *
     * @return Un objeto [SlaHistoricoResult] con la lista de datos históricos.
     * @throws Exception Si ocurre un error durante la consulta a Firestore.
     */
    suspend fun fetchSlaHistorico(): SlaHistoricoResult {
        val snapshot = slaHistoricoCollection
            .orderBy("mes", Query.Direction.ASCENDING)
            .get()
            .await()

        val historico = snapshot.toObjects(SlaHistorico::class.java)
        return SlaHistoricoResult(historico)
    }
}
