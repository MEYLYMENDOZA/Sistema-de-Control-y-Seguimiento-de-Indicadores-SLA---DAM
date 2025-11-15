# Configuración de Firebase para Proyecto1

Este documento describe los pasos para integrar Firebase (Firestore) en el proyecto "Proyecto1" y cómo cargar datos iniciales (seed) usando el `FirestoreSeeder` incluido.

## Requisitos previos
- Cuenta de Firebase y proyecto creado en https://console.firebase.google.com
- Acceso para crear aplicaciones Android en ese proyecto
- Android Studio con Kotlin 2.0
- Dispositivo o emulador con Android 8.0 (API 26) o superior

## Pasos para configurar Firebase en este proyecto

### 1) Crear un proyecto en Firebase
- Ve a Firebase Console y crea un nuevo proyecto o utiliza uno existente.

### 2) Registrar la app Android
- En Firebase Console → Proyecto → Añadir app → Android
- Package name: `com.example.proyecto1` (o el que aparezca en `app/build.gradle.kts`)
- Nickname (opcional)
- SHA-1 (opcional, para auth)
- Descargar `google-services.json` al final del asistente.

### 3) Añadir `google-services.json` al proyecto
- Copia el archivo `google-services.json` dentro de: `app/` (ruta: `app/google-services.json`).
- **IMPORTANTE**: No subas este archivo a repositorios públicos.

### 4) Rebuild / Sync en Android Studio
- En Android Studio, selecciona "Sync Project with Gradle Files" y luego "Rebuild Project".
- Alternativamente, desde consola: `./gradlew --refresh-dependencies clean assembleDebug`

### 5) Activar Firestore (Cloud Firestore)
- En Firebase Console, selecciona Firestore Database → Crear base de datos (en modo de pruebas o producción según prefieras).
- Si inicias en modo pruebas, recuerda configurar reglas antes de publicar.

### 6) Reglas recomendadas para desarrollo (modo pruebas, restringe en producción)
```
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true; // sólo para testing
    }
  }
}
```

**Para reglas de producción, consulta el archivo `FIRESTORE_RULES.md`**

### 7) Estructura de colecciones implementada

El proyecto incluye las siguientes colecciones Firestore que mapean el esquema SQL:

#### Catálogos
- `tipo_solicitud_catalogo` - Tipos de solicitudes (NUEVO_INGRESO, BAJA, MODIFICACION)
- `estado_usuario_catalogo` - Estados de usuario (ACTIVO, INACTIVO, SUSPENDIDO)
- `estado_solicitud_catalogo` - Estados de solicitudes (PENDIENTE, EN_PROCESO, COMPLETADA, RECHAZADA)
- `estado_alerta_catalogo` - Estados de alertas (NUEVA, LEIDA, RESUELTA)
- `tipo_alerta_catalogo` - Tipos de alertas (SLA_PROXIMO_VENCER, SLA_VENCIDO, SOLICITUD_CRITICA)

#### Configuración y RBAC
- `permiso` - Permisos del sistema
- `roles_sistema` - Roles del sistema (ADMIN, GESTOR, USUARIO)
- `rol_registro` - Roles de registro personalizados

#### Entidades principales
- `areas` - Áreas organizacionales (Recursos Humanos, Tecnología, etc.)
- `usuarios` - Usuarios del sistema (usa Firebase Auth UID, no guarda passwords)
- `personal` - Información del personal
- `config_sla` - Configuración de SLAs
- `solicitud` - Solicitudes con timestamps y referencias
- `sla_historico` - Historial de cumplimiento SLA (usado para predicciones)
- `reporte` - Reportes generados
- `alerta` - Alertas del sistema

### 8) Repositorios disponibles

El proyecto incluye repositorios CRUD completos para:

- **AreaRepository** - CRUD completo para áreas
- **UsuarioRepository** - Gestión de usuarios con búsqueda por Firebase UID
- **SolicitudRepository** - CRUD de solicitudes con filtros por área y estado
- **PrediccionRepository** - Cálculo de predicciones SLA usando regresión lineal

Ejemplo de uso:
```kotlin
// En un ViewModel
class MiViewModel : ViewModel() {
    private val areaRepo = AreaRepository()
    
    fun cargarAreas() {
        viewModelScope.launch {
            val areas = areaRepo.getAll()
            // usar las áreas
        }
    }
}
```

### 9) Cargar datos de prueba

El proyecto incluye `FirestoreSeeder.seedIfEmpty()` que inserta:
- 3 tipos de solicitudes
- 3 estados de usuario
- 4 estados de solicitud
- 3 estados de alerta
- 3 tipos de alerta
- 6 permisos básicos
- 3 roles de sistema (Admin, Gestor, Usuario)
- 2 roles de registro
- 4 áreas organizacionales
- 6 meses de historial SLA

**El seeder se ejecuta automáticamente** cuando inicias la app por primera vez. Verifica los logs en Logcat para confirmar:
```
PrediccionViewModel: Iniciando carga de datos y predicción...
FirestoreSeeder: Iniciando seed de datos...
FirestoreSeeder: Seed completado exitosamente
```

### 10) Probar la pantalla de predicción

- Ejecuta la app
- La pantalla principal carga automáticamente datos y calcula la predicción SLA
- `PrediccionRepository` lee de `sla_historico` y calcula la predicción usando regresión lineal
- Los resultados se muestran con la predicción, pendiente e intercepto

### 11) Verificar datos en Firebase Console

- Ve a Firebase Console → Firestore Database
- Deberías ver todas las colecciones creadas con sus datos
- Verifica especialmente `sla_historico` que debe tener 6 documentos con campos:
  - `mes`: "2024-01", etc.
  - `totalSolicitudes`: número
  - `cumplidas`: número
  - `noCumplidas`: número
  - `porcentajeSla`: decimal
  - `orden`: número

## Características implementadas

✅ Firebase Firestore inicializado con persistencia local
✅ Seeder automático que verifica si ya hay datos antes de insertar
✅ Logging completo para debugging (tag: PrediccionViewModel, FirestoreSeeder, etc.)
✅ Manejo robusto de errores con try-catch y logs
✅ Modelos de datos para todas las entidades principales
✅ Repositorios CRUD para Area, Usuario, Solicitud
✅ Repository pattern para separar lógica de datos
✅ Uso de Kotlin Coroutines para operaciones asíncronas
✅ Compatibilidad con Kotlin 2.0 y Jetpack Compose

## Notas de seguridad

- **NO** subas `google-services.json` a repositorios públicos
- Para producción, usa Firestore rules seguras (ver `FIRESTORE_RULES.md`)
- Firebase Auth debe configurarse para gestionar autenticación de usuarios
- Los passwords NO se guardan en Firestore, usa Firebase Authentication
- El campo `firebaseUid` en usuarios referencia al UID de Firebase Auth

## Troubleshooting

### Error: "No hay datos suficientes"
- Verifica que el seeder se haya ejecutado correctamente
- Revisa Logcat para ver los logs de FirestoreSeeder
- Verifica en Firebase Console que la colección `sla_historico` tiene datos

### Error de permisos en Firestore
- Verifica que las reglas de Firestore permitan lectura/escritura
- En desarrollo usa las reglas abiertas (allow read, write: if true)
- En producción configura reglas adecuadas

### Error al inicializar Firebase
- Verifica que `google-services.json` esté en `app/`
- Sincroniza Gradle y reconstruye el proyecto
- Limpia y reconstruye: Build → Clean Project → Rebuild Project

## Próximos pasos sugeridos

1. Implementar Firebase Authentication para login de usuarios
2. Crear pantallas CRUD para solicitudes, áreas, usuarios
3. Implementar sistema de alertas en tiempo real usando Firestore listeners
4. Crear dashboard con gráficos del historial SLA
5. Implementar generación de reportes
6. Configurar reglas de seguridad para producción
7. Implementar notificaciones push con Firebase Cloud Messaging
8. Añadir validaciones de datos en los repositorios

## Estructura del proyecto

```
app/src/main/java/com/example/proyecto1/
├── data/
│   └── remote/
│       ├── dto/
│       │   └── SlaHistoricoDto.kt
│       └── FirestoreSeeder.kt
├── domain/
│   ├── model/
│   │   ├── Area.kt
│   │   ├── SlaHistory.kt
│   │   ├── Solicitud.kt
│   │   ├── TipoSolicitud.kt
│   │   └── Usuario.kt
│   └── repository/
│       ├── AreaRepository.kt
│       ├── PrediccionRepository.kt
│       ├── SolicitudRepository.kt
│       └── UsuarioRepository.kt
├── presentation/
│   └── prediccion/
│       ├── PrediccionScreen.kt
│       └── PrediccionViewModel.kt
├── MainActivity.kt
└── Proyecto1App.kt (Application con init de Firebase)
```

Si necesitas ayuda adicional o quieres implementar funcionalidades adicionales, consulta la documentación de Firebase o revisa los comentarios en el código.
