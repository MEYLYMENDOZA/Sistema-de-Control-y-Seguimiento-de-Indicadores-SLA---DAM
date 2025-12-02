# Script de Diagnóstico de Conexión Backend
# Ejecutar en PowerShell: .\diagnostico-backend.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  DIAGNÓSTICO DE BACKEND - Puerto 5210" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Verificar si el puerto 5210 está en uso
Write-Host "[1/5] Verificando puerto 5210..." -ForegroundColor Yellow
$portCheck = netstat -ano | findstr ":5210"

if ($portCheck) {
    Write-Host "✅ Puerto 5210 está ACTIVO" -ForegroundColor Green
    Write-Host $portCheck
} else {
    Write-Host "❌ Puerto 5210 NO está activo" -ForegroundColor Red
    Write-Host "   SOLUCIÓN: Inicia tu backend .NET primero" -ForegroundColor Yellow
    Write-Host ""
    exit 1
}

Write-Host ""

# 2. Obtener IP local
Write-Host "[2/5] Obteniendo IP local..." -ForegroundColor Yellow
$ipAddress = (Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.InterfaceAlias -notlike "*Loopback*" -and $_.IPAddress -notlike "169.254.*" }).IPAddress | Select-Object -First 1

if ($ipAddress) {
    Write-Host "✅ IP local: $ipAddress" -ForegroundColor Green
} else {
    Write-Host "⚠️  No se pudo obtener IP local" -ForegroundColor Yellow
}

Write-Host ""

# 3. Verificar conectividad localhost
Write-Host "[3/5] Probando conectividad a localhost:5210..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:5210/api/usuarios/roles" -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
    Write-Host "✅ Backend responde correctamente" -ForegroundColor Green
    Write-Host "   Status: $($response.StatusCode)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Backend NO responde" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   SOLUCIÓN: Verifica que tu backend tenga el endpoint /api/usuarios/roles" -ForegroundColor Yellow
}

Write-Host ""

# 4. Verificar regla de firewall
Write-Host "[4/5] Verificando firewall..." -ForegroundColor Yellow
$firewallRule = Get-NetFirewallRule -DisplayName "*5210*" -ErrorAction SilentlyContinue

if ($firewallRule) {
    Write-Host "✅ Regla de firewall encontrada" -ForegroundColor Green
} else {
    Write-Host "⚠️  No hay regla de firewall para el puerto 5210" -ForegroundColor Yellow
    Write-Host "   Creando regla..." -ForegroundColor Yellow

    try {
        New-NetFirewallRule -DisplayName "Backend API 5210" -Direction Inbound -LocalPort 5210 -Protocol TCP -Action Allow -ErrorAction Stop | Out-Null
        Write-Host "✅ Regla de firewall creada exitosamente" -ForegroundColor Green
    } catch {
        Write-Host "❌ Error al crear regla de firewall (requiere permisos de administrador)" -ForegroundColor Red
    }
}

Write-Host ""

# 5. Resumen
Write-Host "[5/5] RESUMEN" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "URLs configuradas en tu app:" -ForegroundColor White
Write-Host "  • Emulador:         http://10.0.2.2:5210/" -ForegroundColor Gray
Write-Host "  • Dispositivo físico: http://${ipAddress}:5210/" -ForegroundColor Gray
Write-Host ""
Write-Host "Para probar manualmente:" -ForegroundColor White
Write-Host "  • Navegador: http://localhost:5210/api/usuarios/roles" -ForegroundColor Gray
Write-Host ""

if ($portCheck -and $response) {
    Write-Host "✅ TODO LISTO - Tu backend está funcionando correctamente" -ForegroundColor Green
    Write-Host ""
    Write-Host "Próximos pasos:" -ForegroundColor Yellow
    Write-Host "  1. En Android Studio: Build > Rebuild Project" -ForegroundColor White
    Write-Host "  2. Ejecuta la app en el emulador" -ForegroundColor White
    Write-Host "  3. Ve a 'Usuarios' y prueba crear un usuario" -ForegroundColor White
} else {
    Write-Host "❌ HAY PROBLEMAS - Revisa los errores arriba" -ForegroundColor Red
    Write-Host ""
    Write-Host "Soluciones:" -ForegroundColor Yellow
    Write-Host "  1. Inicia tu backend .NET (dotnet run o F5 en Visual Studio)" -ForegroundColor White
    Write-Host "  2. Verifica que escuche en puerto 5210" -ForegroundColor White
    Write-Host "  3. Ejecuta este script de nuevo" -ForegroundColor White
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan

