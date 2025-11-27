# Script para configurar el firewall de Windows para la API
# Ejecutar como Administrador

Write-Host "🔥 CONFIGURANDO FIREWALL PARA API" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan
Write-Host ""

# Verificar si se está ejecutando como administrador
$currentPrincipal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
$isAdmin = $currentPrincipal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Write-Host "❌ ERROR: Este script requiere permisos de administrador" -ForegroundColor Red
    Write-Host ""
    Write-Host "Haz clic derecho en PowerShell y selecciona 'Ejecutar como administrador'" -ForegroundColor Yellow
    Write-Host ""
    pause
    exit 1
}

Write-Host "✅ Ejecutando como administrador" -ForegroundColor Green
Write-Host ""

# Puerto de la API
$apiPort = 5120

# Nombre de la regla
$ruleName = "ASP.NET Core API - Puerto $apiPort"

# Verificar si la regla ya existe
$existingRule = Get-NetFirewallRule -DisplayName $ruleName -ErrorAction SilentlyContinue

if ($existingRule) {
    Write-Host "⚠️  La regla de firewall ya existe" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "¿Deseas recrearla? (S/N): " -NoNewline -ForegroundColor Cyan
    $response = Read-Host

    if ($response -eq 'S' -or $response -eq 's') {
        Write-Host "🗑️  Eliminando regla anterior..." -ForegroundColor Yellow
        Remove-NetFirewallRule -DisplayName $ruleName
        Write-Host "✅ Regla anterior eliminada" -ForegroundColor Green
    } else {
        Write-Host "❌ Operación cancelada" -ForegroundColor Red
        pause
        exit 0
    }
}

Write-Host "📝 Creando regla de firewall..." -ForegroundColor Cyan

try {
    # Crear regla de entrada (Inbound)
    New-NetFirewallRule `
        -DisplayName $ruleName `
        -Direction Inbound `
        -LocalPort $apiPort `
        -Protocol TCP `
        -Action Allow `
        -Profile Domain,Private,Public `
        -Description "Permite conexiones entrantes al API ASP.NET Core en el puerto $apiPort para desarrollo local" `
        -ErrorAction Stop | Out-Null

    Write-Host "✅ Regla de firewall creada exitosamente" -ForegroundColor Green
    Write-Host ""
    Write-Host "📋 DETALLES:" -ForegroundColor Cyan
    Write-Host "   Puerto: $apiPort (TCP)" -ForegroundColor White
    Write-Host "   Dirección: Entrante (Inbound)" -ForegroundColor White
    Write-Host "   Perfiles: Dominio, Privado, Público" -ForegroundColor White
    Write-Host ""

    # Mostrar la IP local
    Write-Host "🌐 TU IP LOCAL:" -ForegroundColor Cyan
    $localIPs = Get-NetIPAddress -AddressFamily IPv4 |
                Where-Object { $_.PrefixOrigin -eq "Dhcp" -or $_.PrefixOrigin -eq "Manual" } |
                Where-Object { $_.IPAddress -notlike "127.*" -and $_.IPAddress -notlike "169.254.*" } |
                Select-Object -ExpandProperty IPAddress

    if ($localIPs) {
        foreach ($ip in $localIPs) {
            Write-Host "   http://${ip}:${apiPort}/" -ForegroundColor Green
        }
    } else {
        Write-Host "   No se pudo detectar la IP local" -ForegroundColor Yellow
    }

    Write-Host ""
    Write-Host "✅ CONFIGURACIÓN COMPLETADA" -ForegroundColor Green
    Write-Host ""
    Write-Host "💡 Ahora tu dispositivo Android podrá conectarse a la API" -ForegroundColor Cyan
    Write-Host "   Asegúrate de que:" -ForegroundColor White
    Write-Host "   1. El servidor API esté corriendo" -ForegroundColor White
    Write-Host "   2. PC y celular estén en la misma WiFi" -ForegroundColor White
    Write-Host ""

} catch {
    Write-Host "❌ ERROR al crear la regla de firewall:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host ""
    pause
    exit 1
}

# Verificar si el puerto está en uso
Write-Host "🔍 Verificando si el puerto $apiPort está en uso..." -ForegroundColor Cyan
$portInUse = Get-NetTCPConnection -LocalPort $apiPort -ErrorAction SilentlyContinue

if ($portInUse) {
    Write-Host "✅ El puerto $apiPort está en uso (API corriendo)" -ForegroundColor Green
    Write-Host ""
    $portInUse | ForEach-Object {
        Write-Host "   Estado: $($_.State)" -ForegroundColor White
        Write-Host "   PID: $($_.OwningProcess)" -ForegroundColor White
    }
} else {
    Write-Host "⚠️  El puerto $apiPort NO está en uso" -ForegroundColor Yellow
    Write-Host "   Asegúrate de iniciar la API antes de probar desde Android" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🎉 ¡Todo listo! Ahora prueba tu app Android" -ForegroundColor Green
Write-Host ""
pause

