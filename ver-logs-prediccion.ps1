# Script para probar Predicción con Logging
# Ejecutar después de instalar la app

Write-Host "================================" -ForegroundColor Cyan
Write-Host "  Prueba de Predicción con Logs" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "📱 Asegúrate de tener la app abierta en la pantalla de PREDICCIÓN" -ForegroundColor Yellow
Write-Host ""

$continue = Read-Host "¿La app está abierta en Predicción? (S/N)"
if ($continue -ne "S" -and $continue -ne "s") {
    Write-Host "❌ Abre la app primero" -ForegroundColor Red
    exit
}

Write-Host ""
Write-Host "🔍 Monitoreando logs de Predicción..." -ForegroundColor Cyan
Write-Host "⚠️  Presiona Ctrl+C para detener" -ForegroundColor Yellow
Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Limpiar logs anteriores
adb logcat -c

# Mostrar solo logs relevantes con colores
adb logcat -s "PrediccionViewModel:D" "PrediccionScreen:D" "SlaRepository:D" "RetrofitClient_API:D" | ForEach-Object {
    $line = $_

    # Colorear según tipo de log
    if ($line -match "✅|SUCCESS") {
        Write-Host $line -ForegroundColor Green
    }
    elseif ($line -match "❌|ERROR|Error") {
        Write-Host $line -ForegroundColor Red
    }
    elseif ($line -match "⚠️|WARNING|Warning") {
        Write-Host $line -ForegroundColor Yellow
    }
    elseif ($line -match "🔍|🔵|📡|📊|📈|📝") {
        Write-Host $line -ForegroundColor Cyan
    }
    elseif ($line -match "\[Predicción\]|\[Históricos\]") {
        Write-Host $line -ForegroundColor White
    }
    else {
        Write-Host $line -ForegroundColor Gray
    }
}

