# Guía de Testing - Aplicación SLA Tracker

## Cómo Probar la Aplicación

### 1. **Probar el Flujo de Login**

**Paso 1: Ejecuta la app**
```bash
# En Android Studio
Run → Run 'app'
# O presiona Shift + F10
```

**Paso 2: Verifica que se muestra LoginScreen**
- ✓ Deberías ver la pantalla de login con:
  - Ícono de candado
  - Título: "Sistema SLA Tracker"
  - Subtítulo: "Control y Seguimiento de Indicadores"
  - Campo de usuario
  - Campo de contraseña
  - Botón "Iniciar Sesión"

**Paso 3: Intenta login con credenciales válidas**
```
Usuario: admin
Contraseña: cualquier valor (ej: 123)
```

- ✓ Deberías ver un indicador de carga
- ✓ Después de 1-2 segundos, navegará a CargaDatosScreen
- ✓ Aparecerá la BottomNavBar con 3 opciones: Carga, Gestión, Usuarios

**Paso 4: Intenta login con usuario inválido**
```
Usuario: invaliduser
Contraseña: cualquier valor
```

- ✓ Deberías ver el mensaje de error: "Invalid credentials"
- ✓ Los campos de texto y botón permanecerán habilitados

---

### 2. **Probar la Navegación entre Pantallas**

Una vez autenticado:

**Navegar entre tabs:**
- ✓ Toca "Carga de Datos" → se muestra CargaDatosScreen
- ✓ Toca "Gestión de Datos" → se muestra GestionDatosScreen
- ✓ Toca "Usuarios" → se muestra UserListScreen con lista de usuarios
- ✓ Las transiciones deben ser suaves sin bloqueos

**Verificar que LoginScreen no vuelve:**
- ✓ Usa el botón atrás del dispositivo
- ✓ Deberías navegar entre las pantallas principales, nunca volver a LoginScreen

---

### 3. **Probar la Pantalla de Usuarios**

En la pestaña "Usuarios":

**Verificar datos iniciales:**
- ✓ Se muestran 2 usuarios en la lista
- ✓ Se ven contadores: Verificados: 1, Pendientes: 1

**Prueba de búsqueda:**
- Escribe "admin" en el campo de búsqueda
  - ✓ Se filtra solo el usuario admin
- Escribe "analyst" en el campo de búsqueda
  - ✓ Se filtra solo el usuario analyst
- Borra el texto de búsqueda
  - ✓ Se muestran todos los usuarios nuevamente

**Prueba del botón "Agregar Usuario":**
- ✓ Toca el FAB (botón flotante azul con ícono +)
- ✓ Se abre un diálogo para agregar nuevo usuario
- Completa los datos y presiona "Guardar"
- ✓ El nuevo usuario aparece en la lista

---

### 4. **Usuarios de Prueba Disponibles**

| Usuario | Contraseña | Nota |
|---------|-----------|------|
| admin | cualquiera | Usuario administrador |
| analyst | cualquiera | Usuario analista |
| cualquier otro | cualquiera | No existe → error |

> **Nota técnica:** El sistema actualmente acepta cualquier contraseña. En producción debes agregar hash seguro.

---

### 5. **Pruebas Avanzadas**

**Test de rotación de pantalla:**
- ✓ Durante el login, rota el dispositivo
- ✓ El estado del formulario debe preservarse
- ✓ El estado de carga debe continuar

**Test de cambio rápido de tabs:**
- ✓ Cambia rápidamente entre tabs
- ✓ No debe haber crashes o pantallas en blanco

**Test de entrada de datos:**
- ✓ Intenta dejar campos vacíos en login
- ✓ El botón debe estar deshabilitado
- ✓ Solo habilita cuando ambos campos tienen contenido

---

### 6. **Verificación de Pantalla Negra (Solución)**

**Antes de la corrección:**
- ❌ Al iniciar la app, pantalla negra
- ❌ No se mostraba LoginScreen
- ❌ Poco después la app se cerraba

**Después de la corrección:**
- ✅ Al iniciar, se muestra LoginScreen inmediatamente
- ✅ Todos los elementos están visibles y funcionales
- ✅ El flujo de navegación es consistente

---

### 7. **Logs para Debugging**

Si necesitas debugging, agrega estos logs a LoginViewModel:

```kotlin
fun login(username: String, password: String) {
    viewModelScope.launch {
        _loginState.value = LoginState.Loading
        Log.d("LoginViewModel", "Intentando login con usuario: $username")
        val user = userRepository.login(username, password)
        if (user != null) {
            Log.d("LoginViewModel", "Login exitoso: ${user.username}")
            _loginState.value = LoginState.Success(user)
        } else {
            Log.d("LoginViewModel", "Login fallido: usuario no encontrado")
            _loginState.value = LoginState.Error("Invalid credentials")
        }
    }
}
```

---

### 8. **Checklist Final**

- [ ] App inicia sin pantalla negra
- [ ] Se muestra LoginScreen
- [ ] Login con "admin" funciona
- [ ] Login con usuario inválido muestra error
- [ ] Navegación a CargaDatosScreen funciona
- [ ] BottomNavBar aparece después de login
- [ ] Puedo navegar entre Carga, Gestión y Usuarios
- [ ] ListaScreen de usuarios muestra datos
- [ ] Búsqueda de usuarios funciona
- [ ] No hay crashes durante la navegación

---

## Solución de Problemas Comunes

### "Aún veo pantalla negra"
1. Haz un rebuild del proyecto: `Build → Rebuild Project`
2. Limpia la caché: `File → Invalidate Caches → Invalidate and Restart`
3. Desinstala la app anterior: `adb uninstall com.example.proyecto1`
4. Ejecuta nuevamente

### "El botón de login no funciona"
1. Verifica que hayas escrito en ambos campos
2. El botón debe estar habilitado (azul)
3. Revisa los logs del Android Studio

### "Se cierra la app después de login"
1. Revisa la consola de errores en Android Studio
2. Verifica que UserAdministrationViewModelFactory está correctamente configurado
3. Asegúrate de que todas las importaciones son correctas

### "El usuario no aparece en el formulario de login"
1. Verifica que UserRepository tiene datos inicializados
2. Haz un rebuild
3. Revisa los logs de UserRepository


