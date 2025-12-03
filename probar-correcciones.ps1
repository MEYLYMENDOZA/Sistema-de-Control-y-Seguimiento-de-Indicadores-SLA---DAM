# Script de Prueba - Sistema SLA
# Ejecutar desde PowerShell

Write-Host "================================" -ForegroundColor Cyan
Write-Host "  Sistema de Control SLA - DAM" -ForegroundColor Cyan
Write-Host "  Script de Prueba y Compilación" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Cambiar al directorio del proyecto
$projectPath = "D:\REPOS\Sistema-de-Control-y-Seguimiento-de-Indicadores-SLA---DAM"
Set-Location $projectPath

Write-Host "📁 Directorio del proyecto: $projectPath" -ForegroundColor Green
Write-Host ""

# Función para mostrar el estado
function Show-Status {
    param([string]$message, [string]$color = "White")
    Write-Host "➤ $message" -ForegroundColor $color
}

# 1. Limpiar proyecto
Show-Status "Limpiando proyecto..." "Yellow"
./gradlew clean
if ($LASTEXITCODE -eq 0) {
    Show-Status "✅ Limpieza completada" "Green"
} else {
    Show-Status "❌ Error en la limpieza" "Red"
    exit 1
}
Write-Host ""

# 2. Compilar
Show-Status "Compilando proyecto..." "Yellow"
./gradlew assembleDebug
if ($LASTEXITCODE -eq 0) {
    Show-Status "✅ Compilación exitosa" "Green"
} else {
    Show-Status "❌ Error en la compilación" "Red"
    exit 1
}
Write-Host ""

# 3. Verificar dispositivos conectados
Show-Status "Verificando dispositivos conectados..." "Yellow"
$devices = adb devices | Select-String "device$"
if ($devices.Count -gt 0) {
    Show-Status "✅ Dispositivo(s) encontrado(s):" "Green"
    adb devices
} else {
    Show-Status "⚠️ No se encontraron dispositivos conectados" "Yellow"
    Show-Status "Conecta un dispositivo o inicia un emulador" "Yellow"
}
Write-Host ""

# 4. Instrucciones para probar
Write-Host "================================" -ForegroundColor Cyan
Write-Host "  INSTRUCCIONES DE PRUEBA" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "1️⃣  PROBAR CARGA DE EXCEL:" -ForegroundColor Magenta
Write-Host "   • Abre la app en el dispositivo" -ForegroundColor White
Write-Host "   • Ve a la sección 'Carga'" -ForegroundColor White
Write-Host "   • Presiona 'Plantilla' para descargar" -ForegroundColor White
Write-Host "   • Presiona 'Seleccionar Archivo' y elige un .xlsx" -ForegroundColor White
Write-Host "   • Presiona 'Procesar para Gestión'" -ForegroundColor White
Write-Host ""

Write-Host "2️⃣  PROBAR TENDENCIA:" -ForegroundColor Magenta
Write-Host "   • Ve a la sección 'Tendencia'" -ForegroundColor White
Write-Host "   • Selecciona un Tipo SLA" -ForegroundColor White
Write-Host "   • Selecciona un Año" -ForegroundColor White
Write-Host "   • Verifica que aparezcan gráficos y datos" -ForegroundColor White
Write-Host ""

Write-Host "3️⃣  PROBAR PREDICCIÓN:" -ForegroundColor Magenta
Write-Host "   • Ve a la sección 'Predicción'" -ForegroundColor White
Write-Host "   • Selecciona un Tipo SLA" -ForegroundColor White
Write-Host "   • Selecciona rango de fechas" -ForegroundColor White
Write-Host "   • Presiona 'Actualizar'" -ForegroundColor White
Write-Host "   • Verifica que aparezca la predicción" -ForegroundColor White
Write-Host ""

Write-Host "4️⃣  VER LOGS EN TIEMPO REAL:" -ForegroundColor Magenta
Write-Host "   Ejecuta en otra terminal PowerShell:" -ForegroundColor White
Write-Host '   adb logcat -s "TendenciaViewModel:D" "PrediccionViewModel:D" "CargaViewModel:D" "ExcelHelper:D"' -ForegroundColor Cyan
Write-Host ""

Write-Host "================================" -ForegroundColor Cyan
Write-Host "  ARCHIVOS MODIFICADOS" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ TendenciaScreen.kt - Logging agregado" -ForegroundColor Green
Write-Host "✅ TendenciaViewModel.kt - Logging detallado" -ForegroundColor Green
Write-Host "✅ PrediccionScreen.kt - Logging agregado" -ForegroundColor Green
Write-Host "✅ PrediccionViewModel.kt - Parámetros corregidos + logging" -ForegroundColor Green
Write-Host "✅ CargaViewModel.kt - Logging detallado + manejo de errores" -ForegroundColor Green
Write-Host "✅ ExcelHelper.kt - Logging + compatibilidad API" -ForegroundColor Green
Write-Host ""

Write-Host "📄 Documento de correcciones: CORRECCIONES_CARGA_EXCEL.md" -ForegroundColor Yellow
Write-Host ""

# 5. Opción para instalar
$install = Read-Host "¿Deseas instalar la app en el dispositivo? (S/N)"
if ($install -eq "S" -or $install -eq "s") {
    Show-Status "Instalando app..." "Yellow"
    ./gradlew installDebug
    if ($LASTEXITCODE -eq 0) {
        Show-Status "✅ App instalada exitosamente" "Green"
        Write-Host ""
        Write-Host "🚀 Abre la app en tu dispositivo para probarla" -ForegroundColor Green
    } else {
        Show-Status "❌ Error al instalar" "Red"
    }
}

Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "  Script completado" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan

