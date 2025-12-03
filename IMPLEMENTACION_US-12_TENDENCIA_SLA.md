# 📊 IMPLEMENTACIÓN COMPLETA US-12: TENDENCIA Y PROYECCIÓN SLA

**Responsable:** 22200122 – David Huayhuas  
**Puntos de Historia:** 8  
**Estado:** ✅ IMPLEMENTADO

---

## 📋 RESUMEN

Se implementó exitosamente la funcionalidad de **Tendencia y Proyección de Cumplimiento SLA**, que permite a administradores y gerencia visualizar:

- ✅ Gráfico de tendencia histórica mensual (últimos 12 meses)
- ✅ Línea de regresión lineal (tendencia)
- ✅ Proyección del próximo mes
- ✅ Indicadores visuales de tendencia (↑ ↓ ≈)
- ✅ Filtros por Tipo SLA (SLA1/SLA2), Año y Área
- ✅ Exportación a PDF con gráfica, tabla y KPIs
- ✅ Seguridad OWASP implementada

---

## 🎯 CRITERIOS DE ACEPTACIÓN CUMPLIDOS

### ✅ 1. Vista "Reportes SLA"
- Gráfico de línea implementado con `YCharts` library
- Línea 1: Histórico mensual (azul continuo) ✓
- Línea 2: Tendencia de regresión (naranja) ✓
- Punto 3: Predicción próximo mes (verde con marcador) ✓

### ✅ 2. Filtros Implementados
- **Mes/Año:** Selector dinámico desde base de datos
- **Tipo SLA:** SLA1 (35 días) / SLA2 (20 días)
- **Rol/Área:** Selector con opción "Todas"

### ✅ 3. Tooltip Interactivo
- Muestra mes, SLA%, tipo de dato (Histórico/Tendencia/Proyección)
- Implementado con `SelectionHighlightPopUp` de YCharts

### ✅ 4. Indicador Visual de Tendencia
- ↑ Verde: Tendencia positiva (pendiente > +0.5)
- ↓ Rojo: Tendencia negativa (pendiente < -0.5)
- ≈ Amarillo: Tendencia estable (-0.5 ≤ pendiente ≤ +0.5)

### ✅ 5. Interfaz Responsive
- Diseño adaptativo con Jetpack Compose
- Mantiene proporciones en diferentes tamaños de pantalla
- Scroll vertical para contenido extenso

### ✅ 6. Exportación PDF
- Incluye gráfica simplificada
- Tabla de datos históricos completa
- KPIs principales (Mejor mes, Peor mes, Promedio, Proyección)
- Fecha de generación y filtros aplicados
- Nombre: `reporte_sla_tendencia_{año}.pdf`

### ✅ 7. Prácticas OWASP
- ✓ Sanitización de parámetros (mes, año, tipoSla, rol)
- ✓ Validación de rangos (mes: 1-12, año: 2000-presente)
- ✓ Whitelist para tipoSla (solo SLA1/SLA2)
- ✓ No expone información sensible en respuestas
- ✓ Auditoría con tabla `PrediccionTendenciaLog`
- ✓ Registro de IP del cliente

---

## 🏗️ ARQUITECTURA IMPLEMENTADA

### **BACKEND (ASP.NET Core + EF Core)**

#### 📁 **Modelos**
```
BACKEND_CODIGO/Models/
├── SlaRegistro.cs           # Modelo principal de solicitudes SLA
└── PrediccionTendenciaLog.cs # Auditoría de reportes generados
```

**Tabla de Auditoría:**
```sql
CREATE TABLE PrediccionTendenciaLog (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    FechaGeneracion DATETIME NOT NULL,
    UsuarioSolicitante NVARCHAR(100),
    TipoSla NVARCHAR(10) NOT NULL,
    Mes INT, Anio INT, IdArea INT,
    Prediccion DECIMAL(5,2),
    Pendiente DECIMAL(10,6),
    Intercepto DECIMAL(10,6),
    EstadoTendencia NVARCHAR(20),
    TotalRegistros INT,
    IpCliente NVARCHAR(50),
    Observaciones NVARCHAR(500)
);
```

#### 📁 **Repositorios**
```
BACKEND_CODIGO/Repositories/
├── ISlaRepository.cs         # Interfaz para consultas SLA
├── SlaRepository.cs          # Implementación con EF Core
├── ITendenciaLogRepository.cs
└── TendenciaLogRepository.cs
```

**Métodos principales:**
- `ObtenerSolicitudesPorRango(fechaInicio, fechaFin, tipoSla, idArea)`
- `ObtenerAñosDisponibles()`
- `ObtenerMesesDisponibles(anio)`

#### 📁 **Servicios**
```
BACKEND_CODIGO/Services/
└── TendenciaService.cs       # Lógica de negocio
```

**Funcionalidades:**
1. **Validación de parámetros** (OWASP)
2. **Cálculo de cumplimiento mensual:**
   - SLA1: `FechaIngreso - FechaSolicitud < 35 días`
   - SLA2: `FechaIngreso - FechaSolicitud < 20 días`
3. **Regresión lineal** (Método de mínimos cuadrados):
   ```csharp
   y = mx + b
   m = (n·ΣXY - ΣX·ΣY) / (n·ΣX² - (ΣX)²)
   b = (ΣY - m·ΣX) / n
   ```
4. **Proyección:** `valor = m * (n+1) + b`
5. **Clasificación de tendencia:**
   - Positiva: m > 0.5
   - Negativa: m < -0.5
   - Estable: -0.5 ≤ m ≤ 0.5

#### 📁 **Controlador**
```
BACKEND_CODIGO/Controllers/
└── ReportesController.cs
```

**Endpoint:**
```http
GET /api/reportes/tendencia?mes=12&anio=2024&tipoSla=SLA1&rol=1
```

**Respuesta JSON:**
```json
{
  "historico": [
    {
      "mes": "Ene 2024",
      "valor": 85.5,
      "orden": 1,
      "totalCasos": 150,
      "cumplidos": 128,
      "noCumplidos": 22
    },
    ...
  ],
  "tendencia": [
    { "mes": "Ene 2024", "valor": 84.2, "orden": 1 },
    ...
  ],
  "proyeccion": 86.3,
  "pendiente": 0.5,
  "intercepto": 83.5,
  "estadoTendencia": "positiva",
  "metadata": {
    "totalRegistros": 1250,
    "fechaGeneracion": "2024-11-26T10:30:00"
  }
}
```

---

### **FRONTEND (Android Kotlin + Jetpack Compose)**

#### 📁 **Estructura del Proyecto**
```
app/src/main/java/com/example/proyecto1/
├── data/
│   ├── remote/
│   │   ├── api/
│   │   │   ├── SlaApiService.kt         # [MODIFICADO] +endpoint tendencia
│   │   │   └── RetrofitClient.kt
│   │   └── dto/
│   │       └── TendenciaDto.kt          # [NUEVO] DTOs de respuesta
│   └── repository/
│       └── TendenciaRepository.kt       # [NUEVO] Lógica de datos
├── presentation/
│   └── tendencia/
│       ├── TendenciaScreen.kt           # [NUEVO] UI completa
│       └── TendenciaViewModel.kt        # [NUEVO] Estado y lógica
└── utils/
    └── PdfExporterTendencia.kt          # [NUEVO] Exportación PDF
```

#### 🎨 **Componentes de UI**

**TendenciaScreen.kt:**
- `EncabezadoTendencia()` - Título y descripción
- `FiltrosAnalisis()` - Selectores de filtros
- `TarjetasKPIs()` - 4 tarjetas de indicadores
- `GraficoTendencia()` - Gráfico de líneas con YCharts
- `TablaDetalle()` - Tabla de datos históricos
- `BotonesAccion()` - Exportar PDF / Compartir

**Colores Corporativos:**
```kotlin
AzulCorporativo = #2196F3  // Línea histórico
Verde = #4CAF50            // Tendencia positiva
Rojo = #E53935             // Tendencia negativa
Amarillo = #FFA726         // Tendencia estable
Naranja = #FF9800          // Línea de tendencia
GrisClaro = #F4F6F8        // Fondo
```

---

## 🚀 INSTRUCCIONES DE INSTALACIÓN

### **1. BACKEND (Visual Studio 2022)**

#### Paso 1: Copiar archivos al proyecto
```
Tu_Proyecto.CORE/
├── Domain/Entities/
│   ├── SlaRegistro.cs              ← Copiar
│   └── PrediccionTendenciaLog.cs   ← Copiar
├── Application/
│   ├── Repositories/
│   │   ├── ISlaRepository.cs       ← Copiar
│   │   ├── SlaRepository.cs        ← Copiar
│   │   ├── ITendenciaLogRepository.cs ← Copiar
│   │   └── TendenciaLogRepository.cs  ← Copiar
│   └── Services/
│       └── TendenciaService.cs     ← Copiar

Tu_Proyecto.API/
└── Controllers/
    └── ReportesController.cs       ← Copiar
```

#### Paso 2: Registrar servicios en `Program.cs`
```csharp
// Repositorios
builder.Services.AddScoped<ISlaRepository, SlaRepository>();
builder.Services.AddScoped<ITendenciaLogRepository, TendenciaLogRepository>();

// Servicios
builder.Services.AddScoped<TendenciaService>();
```

#### Paso 3: Actualizar `DbContext`
```csharp
public class Proyecto01DbContext : DbContext
{
    // ...existentes...
    
    public DbSet<PrediccionTendenciaLog> PrediccionTendenciaLogs { get; set; }
}
```

#### Paso 4: Ejecutar migración
```powershell
# Opción A: SQL Script
# Ejecutar: BACKEND_CODIGO/Migrations/001_Create_PrediccionTendenciaLog.sql

# Opción B: Entity Framework
Add-Migration AgregarTablaTendenciaLog
Update-Database
```

#### Paso 5: Verificar CORS (si es necesario)
```csharp
app.UseCors(policy =>
    policy.AllowAnyOrigin()
          .AllowAnyMethod()
          .AllowAnyHeader());
```

#### Paso 6: Probar endpoint
```http
GET http://localhost:5120/api/reportes/tendencia?tipoSla=SLA1&anio=2024
```

---

### **2. FRONTEND (Android Studio)**

#### Paso 1: Copiar archivos nuevos
```
app/src/main/java/com/example/proyecto1/
├── data/remote/dto/TendenciaDto.kt          ← Copiar
├── data/repository/TendenciaRepository.kt   ← Copiar
├── presentation/tendencia/
│   ├── TendenciaScreen.kt                   ← Copiar
│   └── TendenciaViewModel.kt                ← Copiar
└── utils/PdfExporterTendencia.kt            ← Copiar
```

#### Paso 2: Actualizar `SlaApiService.kt`
El archivo ya fue modificado automáticamente con el nuevo endpoint:
```kotlin
@GET("api/reportes/tendencia")
suspend fun obtenerTendenciaSla(...)
```

#### Paso 3: Agregar dependencia de gráficos (si no existe)
En `app/build.gradle.kts`:
```kotlin
dependencies {
    // Librería de gráficos YCharts
    implementation("co.yml:ycharts:2.1.0")
    
    // ...existentes...
}
```

#### Paso 4: Agregar navegación
En tu `NavGraph` o sistema de navegación:
```kotlin
composable("tendencia") {
    val viewModel = viewModel<TendenciaViewModel>()
    TendenciaScreen(vm = viewModel)
}
```

#### Paso 5: Sincronizar y compilar
```bash
./gradlew clean build
```

---

## 📊 CÁLCULOS MATEMÁTICOS IMPLEMENTADOS

### **Regresión Lineal (Mínimos Cuadrados)**

Dada una serie de puntos (x, y) donde:
- x = orden del mes (1, 2, 3, ...)
- y = % de cumplimiento SLA

**Fórmulas:**
```
n = número de puntos

Pendiente (m):
m = (n·ΣXY - ΣX·ΣY) / (n·ΣX² - (ΣX)²)

Intercepto (b):
b = (ΣY - m·ΣX) / n

Proyección:
y_predicho = m·x + b
```

**Ejemplo:**
```
Datos: [85, 87, 86, 88, 89]
m = 0.8 (tendencia positiva)
b = 84.2
Proyección mes 6 = 0.8 * 6 + 84.2 = 89.0%
```

### **Clasificación de Tendencia**
```csharp
if (pendiente > 0.5)  → "positiva"  (mejorando)
if (pendiente < -0.5) → "negativa"  (empeorando)
else                  → "estable"   (sin cambios significativos)
```

---

## 🔒 SEGURIDAD OWASP IMPLEMENTADA

### **1. Validación de Entrada**
```csharp
✅ Mes: 1-12
✅ Año: 2000 - AñoActual+1
✅ TipoSLA: Whitelist ["SLA1", "SLA2"]
✅ IdÁrea: > 0 (si se proporciona)
```

### **2. Sanitización**
- Parámetros parseados con validación explícita
- No se ejecuta SQL dinámico (uso de EF Core)
- Prevención de inyección SQL

### **3. Auditoría**
Cada consulta se registra en `PrediccionTendenciaLog`:
```sql
INSERT INTO PrediccionTendenciaLog (
    FechaGeneracion, UsuarioSolicitante, TipoSla,
    Mes, Anio, IdArea, Prediccion, Pendiente,
    Intercepto, EstadoTendencia, TotalRegistros,
    IpCliente, Observaciones
) VALUES (...)
```

### **4. Manejo de Errores**
```csharp
✅ ArgumentException → 400 Bad Request
✅ InvalidOperationException → 400 Bad Request
✅ Exception → 500 Internal Server Error (sin detalles internos)
```

### **5. No Exponer Información Sensible**
```csharp
❌ No se devuelven: Stack traces, rutas de archivos, credenciales
✅ Solo se retorna: Datos del reporte, mensajes genéricos de error
```

---

## 📱 CAPTURAS DE DISEÑO

### **Vista Principal**
```
┌─────────────────────────────────────────┐
│ Tendencia y Proyección del              │
│ Cumplimiento SLA                        │
├─────────────────────────────────────────┤
│ 🎛️ Filtros de Análisis                  │
│   Tipo SLA: [SLA1  ▼]  Año: [2024 ▼]   │
│   Rol/Área: [Todas ▼]                   │
│   Período: [Todo el período ▼]          │
├─────────────────────────────────────────┤
│ ┌─────────┐  ┌─────────┐               │
│ │ 100%    │  │ 60%     │               │
│ │ Mejor   │  │ Peor    │               │
│ └─────────┘  └─────────┘               │
│ ┌─────────┐  ┌─────────┐               │
│ │ 81.5%   │  │ ↑       │               │
│ │ Promedio│  │ Positiva│               │
│ └─────────┘  └─────────┘               │
├─────────────────────────────────────────┤
│ 📊 Evolución Histórica y Predicción     │
│                                         │
│   100% │        ●─────●                │
│    80% │   ●───●         ●             │
│    60% │ ●                 ○  (Proyec) │
│    40% │                               │
│    20% │                               │
│     0% └──────────────────────────     │
│        Ene Feb Mar Abr May Jun Jul     │
│                                         │
│   Leyenda: ● Histórico  — Tendencia    │
│            ○ Proyección                │
├─────────────────────────────────────────┤
│ 📋 Detalle del desempeño por mes       │
│ ┌─────┬──────┬────────┬──────┬───────┐│
│ │ Mes │Total │Cumplidos│NoCump│ %Cump││
│ ├─────┼──────┼────────┼──────┼───────┤│
│ │ Ene │  11  │   11   │  0   │ 100% ││
│ │ Feb │   9  │    8   │  1   │  89% ││
│ └─────┴──────┴────────┴──────┴───────┘│
├─────────────────────────────────────────┤
│ [📄 Exportar Reporte PDF]              │
│ [📤 Compartir con Dirección]           │
└─────────────────────────────────────────┘
```

---

## 🧪 PRUEBAS Y VALIDACIONES

### **Casos de Prueba**

#### ✅ Caso 1: Datos suficientes
**Entrada:** año=2024, tipoSla=SLA1  
**Resultado esperado:** Gráfico con 12 meses + proyección  
**Estado:** ✓ PASS

#### ✅ Caso 2: Datos insuficientes (<3 meses)
**Entrada:** año=2025, tipoSla=SLA2  
**Resultado esperado:** Error "No es posible generar proyección"  
**Estado:** ✓ PASS

#### ✅ Caso 3: Parámetros inválidos
**Entrada:** mes=13, año=1999  
**Resultado esperado:** 400 Bad Request  
**Estado:** ✓ PASS

#### ✅ Caso 4: Filtro por área
**Entrada:** año=2024, idArea=2  
**Resultado esperado:** Datos filtrados por área 2  
**Estado:** ✓ PASS

#### ✅ Caso 5: Exportación PDF
**Entrada:** Datos válidos + clic en "Exportar PDF"  
**Resultado esperado:** PDF generado con nombre correcto  
**Estado:** ✓ PASS

---

## 📝 NOTAS TÉCNICAS

### **Requisitos Mínimos**
- ✅ Al menos 3 meses de datos históricos
- ✅ Datos con FechaSolicitud y FechaIngreso válidos
- ✅ Configuración SLA (DiasUmbral) en la base de datos

### **Optimizaciones**
- ✅ Índices en `PrediccionTendenciaLog` (FechaGeneracion, TipoSla)
- ✅ Cache de años/meses disponibles en frontend
- ✅ Carga automática al seleccionar año

### **Limitaciones Conocidas**
- ⚠️ Tabla histórica limitada a 25 filas en PDF (paginación pendiente)
- ⚠️ Gráfico en PDF es simplificado (no incluye tendencia visual compleja)
- ⚠️ Selector de área actualmente hardcodeado (1, 2, 3)

---

## 🐛 SOLUCIÓN DE PROBLEMAS

### **Error: "No es posible generar proyección"**
**Causa:** Menos de 3 meses de datos  
**Solución:** Insertar más registros en la tabla Solicitudes

### **Error: 404 Not Found en `/api/reportes/tendencia`**
**Causa:** Controlador no registrado o ruta incorrecta  
**Solución:** Verificar que ReportesController esté en Controllers/

### **Error: CORS en Android**
**Causa:** Política de seguridad del servidor  
**Solución:** Agregar configuración CORS en Program.cs

### **Error: Gráfico no se muestra**
**Causa:** Dependencia YCharts no instalada  
**Solución:** Agregar `implementation("co.yml:ycharts:2.1.0")` y sincronizar

### **Error: PDF no se abre**
**Causa:** FileProvider no configurado  
**Solución:** Verificar AndroidManifest.xml tiene el provider

---

## ✅ CHECKLIST DE VERIFICACIÓN

### **Backend**
- [x] Tabla PrediccionTendenciaLog creada
- [x] Repositorios registrados en DI
- [x] TendenciaService implementado
- [x] ReportesController funcionando
- [x] Endpoint responde correctamente
- [x] Validaciones OWASP aplicadas
- [x] Auditoría registrando logs

### **Frontend**
- [x] TendenciaScreen.kt compilando
- [x] TendenciaViewModel con StateFlow
- [x] Repository llamando a API
- [x] DTOs deserializando correctamente
- [x] Gráfico mostrando datos
- [x] KPIs calculándose bien
- [x] Tabla de detalle visible
- [x] Exportación PDF funcionando
- [x] Filtros interactivos

---

## 📚 REFERENCIAS

### **Tecnologías Utilizadas**
- ASP.NET Core 6.0+
- Entity Framework Core
- SQL Server
- Android Kotlin
- Jetpack Compose
- Retrofit 2
- YCharts (Gráficos)

### **Librerías Externas**
```gradle
// Android
implementation("co.yml:ycharts:2.1.0")
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
```

### **Documentos Relacionados**
- `ESPECIFICACION_API_REST.md`
- `IMPLEMENTACION_US-11_PREDICCION_SLA.md`
- `INSTRUCCIONES_CONFIGURACION_US12.cs`

---

## 👤 AUTOR

**David Huayhuas**  
Código: 22200122  
Fecha: Noviembre 2024  

---

## 📄 LICENCIA

Proyecto académico - Sistema de Control y Seguimiento de Indicadores SLA

---

**FIN DEL DOCUMENTO**

