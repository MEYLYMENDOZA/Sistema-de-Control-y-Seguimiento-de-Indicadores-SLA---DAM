# Script para eliminar el archivo SlaRepository_NEW.kt duplicado
$archivoDuplicado = "c:\Users\meyle\AndroidStudioProjects\Proyecto1\app\src\main\java\com\example\proyecto1\data\repository\SlaRepository_NEW.kt"

if (Test-Path $archivoDuplicado) {
    Remove-Item $archivoDuplicado -Force
    Write-Host "✅ SlaRepository_NEW.kt eliminado exitosamente" -ForegroundColor Green
} else {
    Write-Host "⚠️ El archivo ya no existe o fue eliminado previamente" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Presiona cualquier tecla para cerrar..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

