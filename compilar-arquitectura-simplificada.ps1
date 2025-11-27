# Script para compilar y ejecutar el proyecto con arquitectura simplificada
# Uso: .\compilar-arquitectura-simplificada.ps1

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  COMPILAR ARQUITECTURA SIMPLIFICADA" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Función para verificar si un proceso está ejecutándose
function Test-ProcessRunning {
    param([string]$ProcessName)
    return (Get-Process -Name $ProcessName -ErrorAction SilentlyContinue) -ne $null
}

# PASO 1: Verificar que estamos en el directorio correcto
$currentDir = Get-Location
Write-Host "📁 Directorio actual: $currentDir" -ForegroundColor Yellow
Write-Host ""

if (-not (Test-Path ".\gradlew.bat")) {
    Write-Host "❌ ERROR: No se encontró gradlew.bat" -ForegroundColor Red
    Write-Host "   Asegúrate de estar en el directorio raíz del proyecto Android" -ForegroundColor Red
    exit 1
}

# PASO 2: Limpiar proyecto
Write-Host "🧹 PASO 1: Limpiando proyecto..." -ForegroundColor Green
Write-Host "   Ejecutando: .\gradlew clean" -ForegroundColor Gray
.\gradlew clean

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error al limpiar el proyecto" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Proyecto limpiado exitosamente" -ForegroundColor Green
Write-Host ""

# PASO 3: Compilar proyecto
Write-Host "🔨 PASO 2: Compilando proyecto..." -ForegroundColor Green
Write-Host "   Ejecutando: .\gradlew build" -ForegroundColor Gray
.\gradlew build

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error al compilar el proyecto" -ForegroundColor Red
    Write-Host "   Revisa los errores arriba y:" -ForegroundColor Yellow
    Write-Host "   1. Verifica que Android Studio haya indexado todos los archivos" -ForegroundColor Yellow
    Write-Host "   2. Ejecuta: File → Invalidate Caches / Restart" -ForegroundColor Yellow
    Write-Host "   3. Vuelve a ejecutar este script" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Proyecto compilado exitosamente" -ForegroundColor Green
Write-Host ""

# PASO 4: Verificar dispositivos conectados
Write-Host "📱 PASO 3: Verificando dispositivos..." -ForegroundColor Green

# Buscar adb en rutas comunes
$adbPaths = @(
    "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
    "$env:ANDROID_HOME\platform-tools\adb.exe",
    "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk\platform-tools\adb.exe"
)

$adbPath = $null
foreach ($path in $adbPaths) {
    if (Test-Path $path) {
        $adbPath = $path
        break
    }
}

if ($adbPath -eq $null) {
    Write-Host "⚠️  No se encontró adb.exe" -ForegroundColor Yellow
    Write-Host "   Instala el APK manualmente desde:" -ForegroundColor Yellow
    Write-Host "   app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Gray
} else {
    Write-Host "   Ejecutando: adb devices" -ForegroundColor Gray
    $devices = & $adbPath devices | Select-String -Pattern "device$"

    if ($devices.Count -eq 0) {
        Write-Host "⚠️  No hay dispositivos conectados" -ForegroundColor Yellow
        Write-Host "   1. Conecta un dispositivo físico o inicia un emulador" -ForegroundColor Yellow
        Write-Host "   2. O instala manualmente: app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Yellow
    } else {
        Write-Host "✅ Dispositivos encontrados: $($devices.Count)" -ForegroundColor Green
        Write-Host ""

        # PASO 5: Instalar APK
        Write-Host "📲 PASO 4: Instalando APK..." -ForegroundColor Green
        Write-Host "   Ejecutando: .\gradlew installDebug" -ForegroundColor Gray
        .\gradlew installDebug

        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ APK instalado exitosamente" -ForegroundColor Green
        } else {
            Write-Host "❌ Error al instalar APK" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  COMPILACIÓN COMPLETADA" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# PASO 6: Verificar backend
Write-Host "🔍 VERIFICANDO BACKEND..." -ForegroundColor Magenta
Write-Host ""

# Verificar si Visual Studio está ejecutándose
$vsRunning = Test-ProcessRunning -ProcessName "devenv"

if ($vsRunning) {
    Write-Host "✅ Visual Studio está ejecutándose" -ForegroundColor Green
    Write-Host "   Verifica que la API esté corriendo (F5 en Visual Studio)" -ForegroundColor Yellow
} else {
    Write-Host "⚠️  Visual Studio NO está ejecutándose" -ForegroundColor Yellow
    Write-Host "   1. Abre Visual Studio 2022" -ForegroundColor Yellow
    Write-Host "   2. Abre la solución del backend" -ForegroundColor Yellow
    Write-Host "   3. Agrega ReporteController.cs al proyecto" -ForegroundColor Yellow
    Write-Host "   4. Build → Rebuild Solution" -ForegroundColor Yellow
    Write-Host "   5. Ejecuta la API (F5)" -ForegroundColor Yellow
}

Write-Host ""

# Intentar hacer ping al backend
Write-Host "📡 Probando conexión con backend..." -ForegroundColor Magenta

$apiUrl = "http://localhost:5120/api/reporte/anios-disponibles"

try {
    $response = Invoke-WebRequest -Uri $apiUrl -Method GET -TimeoutSec 3 -ErrorAction Stop
    Write-Host "✅ Backend respondiendo correctamente" -ForegroundColor Green
    Write-Host "   URL: $apiUrl" -ForegroundColor Gray
    Write-Host "   Status: $($response.StatusCode)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Backend NO está respondiendo" -ForegroundColor Red
    Write-Host "   URL probada: $apiUrl" -ForegroundColor Gray
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Gray
    Write-Host ""
    Write-Host "   SOLUCIÓN:" -ForegroundColor Yellow
    Write-Host "   1. Inicia la API en Visual Studio (F5)" -ForegroundColor Yellow
    Write-Host "   2. Verifica que esté escuchando en puerto 5120" -ForegroundColor Yellow
    Write-Host "   3. Si usas dispositivo físico, actualiza la IP:" -ForegroundColor Yellow
    Write-Host "      .\obtener_ip_local.ps1" -ForegroundColor Gray
    Write-Host "      .\actualizar-ip-api.ps1" -ForegroundColor Gray
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  PASOS SIGUIENTES" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Abre la app en tu dispositivo/emulador" -ForegroundColor White
Write-Host "2. Navega al menú 'Tendencia'" -ForegroundColor White
Write-Host "3. Selecciona filtros (año, tipo SLA, área)" -ForegroundColor White
Write-Host "4. Visualiza el gráfico de tendencia" -ForegroundColor White
Write-Host ""
Write-Host "📄 Documentación completa en:" -ForegroundColor Cyan
Write-Host "   ARQUITECTURA_SIMPLIFICADA_IMPLEMENTADA.md" -ForegroundColor Gray
Write-Host ""
Write-Host "🎉 ¡Listo para usar!" -ForegroundColor Green

