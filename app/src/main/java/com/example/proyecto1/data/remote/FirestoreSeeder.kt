package com.example.proyecto1.data.remote

import android.util.Log
import com.example.proyecto1.data.remote.dto.SlaHistoricoDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

object FirestoreSeeder {

    private const val TAG = "FirestoreSeeder"

    suspend fun seedIfEmpty(db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
        try {
            Log.d(TAG, "Iniciando seed de datos...")

            // Verificar si ya hay datos en sla_historico
            val col = db.collection("sla_historico")
            val snapshot = col.get().await()
            if (!snapshot.isEmpty) {
                Log.d(TAG, "Los datos ya existen, saltando seed")
                return
            }

            Log.d(TAG, "Insertando datos de catálogos...")
            seedCatalogos(db)

            Log.d(TAG, "Insertando datos de configuración RBAC...")
            seedRBAC(db)

            Log.d(TAG, "Insertando áreas...")
            seedAreas(db)

            Log.d(TAG, "Insertando historial SLA...")
            seedSlaHistorico(db)

            Log.d(TAG, "Seed completado exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error durante el seed", e)
            throw e
        }
    }

    private suspend fun seedCatalogos(db: FirebaseFirestore) {
        // Tipos de solicitud
        val tiposSolicitud = listOf(
            mapOf("codigo" to "NUEVO_INGRESO", "descripcion" to "Solicitud de nuevo ingreso"),
            mapOf("codigo" to "BAJA", "descripcion" to "Solicitud de baja"),
            mapOf("codigo" to "MODIFICACION", "descripcion" to "Solicitud de modificación de datos")
        )
        tiposSolicitud.forEach { db.collection("tipo_solicitud_catalogo").add(it).await() }

        // Estados de usuario
        val estadosUsuario = listOf(
            mapOf("codigo" to "ACTIVO", "descripcion" to "Usuario activo"),
            mapOf("codigo" to "INACTIVO", "descripcion" to "Usuario inactivo"),
            mapOf("codigo" to "SUSPENDIDO", "descripcion" to "Usuario suspendido")
        )
        estadosUsuario.forEach { db.collection("estado_usuario_catalogo").add(it).await() }

        // Estados de solicitud
        val estadosSolicitud = listOf(
            mapOf("codigo" to "PENDIENTE", "descripcion" to "Solicitud pendiente"),
            mapOf("codigo" to "EN_PROCESO", "descripcion" to "Solicitud en proceso"),
            mapOf("codigo" to "COMPLETADA", "descripcion" to "Solicitud completada"),
            mapOf("codigo" to "RECHAZADA", "descripcion" to "Solicitud rechazada")
        )
        estadosSolicitud.forEach { db.collection("estado_solicitud_catalogo").add(it).await() }

        // Estados de alerta
        val estadosAlerta = listOf(
            mapOf("codigo" to "NUEVA", "descripcion" to "Alerta nueva"),
            mapOf("codigo" to "LEIDA", "descripcion" to "Alerta leída"),
            mapOf("codigo" to "RESUELTA", "descripcion" to "Alerta resuelta")
        )
        estadosAlerta.forEach { db.collection("estado_alerta_catalogo").add(it).await() }

        // Tipos de alerta
        val tiposAlerta = listOf(
            mapOf("codigo" to "SLA_PROXIMO_VENCER", "descripcion" to "SLA próximo a vencer"),
            mapOf("codigo" to "SLA_VENCIDO", "descripcion" to "SLA vencido"),
            mapOf("codigo" to "SOLICITUD_CRITICA", "descripcion" to "Solicitud crítica")
        )
        tiposAlerta.forEach { db.collection("tipo_alerta_catalogo").add(it).await() }
    }

    private suspend fun seedRBAC(db: FirebaseFirestore) {
        // Permisos
        val permisos = listOf(
            mapOf("codigo" to "CREAR_SOLICITUD", "descripcion" to "Crear solicitudes", "nombre" to "Crear Solicitud"),
            mapOf("codigo" to "VER_SOLICITUD", "descripcion" to "Ver solicitudes", "nombre" to "Ver Solicitud"),
            mapOf("codigo" to "EDITAR_SOLICITUD", "descripcion" to "Editar solicitudes", "nombre" to "Editar Solicitud"),
            mapOf("codigo" to "ELIMINAR_SOLICITUD", "descripcion" to "Eliminar solicitudes", "nombre" to "Eliminar Solicitud"),
            mapOf("codigo" to "GESTIONAR_USUARIOS", "descripcion" to "Gestionar usuarios", "nombre" to "Gestionar Usuarios"),
            mapOf("codigo" to "VER_REPORTES", "descripcion" to "Ver reportes", "nombre" to "Ver Reportes")
        )
        val permisosIds = mutableMapOf<String, String>()
        permisos.forEach {
            val doc = db.collection("permiso").add(it).await()
            permisosIds[it["codigo"] as String] = doc.id
        }

        // Roles del sistema
        val roles = listOf(
            mapOf(
                "codigo" to "ADMIN",
                "descripcion" to "Administrador del sistema",
                "es_activo" to true,
                "nombre" to "Administrador"
            ),
            mapOf(
                "codigo" to "GESTOR",
                "descripcion" to "Gestor de solicitudes",
                "es_activo" to true,
                "nombre" to "Gestor"
            ),
            mapOf(
                "codigo" to "USUARIO",
                "descripcion" to "Usuario estándar",
                "es_activo" to true,
                "nombre" to "Usuario"
            )
        )
        val rolesIds = mutableMapOf<String, String>()
        roles.forEach {
            val doc = db.collection("roles_sistema").add(it).await()
            rolesIds[it["codigo"] as String] = doc.id
        }

        // Roles de registro
        val rolesRegistro = listOf(
            mapOf(
                "nombre_rol" to "Desarrollador",
                "descripcion" to "Rol para desarrolladores",
                "bloque_tech" to "IT",
                "es_activo" to true
            ),
            mapOf(
                "nombre_rol" to "Analista",
                "descripcion" to "Rol para analistas",
                "bloque_tech" to "Business",
                "es_activo" to true
            )
        )
        rolesRegistro.forEach { db.collection("rol_registro").add(it).await() }
    }

    private suspend fun seedAreas(db: FirebaseFirestore) {
        val areas = listOf(
            mapOf("nombre_area" to "Recursos Humanos", "descripcion" to "Área de gestión de personal"),
            mapOf("nombre_area" to "Tecnología", "descripcion" to "Área de tecnología e innovación"),
            mapOf("nombre_area" to "Finanzas", "descripcion" to "Área financiera y contable"),
            mapOf("nombre_area" to "Operaciones", "descripcion" to "Área de operaciones")
        )
        areas.forEach { db.collection("areas").add(it).await() }
    }

    private suspend fun seedSlaHistorico(db: FirebaseFirestore) {
        val sample = listOf(
            SlaHistoricoDto("2024-01", 100, 95, 5, 95.0),
            SlaHistoricoDto("2024-02", 120, 114, 6, 95.0),
            SlaHistoricoDto("2024-03", 110, 104, 6, 94.54),
            SlaHistoricoDto("2024-04", 130, 125, 5, 96.15),
            SlaHistoricoDto("2024-05", 115, 110, 5, 95.65),
            SlaHistoricoDto("2024-06", 125, 119, 6, 95.20)
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
            db.collection("sla_historico").add(map).await()
        }
    }
}

