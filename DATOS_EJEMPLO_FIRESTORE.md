# Datos de Ejemplo para Firestore - Predicción SLA
## Colección: sla_historico

Estos datos deben cargarse en Firebase Firestore para que la predicción funcione correctamente.

---

## Formato JSON para Importación

Cada documento debe tener la siguiente estructura:

```json
{
  "mes": "2024-01",
  "porcentajeSla": 95.5,
  "orden": 1
}
```

---

## Datos Demo Completos (12 meses)

### Documento 1
```json
{
  "mes": "2024-01",
  "porcentajeSla": 95.5,
  "orden": 1
}
```

### Documento 2
```json
{
  "mes": "2024-02",
  "porcentajeSla": 92.3,
  "orden": 2
}
```

### Documento 3
```json
{
  "mes": "2024-03",
  "porcentajeSla": 89.7,
  "orden": 3
}
```

### Documento 4
```json
{
  "mes": "2024-04",
  "porcentajeSla": 88.2,
  "orden": 4
}
```

### Documento 5
```json
{
  "mes": "2024-05",
  "porcentajeSla": 85.9,
  "orden": 5
}
```

### Documento 6
```json
{
  "mes": "2024-06",
  "porcentajeSla": 82.4,
  "orden": 6
}
```

### Documento 7
```json
{
  "mes": "2024-07",
  "porcentajeSla": 79.8,
  "orden": 7
}
```

### Documento 8
```json
{
  "mes": "2024-08",
  "porcentajeSla": 76.5,
  "orden": 8
}
```

### Documento 9
```json
{
  "mes": "2024-09",
  "porcentajeSla": 73.2,
  "orden": 9
}
```

### Documento 10
```json
{
  "mes": "2024-10",
  "porcentajeSla": 69.8,
  "orden": 10
}
```

### Documento 11
```json
{
  "mes": "2024-11",
  "porcentajeSla": 65.4,
  "orden": 11
}
```

### Documento 12
```json
{
  "mes": "2024-12",
  "porcentajeSla": 62.1,
  "orden": 12
}
```

---

## Predicción Esperada

Con estos datos de ejemplo, el modelo debería predecir aproximadamente **61.9%** para el próximo mes, con una **tendencia negativa** (pendiente negativa).

### Coeficientes del Modelo (aproximados):
- **Pendiente (m):** -3.9169
- **Intercepto (b):** 97.1294

---

## Cómo Cargar los Datos en Firestore

### Opción 1: Firebase Console (Manual)

1. Accede a [Firebase Console](https://console.firebase.google.com)
2. Selecciona tu proyecto
3. Ve a **Firestore Database**
4. Crea la colección `sla_historico`
5. Agrega cada documento con un ID auto-generado
6. Copia y pega los campos: `mes`, `porcentajeSla`, `orden`

### Opción 2: Código (Automático) - FirestoreSeeder

El archivo `FirestoreSeeder.kt` ya implementado puede cargar estos datos automáticamente:

```kotlin
FirestoreSeeder.seedIfEmpty(db)
```

Verifica que el método `seedSlaHistorico()` contenga estos datos.

### Opción 3: Script de Importación (Firebase CLI)

Crea un archivo `sla_historico.json`:

```json
{
  "sla_historico": {
    "doc1": {
      "mes": "2024-01",
      "porcentajeSla": 95.5,
      "orden": 1
    },
    "doc2": {
      "mes": "2024-02",
      "porcentajeSla": 92.3,
      "orden": 2
    },
    ...
  }
}
```

Luego ejecuta:
```bash
firebase firestore:import sla_historico.json
```

---

## Validación de Datos

Para verificar que los datos se cargaron correctamente:

### Desde Firebase Console:
1. Ve a Firestore Database
2. Abre la colección `sla_historico`
3. Verifica que haya al menos 12 documentos
4. Cada documento debe tener los 3 campos

### Desde la App Android:
1. Abre Logcat en Android Studio
2. Filtra por "PrediccionRepository"
3. Deberías ver:
   ```
   Documentos encontrados: 12
   Mes 1: SLA = 95.5%
   Mes 2: SLA = 92.3%
   ...
   ```

---

## Datos Alternativos (Tendencia Positiva)

Si deseas probar con una tendencia positiva, usa estos valores:

```json
// Documento 1
{"mes": "2024-01", "porcentajeSla": 60.0, "orden": 1}

// Documento 2
{"mes": "2024-02", "porcentajeSla": 63.5, "orden": 2}

// Documento 3
{"mes": "2024-03", "porcentajeSla": 67.2, "orden": 3}

// Documento 4
{"mes": "2024-04", "porcentajeSla": 70.8, "orden": 4}

// Documento 5
{"mes": "2024-05", "porcentajeSla": 74.5, "orden": 5}

// Documento 6
{"mes": "2024-06", "porcentajeSla": 78.1, "orden": 6}

// Documento 7
{"mes": "2024-07", "porcentajeSla": 81.9, "orden": 7}

// Documento 8
{"mes": "2024-08", "porcentajeSla": 85.6, "orden": 8}

// Documento 9
{"mes": "2024-09", "porcentajeSla": 89.3, "orden": 9}

// Documento 10
{"mes": "2024-10", "porcentajeSla": 93.0, "orden": 10}
```

**Predicción esperada:** ~96.7% (tendencia positiva)

---

## Estructura de Base de Datos Completa

```
Firebase Firestore
│
└── sla_historico (Collection)
    ├── [auto-id-1] (Document)
    │   ├── mes: "2024-01"
    │   ├── porcentajeSla: 95.5
    │   └── orden: 1
    │
    ├── [auto-id-2] (Document)
    │   ├── mes: "2024-02"
    │   ├── porcentajeSla: 92.3
    │   └── orden: 2
    │
    └── ... (más documentos)
```

---

## Notas Importantes

1. **Mínimo de datos:** Se requieren al menos 3 documentos para generar predicción
2. **Orden cronológico:** El campo `orden` debe incrementar secuencialmente
3. **Formato de mes:** Puede ser "YYYY-MM" o cualquier string identificador
4. **Valores SLA:** Deben estar entre 0 y 100 (porcentajes)
5. **IDs de documentos:** Pueden ser auto-generados o personalizados

---

## Troubleshooting

### Error: "No hay datos suficientes"
**Causa:** Menos de 3 documentos en la colección  
**Solución:** Agregar más documentos

### Error: "porcentajeSla is null"
**Causa:** Falta el campo o tiene nombre diferente  
**Solución:** Verificar que el campo se llame exactamente `porcentajeSla`

### Predicción = 0.0
**Causa:** Valores SLA todos en cero o inválidos  
**Solución:** Revisar que los valores sean numéricos (Double) y > 0

---

**Última actualización:** 25 de Noviembre de 2025  
**Responsable:** Sistema de Predicción SLA

