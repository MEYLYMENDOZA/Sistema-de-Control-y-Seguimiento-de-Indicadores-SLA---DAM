# Checklist de Verificación - Sistema de Control con Firebase

## ✅ Verificación Paso a Paso

### 1. Verificar archivos de configuración

#### ✓ Archivo `google-services.json`
- [ ] Verificar que existe en: `app/google-services.json`
- [ ] Abrir el archivo y verificar que tiene el campo `project_id`
- [ ] Confirmar que el `package_name` es `com.example.proyecto1`

#### ✓ Archivo `build.gradle.kts` (raíz)
- [ ] Verificar plugin: `id("com.google.gms.google-services") version "4.4.2" apply false`
- [ ] Verificar Kotlin: `id("org.jetbrains.kotlin.android") version "2.0.0" apply false`

#### ✓ Archivo `app/build.gradle.kts`
- [ ] Verificar plugins aplicados:
  ```kotlin
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
  id("com.google.gms.google-services")
  ```
- [ ] Verificar dependencias Firebase:
  ```kotlin
  implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
  implementation("com.google.firebase:firebase-firestore-ktx")
  implementation("com.google.firebase:firebase-auth-ktx")
  implementation("com.google.firebase:firebase-analytics-ktx")
  ```

### 2. Sincronizar y compilar

#### En Android Studio:
- [ ] File → Sync Project with Gradle Files
- [ ] Esperar a que termine (ver barra de progreso abajo)
- [ ] Build → Clean Project
- [ ] Build → Rebuild Project
- [ ] Verificar que no hay errores en la pestaña "Build"

#### En terminal (opcional):
```powershell
cd "D:\REPOS\Sistema de control"
.\gradlew clean build
```

### 3. Verificar Firebase Console

#### Abrir Firebase Console:
- [ ] Ir a https://console.firebase.google.com
- [ ] Seleccionar tu proyecto
- [ ] Verificar que la app Android está registrada

#### Configurar Firestore:
- [ ] Ir a Firestore Database
- [ ] Si no está creada, crear base de datos
- [ ] Seleccionar región (preferiblemente cercana a tus usuarios)
- [ ] Modo de inicio: "Modo de prueba" (temporalmente)

#### Configurar reglas de seguridad (TEMPORAL - solo para desarrollo):
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```
⚠️ **IMPORTANTE:** Estas reglas son SOLO para desarrollo. Ver `FIRESTORE_RULES.md` para reglas de producción.

### 4. Ejecutar la aplicación

#### Conectar dispositivo o emulador:
- [ ] Dispositivo físico: Habilitar modo desarrollador y depuración USB (Android 8.0 / API 26+)
- [ ] Emulador: Crear AVD con API 26+ (preferible API 33 o 34)

#### Ejecutar app:
- [ ] Run → Run 'app' (o Shift+F10)
- [ ] Seleccionar dispositivo/emulador
- [ ] Esperar a que se instale

### 5. Verificar Logcat

#### Abrir Logcat:
- [ ] View → Tool Windows → Logcat
- [ ] Filtrar por nivel: Verbose o Debug

#### Buscar estos logs (en orden):
```
1. Proyecto1App: Inicializando Firebase
2. MainActivity: Activity creada exitosamente
3. PrediccionViewModel: Iniciando carga de datos y predicción...
4. FirestoreSeeder: Iniciando seed de datos...
5. FirestoreSeeder: Insertando datos de catálogos...
6. FirestoreSeeder: Insertando datos de configuración RBAC...
7. FirestoreSeeder: Insertando áreas...
8. FirestoreSeeder: Insertando historial SLA...
9. FirestoreSeeder: Seed completado exitosamente
10. PrediccionRepository: Obteniendo datos de sla_historico...
11. PrediccionRepository: Documentos encontrados: 6
12. PrediccionRepository: Mes 1: SLA = 95.0%
    ...
13. PrediccionRepository: Calculando regresión lineal...
14. PrediccionRepository: Predicción para mes 7: XX.XX%
```

#### Si ves errores:
- [ ] Buscar líneas con `E/` (Error) en Logcat
- [ ] Copiar el stacktrace completo
- [ ] Verificar la sección de Troubleshooting más abajo

### 6. Verificar datos en Firestore Console

#### En Firebase Console → Firestore Database:
- [ ] Verificar que existen estas colecciones:
  - `tipo_solicitud_catalogo` (3 docs)
  - `estado_usuario_catalogo` (3 docs)
  - `estado_solicitud_catalogo` (4 docs)
  - `estado_alerta_catalogo` (3 docs)
  - `tipo_alerta_catalogo` (3 docs)
  - `permiso` (6 docs)
  - `roles_sistema` (3 docs)
  - `rol_registro` (2 docs)
  - `areas` (4 docs)
  - `sla_historico` (6 docs)

#### Verificar contenido de `sla_historico`:
- [ ] Abrir la colección
- [ ] Verificar que tiene 6 documentos
- [ ] Abrir un documento y verificar campos:
  - `mes`: "2024-01" (o similar)
  - `totalSolicitudes`: número
  - `cumplidas`: número
  - `noCumplidas`: número
  - `porcentajeSla`: decimal (ej: 95.0)
  - `orden`: número (1-6)

### 7. Verificar la pantalla de la app

#### En la app ejecutándose:
- [ ] Debe mostrar la pantalla de predicción
- [ ] Debe mostrar un valor de predicción SLA
- [ ] No debe haber mensajes de error visibles

### 8. Segunda ejecución (verificar que no duplica datos)

#### Cerrar y volver a ejecutar:
- [ ] Detener la app
- [ ] Run → Run 'app' nuevamente
- [ ] En Logcat buscar: `FirestoreSeeder: Los datos ya existen, saltando seed`
- [ ] En Firestore Console verificar que sigue habiendo solo 6 docs en `sla_historico`

## 🐛 Troubleshooting

### Error: "Default FirebaseApp is not initialized"
**Solución:**
1. Verificar que `google-services.json` está en `app/`
2. Verificar que el plugin `com.google.gms.google-services` está aplicado
3. Sync y Rebuild
4. Clean → Rebuild Project

### Error: "PERMISSION_DENIED: Missing or insufficient permissions"
**Solución:**
1. Ir a Firebase Console → Firestore → Rules
2. Cambiar temporalmente a modo de prueba:
   ```javascript
   allow read, write: if true;
   ```
3. Publicar las reglas
4. Volver a ejecutar la app

### Error: "No hay datos suficientes"
**Solución:**
1. Verificar en Logcat si el seeder se ejecutó correctamente
2. Buscar errores en FirestoreSeeder
3. Verificar en Firebase Console que `sla_historico` tiene documentos
4. Si está vacía, borrar la colección y volver a ejecutar la app

### Error de compilación con Kotlin
**Solución:**
1. Verificar versión de Kotlin: 2.0.0
2. Verificar kotlinCompilerExtensionVersion: 1.5.14
3. File → Invalidate Caches / Restart
4. Sync Project with Gradle Files

### La app se cierra inmediatamente (crash)
**Solución:**
1. Abrir Logcat
2. Filtrar por nivel "Error"
3. Buscar el stacktrace completo
4. Verificar la línea exacta del error
5. Común: Verificar permisos de INTERNET en AndroidManifest.xml

### Error: "adaptive-icon elements require a sdk version of at least 26"
**Solución:**
1. Este error ocurre cuando minSdk es menor que 26
2. Ya está corregido: `minSdk = 26` en `app/build.gradle.kts`
3. Si tienes este error, verifica que el minSdk sea 26 o superior
4. Sync Project with Gradle Files
5. Clean y Rebuild Project
6. Nota: Esto significa que la app solo funcionará en Android 8.0 (Oreo) o superior
7. En 2024, más del 95% de dispositivos Android usan API 26+

### Gradle sync falla
**Solución:**
1. Verificar conexión a Internet
2. File → Invalidate Caches / Restart
3. Borrar carpetas `.gradle` y `build` del proyecto
4. Sync nuevamente

## 📊 Métricas de éxito

✅ **Todo funciona correctamente si:**
1. Gradle sync sin errores
2. Build exitoso sin errores de compilación
3. App se instala y ejecuta
4. Logs muestran seed completado
5. Firebase Console muestra 10 colecciones con datos
6. Pantalla de predicción muestra un valor
7. Segunda ejecución no duplica datos

## 📝 Checklist final

- [ ] ✅ `google-services.json` en su lugar
- [ ] ✅ Gradle sync exitoso
- [ ] ✅ Build exitoso
- [ ] ✅ Firebase Console configurado
- [ ] ✅ Reglas de Firestore aplicadas
- [ ] ✅ App ejecutando sin crashes
- [ ] ✅ Logs muestran seed completado
- [ ] ✅ Datos visibles en Firestore Console
- [ ] ✅ Predicción calculada correctamente
- [ ] ✅ Segunda ejecución no duplica datos

## 🎉 Si completaste todos los checks:

**¡Felicidades! Tu proyecto está correctamente configurado con Firebase y listo para desarrollo.**

Próximos pasos sugeridos:
1. Leer `REPOSITORY_USAGE_GUIDE.md` para aprender a usar los repositorios
2. Implementar Firebase Authentication
3. Crear pantallas CRUD para gestionar datos
4. Aplicar reglas de seguridad de producción (ver `FIRESTORE_RULES.md`)

## 📞 Recursos adicionales

- **README_FIREBASE.md** - Guía completa de configuración
- **FIRESTORE_RULES.md** - Reglas de seguridad
- **REPOSITORY_USAGE_GUIDE.md** - Ejemplos de código
- **RESUMEN_CAMBIOS.md** - Listado completo de cambios
- [Documentación Firebase](https://firebase.google.com/docs)
- [Documentación Kotlin](https://kotlinlang.org/docs/home.html)
- [Documentación Jetpack Compose](https://developer.android.com/jetpack/compose)

