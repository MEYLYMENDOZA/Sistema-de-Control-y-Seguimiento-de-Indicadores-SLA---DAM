# Reglas de Seguridad para Firestore

## Reglas de Desarrollo (Solo Testing)
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

## Reglas de Producción Recomendadas
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper functions
    function isAuthenticated() {
      return request.auth != null;
    }
    
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }
    
    function hasRole(role) {
      return isAuthenticated() && 
             get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.idRolSistema == role;
    }
    
    // Catálogos - lectura para autenticados, escritura solo admin
    match /tipo_solicitud_catalogo/{document} {
      allow read: if isAuthenticated();
      allow write: if hasRole('ADMIN');
    }
    
    match /estado_usuario_catalogo/{document} {
      allow read: if isAuthenticated();
      allow write: if hasRole('ADMIN');
    }
    
    match /estado_solicitud_catalogo/{document} {
      allow read: if isAuthenticated();
      allow write: if hasRole('ADMIN');
    }
    
    match /estado_alerta_catalogo/{document} {
      allow read: if isAuthenticated();
      allow write: if hasRole('ADMIN');
    }
    
    match /tipo_alerta_catalogo/{document} {
      allow read: if isAuthenticated();
      allow write: if hasRole('ADMIN');
    }
    
    // Áreas
    match /areas/{areaId} {
      allow read: if isAuthenticated();
      allow create, update: if hasRole('ADMIN') || hasRole('GESTOR');
      allow delete: if hasRole('ADMIN');
    }
    
    // RBAC
    match /permiso/{permisoId} {
      allow read: if isAuthenticated();
      allow write: if hasRole('ADMIN');
    }
    
    match /roles_sistema/{rolId} {
      allow read: if isAuthenticated();
      allow write: if hasRole('ADMIN');
    }
    
    match /rol_registro/{rolId} {
      allow read: if isAuthenticated();
      allow write: if hasRole('ADMIN');
    }
    
    // Usuarios - solo admin puede modificar, usuarios pueden ver su propio perfil
    match /usuarios/{userId} {
      allow read: if isAuthenticated() && (isOwner(userId) || hasRole('ADMIN'));
      allow create, update, delete: if hasRole('ADMIN');
    }
    
    // Personal
    match /personal/{personalId} {
      allow read: if isAuthenticated();
      allow write: if hasRole('ADMIN') || hasRole('GESTOR');
    }
    
    // Config SLA
    match /config_sla/{slaId} {
      allow read: if isAuthenticated();
      allow write: if hasRole('ADMIN') || hasRole('GESTOR');
    }
    
    // Solicitudes
    match /solicitud/{solicitudId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated();
      allow update: if isAuthenticated() && 
                     (resource.data.creadoPor == request.auth.uid || 
                      hasRole('ADMIN') || 
                      hasRole('GESTOR'));
      allow delete: if hasRole('ADMIN');
    }
    
    // Reportes
    match /reporte/{reporteId} {
      allow read: if isAuthenticated() && 
                   (resource.data.generadoPor == request.auth.uid || 
                    hasRole('ADMIN'));
      allow create: if isAuthenticated();
      allow update, delete: if hasRole('ADMIN');
    }
    
    match /reporte_detalle/{detalleId} {
      allow read, write: if isAuthenticated();
    }
    
    // Alertas
    match /alerta/{alertaId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated();
      allow update: if isAuthenticated();
      allow delete: if hasRole('ADMIN');
    }
    
    // SLA Histórico - lectura para todos autenticados, escritura solo admin
    match /sla_historico/{historicoId} {
      allow read: if isAuthenticated();
      allow write: if hasRole('ADMIN');
    }
  }
}
```

## Índices Recomendados

### Diferencia entre Índices Compuestos y de Un Solo Campo

- **Índices de un solo campo**: Se crean automáticamente para cada campo. Se gestionan en la pestaña "Single field" de Indexes.
- **Índices compuestos**: Necesarios cuando consultas usan múltiples campos en `where()`, `orderBy()` o combinaciones. Se crean en la pestaña "Composite" de Indexes.

### Índices Compuestos (Composite Indexes)

Para consultas eficientes, crea estos índices compuestos en la consola de Firebase:

### Colección: `solicitud`
- Campos: `idArea` (Asc), `creadoEn` (Desc)
- Campos: `idEstadoSolicitud` (Asc), `creadoEn` (Desc)
- Campos: `creadoPor` (Asc), `creadoEn` (Desc)
- Campos: `idArea` (Asc), `idEstadoSolicitud` (Asc), `fechaSolicitud` (Desc)

### Colección: `alerta`
- Campos: `idSolicitud` (Asc), `fecha_creacion` (Desc)
- Campos: `id_estado_alerta` (Asc), `fecha_creacion` (Desc)
- Campos: `nivel` (Asc), `enviado_email` (Asc), `fecha_creacion` (Desc)

### Colección: `sla_historico`
**No requiere índice compuesto** - El campo `orden` se indexa automáticamente como índice de un solo campo.

Para habilitar el índice de un solo campo:
1. Ve a Firebase Console → Firestore Database → Indexes → Single field
2. Busca la colección `sla_historico`
3. Busca el campo `orden`
4. Asegúrate de que esté habilitado (enabled)

## Aplicar Reglas

1. Ve a Firebase Console → Firestore Database → Rules
2. Copia las reglas apropiadas (desarrollo o producción)
3. Haz clic en "Publicar"

## Notas de Seguridad

- **Nunca uses reglas de desarrollo en producción**
- Las reglas de producción asumen que tienes Firebase Authentication configurado
- Ajusta los roles según tu implementación específica
- Los índices compuestos se crean automáticamente cuando ejecutas una consulta que los requiere, pero es mejor crearlos manualmente
- Prueba las reglas usando el simulador de reglas en Firebase Console

## Troubleshooting de Índices

### Error: "this index is not necessary, configure using single field index controls"

Este error aparece cuando intentas crear un índice compuesto para un solo campo.

**Solución:**
1. NO crees el índice compuesto
2. Los índices de un solo campo ya están habilitados por defecto
3. Si necesitas configurarlo:
   - Ve a Firestore Console → Indexes → Single field
   - Busca tu colección y campo
   - Ajusta la configuración si es necesario

**Ejemplo:** Para `sla_historico` con campo `orden`:
- ❌ NO crear índice compuesto con solo `orden`
- ✅ Firestore ya indexa `orden` automáticamente
- ✅ Tus consultas con `.orderBy("orden")` funcionarán sin configuración adicional

### Cuándo SÍ necesitas un índice compuesto:

```kotlin
// ✅ NECESITA índice compuesto (múltiples campos)
db.collection("solicitud")
  .whereEqualTo("idArea", areaId)
  .orderBy("creadoEn", Query.Direction.DESCENDING)

// ❌ NO necesita índice compuesto (un solo campo)
db.collection("sla_historico")
  .orderBy("orden", Query.Direction.ASCENDING)
```

