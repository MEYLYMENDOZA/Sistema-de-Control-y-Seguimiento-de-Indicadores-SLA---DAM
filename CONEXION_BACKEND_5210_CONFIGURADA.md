# ✅ CONFIGURACIÓN COMPLETADA - Puerto 5210

## 🎯 URLs Actualizadas Correctamente

### Todos los archivos ahora usan:
```
http://10.0.2.2:5210/
```

## 📁 Archivos Modificados

### ✅ 1. RetrofitClient.kt (Principal)
**Ubicación:** `app/src/main/java/com/example/proyecto1/data/remote/RetrofitClient.kt`
```kotlin
private const val BASE_URL = "http://10.0.2.2:5210/"
```

### ✅ 2. RetrofitClient.kt (API)
**Ubicación:** `app/src/main/java/com/example/proyecto1/data/remote/api/RetrofitClient.kt`
```kotlin
private val BASE_URL: String = try {
    BuildConfig.API_BASE_URL
} catch (_: Exception) {
    "http://10.0.2.2:5210/"  // Fallback
}
```

### ✅ 3. build.gradle.kts
**Ubicación:** `app/build.gradle.kts`
```kotlin
buildTypes {
    release {
        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:5210/\"")
    }
    debug {
        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:5210/\"")
    }
}
```

---

## 🚀 Próximos Pasos

### 1. Reconstruye el Proyecto
Es **OBLIGATORIO** hacer rebuild para que los cambios en `build.gradle.kts` se apliquen:

```
En Android Studio:
1. Build > Clean Project
2. Build > Rebuild Project
```

### 2. Verifica tu Backend
Asegúrate de que tu backend .NET esté corriendo en el puerto 5210:

```bash
# Verifica que esté corriendo:
curl http://localhost:5210/api/usuarios/roles

# O abre en tu navegador:
http://localhost:5210/api/usuarios/roles
```

**Respuesta esperada:**
```json
[
    {
        "idRolSistema": 1004,
        "codigo": "TECNICO",
        "nombre": "Técnico",
        ...
    },
    {
        "idRolSistema": 1005,
        "codigo": "CLIENTE",
        "nombre": "Cliente",
        ...
    }
]
```

### 3. Configura los Roles en SQL Server

Si no existen, ejecuta este script en tu base de datos:

```sql
-- Crear roles con IDs exactos
SET IDENTITY_INSERT roles_sistema ON;

INSERT INTO roles_sistema (id_rol_sistema, codigo, nombre, descripcion, es_activo)
VALUES 
    (1004, 'TECNICO', 'Técnico', 'Soporte técnico', 1),
    (1005, 'CLIENTE', 'Cliente', 'Usuario solicitante', 1);

SET IDENTITY_INSERT roles_sistema OFF;

-- Verificar que se crearon correctamente
SELECT * FROM roles_sistema WHERE codigo IN ('TECNICO', 'CLIENTE');
```

### 4. Verifica el Estado "Activo"

```sql
-- Verificar que existe el estado Activo (ID = 1)
SELECT * FROM estado_usuario_catalogo WHERE id_estado_usuario = 1;

-- Si no existe, créalo:
SET IDENTITY_INSERT estado_usuario_catalogo ON;

INSERT INTO estado_usuario_catalogo (id_estado_usuario, codigo, descripcion)
VALUES (1, 'ACTIVO', 'Usuario activo');

SET IDENTITY_INSERT estado_usuario_catalogo OFF;
```

---

## 🧪 Prueba la Conexión

### Desde Logcat (Android Studio)

Después de ejecutar la app, busca estas líneas en Logcat:

```
✅ D/RetrofitClient: 🌐 API Base URL configurada: http://10.0.2.2:5210/
✅ D/UsuariosViewModel: 📋 Obteniendo lista de usuarios...
✅ D/UsuariosViewModel: 📊 Cargando roles del sistema...
✅ D/UsuariosViewModel: ✅ 2 roles cargados
✅ D/UsuariosViewModel:   - Rol ID: 1004, Código: TECNICO, Nombre: Técnico
✅ D/UsuariosViewModel:   - Rol ID: 1005, Código: CLIENTE, Nombre: Cliente
```

### Si ves errores:

#### Error: "Failed to connect to /10.0.2.2:5210"
**Causa:** El backend no está corriendo o está en otro puerto

**Solución:**
1. Verifica que tu backend esté corriendo: `netstat -ano | findstr :5210`
2. Inicia tu backend .NET
3. Asegúrate de que escuche en `http://0.0.0.0:5210` o `http://localhost:5210`

#### Error: "Connection refused"
**Causa:** Firewall bloqueando la conexión

**Solución:**
```bash
# En PowerShell como administrador:
netsh advfirewall firewall add rule name="Backend API 5210" dir=in action=allow protocol=TCP localport=5210
```

#### Error: "404 Not Found"
**Causa:** Las rutas de tu backend no coinciden

**Solución:** Verifica que tu controlador tenga estas rutas:
```csharp
[ApiController]
[Route("api/usuarios")]
public class UsuariosController : ControllerBase
{
    [HttpGet("roles")]
    public IActionResult ObtenerRoles() { ... }
    
    [HttpGet]
    public IActionResult ObtenerUsuarios() { ... }
}
```

---

## ✅ Checklist de Verificación

Antes de ejecutar la app, verifica:

- [ ] Backend corriendo en `http://localhost:5210`
- [ ] Roles 1004 y 1005 existen en la base de datos
- [ ] Estado 1 (Activo) existe en estado_usuario_catalogo
- [ ] Endpoint `/api/usuarios/roles` responde correctamente
- [ ] Endpoint `/api/usuarios` responde correctamente
- [ ] Proyecto Android reconstruido (Clean + Rebuild)
- [ ] Firewall permite conexiones en puerto 5210

---

## 🎯 Endpoints Requeridos

Tu backend .NET debe tener estos endpoints funcionando:

### 1. Listar Roles
```
GET http://localhost:5210/api/usuarios/roles
```

### 2. Listar Usuarios
```
GET http://localhost:5210/api/usuarios
```

### 3. Crear Usuario
```
POST http://localhost:5210/api/usuarios
Content-Type: application/json

{
    "username": "jperez",
    "correo": "jperez@ejemplo.com",
    "password": "password123",
    "idRolSistema": 1005,
    "idEstadoUsuario": 1,
    "nombres": "jperez",
    "apellidos": "",
    "documento": null,
    "telefono": null
}
```

### 4. Actualizar Usuario
```
PUT http://localhost:5210/api/usuarios/{id}
```

### 5. Eliminar Usuario
```
DELETE http://localhost:5210/api/usuarios/{id}
```

---

## 📱 Flujo de Prueba en la App

1. **Abre la app** en el emulador
2. **Ve a "Usuarios"** desde el menú principal
3. **Verifica que se cargue la lista** de usuarios existentes
4. **Haz clic en "Agregar Usuario"** (botón azul)
5. **Llena el formulario:**
   - Username: `testuser`
   - Correo: `test@ejemplo.com`
   - Contraseña: `password123`
   - Rol: `Cliente`
6. **Haz clic en "Crear"**
7. **Verifica que aparezca** en la lista con:
   - Username: testuser
   - Correo: test@ejemplo.com
   - Rol: Cliente
   - Fecha de creación: (fecha actual)

---

## 🔧 Configuración de Program.cs (Backend .NET)

Asegúrate de que tu backend escuche en todas las interfaces:

```csharp
var builder = WebApplication.CreateBuilder(args);

// Configurar URLs
builder.WebHost.UseUrls("http://0.0.0.0:5210");

// ... resto de configuración

var app = builder.Build();

// Habilitar CORS para desarrollo
app.UseCors(policy => 
    policy.AllowAnyOrigin()
          .AllowAnyMethod()
          .AllowAnyHeader());

app.Run();
```

---

## 🎉 ¡Listo!

Ahora tu app Android está configurada para conectarse correctamente a tu backend en el puerto **5210**.

**Recuerda:**
- `10.0.2.2` = localhost del PC desde el emulador Android
- Si usas dispositivo físico, cambia a la IP real de tu PC (ej: `192.168.1.100`)

**Para obtener tu IP (si usas dispositivo físico):**
```bash
ipconfig
# Busca "IPv4 Address" en tu conexión WiFi
```

---

**Estado:** ✅ **TODO CONFIGURADO CORRECTAMENTE**
**Puerto:** `5210`
**URL:** `http://10.0.2.2:5210/`

