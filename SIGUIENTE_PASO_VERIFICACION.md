# 🔧 CONFIGURACIÓN CORRECTA - Backend /api/User

## ✅ Cambios Aplicados

He cambiado todas las rutas de la app para que coincidan con tu backend:

| Endpoint Anterior | Endpoint Actual | Estado |
|-------------------|-----------------|--------|
| `/api/usuarios` | `/api/User` | ✅ Corregido |
| `/api/usuarios/{id}` | `/api/User/{id}` | ✅ Corregido |
| `/api/usuarios/roles` | `/api/User/roles` | ✅ Corregido |

---

## 🎯 Próximos Pasos OBLIGATORIOS

### 1. Verifica la Estructura de Respuesta

Ejecuta el script que creé:
```powershell
cd C:\Users\meyle\AndroidStudioProjects\Proyecto1
.\probar-backend-user.ps1
```

O prueba manualmente en Postman:
```
GET http://localhost:5120/api/User
```

**Copia la respuesta completa aquí**

---

## 📊 Posibles Estructuras de Respuesta

Tu backend puede devolver los usuarios en 3 formatos diferentes:

### Opción A: Array Directo (Más común)
```json
[
    {
        "idUsuario": 1,
        "username": "Martin",
        "correo": "22200248@ue.edu.pe",
        "idRolSistema": 1003,
        "rolNombre": "Administrador",
        "idEstadoUsuario": 1,
        "creadoEn": "2025-11-27T10:30:00",
        "personal": {
            "nombres": "Martin",
            "apellidos": "Lucas"
        }
    }
]
```

### Opción B: Objeto con Array
```json
{
    "success": true,
    "usuarios": [...],
    "total": 1
}
```

### Opción C: Solo Campos Básicos
```json
[
    {
        "id_usuario": 1,
        "username": "Martin",
        "correo": "22200248@ue.edu.pe"
    }
]
```

---

## 🔧 Según tu Respuesta, Necesitamos Ajustar

### Si es Opción A (Array Directo):

**Necesito modificar `AuthRepository.kt`:**

```kotlin
// Cambiar de:
val body = response.body()
if (body != null) {
    Result.success(body) // Espera ListaUsuariosResponseDto
}

// A:
val usuarios = response.body()
if (usuarios != null) {
    val wrapped = ListaUsuariosResponseDto(
        success = true,
        usuarios = usuarios,
        total = usuarios.size
    )
    Result.success(wrapped)
}
```

### Si es Opción B (ya está lista) ✅

La app ya está configurada para esto.

### Si es Opción C (Campos diferentes):

Necesito ver los nombres exactos de los campos para mapearlos.

---

## 📝 Endpoints que Debe Tener tu Backend

Para que la app funcione completamente, tu backend debe tener:

### 1. Listar Usuarios ✅
```
GET /api/User
```
**Ya existe según tus logs**

### 2. Crear Usuario ⚠️
```
POST /api/User
```
**Verifica si existe**

### 3. Actualizar Usuario ⚠️
```
PUT /api/User/{id}
```
**Verifica si existe**

### 4. Eliminar Usuario ⚠️
```
DELETE /api/User/{id}
```
**Verifica si existe**

### 5. Obtener Roles ❓
```
GET /api/User/roles
```
**Probablemente NO existe, necesitas crearlo**

---

## 🔍 Verifica tus Endpoints

En Postman, prueba:

1. ✅ `GET http://localhost:5120/api/User` (ya funciona)
2. ❓ `GET http://localhost:5120/api/User/roles`
3. ❓ `POST http://localhost:5120/api/User` con body:
   ```json
   {
       "username": "test",
       "correo": "test@test.com",
       "password": "123456",
       "idRolSistema": 1004,
       "idEstadoUsuario": 1
   }
   ```

---

## 🚀 Mientras Tanto, Rebuild de la App

```
Build > Clean Project
Build > Rebuild Project
```

**Esto aplicará los cambios de rutas que hice.**

---

## 📞 Necesito que me Digas

1. **¿Qué estructura devuelve `GET /api/User`?**
   - Copia y pega el JSON completo

2. **¿Existe el endpoint `/api/User/roles`?**
   - Sí / No

3. **¿Tu backend tiene POST, PUT, DELETE para User?**
   - Sí / No

Con esta información, ajustaré el código perfectamente para tu backend.

---

## 🎯 Lo Que Cambié

✅ Rutas de `/api/usuarios` → `/api/User`
✅ Puerto correcto 5120
✅ Script de prueba creado

**Falta:**
⏳ Ajustar estructura de respuesta según tu backend
⏳ Verificar endpoints faltantes
⏳ Rebuild de la app

