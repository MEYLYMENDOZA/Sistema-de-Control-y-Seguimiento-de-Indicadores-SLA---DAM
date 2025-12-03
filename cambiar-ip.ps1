# Script para cambiar la IP del servidor en la app Android
# Uso: .\cambiar-ip.ps1 [IP]
# Ejemplo: .\cambiar-ip.ps1 192.168.100.4

param(
    [string]$NuevaIP
)

$ServerConfigPath = "app\src\main\res\values\server_config.xml"

# Si no se proporciona IP, obtener la IP actual de la máquina
if ([string]::IsNullOrEmpty($NuevaIP)) {
    Write-Host "🔍 Detectando IP de la máquina..." -ForegroundColor Cyan

    # Obtener todas las IPs IPv4 (excluyendo loopback y Bluetooth)
    $IPs = Get-NetIPAddress -AddressFamily IPv4 |
           Where-Object {
               $_.InterfaceAlias -notlike "*Bluetooth*" -and
               $_.InterfaceAlias -notlike "*Loopback*" -and
               $_.IPAddress -ne "127.0.0.1"
           } |
           Select-Object IPAddress, InterfaceAlias

    if ($IPs.Count -eq 0) {
        Write-Host "❌ No se encontraron interfaces de red válidas" -ForegroundColor Red
        exit 1
    }

    Write-Host "`n📋 Interfaces de red disponibles:" -ForegroundColor Yellow
    for ($i = 0; $i -lt $IPs.Count; $i++) {
        Write-Host "  [$i] $($IPs[$i].IPAddress) - $($IPs[$i].InterfaceAlias)"
    }

    if ($IPs.Count -eq 1) {
        $NuevaIP = $IPs[0].IPAddress
        Write-Host "`n✅ Usando automáticamente: $NuevaIP" -ForegroundColor Green
    } else {
        $seleccion = Read-Host "`nSelecciona el número de la interfaz (0-$($IPs.Count - 1))"
        if ($seleccion -match '^\d+$' -and [int]$seleccion -lt $IPs.Count) {
            $NuevaIP = $IPs[[int]$seleccion].IPAddress
            Write-Host "✅ Seleccionada: $NuevaIP" -ForegroundColor Green
        } else {
            Write-Host "❌ Selección inválida" -ForegroundColor Red
            exit 1
        }
    }
}

# Validar formato de IP
if ($NuevaIP -notmatch '^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$') {
    Write-Host "❌ IP inválida: $NuevaIP" -ForegroundColor Red
    Write-Host "   Formato esperado: 192.168.1.100" -ForegroundColor Yellow
    exit 1
}

# Verificar que el archivo existe
if (-not (Test-Path $ServerConfigPath)) {
    Write-Host "❌ No se encontró el archivo: $ServerConfigPath" -ForegroundColor Red
    exit 1
}

# Leer el contenido actual
$contenido = Get-Content $ServerConfigPath -Raw

# Obtener la IP actual
if ($contenido -match '<string name="server_ip"[^>]*>([^<]+)</string>') {
    $IPActual = $matches[1]
    Write-Host "`n📍 IP actual: $IPActual" -ForegroundColor Yellow
} else {
    Write-Host "`n⚠️  No se pudo detectar la IP actual" -ForegroundColor Yellow
}

# Reemplazar la IP
$nuevoContenido = $contenido -replace '(<string name="server_ip"[^>]*>)[^<]+(</string>)', "`${1}$NuevaIP`$2"

# Guardar el archivo
Set-Content -Path $ServerConfigPath -Value $nuevoContenido -NoNewline

Write-Host "✅ IP actualizada a: $NuevaIP" -ForegroundColor Green

# Preguntar si desea recompilar
$respuesta = Read-Host "`n¿Deseas recompilar el proyecto ahora? (S/N)"
if ($respuesta -eq 'S' -or $respuesta -eq 's') {
    Write-Host "`n🔨 Limpiando proyecto..." -ForegroundColor Cyan
    & .\gradlew.bat clean

    Write-Host "`n🔨 Compilando proyecto..." -ForegroundColor Cyan
    & .\gradlew.bat assembleDebug

    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n✅ ¡Proyecto compilado exitosamente!" -ForegroundColor Green
        Write-Host "   Ahora puedes ejecutar la app en tu dispositivo" -ForegroundColor Green
    } else {
        Write-Host "`n❌ Error en la compilación" -ForegroundColor Red
    }
} else {
    Write-Host "`n⚠️  Recuerda recompilar el proyecto antes de ejecutar la app:" -ForegroundColor Yellow
    Write-Host "   .\gradlew.bat clean" -ForegroundColor Cyan
    Write-Host "   .\gradlew.bat assembleDebug" -ForegroundColor Cyan
}

Write-Host "`n📄 Archivo modificado: $ServerConfigPath" -ForegroundColor Cyan

