# 🚨 SOLUCIÓN URGENTE - Error de Conexión

## El problema es SIMPLE: Tu backend NO está respondiendo

---

## ✅ SOLUCIÓN EN 3 PASOS

### 📌 PASO 1: Verifica si tu Backend está Corriendo

Abre **CMD** o **PowerShell** y ejecuta:

```cmd
netstat -ano | findstr :5210
```

**¿Qué deberías ver?**
```
TCP    0.0.0.0:5210    0.0.0.0:0    LISTENING    12345
```

### ❌ Si NO ves NADA:
Tu backend **NO ESTÁ CORRIENDO**. Ve al **PASO 2**.

### ✅ Si VES algo:
Tu backend está corriendo. Ve al **PASO 3**.

---

### 📌 PASO 2: Inicia tu Backend .NET

**Opción A: Desde Visual Studio**
1. Abre tu proyecto backend en Visual Studio
2. Presiona **F5** o clic en ▶️ (Run)
3. Espera a ver: `Now listening on: http://localhost:5210`

**Opción B: Desde CMD**
```cmd
cd C:\ruta\a\tu\backend
dotnet run --urls "http://0.0.0.0:5210"
```

**Opción C: Script PowerShell**
```powershell
cd C:\Users\meyle\AndroidStudioProjects\Proyecto1
.\diagnostico-backend.ps1
```

---

### 📌 PASO 3: Prueba el Backend Manualmente

Abre tu navegador y ve a:
```
http://localhost:5210/api/usuarios/roles
```

**✅ Si funciona:**
Verás JSON con roles:
```json
[
    {"idRolSistema": 1004, "codigo": "TECNICO", "nombre": "Técnico"},
    {"idRolSistema": 1005, "codigo": "CLIENTE", "nombre": "Cliente"}
]
```

**❌ Si NO funciona (Error 404):**
Tu backend NO tiene el endpoint `/api/usuarios/roles`

---

## 🔥 CONFIGURACIÓN DEL BACKEND

### Si tu backend NO tiene los endpoints correctos:

Asegúrate de tener un controlador `UsuariosController.cs`:

```csharp
using Microsoft.AspNetCore.Mvc;

[ApiController]
[Route("api/usuarios")]
public class UsuariosController : ControllerBase
{
    // GET /api/usuarios/roles
    [HttpGet("roles")]
    public IActionResult ObtenerRoles()
    {
        var roles = new[]
        {
            new { idRolSistema = 1004, codigo = "TECNICO", nombre = "Técnico", descripcion = "Soporte técnico", esActivo = true },
            new { idRolSistema = 1005, codigo = "CLIENTE", nombre = "Cliente", descripcion = "Usuario solicitante", esActivo = true }
        };
        
        return Ok(roles);
    }

    // GET /api/usuarios
    [HttpGet]
    public IActionResult ObtenerUsuarios()
    {
        // Tu lógica aquí
        return Ok(new { success = true, usuarios = new List<object>(), total = 0 });
    }

    // POST /api/usuarios
    [HttpPost]
    public IActionResult CrearUsuario([FromBody] object usuario)
    {
        // Tu lógica aquí
        return Ok(usuario);
    }
}
```

### Configuración en `Program.cs`:

```csharp
var builder = WebApplication.CreateBuilder(args);

// IMPORTANTE: Escuchar en TODAS las interfaces
builder.WebHost.UseUrls("http://0.0.0.0:5210");

builder.Services.AddControllers();

// CORS para desarrollo
builder.Services.AddCors(options =>
{
    options.AddDefaultPolicy(policy =>
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader());
});

var app = builder.Build();

app.UseCors();
app.UseAuthorization();
app.MapControllers();

Console.WriteLine("🚀 Backend corriendo en http://localhost:5210");
app.Run();
```

---

## 🛡️ FIREWALL (Solo si es necesario)

Si el backend funciona en el navegador pero NO desde la app:

**Ejecuta en PowerShell como Administrador:**
```powershell
netsh advfirewall firewall add rule name="Backend 5210" dir=in action=allow protocol=TCP localport=5210
```

---

## 📱 VERIFICA LA APP

### build.gradle.kts debe tener:
```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:5210/\"")
```

### Rebuild obligatorio:
```
Build > Clean Project
Build > Rebuild Project
```

---

## ✅ CHECKLIST FINAL

Marca cada uno:

- [ ] Backend corriendo en puerto 5210 ✅
- [ ] `netstat -ano | findstr :5210` muestra LISTENING ✅
- [ ] `http://localhost:5210/api/usuarios/roles` funciona ✅
- [ ] Program.cs tiene `UseUrls("http://0.0.0.0:5210")` ✅
- [ ] UsuariosController.cs existe con endpoints ✅
- [ ] CORS habilitado en Program.cs ✅
- [ ] Android Studio hizo Rebuild ✅

---

## 🎯 PRUEBA FINAL

1. **Backend corriendo** → Verifica en navegador
2. **Ejecuta la app** → Ve a "Usuarios"
3. **Clic en "Agregar Usuario"**
4. **Llena el formulario** y clic en "Crear"
5. **Debería funcionar** ✅

---

## ⚠️ SI TODAVÍA NO FUNCIONA

### Usa IP fija temporalmente:

1. Obtén tu IP:
   ```cmd
   ipconfig
   ```
   Busca "IPv4 Address" (ejemplo: `192.168.1.100`)

2. Edita `build.gradle.kts`:
   ```kotlin
   buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.100:5210/\"")
   ```

3. Rebuild y prueba de nuevo

---

## 📞 CONTACTO DE EMERGENCIA

Si NADA funciona, necesitas:

1. ✅ Que tu backend esté corriendo
2. ✅ Que responda en `http://localhost:5210`
3. ✅ Que tenga los endpoints correctos

**Sin esto, la app NUNCA se conectará.**

---

**RESUMEN: El 99% de las veces el problema es que el backend NO está corriendo o NO está en el puerto 5210.**

