# 🔥 Solución al Error de Conexión desde Emulador

## ❌ Problema Identificado

- ✅ **http://localhost:5120/api/sla/ping** → Funciona (desde PC)
- ❌ **http://10.0.2.2:5120/api/sla/solicitudes** → Error (desde emulador)

**Causa:** El Firewall de Windows está bloqueando conexiones externas al puerto 5120.

---

## 🛠️ Solución 1: Configurar Firewall (RECOMENDADO)

### Paso 1: Abrir PowerShell como Administrador
1. Presiona `Windows + X`
2. Selecciona **"Windows PowerShell (Administrador)"** o **"Terminal (Administrador)"**

### Paso 2: Ejecutar comandos de Firewall

Copia y pega estos comandos uno por uno:

```powershell
# Crear regla para HTTP (puerto 5120)
New-NetFirewallRule -DisplayName "ASP.NET Core API - Puerto 5120" -Direction Inbound -Protocol TCP -LocalPort 5120 -Action Allow -Profile Any

# Crear regla para HTTPS (puerto 7263)
New-NetFirewallRule -DisplayName "ASP.NET Core API - Puerto 7263" -Direction Inbound -Protocol TCP -LocalPort 7263 -Action Allow -Profile Any
```

### Paso 3: Verificar que se crearon las reglas
```powershell
Get-NetFirewallRule -DisplayName "*ASP.NET Core API*"
```

**Resultado esperado:**
```
DisplayName                 Enabled  Direction  Action
-----------                 -------  ---------  ------
ASP.NET Core API - Puerto 5120  True     Inbound    Allow
ASP.NET Core API - Puerto 7263  True     Inbound    Allow
```

### Paso 4: Reiniciar API
1. En Visual Studio, detén la API (Shift + F5)
2. Vuelve a ejecutar (F5)

### Paso 5: Probar desde navegador del emulador
1. Abre el navegador **dentro del emulador Android**
2. Navega a: `http://10.0.2.2:5120/api/sla/ping`
3. Deberías ver el JSON de respuesta

---

## 🛠️ Solución 2: Cambiar launchSettings.json (ALTERNATIVA)

Si la Solución 1 no funciona, modifica la configuración de Kestrel:

### Ubicación del archivo:
```
D:\REPOS\Proyecto movil backend\Proyecto01.API\Properties\launchSettings.json
```

### Modificación necesaria:

Busca la sección de Kestrel y cámbiala a:

```json
{
  "profiles": {
    "http": {
      "commandName": "Project",
      "dotnetRunMessages": true,
      "launchBrowser": false,
      "applicationUrl": "http://0.0.0.0:5120",
      "environmentVariables": {
        "ASPNETCORE_ENVIRONMENT": "Development"
      }
    },
    "https": {
      "commandName": "Project",
      "dotnetRunMessages": true,
      "launchBrowser": false,
      "applicationUrl": "https://0.0.0.0:7263;http://0.0.0.0:5120",
      "environmentVariables": {
        "ASPNETCORE_ENVIRONMENT": "Development"
      }
    }
  }
}
```

**Clave:** Cambiar `localhost` por `0.0.0.0` para que escuche en todas las interfaces de red.

---

## 🛠️ Solución 3: Deshabilitar Firewall Temporalmente (SOLO PARA PRUEBAS)

**⚠️ ADVERTENCIA:** Esto reduce la seguridad. Solo úsalo para probar.

```powershell
# Deshabilitar (temporal)
Set-NetFirewallProfile -Profile Domain,Public,Private -Enabled False

# Habilitar nuevamente (después de probar)
Set-NetFirewallProfile -Profile Domain,Public,Private -Enabled True
```

---

## 🛠️ Solución 4: Usar IP Local en lugar de 10.0.2.2

### Paso 1: Obtener tu IP local
```powershell
ipconfig
```

Busca tu **IPv4** (algo como `192.168.1.50` o `192.168.0.100`)

### Paso 2: Probar con esa IP
En el navegador del emulador:
```
http://192.168.1.50:5120/api/sla/ping
```

### Paso 3: Si funciona, actualizar Android
Edita `build.gradle.kts`:

```kotlin
buildTypes {
    debug {
        buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.50:5120/\"")
    }
}
```

Luego: **Sync Now → Rebuild → Run**

---

## 🧪 Verificación Paso a Paso

### Test 1: Desde PC (localhost)
```
✅ http://localhost:5120/api/sla/ping
```
**Estado:** Ya funciona

### Test 2: Desde PC con IP local
```powershell
# Obtener IP
ipconfig
# Probar en navegador
http://TU_IP_LOCAL:5120/api/sla/ping
```
**Esperado:** Debe funcionar

### Test 3: Desde navegador del emulador (10.0.2.2)
```
http://10.0.2.2:5120/api/sla/ping
```
**Esperado:** Debe funcionar después de configurar firewall

### Test 4: Desde la app Android
1. Ejecutar app
2. Ir a Predicción
3. Ver banner verde

---

## 📋 Checklist de Diagnóstico

- [ ] PowerShell abierto como Administrador
- [ ] Reglas de firewall creadas
- [ ] Reglas verificadas con Get-NetFirewallRule
- [ ] API reiniciada en Visual Studio
- [ ] Test desde PC con localhost (✅ ya funciona)
- [ ] Test desde PC con IP local (http://192.168.x.x:5120)
- [ ] Test desde navegador del emulador (http://10.0.2.2:5120)
- [ ] Test desde app Android

---

## 🔍 Diagnóstico Avanzado

### Ver qué está bloqueando el puerto
```powershell
# Ver todas las reglas de firewall para puerto 5120
Get-NetFirewallPortFilter | Where-Object { $_.LocalPort -eq 5120 } | Get-NetFirewallRule

# Ver si hay algo escuchando en el puerto
netstat -ano | findstr :5120
```

### Verificar que Kestrel escucha en todas las interfaces
En los logs de Visual Studio, busca:
```
✅ Now listening on: http://0.0.0.0:5120
```

Si dice `http://localhost:5120`, necesitas cambiar launchSettings.json (Solución 2).

---

## 🎯 Solución Rápida (Combinada)

Si tienes prisa, ejecuta esto en PowerShell como Admin:

```powershell
# 1. Crear reglas de firewall
New-NetFirewallRule -DisplayName "ASP.NET Core API - Puerto 5120" -Direction Inbound -Protocol TCP -LocalPort 5120 -Action Allow -Profile Any

# 2. Verificar IP local
ipconfig | findstr IPv4

# 3. Verificar que la API está escuchando
netstat -ano | findstr :5120
```

Luego:
1. Reinicia la API en Visual Studio (Shift + F5, luego F5)
2. Prueba `http://10.0.2.2:5120/api/sla/ping` en el navegador del emulador

---

## 💡 Explicación Técnica

### ¿Por qué localhost funciona pero 10.0.2.2 no?

- **localhost (127.0.0.1)**: Conexión interna dentro del mismo PC
- **10.0.2.2**: Conexión desde el emulador hacia el host (PC)
- El firewall **bloquea conexiones externas** pero permite internas

### ¿Qué hace 0.0.0.0?

- `0.0.0.0` significa "escuchar en todas las interfaces de red"
- Permite conexiones desde localhost, IP local, y el emulador

---

## 📱 Resultado Final

Después de aplicar la Solución 1 (firewall), deberías ver:

**En el navegador del emulador:**
```json
{
  "status": "online",
  "message": "SLA Controller funcionando correctamente",
  "timestamp": "2025-01-25T15:30:00",
  "version": "1.0"
}
```

**En la app Android:**
- Banner verde: **"✓ Conectado a API Real"**
- Predicción con datos reales de tu base de datos

---

## 🆘 Si Aún No Funciona

1. **Verifica que la API usa 0.0.0.0**:
   - Mira los logs en Visual Studio
   - Debe decir: `Now listening on: http://0.0.0.0:5120`

2. **Verifica el firewall**:
   ```powershell
   Get-NetFirewallRule -DisplayName "*ASP.NET Core API*" | Format-Table -AutoSize
   ```

3. **Prueba con IP local**:
   - Obtén tu IP: `ipconfig`
   - Prueba: `http://TU_IP:5120/api/sla/ping`
   - Si funciona, usa esa IP en Android

4. **Revisa antivirus**:
   - Algunos antivirus tienen firewall adicional
   - Temporalmente desactívalo para probar

---

**🎉 ¡Con el firewall configurado correctamente, tu emulador podrá conectarse a la API sin problemas!**

