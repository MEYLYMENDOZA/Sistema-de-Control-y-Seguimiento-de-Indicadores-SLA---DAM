# Script para obtener IP local y generar configuración para Android

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  CONFIGURACIÓN PARA DISPOSITIVO FÍSICO" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Obtener IP local
$ipAddress = (Get-NetIPAddress -AddressFamily IPv4 -InterfaceAlias (Get-NetAdapter | Where-Object {$_.Status -eq "Up" -and $_.Name -like "*Wi-Fi*" -or $_.Name -like "*Ethernet*"} | Select-Object -First 1).Name).IPAddress

if ($ipAddress) {
    Write-Host "✅ IP Local detectada: $ipAddress" -ForegroundColor Green
    Write-Host ""

    Write-Host "📋 PASOS A SEGUIR:" -ForegroundColor Yellow
    Write-Host ""

    Write-Host "1️⃣  Verifica que tu celular y PC están en la MISMA red WiFi" -ForegroundColor White
    Write-Host ""

    Write-Host "2️⃣  Prueba desde el navegador de tu celular:" -ForegroundColor White
    Write-Host "   http://$ipAddress:5120/api/sla/ping" -ForegroundColor Cyan
    Write-Host ""

    Write-Host "3️⃣  Si funciona, actualiza build.gradle.kts:" -ForegroundColor White
    Write-Host ""
    Write-Host "   BUSCA (línea 40):" -ForegroundColor Yellow
    Write-Host '   buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:5120/\"")' -ForegroundColor Red
    Write-Host ""
    Write-Host "   REEMPLAZA POR:" -ForegroundColor Yellow
    Write-Host "   buildConfigField(`"String`", `"API_BASE_URL`", `"`"http://$ipAddress:5120/`"`")" -ForegroundColor Green
    Write-Host ""

    Write-Host "4️⃣  En Android Studio:" -ForegroundColor White
    Write-Host "   - Click en 'Sync Now'" -ForegroundColor White
    Write-Host "   - Rebuild Project" -ForegroundColor White
    Write-Host "   - Run en tu celular" -ForegroundColor White
    Write-Host ""

    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "URL COMPLETA PARA COPIAR:" -ForegroundColor Yellow
    Write-Host "http://$ipAddress:5120/" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan

} else {
    Write-Host "❌ No se pudo detectar la IP automáticamente" -ForegroundColor Red
    Write-Host ""
    Write-Host "Ejecuta manualmente: ipconfig" -ForegroundColor Yellow
    Write-Host "Y busca la 'Dirección IPv4' de tu adaptador WiFi o Ethernet" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Presiona cualquier tecla para continuar..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

