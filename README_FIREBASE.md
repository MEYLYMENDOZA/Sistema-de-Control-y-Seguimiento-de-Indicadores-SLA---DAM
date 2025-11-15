# Configuración de Firebase para Proyecto1

Este documento describe los pasos para integrar Firebase (Firestore) en el proyecto "Proyecto1" y cómo cargar datos iniciales (seed) usando el `FirestoreSeeder` incluido.

Requisitos previos
- Cuenta de Firebase y proyecto creado en https://console.firebase.google.com
- Acceso para crear aplicaciones Android en ese proyecto

Pasos para configurar Firebase en este proyecto

1) Crear un proyecto en Firebase
- Ve a Firebase Console y crea un nuevo proyecto o utiliza uno existente.

2) Registrar la app Android
- En Firebase Console -> Proyecto -> Añadir app -> Android
- Package name: `com.example.proyecto1` (o el que aparezca en `app/build.gradle.kts`)
- Nickname (opcional)
- SHA-1 (opcional, para auth)
- Descargar `google-services.json` al final del asistente.

3) Añadir `google-services.json` al proyecto
- Copia el archivo `google-services.json` dentro de: `app/` (ruta: `app/google-services.json`).

4) Rebuild / Sync en Android Studio
- En Android Studio, selecciona "Sync Project with Gradle Files" y luego "Rebuild Project".
- Alternativamente, desde consola: `./gradlew --refresh-dependencies clean assembleDebug`

5) Activar Firestore (Cloud Firestore)
- En Firebase Console, selecciona Firestore Database -> Crear base de datos (en modo de pruebas o producción según prefieras).
- Si inicias en modo pruebas, recuerda configurar reglas antes de publicar.

6) Reglas recomendadas para desarrollo (modo pruebas, restringe en producción)
```
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true; // sólo para testing
    }
  }
}
```

7) Estructura de colecciones sugerida (map del SQL original)
- `area` -> collection: `areas` (docs con `nombre_area`, `descripcion`)
- `tipo_solicitud_catalogo` -> `tipo_solicitud_catalogo`
- `estado_usuario_catalogo` -> `estado_usuario_catalogo`
- `estado_solicitud_catalogo` -> `estado_solicitud_catalogo`
- `tipo_alerta_catalogo`, `estado_alerta_catalogo` -> colecciones equivalentes
- `roles_sistema`, `permiso`, `rol_permiso` -> colecciones / subcollections
- `usuario` -> collection `usuarios` (no guardar `password` sin seguridad; usar Firebase Auth en su lugar)
- `personal` -> `personal` (con referencia `id_usuario` a `usuarios`)
- `config_sla` -> `config_sla`
- `solicitud` -> `solicitud` (usa campos tipo timestamp para fechas)
- `sla_historico` -> collection `sla_historico` (usado actualmente por `PrediccionRepository`)

8) Cargar datos de prueba
- El proyecto incluye `FirestoreSeeder.seedIfEmpty()` que inserta documentos ejemplo en `sla_historico`.
- Para ejecutar el seeder, llama a `FirestoreSeeder.seedIfEmpty()` desde un coroutine (por ejemplo, en `MainActivity` o en `PrediccionViewModel`) una sola vez.

9) Probar la pantalla de predicción
- Ejecuta la app; `PrediccionRepository` lee de `sla_historico` y calcula la predicción.

Notas de seguridad
- **No** subas `google-services.json` a repositorios públicos.
- Para producción, usa Firestore rules seguras y Firebase Auth para gestionar usuarios.

Si quieres que ejecute pasos adicionales (por ejemplo, llamar al seeder automáticamente desde el ViewModel, o crear funciones CRUD para las entidades principales), dime cuáles prefieres y lo implemento.

