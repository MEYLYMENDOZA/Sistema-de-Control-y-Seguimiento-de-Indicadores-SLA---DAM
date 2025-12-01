# Script para eliminar el archivo duplicado SlaRepository_NEW.kt
# Ejecuta esto en PowerShell

$archivo = "D:\REPOS\Sistema-de-Control-y-Seguimiento-de-Indicadores-SLA---DAM\app\src\main\java\com\example\proyecto1\data\repository\SlaRepository_NEW.kt"

Write-Host "🔍 Verificando archivo duplicado..." -ForegroundColor Cyan

if (Test-Path $archivo) {
    Write-Host "📄 Archivo encontrado: SlaRepository_NEW.kt" -ForegroundColor Yellow
    Write-Host "❌ Eliminando archivo duplicado..." -ForegroundColor Red

    Remove-Item $archivo -Force

    Write-Host "✅ ¡Archivo SlaRepository_NEW.kt eliminado exitosamente!" -ForegroundColor Green
    Write-Host ""
    Write-Host "📋 Próximos pasos:" -ForegroundColor Cyan
    Write-Host "   1. Abre Android Studio" -ForegroundColor White
    Write-Host "   2. File → Sync Project with Gradle Files" -ForegroundColor White
    Write-Host "   3. Build → Clean Project" -ForegroundColor White
    Write-Host "   4. Build → Rebuild Project" -ForegroundColor White
    Write-Host ""
    Write-Host "✅ El error 'Redeclaration: class SlaRepository' desaparecerá" -ForegroundColor Green
} else {
    Write-Host "⚠️ Archivo SlaRepository_NEW.kt no encontrado" -ForegroundColor Yellow
    Write-Host "   Es posible que ya haya sido eliminado" -ForegroundColor White
}

Write-Host ""
Write-Host "Presiona cualquier tecla para salir..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

