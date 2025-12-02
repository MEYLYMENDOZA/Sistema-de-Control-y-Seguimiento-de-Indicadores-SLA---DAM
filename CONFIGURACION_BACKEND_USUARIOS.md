# 🔧 Configuración del Backend para Usuarios

## ✅ Cambios Realizados en la App Android

### 1. URL del Backend Actualizada
- **Nueva URL**: `http://10.0.2.2:5120/`
- **Importante**: `10.0.2.2` es la IP especial que el emulador Android usa para acceder a `localhost` del PC
- Si usas un **dispositivo físico**, debes:
  1. Conectar el celular y la PC a la misma red WiFi
  2. Cambiar `10.0.2.2` por la IP real de tu PC (ejemplo: `192.168.1.100`)
  3. Ejecutar: `ipconfig` en CMD para ver tu IP

### 2. Formulario Simplificado
Ahora el formulario solo solicita:
- ✅ **Nombre de usuario** (username)
- ✅ **Correo electrónico**
- ✅ **Contraseña** (mínimo 6 caracteres)
- ✅ **Rol** (Técnico o Cliente)

### 3. Roles Configurados
La app espera encontrar estos roles con estos IDs exactos:
- **ID 1004** - Código: `TECNICO` - Nombre: `Técnico`
- **ID 1005** - Código: `CLIENTE` - Nombre: `Cliente`

---

## 🗄️ Configuración Requerida en SQL Server

### Paso 1: Verifica que tengas los roles correctos

Ejecuta este query en tu SQL Server:

```sql
SELECT * FROM roles_sistema WHERE codigo IN ('TECNICO', 'CLIENTE');
```

**Resultado esperado:**
| id_rol_sistema | codigo   | nombre   | descripcion       | es_activo |
|----------------|----------|----------|-------------------|-----------|
| 1004           | TECNICO  | Técnico  | Soporte técnico   | 1         |
| 1005           | CLIENTE  | Cliente  | Usuario solicitante| 1        |

### Paso 2: Si NO existen, créalos con estos comandos:

```sql
-- Insertar rol Técnico (ID 1004)
SET IDENTITY_INSERT roles_sistema ON;

INSERT INTO roles_sistema (id_rol_sistema, codigo, nombre, descripcion, es_activo)
VALUES 
    (1004, 'TECNICO', 'Técnico', 'Soporte técnico', 1),
    (1005, 'CLIENTE', 'Cliente', 'Usuario solicitante', 1);

SET IDENTITY_INSERT roles_sistema OFF;
```

### Paso 3: Verifica el endpoint de roles

Tu backend debe tener este endpoint funcionando:

```
GET http://localhost:5120/api/usuarios/roles
```

**Respuesta esperada:**
```json
[
    {
        "idRolSistema": 1004,
        "codigo": "TECNICO",
        "nombre": "Técnico",
        "descripcion": "Soporte técnico",
        "esActivo": true
    },
    {
        "idRolSistema": 1005,
        "codigo": "CLIENTE",
        "nombre": "Cliente",
        "descripcion": "Usuario solicitante",
        "esActivo": true
    }
]
```

---

## 🚀 Endpoints Requeridos en el Backend

### 1. Login
```
POST http://localhost:5120/api/auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "admin123"
}
```

### 2. Listar Usuarios
```
GET http://localhost:5120/api/usuarios
```

### 3. Crear Usuario
```
POST http://localhost:5120/api/usuarios
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

### 4. Obtener Roles
```
GET http://localhost:5120/api/usuarios/roles
```

### 5. Obtener Estados
```
GET http://localhost:5120/api/usuarios/estados
```

---

## 🧪 Prueba la Conexión

### Desde tu navegador:
1. Abre: `http://localhost:5120/api/usuarios/roles`
2. Deberías ver el JSON con los roles

### Desde Postman:
1. Importa esta colección:
```json
{
    "info": {
        "name": "SLA Tracker - Usuarios"
    },
    "item": [
        {
            "name": "Obtener Roles",
            "request": {
                "method": "GET",
                "url": "http://localhost:5120/api/usuarios/roles"
            }
        },
        {
            "name": "Listar Usuarios",
            "request": {
                "method": "GET",
                "url": "http://localhost:5120/api/usuarios"
            }
        }
    ]
}
```

---

## 🔥 Solución de Problemas

### Problema 1: "No se pudo conectar al servidor"
**Causa**: El emulador no puede conectar con tu backend

**Solución**:
1. Verifica que tu backend esté corriendo en `http://localhost:5120`
2. En tu backend .NET, asegúrate de que escuche en todas las interfaces:

```csharp
// En Program.cs o Startup.cs
builder.WebHost.UseUrls("http://0.0.0.0:5120");
```

3. Verifica el firewall de Windows:
   - Permite conexiones entrantes en el puerto 5120

### Problema 2: "No aparecen los roles"
**Causa**: Los roles no existen en la base de datos o tienen IDs diferentes

**Solución**:
1. Ejecuta el query de verificación (Paso 1)
2. Si los IDs son diferentes, ejecuta:
```sql
-- Cambiar los IDs existentes a los esperados
UPDATE roles_sistema SET id_rol_sistema = 1004 WHERE codigo = 'TECNICO';
UPDATE roles_sistema SET id_rol_sistema = 1005 WHERE codigo = 'CLIENTE';
```

### Problema 3: Error 404 en endpoints
**Causa**: Las rutas del backend no coinciden

**Solución**:
Verifica que tu controlador tenga estas rutas:

```csharp
[ApiController]
[Route("api/usuarios")]
public class UsuariosController : ControllerBase
{
    [HttpGet("roles")]
    public IActionResult ObtenerRoles() { ... }
    
    [HttpGet]
    public IActionResult ObtenerUsuarios() { ... }
    
    [HttpPost]
    public IActionResult CrearUsuario([FromBody] CrearUsuarioDto dto) { ... }
}
```

---

## 📝 Verificación Final

Antes de probar la app, ejecuta estos comandos en SQL:

```sql
-- 1. Verificar roles
SELECT id_rol_sistema, codigo, nombre FROM roles_sistema 
WHERE codigo IN ('TECNICO', 'CLIENTE');

-- 2. Verificar estados de usuario
SELECT id_estado_usuario, codigo, descripcion FROM estado_usuario_catalogo;

-- 3. Verificar usuarios existentes
SELECT u.id_usuario, u.username, u.correo, r.nombre as rol, e.descripcion as estado
FROM usuario u
LEFT JOIN roles_sistema r ON u.id_rol_sistema = r.id_rol_sistema
LEFT JOIN estado_usuario_catalogo e ON u.id_estado_usuario = e.id_estado_usuario;
```

---

## ✅ Checklist de Configuración

- [ ] Backend corriendo en `http://localhost:5120`
- [ ] Roles 1004 (TECNICO) y 1005 (CLIENTE) existen en la BD
- [ ] Estado 1 (Activo) existe en estado_usuario_catalogo
- [ ] Endpoint `/api/usuarios/roles` responde correctamente
- [ ] Endpoint `/api/usuarios` responde correctamente
- [ ] Firewall permite conexiones en puerto 5120
- [ ] App Android configurada con URL correcta

---

## 🎯 Resultado Esperado

Cuando todo esté configurado:
1. La app se conectará exitosamente al backend
2. Al abrir "Usuarios", verás la lista de usuarios
3. Al hacer clic en "Agregar Usuario", verás el formulario con:
   - Campo de username
   - Campo de correo
   - Campo de contraseña
   - Selector de rol con "Técnico" y "Cliente"
4. Al crear un usuario, aparecerá en la lista con:
   - Username
   - Correo
   - Rol
   - Fecha de creación

¡Listo! 🚀

