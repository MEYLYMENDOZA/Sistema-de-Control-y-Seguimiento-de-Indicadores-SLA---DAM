# 🔧 DIAGNÓSTICO Y SOLUCIÓN - Error de Conexión

## ❌ Error Actual
```
failed to connect to /10.0.2.2 (port 5210) from /10.0.2.16 (port 55340) after 60000ms
```

## 🔍 PASO 1: Verifica si tu Backend está Corriendo

### Opción A: Usando CMD
```cmd
netstat -ano | findstr :5210
```

**Si está corriendo, verás algo como:**
```
TCP    0.0.0.0:5210    0.0.0.0:0    LISTENING    12345
```

**Si NO ves nada**, tu backend NO está corriendo.

### Opción B: En tu Navegador
Abre:
```
http://localhost:5210/api/usuarios/roles
```

- ✅ **Si funciona**: Verás JSON con roles
- ❌ **Si NO funciona**: Backend no está corriendo

---

## 🚀 PASO 2: Inicia tu Backend .NET

### Desde Visual Studio:
1. Abre tu proyecto .NET
2. Presiona **F5** o clic en ▶️ Run
3. Verifica la consola que diga: `Now listening on: http://localhost:5210`

### Desde CMD:
```cmd
cd C:\ruta\a\tu\backend
dotnet run
```

---

## ⚙️ PASO 3: Configura tu Backend para Puerto 5210

### En `Program.cs` o `launchSettings.json`:

**Opción 1: Program.cs**
```csharp
var builder = WebApplication.CreateBuilder(args);

// IMPORTANTE: Escuchar en TODAS las interfaces
builder.WebHost.UseUrls("http://0.0.0.0:5210");

// ... resto del código

var app = builder.Build();

// Habilitar CORS
app.UseCors(policy => 
    policy.AllowAnyOrigin()
          .AllowAnyMethod()
          .AllowAnyHeader());

app.Run();
```

**Opción 2: launchSettings.json**
```json
{
  "profiles": {
    "tu_proyecto": {
      "commandName": "Project",
      "launchBrowser": true,
      "applicationUrl": "http://0.0.0.0:5210",
      "environmentVariables": {
        "ASPNETCORE_ENVIRONMENT": "Development"
      }
    }
  }
}
```

---

## 🔥 PASO 4: Configura el Firewall de Windows

El firewall puede estar bloqueando el puerto 5210.

**Ejecuta en PowerShell como Administrador:**
```powershell
# Permitir puerto 5210 entrante
netsh advfirewall firewall add rule name="Backend API 5210" dir=in action=allow protocol=TCP localport=5210

# Verificar que se agregó
netsh advfirewall firewall show rule name="Backend API 5210"
```

---

## 🧪 PASO 5: Prueba la Conexión Manualmente

### Desde el navegador del PC:
```
http://localhost:5210/api/usuarios/roles
```

### Desde Postman:
```
GET http://localhost:5210/api/usuarios/roles
```

**Respuesta esperada:**
```json
[
    {
        "idRolSistema": 1004,
        "codigo": "TECNICO",
        "nombre": "Técnico"
    },
    {
        "idRolSistema": 1005,
        "codigo": "CLIENTE",
        "nombre": "Cliente"
    }
]
```

---

## 📱 PASO 6: Verifica la IP Correcta

### Si usas EMULADOR:
- ✅ Usa: `http://10.0.2.2:5210`
- La app YA está configurada correctamente

### Si usas DISPOSITIVO FÍSICO:
1. Obtén la IP de tu PC:
   ```cmd
   ipconfig
   ```
   Busca "IPv4 Address" (ejemplo: `192.168.1.100`)

2. Cambia la URL en `build.gradle.kts`:
   ```kotlin
   buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.100:5210/\"")
   ```

3. Conecta el celular a la **MISMA red WiFi** que tu PC

---

## ✅ CHECKLIST DE VERIFICACIÓN

Marca cada uno antes de ejecutar la app:

- [ ] Backend está corriendo en puerto 5210
- [ ] `netstat -ano | findstr :5210` muestra LISTENING
- [ ] `http://localhost:5210/api/usuarios/roles` funciona en navegador
- [ ] Firewall permite conexiones en puerto 5210
- [ ] Program.cs tiene `UseUrls("http://0.0.0.0:5210")`
- [ ] CORS está habilitado en el backend
- [ ] App Android usa `http://10.0.2.2:5210` (emulador)
- [ ] Proyecto Android reconstruido (Clean + Rebuild)

---

## 🔧 SOLUCIÓN RÁPIDA

Si NADA funciona, prueba esto:

### 1. Cambia temporalmente a IP fija

**En tu PC, abre CMD:**
```cmd
ipconfig
```

**Anota tu IPv4 (ejemplo: 192.168.1.100)**

**En Android Studio, edita `build.gradle.kts`:**
```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://TU_IP_AQUI:5210/\"")
```

Ejemplo:
```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.100:5210/\"")
```

**Rebuild:**
```
Build > Clean Project
Build > Rebuild Project
```

---

## 📊 LOGS ESPERADOS SI FUNCIONA

En Logcat deberías ver:
```
D/RetrofitClient: 🌐 API Base URL configurada: http://10.0.2.2:5210/
D/UsuariosViewModel: 📋 Obteniendo lista de usuarios...
D/UsuariosViewModel: ✅ 2 roles cargados
I/okhttp.OkHttpClient: <-- 200 OK http://10.0.2.2:5210/api/usuarios/roles
```

---

## ⚠️ ERRORES COMUNES

### Error 1: "Connection refused"
**Causa:** Backend no está corriendo
**Solución:** Inicia tu backend .NET

### Error 2: "Timeout after 60000ms"
**Causa:** Firewall bloqueando
**Solución:** Agrega regla de firewall (ver Paso 4)

### Error 3: "Failed to connect"
**Causa:** Puerto incorrecto o IP incorrecta
**Solución:** Verifica que el backend esté en 5210

---

## 🎯 CONFIGURACIÓN BACKEND .NET COMPLETA

Copia esto en tu `Program.cs`:

```csharp
using Microsoft.AspNetCore.Builder;
using Microsoft.Extensions.DependencyInjection;

var builder = WebApplication.CreateBuilder(args);

// Configurar para escuchar en todas las interfaces
builder.WebHost.UseUrls("http://0.0.0.0:5210");

// Agregar servicios
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Configurar CORS
builder.Services.AddCors(options =>
{
    options.AddDefaultPolicy(policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

var app = builder.Build();

// Configurar pipeline
app.UseSwagger();
app.UseSwaggerUI();
app.UseCors(); // IMPORTANTE
app.UseAuthorization();
app.MapControllers();

Console.WriteLine("🚀 Backend corriendo en: http://localhost:5210");
Console.WriteLine("📡 Accesible desde emulador en: http://10.0.2.2:5210");

app.Run();
```

---

## ✅ RESULTADO ESPERADO

Cuando todo funcione:
1. Backend corriendo en puerto 5210
2. App conectada exitosamente
3. Puedes crear usuarios sin errores
4. La búsqueda filtra correctamente

---

**ACCIÓN INMEDIATA:**
1. Abre CMD y ejecuta: `netstat -ano | findstr :5210`
2. Si NO ves nada, inicia tu backend
3. Verifica en navegador: `http://localhost:5210/api/usuarios/roles`
4. Si funciona, ejecuta la app de nuevo

