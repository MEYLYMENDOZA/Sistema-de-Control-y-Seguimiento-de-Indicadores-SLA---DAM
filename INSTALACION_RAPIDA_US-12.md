# 🚀 GUÍA RÁPIDA DE INSTALACIÓN - US-12

## ⚡ INSTALACIÓN RÁPIDA (5 MINUTOS)

### 📦 **BACKEND (Visual Studio)**

```powershell
# 1. Copiar archivos del backend
# Desde: BACKEND_CODIGO/
# Hacia tu proyecto Visual Studio en las carpetas correspondientes

# 2. Ejecutar migración SQL
# Abrir SSMS y ejecutar:
BACKEND_CODIGO/Migrations/001_Create_PrediccionTendenciaLog.sql

# 3. Agregar en Program.cs (antes de builder.Build()):
builder.Services.AddScoped<ISlaRepository, SlaRepository>();
builder.Services.AddScoped<ITendenciaLogRepository, TendenciaLogRepository>();
builder.Services.AddScoped<TendenciaService>();

# 4. En tu DbContext, agregar:
public DbSet<PrediccionTendenciaLog> PrediccionTendenciaLogs { get; set; }

# 5. Compilar y ejecutar
dotnet build
dotnet run

# 6. Probar endpoint
curl http://localhost:5120/api/reportes/tendencia?tipoSla=SLA1&anio=2024
```

---

### 📱 **FRONTEND (Android Studio)**

```bash
# 1. Copiar archivos nuevos:
# - TendenciaDto.kt → data/remote/dto/
# - TendenciaRepository.kt → data/repository/
# - TendenciaScreen.kt → presentation/tendencia/
# - TendenciaViewModel.kt → presentation/tendencia/
# - PdfExporterTendencia.kt → utils/

# El archivo SlaApiService.kt ya fue actualizado automáticamente ✓

# 2. Agregar dependencia de gráficos en app/build.gradle.kts:
dependencies {
    implementation("co.yml:ycharts:2.1.0")
}

# 3. Sync & Build
./gradlew clean build

# 4. Agregar navegación (en tu NavHost):
composable("tendencia") {
    val viewModel = viewModel<TendenciaViewModel>()
    TendenciaScreen(vm = viewModel)
}

# 5. Ejecutar app
```

---

## 📋 CHECKLIST RÁPIDO

### Backend ✅
- [ ] Archivos copiados (Models, Repositories, Services, Controller)
- [ ] Tabla `PrediccionTendenciaLog` creada en SQL Server
- [ ] Servicios registrados en `Program.cs`
- [ ] DbContext actualizado
- [ ] Endpoint responde: `GET /api/reportes/tendencia`

### Frontend ✅
- [ ] 5 archivos nuevos copiados
- [ ] Dependencia `ycharts` agregada
- [ ] Proyecto compilado sin errores
- [ ] Navegación configurada
- [ ] App conecta con API

---

## 🎯 PRUEBA RÁPIDA

### Test Backend
```http
GET http://localhost:5120/api/reportes/tendencia?tipoSla=SLA1&anio=2024

Respuesta esperada:
{
  "historico": [...],
  "tendencia": [...],
  "proyeccion": 86.3,
  "estadoTendencia": "positiva"
}
```

### Test Frontend
1. Abrir app en Android
2. Navegar a la pantalla de Tendencia
3. Seleccionar Año: 2024, Tipo SLA: SLA1
4. Verificar que aparezcan:
   - 4 tarjetas de KPIs
   - Gráfico de líneas
   - Tabla de datos
5. Presionar "Exportar Reporte PDF"
6. Verificar que se genere el PDF

---

## ❌ ERRORES COMUNES

| Error | Solución |
|-------|----------|
| "No es posible generar proyección" | Insertar más datos (mínimo 3 meses) |
| 404 Not Found | Verificar que ReportesController esté registrado |
| CORS error | Agregar configuración CORS en Program.cs |
| Gráfico no aparece | Verificar dependencia ycharts instalada |
| PDF no abre | Verificar FileProvider en AndroidManifest.xml |

---

## 📁 ESTRUCTURA DE ARCHIVOS

```
BACKEND_CODIGO/
├── Models/
│   ├── SlaRegistro.cs
│   └── PrediccionTendenciaLog.cs
├── Repositories/
│   ├── ISlaRepository.cs
│   ├── SlaRepository.cs
│   ├── ITendenciaLogRepository.cs
│   └── TendenciaLogRepository.cs
├── Services/
│   └── TendenciaService.cs
├── Controllers/
│   └── ReportesController.cs
├── Migrations/
│   └── 001_Create_PrediccionTendenciaLog.sql
└── INSTRUCCIONES_CONFIGURACION_US12.cs

app/src/main/java/com/example/proyecto1/
├── data/
│   ├── remote/
│   │   ├── api/
│   │   │   └── SlaApiService.kt [ACTUALIZADO]
│   │   └── dto/
│   │       └── TendenciaDto.kt [NUEVO]
│   └── repository/
│       └── TendenciaRepository.kt [NUEVO]
├── presentation/
│   └── tendencia/
│       ├── TendenciaScreen.kt [NUEVO]
│       └── TendenciaViewModel.kt [NUEVO]
└── utils/
    └── PdfExporterTendencia.kt [NUEVO]
```

---

## 📞 SOPORTE

Si tienes problemas:
1. Revisa `IMPLEMENTACION_US-12_TENDENCIA_SLA.md` (documentación completa)
2. Verifica logs en:
   - Backend: Console de Visual Studio
   - Frontend: Logcat de Android Studio
3. Busca mensajes con tag "TendenciaViewModel" o "TendenciaService"

---

**¡Listo para usar! 🎉**

