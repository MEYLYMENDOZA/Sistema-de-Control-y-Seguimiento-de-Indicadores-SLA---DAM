# Script para limpiar y preparar el proyecto para compilación

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Limpieza del Proyecto Android" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Navegar al directorio del proyecto
$projectDir = "D:\REPOS\Sistema-de-Control-y-Seguimiento-de-Indicadores-SLA---DAM"
Set-Location $projectDir

Write-Host "[1/5] Limpiando directorios de build..." -ForegroundColor Yellow
if (Test-Path "app\build") {
    Remove-Item -Path "app\build" -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "  ✓ app\build eliminado" -ForegroundColor Green
}

if (Test-Path "build") {
    Remove-Item -Path "build" -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "  ✓ build eliminado" -ForegroundColor Green
}

if (Test-Path ".gradle") {
    Remove-Item -Path ".gradle" -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "  ✓ .gradle eliminado" -ForegroundColor Green
}

Write-Host ""
Write-Host "[2/5] Limpiando caché de Gradle local..." -ForegroundColor Yellow
$gradleCache = "$env:USERPROFILE\.gradle\caches"
if (Test-Path $gradleCache) {
    Get-ChildItem $gradleCache -Filter "*proyecto1*" -Recurse -ErrorAction SilentlyContinue | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "  ✓ Caché de Gradle limpiada" -ForegroundColor Green
}

Write-Host ""
Write-Host "[3/5] Invalidando caché de Kotlin..." -ForegroundColor Yellow
if (Test-Path "app\.kotlin") {
    Remove-Item -Path "app\.kotlin" -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "  ✓ Caché de Kotlin eliminada" -ForegroundColor Green
}

Write-Host ""
Write-Host "[4/5] Limpiando archivos temporales..." -ForegroundColor Yellow
Get-ChildItem -Path "." -Include "*.iml" -Recurse -ErrorAction SilentlyContinue | Remove-Item -Force -ErrorAction SilentlyContinue
Get-ChildItem -Path "." -Include "local.properties" -Recurse -ErrorAction SilentlyContinue | Remove-Item -Force -ErrorAction SilentlyContinue
Write-Host "  ✓ Archivos temporales eliminados" -ForegroundColor Green

Write-Host ""
Write-Host "[5/5] Verificando configuración..." -ForegroundColor Yellow

# Verificar Java
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "  ✓ Java encontrado: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "  ⚠ Java no encontrado en PATH" -ForegroundColor Red
}

# Verificar Android Studio
$androidStudio = "C:\Program Files\Android\Android Studio\bin\studio64.exe"
if (Test-Path $androidStudio) {
    Write-Host "  ✓ Android Studio encontrado" -ForegroundColor Green
} else {
    Write-Host "  ⚠ Android Studio no encontrado en la ruta predeterminada" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Limpieza Completada" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "SIGUIENTES PASOS:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. Abre Android Studio" -ForegroundColor White
Write-Host "2. Abre el proyecto desde:" -ForegroundColor White
Write-Host "   $projectDir" -ForegroundColor Cyan
Write-Host "3. Espera a que Android Studio sincronice el proyecto" -ForegroundColor White
Write-Host "4. Ve a: Build → Clean Project" -ForegroundColor White
Write-Host "5. Ve a: Build → Rebuild Project" -ForegroundColor White
Write-Host "6. Ejecuta la aplicación en un dispositivo/emulador" -ForegroundColor White
Write-Host ""
Write-Host "NOTA: No uses 'gradlew' desde terminal PowerShell." -ForegroundColor Red
Write-Host "      Usa Android Studio para compilar y ejecutar." -ForegroundColor Red
Write-Host ""

Read-Host "Presiona Enter para cerrar"

