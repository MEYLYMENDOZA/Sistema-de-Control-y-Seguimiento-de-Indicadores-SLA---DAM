# Script para detectar la IP WiFi automáticamente y actualizar el proyecto
# Ejecuta este script cuando cambies de red WiFi

Write-Host "🌐 Detectando IP de WiFi..." -ForegroundColor Cyan

# Obtener la IP del adaptador WiFi
$wifiAdapter = Get-NetIPAddress -AddressFamily IPv4 -InterfaceAlias "*Wi-Fi*" -ErrorAction SilentlyContinue | Where-Object { $_.IPAddress -notlike "169.254.*" }

if ($null -eq $wifiAdapter) {
    Write-Host "❌ No se detectó adaptador WiFi o no está conectado" -ForegroundColor Red
    Write-Host "ℹ️  Conecta tu PC a WiFi e intenta de nuevo" -ForegroundColor Yellow
    exit 1
}

$currentIP = $wifiAdapter.IPAddress
Write-Host "✅ IP WiFi detectada: $currentIP" -ForegroundColor Green

# Mostrar configuración de red
Write-Host ""
Write-Host "📡 Información de red:" -ForegroundColor Cyan
Write-Host "  IP: $currentIP"
Write-Host "  Máscara: $($wifiAdapter.PrefixLength)"
Write-Host "  Interfaz: $($wifiAdapter.InterfaceAlias)"

# Construir la nueva URL de API
$newApiUrl = "http://${currentIP}:5120/"
Write-Host ""
Write-Host "🔧 Nueva URL de API: $newApiUrl" -ForegroundColor Yellow

# Actualizar build.gradle.kts
$buildGradlePath = "D:\REPOS\Sistema-de-Control-y-Seguimiento-de-Indicadores-SLA---DAM\app\build.gradle.kts"

if (Test-Path $buildGradlePath) {
    Write-Host ""
    Write-Host "📝 Actualizando build.gradle.kts..." -ForegroundColor Cyan

    $content = Get-Content $buildGradlePath -Raw

    # Buscar y reemplazar las URLs en buildTypes
    $pattern = 'buildConfigField\("String", "API_BASE_URL", "\\\"http://[0-9.]+:5120/\\\"\"\)'
    $replacement = "buildConfigField(`"String`", `"API_BASE_URL`", `"`\`"$newApiUrl`\`"`")"

    $newContent = $content -replace $pattern, $replacement

    Set-Content -Path $buildGradlePath -Value $newContent

    Write-Host "  ✅ build.gradle.kts actualizado" -ForegroundColor Green
} else {
    Write-Host "❌ No se encontró build.gradle.kts" -ForegroundColor Red
    exit 1
}

# Actualizar RetrofitClient.kt
$retrofitClientPath = "D:\REPOS\Sistema-de-Control-y-Seguimiento-de-Indicadores-SLA---DAM\app\src\main\java\com\example\proyecto1\data\remote\api\RetrofitClient.kt"

if (Test-Path $retrofitClientPath) {
    Write-Host "📝 Actualizando RetrofitClient.kt..." -ForegroundColor Cyan

    $content = Get-Content $retrofitClientPath -Raw

    # Reemplazar la IP en el fallback
    $pattern = '"http://[0-9.]+:5120/"'
    $replacement = "`"$newApiUrl`""

    $newContent = $content -replace $pattern, $replacement

    Set-Content -Path $retrofitClientPath -Value $newContent

    Write-Host "  ✅ RetrofitClient.kt actualizado" -ForegroundColor Green
} else {
    Write-Host "❌ No se encontró RetrofitClient.kt" -ForegroundColor Red
}

Write-Host ""
Write-Host "✨ Configuración actualizada exitosamente!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 PRÓXIMOS PASOS:" -ForegroundColor Cyan
Write-Host "1. En Android Studio:" -ForegroundColor White
Write-Host "   - Build → Clean Project" -ForegroundColor White
Write-Host "   - Build → Rebuild Project" -ForegroundColor White
Write-Host "   - File → Invalidate Caches / Restart" -ForegroundColor White
Write-Host ""
Write-Host "2. En tu celular:" -ForegroundColor White
Write-Host "   - Desinstala la app completamente" -ForegroundColor White
Write-Host ""
Write-Host "3. En Android Studio:" -ForegroundColor White
Write-Host "   - Run (Shift+F10) para reinstalar" -ForegroundColor White
Write-Host ""
Write-Host "🧪 VERIFICACIÓN PREVIA:" -ForegroundColor Yellow
Write-Host "Abre en el navegador de tu celular:" -ForegroundColor White
Write-Host "  $newApiUrl" -NoNewline -ForegroundColor Cyan
Write-Host "api/sla/ping" -ForegroundColor Cyan
Write-Host ""
Write-Host "Debe retornar: { `"status`": `"online`" ... }" -ForegroundColor Gray
Write-Host ""

# Prueba de conectividad local
Write-Host "🔍 Probando conectividad local..." -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "${newApiUrl}api/sla/ping" -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
    Write-Host "✅ API respondiendo correctamente en localhost" -ForegroundColor Green
    Write-Host "  Status: $($response.StatusCode)" -ForegroundColor Gray
} catch {
    Write-Host "⚠️  API no está respondiendo en localhost" -ForegroundColor Yellow
    Write-Host "  Asegúrate de que Visual Studio 2022 esté ejecutando la API" -ForegroundColor Gray
}

Write-Host ""
Write-Host "🎯 Configuración de red guardada:" -ForegroundColor Cyan
Write-Host "  IP actual: $currentIP" -ForegroundColor White
Write-Host "  URL API: $newApiUrl" -ForegroundColor White
Write-Host ""

